#!/usr/bin/env bash
# Reset the demo environment to a clean state.
#
# - Rebuilds the shelter binary via Gradle installDist.
# - Symlinks `shelter` into ~/.local/bin so it resolves in every shell
#   (including Claude Code's non-interactive bash — no sudo, no PATH exports).
# - Wipes ~/shelter so the next `shelter` run regenerates CLAUDE.md and data/.

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BIN_SOURCE="$PROJECT_DIR/build/install/shelter/bin/shelter"
BIN_LINK="$HOME/.local/bin/shelter"

echo "==> Building shelter binary"
cd "$PROJECT_DIR"
./gradlew installDist

echo "==> Linking $BIN_LINK -> $BIN_SOURCE"
mkdir -p "$HOME/.local/bin"
ln -sf "$BIN_SOURCE" "$BIN_LINK"

echo "==> Wiping ~/shelter"
rm -rf "$HOME/shelter"

echo "==> Regenerating ~/shelter (CLAUDE.md + data/)"
"$BIN_LINK" print >/dev/null

echo
echo "Done."
echo "  binary:    $BIN_LINK"
echo "  workdir:   $HOME/shelter"
echo "  CLAUDE.md: $HOME/shelter/CLAUDE.md"
echo
echo "Open a fresh terminal and run 'shelter print' to confirm."
