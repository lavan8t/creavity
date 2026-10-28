#!/usr/bin/env bash
set -e

APP_NAME="creavity"
APP_DISPLAY_NAME="CreaVITy"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Determine installation mode:
# If root, install system-wide in /opt/creavity and /usr/local/bin.
# If regular user, install into $HOME/.local/share/creavity and $HOME/.local/bin.
if [ "$(id -u)" -eq 0 ]; then
    INSTALL_DIR="/opt/creavity"
    BIN_DIR="/usr/local/bin"
    APP_DIR="/usr/share/applications"
    ICON_DIR="/usr/share/icons/hicolor/192x192/apps"
else
    INSTALL_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/creavity"
    BIN_DIR="$HOME/.local/bin"
    APP_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/applications"
    ICON_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor/192x192/apps"
fi

echo "=============================================="
echo " Installing $APP_DISPLAY_NAME for Linux"
echo " Target directory: $INSTALL_DIR"
echo "=============================================="

mkdir -p "$INSTALL_DIR" "$BIN_DIR" "$APP_DIR" "$ICON_DIR"

# Clean old installation files
rm -rf "$INSTALL_DIR/bin" "$INSTALL_DIR/lib"

# Copy binary distribution
cp -r "$SCRIPT_DIR/bin" "$INSTALL_DIR/"
cp -r "$SCRIPT_DIR/lib" "$INSTALL_DIR/"
chmod +x "$INSTALL_DIR/bin/CreaVITy"

# Install icon if available
ICON_TARGET="$ICON_DIR/creavity.png"
if [ -f "$SCRIPT_DIR/icon.png" ]; then
    cp "$SCRIPT_DIR/icon.png" "$ICON_TARGET"
fi

# Create launcher wrapper in BIN_DIR
LAUNCHER="$BIN_DIR/creavity"
cat << EOF > "$LAUNCHER"
#!/usr/bin/env bash
# CreaVITy desktop launcher
exec "$INSTALL_DIR/bin/CreaVITy" "\$@"
EOF
chmod +x "$LAUNCHER"

# Install Desktop Entry
DESKTOP_FILE="$APP_DIR/creavity.desktop"
cat << EOF > "$DESKTOP_FILE"
[Desktop Entry]
Name=$APP_DISPLAY_NAME
GenericName=Collaborative Whiteboard
Comment=CreaVITy Infinite Collaborative Whiteboard
Exec=$LAUNCHER %U
Icon=$ICON_TARGET
Terminal=false
Type=Application
Categories=Graphics;Office;2DGraphics;
StartupWMClass=CreaVITy
MimeType=application/x-creavity;
EOF
chmod +x "$DESKTOP_FILE"

# Update desktop and icon caches if available
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "$APP_DIR" || true
command -v gtk-update-icon-cache >/dev/null 2>&1 && gtk-update-icon-cache -f -t "$(dirname "$ICON_DIR")" 2>/dev/null || true

echo ""
echo "✓ $APP_DISPLAY_NAME successfully installed!"
echo "  Run via terminal:  $LAUNCHER"
echo "  Or launch from your application launcher / desktop menu."
if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
    echo ""
    echo "Notice: $BIN_DIR is not currently in your \$PATH."
    echo "Add 'export PATH=\"\$PATH:$BIN_DIR\"' to your ~/.bashrc or ~/.zshrc."
fi
