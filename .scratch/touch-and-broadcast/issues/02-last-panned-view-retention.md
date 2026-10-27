# Issue 02: Last Panned View Retention Across Navigation and Board Switching

Status: resolved
Blockers: none

## Context
When navigating between the boards library and the canvas, or when switching boards, `WhiteboardViewModel` explicitly reset `panOffset = Offset.Zero` and `zoom = 1.0f`, wiping out the user's panned canvas coordinates.

## Acceptance Criteria
1. When switching away from a board, record its active `panOffset` and `zoom`.
2. When switching to or opening a board, restore its recorded `panOffset` and `zoom`.
3. Returning to the canvas from the boards library preserves the current board's panned view instead of resetting to origin `(0, 0)`.
