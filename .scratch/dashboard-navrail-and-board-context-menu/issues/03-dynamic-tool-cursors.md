# Issue 03: Dynamic Tool Cursors for Whiteboard Tools

## Status
closed

## Description
User requested: "when a tool is selected use that as cursor and change cursor look too" and "remove filw new eid tthat toolbar."

## Resolution
- Removed `DesktopBoardTopMenuBar.kt` and its wiring from `WhiteboardTopBar` and `CanvasViewportScreen.kt`.
- Implemented `expect fun getToolPointerIcon(tool: DrawingTool): PointerIcon` in `commonMain`.
- Implemented platform-specific `getToolPointerIcon` in `desktopMain` generating high-contrast custom tool cursors via `java.awt.Toolkit.createCustomCursor`:
  - `PEN` / `PENCIL` / `MARKER` / `BRUSH` / `CALLIGRAPHY`: Fountain pen nib with gold tip and white outline, hotspot at `(1, 1)`.
  - `HIGHLIGHTER`: Chisel-tip yellow highlighter, hotspot at `(1, 1)`.
  - `ERASER` / `SELECT_ERASE`: Pink/white angled eraser block, hotspot at `(9, 9)`.
  - `LASER`: Glowing red target reticle with central dot, hotspot at `(10, 10)`.
  - `STICKY_NOTE`: Post-it note with folded corner, hotspot at `(2, 2)`.
  - `RECTANGLE`, `CIRCLE`, `ARROW`, `LINE`, `STAR`, `DIAMOND`, etc.: Precision crosshair cursor.
  - `TEXT`: I-beam text cursor.
  - `SELECT`: Default pointer cursor.
- Wired `canvasHoverIcon` dynamically via `remember(selectedTool)` in `WhiteboardCanvas.kt`.
