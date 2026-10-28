# Issue 02: Pure Compose Material 3 Expressive Board Top Menu

## Status
closed

## Resolution
- Implemented `DesktopBoardTopMenuBar.kt` with Compose Material 3 Expressive dropdown menus (`File`, `Edit`, `View`, `Tools`), keyboard shortcuts, and Material Symbols Rounded 700 filled icons.
- Integrated into `WhiteboardTopBar` and `CanvasViewportScreen.kt` guarded with `isDesktopPlatform()`, so it is only visible when an active board is open on desktop.
- Verified all unit and desktop tests pass.

## Description
User requested: "no, that file edit should look like compose one not java one. and only visible inside a board. dawg."

## Plan
1. Create `DesktopBoardTopMenuBar.kt` in `presentation/ui/components/`:
   - Styled as an elevated Material 3 Expressive horizontal menu bar inside the board top bar.
   - Menus:
     - `File`: New Board (`Ctrl+N`), Export Board... (`Ctrl+E`), Close Board (`Esc` / Back).
     - `Edit`: Undo (`Ctrl+Z`), Redo (`Ctrl+Y`), Duplicate (`Ctrl+D`), Delete (`Del`), Select All (`Ctrl+A`), Clear Canvas.
     - `View`: Zoom In (`Ctrl++`), Zoom Out (`Ctrl+-`), Reset Zoom 100% (`Ctrl+0`), Birds-Eye Navigator, Toggle Grid / Background.
     - `Tools`: Pen (`P`), Highlighter (`H`), Eraser (`E`), Shapes (`R`), Select (`V`), Sticky Note (`S`), Text (`T`).
   - Every dropdown menu rendered with pure Compose `DropdownMenu`, `surfaceContainerHigh`, `RoundedCornerShape(16.dp)`, tonal elevation 6.dp, shadow elevation 8.dp, and Material Symbols Rounded 700 filled icons with shortcut tags.
2. Integrate `DesktopBoardTopMenuBar` into `WhiteboardTopBar` / `CanvasViewportScreen` gated by `isDesktopPlatform() == true`.
3. Verify test compilation and packaging.
