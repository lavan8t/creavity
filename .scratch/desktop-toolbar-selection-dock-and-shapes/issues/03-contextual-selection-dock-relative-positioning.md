# Issue 03: Contextual Selection Dock Relative Positioning Around Curves/Shapes

## Status
resolved

## Description
User requested: "when seelcting a curve with select, then it will have the copy delete stuff right, it has to be shown right around it, not always in top center hoerving like bullshit."
Currently `FloatingSelectionDock` is hardcoded at `.align(Alignment.TopCenter).statusBarsPadding().padding(top = 54.dp)`, completely detached from the selected curve or element.

## Plan
1. In `CanvasViewportScreen.kt`:
   - Use `selectionBounds: Rect?` which is updated whenever selection changes.
   - Transform `selectionBounds` into screen coordinates:
     - `screenLeft = bounds.left * zoom + panOffset.x`
     - `screenRight = bounds.right * zoom + panOffset.x`
     - `screenTop = bounds.top * zoom + panOffset.y`
     - `screenBottom = bounds.bottom * zoom + panOffset.y`
   - Calculate contextual dock position:
     - `centerX = (screenLeft + screenRight) / 2f`
     - Place above the element: `targetY = screenTop - dockHeight - 12.dp`
     - If `targetY` is too close to the top of the canvas / header bar (< 72.dp), place directly below the element: `targetY = screenBottom + 12.dp`.
     - Clamp `targetX` within viewport `[16.dp, canvasWidth - dockWidth - 16.dp]`.
     - Clamp `targetY` within viewport `[64.dp, canvasHeight - dockHeight - 16.dp]`.
   - Apply `.offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }` to `FloatingSelectionDock`.
2. Add unit test for relative dock positioning and edge boundary clamping.
