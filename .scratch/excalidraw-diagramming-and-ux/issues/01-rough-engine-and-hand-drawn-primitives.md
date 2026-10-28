# Ticket 01: Native Rough Shape Generator & Hand-Drawn Fill Engine

Status: resolved
Blocked by: none

## What it delivers
- Data models:
  - `RoughnessStyle` (`CLEAN`, `SKETCHY`, `CARTOON`)
  - `FillStyle` (`TRANSPARENT`, `SEMI_TRANSPARENT`, `SOLID`, `HACHURE`)
  - Extended `Stroke` model with `roughness: RoughnessStyle` and `fillStyle: FillStyle`.
- `RoughShapeGenerator.kt`:
  - Multi-pass wobbly line generation with controlled Bézier displacement based on `RoughnessStyle`.
  - Hand-drawn cross-hatch shading (`HACHURE`) for polygons (rectangles, diamonds, ellipses, triangles).
  - Shape geometry caching keyed by stroke ID to ensure static jitter stability between render frames.
- Updated `DrawingToolsRenderer.kt`:
  - Renders rough paths and hachure lines for rectangles, circles, triangles, lines, and arrows.

## Verification
- Unit test in `RoughShapeGeneratorTest.kt` verifying jitter offsets and hachure path generation.
- `./gradlew :app:testDebugUnitTest` passes.
