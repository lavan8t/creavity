# Ticket 03: Participant Engagement Telemetry & Lifecycle Observers

Status: resolved
Blocked by: 01

## What it delivers
- Define `EngagementState` enum: `WATCHING`, `NOT_WATCHING`, `OFFLINE`.
- Extend `PeerPresence` model with `engagementState: EngagementState` and `lastHeartbeatMs: Long`.
- Implement `EngagementObserver` on Android using `LifecycleEventObserver`:
  - `ON_PAUSE` / `ON_STOP` immediately sends `WhiteboardPacket.EngagementHeartbeat(..., NOT_WATCHING)`.
  - `ON_RESUME` immediately sends `WhiteboardPacket.EngagementHeartbeat(..., WATCHING)`.
- On Desktop, bind to window focus/activation listeners.
- Add heartbeat loop in `WhiteboardViewModel` (every 3 seconds). When peer heartbeats lapse for > 9 seconds, mark peer as `OFFLINE`.
- Render live presence status pills in connected peers bottom sheet and participant lists:
  - 🟢 Watching (Active in foreground)
  - 🟠 Away (Minimized / Not watching)
  - ⚪ Offline (Disconnected)

## Verification
- Unit test verifying lifecycle event transitions update local and remote peer engagement states.
- Unit test verifying missed heartbeat timeout marks peer as OFFLINE.
- `./gradlew :app:testDebugUnitTest` passes.
