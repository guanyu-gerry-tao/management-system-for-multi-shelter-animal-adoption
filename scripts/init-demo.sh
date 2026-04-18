#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SHELTER_HOME="${SHELTER_HOME:-$HOME/shelter}"

echo "==> Removing existing shelter home: $SHELTER_HOME"
rm -rf "$SHELTER_HOME"

echo "==> Building project (installDist)"
cd "$PROJECT_ROOT"
./gradlew installDist

echo "==> Generating fresh shelter home via first CLI invocation"
export PATH="$PROJECT_ROOT/build/install/shelter/bin:$PATH"
shelter --version >/dev/null

echo "==> Done. Shelter home initialized at: $SHELTER_HOME"
echo
echo "    binary:    $PROJECT_ROOT/build/install/shelter/bin/shelter"
echo "    workdir:   $SHELTER_HOME"
echo "    CLAUDE.md: $SHELTER_HOME/CLAUDE.md"
