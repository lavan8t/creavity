# Ticket 03: Magnetic Connectors & Smart Shape Anchor Snapping

Status: resolved
Blocked by: 01, 02

## What it delivers
- Data model for anchors and bindings:
  - `ShapeAnchor(id, elementId, position: Offset, direction: AnchorDirection)` (North, South, East, West, Center)
  - `Stroke.connectedStartAnchor: String?`, `Stroke.connectedEndAnchor: String?`
- `ConnectorManager.kt`:
  - Computes magnetic anchors for any geometric shape and sticky note.
  - Detects snap proximity when inking an arrow or line ($< 16\text{dp}$) and snaps endpoints directly to the anchor.
  - Visual indicator of active snap anchor (subtle glowing dot on the target shape perimeter).
  - Dynamic Rerouting: When shapes are moved (`BoardAction.MoveElements`), automatically updates connected arrow start/end points.

## Verification
- Unit test in `ConnectorManagerTest.kt` verifying anchor calculation, snapping threshold, and dynamic endpoint updates on shape translation.
- `./gradlew :app:testDebugUnitTest` passes.
