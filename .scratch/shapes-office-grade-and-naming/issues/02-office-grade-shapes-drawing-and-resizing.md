# Issue 02: Office-Grade Shapes Drawing & Interactive 8-Point Bounding Box Resizing

## Status
resolved

## Description
Shapes should behave like professional vector objects in office suites (Word, PowerPoint, Google Slides, Figma). Default shape roughness must be `RoughnessStyle.CLEAN` (crisp lines and smooth curves instead of wobbly sketchy tremors). Additionally, selected shapes must feature interactive resize handles (8-point bounding box) allowing users to scale and reshape objects directly on canvas.

## Tasks
- [x] Set default `roughness` in `Stroke` model to `RoughnessStyle.CLEAN`.
- [x] In `StrokeDrawer.kt`, render clean geometric paths directly for `DrawingTool.RECTANGLE`, `DrawingTool.CIRCLE`, `DrawingTool.TRIANGLE`, etc. when `roughness == RoughnessStyle.CLEAN`.
- [x] Implement 8-point resize handles in `CanvasGestureController.kt` (NW, N, NE, E, SE, S, SW, W).
- [x] Render 8 resize handles in `WhiteboardCanvas.kt` on the selection bounding box.
- [x] Support interactive dragging of handles to resize selected strokes/shapes horizontally, vertically, or diagonally.
- [x] Add unit tests verifying clean shape geometry and resize computation.
