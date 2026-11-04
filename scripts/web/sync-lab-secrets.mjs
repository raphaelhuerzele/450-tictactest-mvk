// Credentials stay in process memory and reach gh only through stdin.
import { execFileSync } from 'node:child_process';
const profile = process.argv[2] ?? 'lab';
const repository = process.env.GITHUB_REPOSITORY ?? 'raphaelhuerzele/450-tictactest-mvk';
const capture = (command, args) => execFileSync(command, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
try { capture('aws', ['sts', 'get-caller-identity', '--profile', profile]); }
catch { throw new Error('The local Lab profile is not valid. No GitHub secrets were modified.'); }
const credentials = JSON.parse(capture('aws', ['configure', 'export-credentials', '--profile', profile, '--format', 'process']));
if (!credentials.AccessKeyId || !credentials.SecretAccessKey || !credentials.SessionToken) throw new Error('Expected three temporary Lab credentials');
for (const [name, value] of Object.entries({
  AWS_ACCESS_KEY_ID: credentials.AccessKeyId,
  AWS_SECRET_ACCESS_KEY: credentials.SecretAccessKey,
  AWS_SESSION_TOKEN: credentials.SessionToken
})) {
  try {
    execFileSync('gh', ['secret', 'set', name, '--env', 'lab', '--repo', repository],
      { input: value, encoding: 'utf8', stdio: ['pipe', 'ignore', 'pipe'] });
  } catch { throw new Error(`Could not update ${name}. Retry the complete refresh; credential values were not printed.`); }
}
console.log('All three Lab environment secrets updated; no credential values logged.');
