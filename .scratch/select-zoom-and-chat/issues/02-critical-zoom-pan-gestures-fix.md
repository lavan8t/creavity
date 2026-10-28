# Issue 02: Critical Canvas Zoom & Pan Gestures Fix

## Status
resolved

## Description
Resolve critical issues where:
1. Canvas only scrolled vertically (up/down). Horizontal scrolling (left/right) failed because mouse wheel only generates `scrollDeltaY` and `isShiftPressed` was ignored.
2. Middle-mouse button (`isTertiaryPressed`) and secondary clicks were being marked as `CanvasPointerAction.CANCEL` in `CanvasInputEvent.kt` because `isPrimaryDown` checked only `buttons.isPrimaryPressed`.
3. Zoom with mouse wheel failed when Ctrl/Meta/Alt modifiers were not handled uniformly.
4. Add visible on-screen Zoom pill controls (`[-] 100% [+]`) in `JamboardTopBar.kt` and keyboard shortcuts (`Ctrl++`, `Ctrl+-`, `Ctrl+0`).

## Tasks
- [x] In `CanvasInputEvent.kt`:
  - Capture `isShiftPressed`, `isAltPressed`, `isMetaPressed`, `isTertiaryPressed`.
  - Fix `isDown` to recognize mouse primary, secondary, and tertiary buttons so drag events are not cancelled.
- [x] In `CanvasGestureController.kt`:
  - Support `Shift + scrollDeltaY` and `scrollDeltaX` for horizontal panning (left/right).
  - Support `isCtrl || isMeta || isAlt` scroll for zooming centered on focal point.
  - Support middle-mouse button (`isTertiaryPressed`) drag for omnidirectional panning.
- [x] In `JamboardTopBar.kt`:
  - Add Material 3 Expressive Zoom pill (`[-] 100% [+]`) with zoom in, zoom out, and reset zoom actions.
- [x] In `AdaptiveWhiteboardScreen.kt`:
  - Add keyboard shortcuts for Zoom (`Ctrl` + `+`/`=`, `Ctrl` + `-`, `Ctrl` + `0`).
