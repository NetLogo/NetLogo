#!/bin/sh

set -e

DEST="$1/NetLogo Launcher $2.app"

rm -rf "$DEST"

mkdir -p "$DEST/Contents/MacOS" "$DEST/Contents/Resources"

sed s/{{version}}/$2/ launch.sh.mustache > "$DEST/Contents/MacOS/launch.sh"

chmod +x "$DEST/Contents/MacOS/launch.sh"

sed s/{{version}}/$2/ Info.plist.mustache > "$DEST/Contents/Info.plist"

cp "$3" "$DEST/Contents/Resources/NetLogo Launcher.icns"
