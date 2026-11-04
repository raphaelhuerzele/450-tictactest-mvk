#!/usr/bin/env bash
set -euo pipefail
runtime_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$runtime_dir/versions.env"
destination="${1:?usage: install-tools.sh ABSOLUTE_DIRECTORY}"
[[ "$(uname -m)" == x86_64 ]] || { echo 'This pinned runtime requires x86_64.' >&2; exit 1; }
mkdir -p "$destination"
if [[ ! -x "$destination/ttyd" ]]; then
  curl --fail --location --retry 3 "https://github.com/tsl0922/ttyd/releases/download/${TTYD_VERSION}/ttyd.x86_64" -o "$destination/ttyd.download"
  printf '%s  %s\n' "$TTYD_SHA256" "$destination/ttyd.download" | sha256sum --check -
  mv "$destination/ttyd.download" "$destination/ttyd"
  chmod 755 "$destination/ttyd"
fi
if [[ ! -x "$destination/java/bin/java" ]]; then
  curl --fail --location --retry 3 "$JAVA_URL" -o "$destination/java.tar.gz"
  printf '%s  %s\n' "$JAVA_SHA256" "$destination/java.tar.gz" | sha256sum --check -
  mkdir -p "$destination/java"
  tar -xzf "$destination/java.tar.gz" --strip-components=1 -C "$destination/java"
  rm "$destination/java.tar.gz"
fi
"$destination/ttyd" --version
"$destination/java/bin/java" -version
