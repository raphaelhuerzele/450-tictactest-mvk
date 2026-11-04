import test from 'node:test';
import assert from 'node:assert/strict';
import { classify, previousRelease } from './changes.mjs';

test('independent frontend/backend deployment, docs-only no-op, and explicit rollback', () => {
  assert.deepEqual(classify(['frontend/style.css']), { frontend: true, backend: false });
  assert.deepEqual(classify(['src/main/java/Game.java']), { frontend: false, backend: true });
  assert.deepEqual(classify(['frontend/index.html', 'src/main/java/Game.java']), { frontend: true, backend: true });
  assert.deepEqual(classify(['docs/deployment.md', 'infra/main.tf', 'src/test/java/GameTest.java']), { frontend: false, backend: false });
  assert.deepEqual(classify(['build.gradle']), { frontend: true, backend: true });
  assert.deepEqual(classify(['.github/workflows/release.yml']), { frontend: true, backend: true });
  assert.deepEqual(classify([], true), { frontend: true, backend: true });
});
test('compare with previous stable release, ignoring drafts, prereleases and container tags', () => {
  const releases = [
    { tag_name: 'v1.2.0', published_at: '2026-11-04T14:00:00Z' },
    { tag_name: 'v1.1.0', published_at: '2026-11-04T12:00:00Z' },
    { tag_name: 'v1.1.1', published_at: '2026-11-04T13:00:00Z', draft: true },
    { tag_name: 'v1.2.0-rc1', published_at: '2026-11-04T13:30:00Z', prerelease: true },
    { tag_name: 'devcontainer-v4.0.0', published_at: '2026-11-04T13:50:00Z' }
  ];
  assert.equal(previousRelease(releases, 'v1.2.0'), 'v1.1.0');
  assert.equal(previousRelease(releases, 'v1.1.0'), null);
  assert.throws(() => previousRelease(releases, 'v9.0.0'), /published stable/);
});
