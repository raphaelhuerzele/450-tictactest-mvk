import { execFileSync } from 'node:child_process';

const shared = path => ['build.gradle', '.github/workflows/release.yml'].includes(path) || path.startsWith('scripts/web/package-');
export function classify(paths, force = false) {
  if (force) return { backend: true, frontend: true };
  return {
    backend: paths.some(path => /^(src\/main\/|gradle\/|gradlew(?:\.bat)?$|build\.gradle$|settings\.gradle$|gradle\.properties$)/.test(path)) || paths.some(shared),
    frontend: paths.some(path => path.startsWith('frontend/')) || paths.some(shared)
  };
}
export function previousRelease(releases, currentTag) {
  const current = releases.find(release => release.tag_name === currentTag);
  if (!current || current.draft || current.prerelease) throw new Error('A published stable release is required.');
  return releases.filter(release => /^v\d+\.\d+\.\d+$/.test(release.tag_name)
      && !release.draft && !release.prerelease && release.published_at < current.published_at)
    .sort((a, b) => b.published_at.localeCompare(a.published_at))[0]?.tag_name ?? null;
}
export function changedPaths(previous, current) {
  return execFileSync('git', ['diff', '--name-only', previous, current], { encoding: 'utf8' })
    .trim().split(/\r?\n/).filter(Boolean);
}
