# Ticket 05: Floating Contextual Selection Dock & Smart Alignment Guides

Status: resolved
Blocked by: 01, 02, 03, 04

## What it delivers
- Floating Contextual Selection Dock (`FloatingSelectionDock.kt`):
  - Material 3 `ElevatedCard` (`shape = RoundedCornerShape(16.dp)`, `border = null`, `containerColor = surfaceContainerHigh`).
  - Positioned dynamically adjacent to the selection bounding box.
  - Interactive controls:
    - Color swatch palette & stroke width chips
    - Fill style selector (Transparent, Tint, Solid, Hachure)
    - Roughness style selector (Clean, Sketchy, Cartoon)
    - Alignment actions (Align Left, Center, Right, Distribute Vertically/Horizontally)
    - Layer actions (Bring to Front, Send to Back)
    - Quick Duplicate & Delete buttons.
- Smart Alignment Guides (`SmartGuideEngine.kt`):
  - Snaps element bounds (Left, CenterX, Right, Top, CenterY, Bottom) against neighboring elements within 8dp threshold.
  - Draws dashed alignment guides in `MaterialTheme.colorScheme.primary` during translation.

## Verification
- Unit test in `SmartGuideEngineTest.kt` verifying snapping threshold and axis detection.
- `./gradlew :app:testDebugUnitTest` passes.
