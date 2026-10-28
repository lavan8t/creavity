# ADR 0004: Excalidraw-Style Diagramming, Hand-Drawn Rough Engine, and Magnetic Connectors

## Status
Accepted

## Context
1. **Expressive Visual Identity (Hand-Drawn vs Technical)**: Digital whiteboards such as Excalidraw and tldraw resonate strongly because hand-drawn diagramming feels collaborative, approachable, and creative compared to rigid CAD/vector boxes. CreaVITy requires a native Rough.js-inspired rendering algorithm that injects controlled multi-pass jitter into lines and shapes while maintaining performant 60–120 FPS rendering.
2. **Technical Diagramming & Flowcharts**: Users need first-class support for flowcharts, decision trees, and system architectures. Standard drawing shapes (rectangle, circle, triangle) are insufficient without `DIAMOND` (decision nodes), `CYLINDER` (database storage), and `STAR` (milestones/highlights).
3. **Smart Magnetic Connectors**: Diagramming requires connections between components (flowchart steps, architecture blocks). In static drawing tools, moving a box leaves lines disconnected. In modern diagramming tools, arrows magnetically bind to shape anchor perimeters and dynamically update when objects move.
4. **Embedded In-Shape Typography**: Separate text boxes are cumbersome to maintain when diagramming. Double-tapping any geometric shape should allow direct in-shape text editing where text is automatically centered and bound to the node.
5. **Contextual In-Situ Editing UX**: Users should not need to travel back and forth to bottom sheets or top bars for simple property changes (fill, stroke color, stroke width, roughness, alignment). A floating contextual dock positioned adjacent to active selections provides immediate, tactile control.
6. **Smart Snapping & Alignment Guides**: Freehand dragging often leads to messy diagrams unless dynamic alignment guides magnetically snap collinear edges and centroids.
7. **Vector & Raster Export**: Fast export to transparent/solid PNG, clean SVG vectors, and immediate OS clipboard copy is essential for sharing whiteboard diagrams to chat, docs, and presentation decks.

## Decision

1. **Native Compose / Skia Roughness Engine**:
   - Create `RoughShapeGenerator`: Computes multi-pass jittered lines using controlled Bézier curve perturbation based on `RoughnessStyle` (`CLEAN`, `SKETCHY`, `CARTOON`).
   - Implement `FillStyle` (`TRANSPARENT`, `SEMI_TRANSPARENT`, `SOLID`, `HACHURE`). Hachure generates parallel cross-hatch shading lines with slight angular variance to emulate architectural marker sketching.
   - Cache generated rough paths per stroke/shape ID so static shapes do not re-jitter on every frame, preserving battery and rendering performance.

2. **Expanded Diagramming Primitives (`DIAMOND`, `CYLINDER`, `STAR`)**:
   - Add `DrawingTool.DIAMOND`, `DrawingTool.CYLINDER`, and `DrawingTool.STAR` to the tool registry and binary packet serialization.
   - Render each with configurable roughness and fill styles.

3. **Magnetic Anchor Snap System**:
   - Equip all canvas elements (rectangles, diamonds, cylinders, circles, sticky notes) with 5 magnetic anchors: `North`, `South`, `East`, `West`, and `Center`.
   - When drawing or moving an arrow or line near an anchor point (< 16dp), snap the endpoint to the anchor.
   - When an element is moved, any connected arrow endpoints automatically track and update to stay seamlessly attached.

4. **Embedded Shape Typography**:
   - Add `embeddedText: String?` and `embeddedTextColor: Long` to geometric shape models.
   - Double-tapping a shape focuses an inline text editor centered directly inside the shape bounding box.
   - Embedded text is drawn centered with automatic wrapping and moves synchronously with the shape during translation, scaling, or duplication.

5. **Floating Material 3 Expressive Contextual Property Dock**:
   - When elements are selected (`selectedStrokes.isNotEmpty() || selectedTexts.isNotEmpty()`), render an elevated floating dock (`ElevatedCard`, shape `RoundedCornerShape(16.dp)`, `border = null`) anchored adjacent to the selection bounding box.
   - Provides quick-touch buttons for:
     - Color swatch selector & stroke width chips,
     - Fill style toggle (Transparent, Translucent, Solid, Hachure),
     - Roughness style toggle (Clean, Sketchy, Cartoon),
     - Alignment tools (Align Left, Center, Right, Distribute Vertically/Horizontally),
     - Duplicate, Bring Forward, Send Backward, and Delete.

6. **Smart Alignment Guides**:
   - During element translation, compute bounding box edges and center points against all other canvas elements within a snap threshold (8dp).
   - Draw subtle alignment lines in `MaterialTheme.colorScheme.primary` and snap the active translation coordinates to snap axes.

7. **Multi-Format Canvas Export Pipeline**:
   - Provide an Export modal supporting:
     - Scope: Full Board vs Selected Elements Only,
     - Format: PNG (300 DPI high-res, optional transparent background) and SVG (vector XML path elements),
     - Quick "Copy Image to Clipboard" button utilizing platform clipboard APIs.

## Consequences
- Creates a comprehensive, hand-drawn diagramming experience comparable to Excalidraw and tldraw natively inside Android and Desktop Compose.
- Maintains strict compliance with Material 3 Expressive principles: zero custom borders, semantic token colors, expressive rounded shapes, and fluid motion.
- Diagramming workflows (flowcharts, architectures, mind maps) become effortless with embedded text and magnetic arrows.
- Path caching prevents battery drain or jank from repeated jitter recalculation.
