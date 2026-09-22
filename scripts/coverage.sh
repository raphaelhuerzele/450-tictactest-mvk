#!/usr/bin/env bash
# Prints the overall JaCoCo line coverage as a percentage with one decimal place.
set -euo pipefail

xml="${1:-build/reports/jacoco/test/jacocoTestReport.xml}"

if [ ! -f "$xml" ]; then
  echo "JaCoCo XML report not found: $xml" >&2
  exit 1
fi

counter="$(grep -oE '<counter type="LINE" missed="[0-9]+" covered="[0-9]+"/>' "$xml" | tail -1)"
missed="$(printf '%s\n' "$counter" | sed -E 's/.*missed="([0-9]+)".*/\1/')"
covered="$(printf '%s\n' "$counter" | sed -E 's/.*covered="([0-9]+)".*/\1/')"

awk -v missed="$missed" -v covered="$covered" '
BEGIN {
  total = missed + covered
  if (total == 0) {
    print "0.0"
    exit
  }
  printf "%.1f\n", 100 * covered / total
}'
