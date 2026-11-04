// Only non-secret OpenTofu outputs are transferred. No AWS credentials are read.
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
const outputs = JSON.parse(readFileSync(process.argv[2] ?? 'infra/outputs.json', 'utf8').replace(/^\uFEFF/, ''));
const repository = process.env.GITHUB_REPOSITORY ?? 'raphaelhuerzele/450-tictactest-mvk';
execFileSync('gh', ['api', '--method', 'PUT', `repos/${repository}/environments/lab`], { stdio: 'ignore' });
for (const name of ['aws_region', 'instance_id', 'backend_version_url', 'frontend_url']) {
  const value = outputs[name]?.value;
  if (typeof value !== 'string' || !value) throw new Error(`Missing output ${name}`);
  execFileSync('gh', ['variable', 'set', name.toUpperCase(), '--repo', repository, '--env', 'lab', '--body', value], { stdio: 'inherit' });
}
console.log('Environment variables updated. Set the three Lab secrets separately, then enable the repository variable AWS_DEPLOY_ENABLED.');
