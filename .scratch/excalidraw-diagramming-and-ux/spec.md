# Specification: Excalidraw-Style Diagramming, Hand-Drawn Rough Engine, and UX Upgrades

## Overview
Transform CreaVITy into an expressive, Excalidraw-grade visual thinking and technical diagramming environment natively built on Jetpack Compose and Compose Multiplatform (Android & Desktop), featuring hand-drawn rough vector rendering, flowchart shapes, magnetic arrow connectors, embedded shape text, contextual floating property docks, smart alignment guides, and high-fidelity PNG/SVG export.

---

## 1. Domain & Data Models

### 1.1 Roughness and Fill Enums
```kotlin
enum class RoughnessStyle {
    CLEAN,     // Geometric crisp lines (Roughness = 0f)
    SKETCHY,   // Natural hand-drawn multi-pass tremor (Roughness = 1.2f)
    CARTOON    // High-jitter comic style (Roughness = 2.4f)
}

enum class FillStyle {
    TRANSPARENT,      // Outline only
    SEMI_TRANSPARENT, // Translucent color tint (alpha = 0.2f)
    SOLID,            // Opaque color fill
    HACHURE           // Parallel cross-hatch hand-drawn shading
}
```

### 1.2 Drawing Tools Extension
Add flowchart primitives to `DrawingTool`:
- `DrawingTool.DIAMOND`
- `DrawingTool.CYLINDER`
- `DrawingTool.STAR`

### 1.3 Element Properties Extension
Update `Stroke` (or dedicated shape metadata):
- `roughness: RoughnessStyle = RoughnessStyle.SKETCHY`
- `fillStyle: FillStyle = FillStyle.TRANSPARENT`
- `embeddedText: String? = null`
- `connectedStartAnchor: String? = null` // e.g. "sn_123_EAST" or "stroke_456_CENTER"
- `connectedEndAnchor: String? = null`

---

## 2. Roughness Algorithm & Path Generator (`RoughShapeGenerator.kt`)

1. **Jittered Lines**:
   - For line segments $(p_1, p_2)$, perturb end points by $R \cdot (\text{rand} - 0.5)$.
   - Generate 2 overlapping paths with slight Bézier midpoint displacement ($R \cdot 1.5$) to simulate two pen passes.
2. **Hachure Shading**:
   - For enclosed polygons (rectangles, diamonds, ellipses), calculate bounding box and render lines angled at $-45^\circ$ spaced by step interval (e.g. $12\text{dp}$).
   - Clip hachure lines to shape boundary and apply slight jitter to each hatch line.
3. **Geometry Cache**:
   - Cache generated `List<Path>` by `(strokeId, strokePoints, roughness, fillStyle)` so re-rendering unchanged frames does not re-randomize jitter.

---

## 3. Magnetic Connectors & Snapping (`ConnectorManager.kt`)

1. **Anchor Generation**:
   - For every shape and sticky note, compute 5 canonical anchors in canvas space:
     - `NORTH`: Top edge midpoint
     - `SOUTH`: Bottom edge midpoint
     - `EAST`: Right edge midpoint
     - `WEST`: Left edge midpoint
     - `CENTER`: Centroid
2. **Snapping on Inking / Dragging**:
   - When drawing an Arrow or Line, if the start or end point is within $16\text{dp}$ of an anchor, snap the point directly to the anchor coordinate.
   - Record the anchor binding ID on the stroke.
3. **Dynamic Rerouting on Move**:
   - When an element is moved, any connected arrow endpoints bound to its anchors update their endpoints automatically.

---

## 4. In-Shape Embedded Text (`ShapeEmbeddedText.kt`)

1. **Text Attachment**:
   - Double-tapping any geometric shape opens inline basic text editing centered in the shape.
   - Drawn using `drawText` centered vertically and horizontally within the shape bounds.
   - When moving, resizing, or duplicating the shape, embedded text stays bound.

---

## 5. Floating Contextual Selection Dock (`FloatingPropertyDock.kt`)

1. **Material 3 Expressive Component**:
   - An `ElevatedCard` (`shape = RoundedCornerShape(16.dp)`, `border = null`, `containerColor = surfaceContainerHigh`).
   - Positioned floating above or below the selection bounding box.
   - Controls:
     - Color swatch selector (8 palette tones),
     - Stroke width selector,
     - Fill style toggle (`TRANSPARENT`, `TINT`, `SOLID`, `HACHURE`),
     - Roughness toggle (`CLEAN`, `SKETCHY`, `CARTOON`),
     - Alignment buttons (Align Left, Center, Right, Distribute Vertically/Horizontally),
     - Quick actions: Duplicate, Bring to Front, Send to Back, Delete.

---

## 6. Smart Alignment Guides (`SmartGuideEngine.kt`)

1. **Snapping Detection**:
   - When translating an element, compare its left, center X, right, top, center Y, and bottom coordinates against all other canvas objects.
   - If any delta is $\le 8\text{dp}$, snap translation to that coordinate and return active guide line segments.
2. **Visual Rendering**:
   - Render subtle dashed guide lines in `MaterialTheme.colorScheme.primary` with indicator markers showing equal spacing or aligned centers.

---

## 7. Vector & Raster Export (`CanvasExportManager.kt`)

1. **PNG Export**:
   - Render canvas bounding box to an offscreen Skia bitmap (`ImageBitmap`) at 2x/3x DPI.
   - Support transparent background or canvas theme background.
2. **SVG Export**:
   - Serialize canvas elements into valid SVG XML paths, rects, circles, texts, and polygons with rough paths.
3. **Copy to Clipboard**:
   - Export image bytes and copy directly to system clipboard for instant paste into chat/docs.

---

## 8. Verification Strategy
- Unit tests for `RoughShapeGeneratorTest.kt`: verifies jitter bounds, multi-pass generation, and hachure line calculation.
- Unit tests for `ConnectorManagerTest.kt`: verifies anchor computation, magnetic snapping, and rerouting on element translation.
- Unit tests for `SmartGuideEngineTest.kt`: verifies collinear edge and center detection.
- Unit tests for `CanvasExportTest.kt`: verifies SVG markup generation and PNG rasterization.
- Full verification: `./gradlew :app:testDebugUnitTest :app:compileKotlinDesktop`.
