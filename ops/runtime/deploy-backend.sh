#!/usr/bin/env bash
# Installed by infrastructure. Only a trusted SSM operator can invoke it as root.
set -euo pipefail
repository="${1:?public GitHub owner/repository}"
tag="${2:?release tag}"
digest="${3:?expected SHA256}"
[[ "$repository" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]] || exit 2
[[ "$tag" =~ ^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]] || exit 2
[[ "$digest" =~ ^[a-f0-9]{64}$ ]] || exit 2
exec 9>/run/tictactest-deploy.lock
flock -n 9 || { echo 'Another backend deployment is active.' >&2; exit 1; }
root=/opt/tictactest
release="$root/releases/$tag-$digest"
mkdir -p "$release"
curl --fail --location --retry 3 "https://github.com/$repository/releases/download/$tag/tictactest-${tag#v}.jar" -o "$release/game.jar.download"
printf '%s  %s\n' "$digest" "$release/game.jar.download" | sha256sum --check -
# Exercise the release JAR before touching the running service; never build it here.
printf '4\n8\n2\n6\n' | timeout 15 "$root/tools/java/bin/java" -Xmx64m -jar "$release/game.jar.download" > "$release/smoke.log" 2>&1
grep -Eq 'winner is:|it.s a draw!' "$release/smoke.log"
mv "$release/game.jar.download" "$release/game.jar"
printf '%s\n' "${tag#v}" > "$release/version.txt"
chmod -R a+rX "$release"
previous="$(readlink "$root/current" || true)"
ln -sfn "$release" "$root/current.next"
mv -Tf "$root/current.next" "$root/current"
if systemctl restart tictactest && sleep 2 && systemctl is-active --quiet tictactest \
    && curl --fail --silent http://127.0.0.1/health >/dev/null \
    && [[ "$(curl --fail --silent http://127.0.0.1/version)" == "${tag#v}" ]]; then
  echo "Backend ${tag#v} activated."
else
  if [[ -n "$previous" ]]; then
    ln -sfn "$previous" "$root/current.rollback"
    mv -Tf "$root/current.rollback" "$root/current"
    systemctl restart tictactest
  fi
  echo 'Backend activation failed; previous version restored when available.' >&2
  exit 1
fi
