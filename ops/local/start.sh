#!/usr/bin/env bash
set -euo pipefail
runtime_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
assets="$(realpath "${1:?usage: start.sh RELEASE_ASSET_DIRECTORY vX.Y.Z}")"
tag="${2:?release tag required}"
[[ "$tag" =~ ^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]] || exit 2
version="${tag#v}"
cd "$assets"
sha256sum --check SHA256SUMS
bash "$runtime_dir/runtime/install-tools.sh" "$assets/tools"
python3 -m zipfile -e "tictactest-frontend-${version}.zip" "$assets/site"
cp "$assets/site/config.example.js" "$assets/site/config.js"
children=()
cleanup() { for child in "${children[@]}"; do kill "$child" 2>/dev/null || true; done; }
trap cleanup EXIT INT TERM
python3 "$runtime_dir/local/serve.py" --port 8000 --directory "$assets/site" & children+=("$!")
python3 "$runtime_dir/local/serve.py" --port 7682 --version "$version" & children+=("$!")
"$assets/tools/ttyd" --interface 127.0.0.1 --port 7681 --writable --check-origin --max-clients 4 \
  -t disableReconnect=true -t disableResizeOverlay=true -t fontSize=18 -t titleFixed=TicTacTest \
  "$assets/tools/java/bin/java" -Xms16m -Xmx64m -XX:MaxMetaspaceSize=64m \
  -XX:ReservedCodeCacheSize=32m -XX:+UseSerialGC -XX:ActiveProcessorCount=1 \
  -jar "$assets/tictactest-${version}.jar" & children+=("$!")
printf 'Frontend: http://localhost:8000\nTerminal: http://localhost:7681\nVersion: http://localhost:7682/version\n'
wait -n "${children[@]}"
