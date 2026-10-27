# Issue 02: Historical Digitizer Points Ingestion for Maximum Touch Sensitivity

Status: resolved
Blockers: none

## Context
High-refresh Android digitizers (120Hz–480Hz) sample micro-points between Compose display frames and store them in `PointerInputChange.historical`. Because `toCanvasInputEvent()` only evaluated `change.position` at the frame boundary, high-speed drawing and cursive handwriting dropped intermediate samples, causing corner clipping and reduced touch sensitivity.

## Acceptance Criteria
1. `CanvasInputEvent` supports carrying intermediate historical points for the active pointer.
2. `PointerEvent.toCanvasInputEvent()` unpacks all entries from `change.historical` with intermediate sub-pixel coordinates, timestamps, and pressure values.
3. `CanvasGestureController` during `MOVE` processes each historical micro-point along the stroke trajectory before applying the latest point, creating butter-smooth quadratic bezier segments.
4. Fast strokes and rapid handwriting preserve continuous curvature without angular corner clipping.
