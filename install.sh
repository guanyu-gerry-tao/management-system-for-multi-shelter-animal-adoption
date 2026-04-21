#!/usr/bin/env bash
# One-shot installer for the Shelter CLI.
# Usage:  ./install.sh
# After it finishes you are dropped into ~/shelter/ with `shelter` on PATH.

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BIN_DIR="$PROJECT_ROOT/build/install/shelter/bin"
SHELTER_HOME="$HOME/shelter"
SYMLINK_PATH="/usr/local/bin/shelter"

echo "==> Checking Java (need 21+)"
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is not installed. Please install JDK 21 or newer." >&2
  exit 1
fi
JAVA_MAJOR="$(java -version 2>&1 | head -n1 | sed -E 's/.*"([0-9]+).*/\1/')"
if [ -z "$JAVA_MAJOR" ] || [ "$JAVA_MAJOR" -lt 21 ]; then
  echo "ERROR: Java 21+ required. You have: $(java -version 2>&1 | head -n1)" >&2
  exit 1
fi
java -version

echo "==> Building project (./gradlew installDist)"
cd "$PROJECT_ROOT"
chmod +x ./gradlew
./gradlew installDist

echo "==> Creating work directory: $SHELTER_HOME"
mkdir -p "$SHELTER_HOME"

echo "==> Running first-launch init (creates data/, CLAUDE.md, AGENTS.md, .claude/settings.json)"
"$BIN_DIR/shelter" --version >/dev/null

echo "==> Installing 'shelter' symlink at $SYMLINK_PATH"
if ln -sf "$BIN_DIR/shelter" "$SYMLINK_PATH" 2>/dev/null; then
  echo "    linked."
else
  echo "    (needs sudo)"
  sudo ln -sf "$BIN_DIR/shelter" "$SYMLINK_PATH"
fi

echo
echo "==> Done. Dropping you into $SHELTER_HOME"
echo "    Try: shelter --help"
echo
cd "$SHELTER_HOME"
exec "$SHELL"
