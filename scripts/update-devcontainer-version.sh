#!/usr/bin/env bash
set -euo pipefail

version="${1:-}"
if [[ ! "$version" =~ ^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
  echo "Usage: $0 vMAJOR.MINOR.PATCH" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to update the Dev Container configuration." >&2
  exit 1
fi

config=".devcontainer/devcontainer.json"
image="ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:${version}"
temporary_file="$(mktemp)"
trap 'rm -f "$temporary_file"' EXIT

jq --arg image "$image" 'del(.build) | .image = $image' "$config" > "$temporary_file"
mv "$temporary_file" "$config"
printf '%s\n' "$version" > .devcontainer/VERSION
