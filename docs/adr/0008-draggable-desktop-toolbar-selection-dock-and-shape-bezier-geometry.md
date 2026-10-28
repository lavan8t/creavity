# 0008. Draggable Desktop Toolbar, Contextual Selection Dock, and Cubic Bézier Shape Geometry

Date: 2026-10-28

## Status
Accepted

## Context
User reported three critical canvas interaction and rendering issues:
1. **Broken Shape Geometry & Vertex Construction**:
   - Circles were drawn as rough 12-segment polygons (decagons / dodecagons) rather than smooth curves.
   - Rectangles and triangles exhibited disconnected, overshooting or unclosed corners when drawn upwards or leftwards.
   - Root causes:
     - In `RoughShapeGenerator.createRoughEllipse`, ellipses used a naive 12-segment polyline (`stepCount = 12`) connected by straight `lineTo` segments.
     - In `WhiteboardCanvas.kt`, `drawStyledStroke` omitted the stroke's `roughness` parameter, falling back to default `SKETCHY`, which routed all shapes to the polygon generator.
     - In `PathUtils.kt` and `RoughShapeGenerator.kt`, coordinate bounds for shapes drawn in negative quadrants (dragged up-left) were not normalized with `minOf`/`maxOf`, causing skewed vertices.
2. **Desktop Drawing Toolbar**:
   - The fixed bottom-center toolbar is designed for thumb reach on mobile phones and tablets. On widescreen desktop monitors, it consumes awkward horizontal canvas real estate and cannot be moved away from artwork.
3. **Contextual Selection Dock**:
   - When selecting an element (curve, shape, or text), the action bar (copy, delete, roughness, layers) was fixed at screen `TopCenter`, completely detached from the selected curve/shape.

## Decision

1. **Cubic Bézier Ellipse Geometry & Clean Shape Default**:
   - Replaced the 12-segment polygon in `RoughShapeGenerator.createRoughEllipse` with 4-quadrant cubic Bézier curves using Skia standard cubic ratio $k = 0.55228475f$. Even with organic hand-drawn roughness, the curve maintains $C^1$ continuity and smooth curvature without polygon corners.
   - When `roughness <= 0.05f` (or `RoughnessStyle.CLEAN`), `createRoughEllipse` directly emits an exact vector oval (`addOval`) without jitter.
   - Updated `StrokeDrawer.kt` default roughness from `SKETCHY` to `RoughnessStyle.CLEAN`.
   - Updated `WhiteboardCanvas.kt` to forward `roughness = activeStroke.roughness`, `fillStyle = activeStroke.fillStyle`, and `strokeId = activeStroke.id` during both live drawing and static rendering.
   - Normalized bounding coordinates in `PathUtils.kt` (`DrawingTool.TRIANGLE`, `RECTANGLE`, `STAR`, `DIAMOND`) so shapes dragged upwards/leftwards maintain closed, clean corners.

2. **Draggable Desktop Material 3 Expressive Toolbar**:
   - Built `DesktopDraggableToolbar.kt` in `presentation/ui/components/`:
     - Vertical dock styled with `RoundedCornerShape(24.dp)`, `surfaceContainerHigh`, tonal elevation 6.dp, shadow elevation 8.dp.
     - Distinctive top drag handle pill supporting pointer drag gestures (`detectDragGestures`).
     - Default position on the left edge (`x = 24.dp, y = 100.dp`), clamped within the active window boundaries.
     - Automatically repositions flyout callout cards (pen options, shape pickers, eraser modes) adjacent to the current toolbar coordinate.
   - On mobile (`isDesktopPlatform() == false`), the classic bottom toolbar with FAB is preserved.

3. **Contextual Selection Dock Relative Positioning**:
   - Created `SelectionDockPositioner.kt` in `presentation/ui/components/`:
     - Converts world selection bounds (`selectionBounds`) to screen viewport coordinates using the canvas `zoom` and `panOffset`.
     - Dynamically positions `FloatingSelectionDock` centered directly above the selection (`targetY = screenTop - dockHeight - 16.dp`).
     - Automatically flips directly below the selection (`targetY = screenBottom + 16.dp`) if the element is near the top edge (< 72.dp from the status bar / top bar).
     - Clamps horizontally within viewport margins (`[16.dp, canvasWidth - dockWidth - 16.dp]`).
   - In `CanvasViewportScreen.kt`, replaced the hardcoded `.align(Alignment.TopCenter)` with `.offset { dockOffset }`.

## Consequences
- Circles and ellipses render as smooth, anti-aliased curves without decagonal or polygonal facets.
- Rectangles, triangles, and stars connect cleanly at vertices regardless of drawing direction.
- On Desktop, artists have a floating, draggable M3 Expressive dock on the left, keeping the canvas center unobstructed.
- Selection actions (copy, delete, layers, roughness) appear directly adjacent to the selected curve or shape.
- Zero regressions across JVM desktop tests and Android unit tests.
