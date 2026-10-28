# Issue 02: Draggable Desktop Material 3 Expressive Toolbar

## Status
resolved

## Description
User requested: "and for desktop move the drawinf tools to left use mtaeril 3 expressive toolbar, make it draggable."
On desktop monitors, the bottom-center mobile toolbar occupies awkward horizontal space and is fixed in place.

## Plan
1. Create `DesktopDraggableToolbar.kt` in `presentation/ui/components/`:
   - Material 3 Expressive vertical dock styled with `RoundedCornerShape(24.dp)`, `surfaceContainerHigh`, tonal elevation 6.dp, shadow elevation 8.dp.
   - Distinctive drag handle header pill at top with drag indicator dots/bar.
   - Drag detection gesture (`detectDragGestures`) updating `Offset(x, y)` clamped within screen width and height boundaries.
   - Tools: Pen, Marker, Highlighter, Brush, Eraser, Shapes, Select, Sticky Note, Text, Undo, Redo, Background.
   - Active tool indicator pill in `colorScheme.primaryContainer`.
2. In `CanvasViewportScreen.kt`:
   - Check `isDesktopPlatform()`.
   - If desktop: render `DesktopDraggableToolbar` on the left by default (e.g. `x = 24.dp, y = 100.dp`), allowing free dragging.
   - If mobile: render `CanvasBottomToolbar`.
   - Anchor callout cards (Pen palette, Shape callout, Eraser) adjacent to the draggable toolbar.
3. Test drag offset state and boundary clamping.
