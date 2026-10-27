# Ticket 01: Remove Direct IP Connect and Auto-Connect UI

Status: resolved
Blocked by: none

## What it delivers
Removes the manual "Same-Network Connect" card, text field, connect button, and local IP display from `PeerConnectScreen.kt`. Removes the `autoConnectSameBoard` preference from `AppSettingsManager.kt` and auto-join loop from `WhiteboardViewModel.kt`. Discovery and connections now occur cleanly through standard peer beaconing.

## Verification
- `PeerConnectScreen` renders without any manual IP input card.
- `./gradlew :app:testDebugUnitTest` passes.
