# Issue 06: Desktop Material 3 Expressive Right-Click Context Menu

## Status
resolved

## Description
Add a desktop right-click context menu using Material 3 Expressive specifications. When users right-click on the canvas on Desktop, display a contextual popup menu at the mouse pointer location.

## Details
1. In `CanvasGestureController.kt`, detect right-click releases without drag (`event.activePointer.isSecondaryButton && totalDrag < 8px`).
   - If right-clicking on an element, select it and trigger `onContextMenu(screenPos, worldPoint)`.
   - If right-clicking on empty canvas, trigger `onContextMenu(screenPos, worldPoint)` with canvas actions.
2. Create `DesktopCanvasContextMenu.kt` in `presentation/ui/components/`:
   - Material 3 Expressive `DropdownMenu(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp, shadowElevation = 8.dp)`.
   - Selection actions: Duplicate, Bring to Front, Send to Back, Delete.
   - Canvas actions: Insert Sticky Note, Insert Text, Insert Rectangle, Insert Circle, Select All, Reset Zoom, Clear Canvas.
3. Wire in `CanvasViewportScreen.kt` and `WhiteboardCanvas.kt`.
4. Add unit tests for context menu trigger and action dispatching.
