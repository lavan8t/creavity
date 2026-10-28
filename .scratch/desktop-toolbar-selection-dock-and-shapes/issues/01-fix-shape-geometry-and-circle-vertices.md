# Issue 01: Fix Shape Geometry and Circle/Vertex Construction

## Status
resolved

## Description
User reported: "shapes dont work well. teh corners, the circle is a decagod or nonagon wtf. fix the vertices proeprly sitting. etc."
Currently:
1. In `WhiteboardCanvas.kt` (lines 331 and 471), `drawStyledStroke` was invoked without passing `roughness = activeStroke.roughness` and `fillStyle = activeStroke.fillStyle`. This caused all shapes to fall back to the default `RoughnessStyle.SKETCHY`.
2. In `RoughShapeGenerator.kt:208`, `createRoughEllipse` used a crude loop with `stepCount = 12` connecting points with straight `lineTo(x, y)` calls. This generated a 12-sided polygon (decagon/dodecagon) instead of a smooth circle!
3. Corner vertices in rectangles and triangles had overshoot lines that did not meet cleanly at vertex points.

## Plan
1. In `StrokeDrawer.kt`, update default `roughness` parameter to `RoughnessStyle.CLEAN`.
2. In `WhiteboardCanvas.kt`:
   - Pass `roughness = activeStroke.roughness`, `fillStyle = activeStroke.fillStyle`, and `strokeId = activeStroke.id` in all `drawStyledStroke` calls.
   - For in-progress shape drawing, pass `roughness = RoughnessStyle.CLEAN` (or the tool's selected roughness).
3. In `RoughShapeGenerator.kt`:
   - Upgrade `createRoughEllipse`: replace the 12-step polygon with smooth cubic Bézier segments (4 cubic segments per pass with subtle organic control point jitter) and 48+ point smoothing, ensuring circles are always smooth and curvy, never decagons or nonagons.
   - For `createRoughRectangle` and `createRoughTriangle`: ensure vertices connect precisely at corner endpoints without broken or floating lines.
4. Add unit test verifying circle curvature and path smoothness.
