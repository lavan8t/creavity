# Issue 01: View-Only Toolbar Gating & Read-Only Status Pill

## Status
Implemented and Verified

## Description
When a board is read-only (`isCanvasReadOnly == true`), drawing and creation tools should be completely suppressed. A subtle Material 3 Expressive pill should inform the user that the canvas is in view-only mode, without blocking viewport gestures (pan, pinch-to-zoom) or showing disabled buttons.

## Tasks
- [x] Inspect `CanvasViewportScreen.kt` for toolbars, floating property dock, FABs, and callouts rendered when editing.
- [x] Conditionally suppress `CanvasBottomToolbar` and floating docks when `isCanvasReadOnly == true`.
- [x] Add `ViewOnlyPill`: A centered bottom `ElevatedCard(shape = CircleShape, border = null)` containing a visibility/lock icon and `"View Only"` text styled with Material 3 Expressive typography.
- [x] Ensure pan, zoom, fit-to-screen, and laser pointer interactions remain fully functional in read-only mode.
- [x] Add unit tests verifying view-only gating flags and UI state invariants.
