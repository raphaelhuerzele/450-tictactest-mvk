#!/usr/bin/env bash
set -euo pipefail

version="${1:-}"
if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
  echo "Usage: $0 MAJOR.MINOR.PATCH" >&2
  exit 1
fi

awk -v version="$version" '
  index($0, "## [" version "] - ") == 1 {
    found = 1
  }
  found && /^## \[/ && index($0, "## [" version "] - ") != 1 {
    exit
  }
  found {
    print
  }
  END {
    if (!found) {
      exit 1
    }
  }
' CHANGELOG.md
