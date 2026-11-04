import { copyFileSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { resolve } from 'node:path';

const version = process.argv[2];
if (!/^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/.test(version ?? '')) throw new Error('Expected stable SemVer');
const directory = resolve('build/release-assets');
mkdirSync(directory, { recursive: true });
const assets = [
  [`build/libs/tictactest-${version}.jar`, `tictactest-${version}.jar`],
  [`build/distributions/tictactest-frontend-${version}.zip`, `tictactest-frontend-${version}.zip`],
  [`build/distributions/tictactest-runtime-${version}.tar.gz`, `tictactest-runtime-${version}.tar.gz`]
];
const checksums = assets.map(([source, name]) => {
  const digest = createHash('sha256').update(readFileSync(source)).digest('hex');
  copyFileSync(source, resolve(directory, name));
  return `${digest}  ${name}`;
});
writeFileSync(resolve(directory, 'SHA256SUMS'), `${checksums.join('\n')}\n`);
writeFileSync(resolve(directory, `tictactest-${version}.jar.sha256`), `${checksums[0]}\n`);
