import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { mkdtempSync, readFileSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { classify, previousRelease, changedPaths } from './changes.mjs';

const tag = process.argv[2];
if (!/^v(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/.test(tag ?? '')) throw new Error('Use a stable published tag vX.Y.Z');
const repository = process.env.GITHUB_REPOSITORY ?? 'raphaelhuerzele/450-tictactest-mvk';
if (!/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(repository)) throw new Error('Invalid repository');
const version = tag.slice(1);
const force = process.argv.includes('--force');
const run = (command, args) => execFileSync(command, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim();
const json = (command, args) => JSON.parse(run(command, args));
const releases = json('gh', ['api', `repos/${repository}/releases?per_page=100`, '--paginate', '--slurp']).flat();
const previous = previousRelease(releases, tag);
const selection = classify(previous ? changedPaths(previous, tag) : [], force || !previous);
console.log(JSON.stringify({ tag, previous, force, ...selection }));
if (!selection.backend && !selection.frontend) {
  console.log('No application changes: nothing deployed.');
  process.exit(0);
}
for (const name of ['AWS_REGION', 'INSTANCE_ID', 'BACKEND_VERSION_URL', 'FRONTEND_URL']) {
  if (!process.env[name]) throw new Error(`Missing deployment variable ${name}; use OpenTofu outputs.`);
}
try { run('aws', ['sts', 'get-caller-identity']); }
catch { throw new Error('AWS authentication failed. Renew all three Lab secrets, then manually redeploy the release.'); }
const directory = mkdtempSync(join(tmpdir(), 'tictactest-deploy-'));
const names = [
  ...(selection.backend ? [`tictactest-${version}.jar`] : []),
  ...(selection.frontend ? [`tictactest-frontend-${version}.zip`] : [])
];
run('gh', ['release', 'download', tag, '--repo', repository, '--dir', directory, '--pattern', 'SHA256SUMS',
  ...names.flatMap(name => ['--pattern', name])]);
const checksums = new Map(readFileSync(join(directory, 'SHA256SUMS'), 'utf8').trim().split(/\r?\n/).map(line => {
  const match = /^([a-f0-9]{64})  ([a-zA-Z0-9.\-]+)$/.exec(line);
  if (!match) throw new Error('Malformed release checksum manifest');
  return [match[2], match[1]];
}));
for (const name of names) {
  if (createHash('sha256').update(readFileSync(join(directory, name))).digest('hex') !== checksums.get(name)) throw new Error(`Checksum mismatch: ${name}`);
}
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
async function verify(url, expected) {
  for (let attempt = 0; attempt < 30; attempt++) {
    try {
      const response = await fetch(`${url}${url.includes('?') ? '&' : '?'}check=${Date.now()}`, { signal: AbortSignal.timeout(4000), cache: 'no-store' });
      if (response.ok && (await response.text()).trim() === expected) return;
    } catch { /* Retry bounded startup delays. */ }
    await sleep(2000);
  }
  throw new Error(`Version verification timed out: expected ${expected} at ${url}`);
}
async function install(helper, digest) {
  const parameters = JSON.stringify({ commands: [`/usr/local/sbin/${helper} '${repository}' '${tag}' '${digest}'`], executionTimeout: ['180'] });
  const sent = json('aws', ['ssm', 'send-command', '--instance-ids', process.env.INSTANCE_ID,
    '--document-name', 'AWS-RunShellScript', '--timeout-seconds', '120', '--parameters', parameters, '--output', 'json']);
  const commandId = sent.Command.CommandId;
  for (let attempt = 0; attempt < 90; attempt++) {
    let result;
    try { result = json('aws', ['ssm', 'get-command-invocation', '--command-id', commandId, '--instance-id', process.env.INSTANCE_ID, '--output', 'json']); }
    catch { await sleep(2000); continue; }
    if (result.Status === 'Success') return;
    if (['Failed', 'Cancelled', 'TimedOut', 'Cancelling'].includes(result.Status)) throw new Error(`SSM command ${commandId} failed (${result.Status}); inspect its instance log.`);
    await sleep(2000);
  }
  throw new Error(`SSM command ${commandId} exceeded the deadline.`);
}
if (selection.backend) {
  await install('tictactest-deploy', checksums.get(`tictactest-${version}.jar`));
  await verify(process.env.BACKEND_VERSION_URL, version);
}
if (selection.frontend) {
  await install('tictactest-deploy-frontend', checksums.get(`tictactest-frontend-${version}.zip`));
  await verify(`${process.env.FRONTEND_URL.replace(/\/$/, '')}/frontend-version.txt`, version);
}
mkdirSync(resolve('build/deployment'), { recursive: true });
writeFileSync(resolve('build/deployment/result.json'), `${JSON.stringify({ tag, previous, ...selection, verifiedAt: new Date().toISOString() }, null, 2)}\n`);
console.log('Selected public release components deployed and verified.');
