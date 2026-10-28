# Issue 01: Board Card Badge Layout and Select Tool Cursor Pointer

## Status
resolved

## Description
1. Fix card footer in `BoardCardComponents.kt` so Title and Actions (Star, 3-dots) are horizontally aligned on the first row, and `BoardMetadataBadges` ("Shared by fedora", venue, tags) sit comfortably below on their own row with proper padding (never clipped or touching the bottom edge).
2. Generate official Material Symbols Rounded `arrow_selector_tool` ImageVector. Map `"cursor"`, `"arrow_selector_tool"`, and `"select"` to it.
3. Update `CanvasBottomToolbar.kt`, `DesktopDraggableToolbar.kt`, and `ToolPointerIcon.desktop.kt` to use the cursor pointer arrow instead of the hand (`pan_tool_alt`).
4. Update `FloatingSelectionDock.kt` to add a "Done" / deselect button.
5. In `CanvasViewportScreen.kt` and `AdaptiveWhiteboardScreen.kt`, auto-dismiss the dock / clear selections when user switches to a non-selection tool or presses Escape.

## Tasks
- [x] Refactor `BoardCard` footer in `BoardCardComponents.kt`.
- [x] Create `Arrow_selector_toolSymbol.kt` in `presentation/ui/symbols/generated/`.
- [x] Map `"cursor"` and `"arrow_selector_tool"` in `MaterialSymbol.kt`.
- [x] Update `CanvasBottomToolbar.kt` and `DesktopDraggableToolbar.kt` to use `"cursor"`.
- [x] Update `ToolPointerIcon.desktop.kt` cursor spec to use `arrow_selector_tool` at hotspot `(5, 3)`.
- [x] Add Done / deselect action in `FloatingSelectionDock.kt` and tool-switch auto-clear in `CanvasViewportScreen.kt`.
