#!/usr/bin/env bash
set -euo pipefail
repository="${1:?public GitHub owner/repository}"
tag="${2:?release tag}"
digest="${3:?expected SHA256}"
[[ "$repository" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]] || exit 2
[[ "$tag" =~ ^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]] || exit 2
[[ "$digest" =~ ^[a-f0-9]{64}$ ]] || exit 2
exec 9>/run/tictactest-frontend-deploy.lock
flock -n 9 || exit 1
root=/opt/tictactest/frontend
release="$root/$tag-$digest"
mkdir -p "$release"
curl --fail --location --retry 3 "https://github.com/$repository/releases/download/$tag/tictactest-frontend-${tag#v}.zip" -o "$release/archive.zip"
printf '%s  %s\n' "$digest" "$release/archive.zip" | sha256sum --check -
python3 - "$release/archive.zip" "$release/site" <<'PY'
import pathlib, sys, zipfile
archive = zipfile.ZipFile(sys.argv[1])
for name in archive.namelist():
    path = pathlib.PurePosixPath(name)
    if path.is_absolute() or '..' in path.parts or '\\' in name or name == 'config.js':
        raise ValueError('Unsafe frontend archive or infrastructure config.js present')
archive.extractall(sys.argv[2])
PY
[[ "$(cat "$release/site/frontend-version.txt")" == "${tag#v}" ]] || exit 1
chmod -R a+rX "$release/site"
ln -sfn "$release/site" "$root/current.next"
mv -Tf "$root/current.next" "$root/current"
# config.js is outside the release root and cannot be replaced by the archive.
echo "Frontend ${tag#v} activated without restarting the game service."
