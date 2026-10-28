# Ticket 02: Flowchart & Architecture Shapes (Diamond, Cylinder, Star)

Status: resolved
Blocked by: 01

## What it delivers
- Tool registry additions in `DrawingTool`:
  - `DIAMOND` (decision points and branching in flowcharts)
  - `CYLINDER` (database storage, tables, caches)
  - `STAR` (milestones, tags, callouts)
- Tool icons in `MaterialSymbol.kt` mapping "diamond", "database", and "star".
- Geometry computation in `DrawingToolsRenderer.kt` and `RoughShapeGenerator.kt` for Diamond, Cylinder (3D elliptical top + body + base), and 5-point Star.
- Packet serialization support in `PacketSerializer.kt` preserving new tool ordinals across the network.
- Integration into toolbar and keyboard shortcuts.

## Verification
- Unit test in `FlowchartShapesTest.kt` verifying point generation, rough path creation, and bounding box calculations for Diamond, Cylinder, and Star.
- `./gradlew :app:testDebugUnitTest` passes.
