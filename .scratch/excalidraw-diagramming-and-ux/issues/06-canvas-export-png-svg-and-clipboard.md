# Ticket 06: High-Fidelity Canvas Export (PNG, SVG, Clipboard)

Status: resolved
Blocked by: 01, 02

## What it delivers
- Export Engine (`CanvasExportManager.kt`):
  - Raster PNG generation (2x/3x high resolution) with optional transparent background.
  - Vector SVG export serializing rough paths, geometric shapes, text elements, and sticky notes into standard SVG XML markup.
  - OS Clipboard integration: copy rendered image buffer directly to the clipboard.
- Export Dialog (`CanvasExportDialog.kt`):
  - Material 3 `AlertDialog` or `ModalBottomSheet` (`shape = RoundedCornerShape(24.dp)`, `border = null`).
  - Scope selection (Full Canvas vs Selected Elements).
  - Background toggle (Transparent vs Whiteboard Canvas color).
  - Download / Save file and "Copy to Clipboard" buttons.

## Verification
- Unit test in `CanvasExportTest.kt` verifying valid SVG structure and PNG byte generation.
- `./gradlew :app:testDebugUnitTest` passes.
- `./gradlew :app:compileKotlinDesktop` passes.
