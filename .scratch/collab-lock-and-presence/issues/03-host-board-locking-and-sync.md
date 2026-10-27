# Ticket 03: Enforce Authoritative Host Board Locking and Scoped Edits

Status: resolved
Blocked by: 01

## What it delivers
- In `WhiteboardViewModel`, when a peer is connected and local device is host, immediately send `WhiteboardPacket.BoardSync` with the active board.
- When a non-host peer receives `BoardSync`, immediately switch the peer's active board to match `hostOwnedBoardId`.
- Add `isBoardSwitchingLocked` to `WhiteboardUiState` (true when connected and not host).
- When locked, disable board switching in the top bar / navigation drawer.
- In `WhiteboardSyncHandler` and `WhiteboardViewModel`, reject peer edits and stroke packets that target a mismatched board ID.

## Verification
- Unit test in `RemoteStrokeReceiveTest.kt` verifying that strokes arriving for a mismatched board ID are discarded.
- Unit test verifying `BoardSync` switches peer board and locks board switching.
- `./gradlew :app:testDebugUnitTest` passes.
