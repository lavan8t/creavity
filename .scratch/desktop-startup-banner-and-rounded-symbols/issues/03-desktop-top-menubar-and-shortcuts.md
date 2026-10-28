# Issue 03: Desktop Top Toolbar / MenuBar & Pro Navigation Shortcuts

## Status
resolved

## Description
User requested: "add that toolbar on top. etc all stuff do."

## Plan
1. In `Main.kt`, add Compose Desktop native `MenuBar`:
   - `File`: New Board (`Ctrl+N`), Open Library (`Ctrl+O`), Export Canvas (`Ctrl+E`), Exit (`Ctrl+Q`).
   - `Edit`: Undo (`Ctrl+Z`), Redo (`Ctrl+Shift+Z` / `Ctrl+Y`), Duplicate (`Ctrl+D`), Delete (`Del`), Select All (`Ctrl+A`).
   - `View`: Zoom In (`Ctrl++`), Zoom Out (`Ctrl+-`), Reset Zoom (`Ctrl+0`), Toggle Fullscreen (`F11`).
   - `Tools`: Pen (`P`), Highlighter (`H`), Eraser (`E`), Shapes (`R`/`C`), Select (`V`), Sticky Note (`S`), Text (`T`).
   - `Help`: About CreaVITy.
2. In `AdaptiveWhiteboardScreen.kt`:
   - Add shortcuts for `Key.Equals` (`Ctrl++`), `Key.Minus` (`Ctrl+-`), `Key.Zero` (`Ctrl+0`) for instant zoom control.
   - Add `Key.Space` temporary Pan mode: holding Space turns cursor to hand and enables click-drag canvas panning, releasing returns to prior tool.
   - Add `pointerHoverIcon` to canvas (Crosshair when drawing, Hand when panning, Text when on text).
3. Verify test compilation and desktop run.
