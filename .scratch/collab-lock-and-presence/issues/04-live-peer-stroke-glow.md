# Ticket 04: Live Luminous Glow for In-Flight Peer Strokes

Status: resolved
Blocked by: 03

## What it delivers
- In `StrokeDrawer.kt` / `WhiteboardCanvas.kt`, when rendering live strokes streamed in by remote peers (`remoteStrokesMap`), draw an outer soft radiant glow halo using the peer's stroke color (multi-layer graduated blur/alpha pass).
- When `StrokeEnd` is received, the stroke commits into `uiState.strokes` and renders as the normal clean vector stroke.

## Verification
- Remote in-flight strokes render with radiant glow pass.
- `./gradlew :app:testDebugUnitTest` passes.
