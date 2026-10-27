# Spec: Collaborative Board Locking, Peer Presence Glow & Badges, and Cleanup

Status: ready-for-agent

## Problem Statement

Users collaborating in CreaVITy encounter several issues:
1. Connecting via manual IP inputs is cumbersome, unintuitive, and clutters the connection UI with technical networking details like ports and subnet IP addresses.
2. In collaborative sessions, participants can desynchronize by switching boards or submitting edits from different boards, leading to data confusion where participants are not looking at or modifying the same workspace.
3. During live multi-user drawing, it is difficult to spot where collaborators are actively inking, as remote in-progress strokes lack visual pop and peer cursors lack distinct visual personality.
4. The codebase carries dead code, duplicate assets (such as a 4.15MB duplicate font file), and unused dependencies reported in the ponytail audit.

## Solution

1. Simplify the connection surface by removing manual direct IP text fields and auto-connect heuristics; rely purely on zero-config local peer discovery and intentional tap-to-connect.
2. Enforce strict Host-Owned Board locking: upon connection, the host's active board is immediately broadcast and locked across all participants. Only the host can switch or select the board. Edits and stroke packets outside the active board are rejected.
3. Render a luminous outer glow around active, in-flight strokes drawn by remote peers, dynamically matching the peer's chosen stroke color, which cleanly settles once the stroke is committed.
4. Render expressive peer presence cursors using Material shape families (Circle, Rounded Square, Diamond, Triangle, Star) with deterministically assigned vibrant colors for collaborator badges.
5. Apply safe ponytail audit cleanups to reduce binary footprint and prune dead code without affecting production stability.

## User Stories

1. As a whiteboard host, I want my active board to be automatically shared with joining peers, so that we are immediately working on the same canvas without manual board selection.
2. As a whiteboard host, I want to be the sole authority who can change or select the shared board, so that participants remain synchronized on the same topic.
3. As a joining peer, I want to be automatically guided to the host's active board upon connecting, so that I don't have to search for the right board in a library.
4. As a joining peer, I want board switching to be disabled during an active session, so that I cannot accidentally navigate away from the group's active board.
5. As a collaborator, I want to see a luminous glowing aura around lines being actively drawn by other users, so that I immediately notice where someone else is inking on the board.
6. As a collaborator, I want in-progress peer strokes to settle into clean vector lines when drawing is complete, so that the final canvas remains crisp and readable.
7. As a collaborator, I want each peer's cursor on the board to display a distinct Material expressive shape and a unique color-coded badge, so that I can easily distinguish between multiple participants.
8. As a user, I want the peer connection screen to be free of confusing IP address inputs, so that connecting with nearby colleagues is a clean one-tap experience.
9. As an Android user, I want the application size to be minimized by eliminating duplicate font files and unneeded dependencies, so that the app installs faster and uses less storage.

## Implementation Decisions

- **Direct IP Removal**: Remove the "Same-Network Connect" card and `directIpInput` state from `PeerConnectScreen.kt`. Remove `autoConnectSameBoard` preference from `AppSettingsManager.kt` and auto-connect polling in `WhiteboardViewModel.kt`.
- **Host-Owned Board Locking**:
  - In `WhiteboardViewModel`, when a peer connects (`onPeerConnected`), if local device is host, immediately broadcast `WhiteboardPacket.BoardSync` with the active board.
  - In `WhiteboardSyncHandler`, when `BoardSync` is received by a non-host peer, switch immediately to that board and set `hostOwnedBoardId`.
  - In `WhiteboardUiState`, add `isBoardSwitchingLocked = !isHost && connectedPeers.isNotEmpty()`.
  - In `AdaptiveWhiteboardScreen.kt` and `JamboardTopBar.kt`, disable board switching / navigation drawer when locked.
  - In `WhiteboardSyncHandler` and `WhiteboardViewModel`, enforce that stroke and element mutations from peers are only applied if they belong to `hostOwnedBoardId`.
- **Peer Stroke Glow Rendering**:
  - In `WhiteboardCanvas.kt` and `StrokeDrawer.kt`, when rendering live remote strokes from `uiState.livePeerPresences` / `remoteStrokesMap`, draw a multi-layered radiant glow behind the stroke path using the stroke's color with high radius and graduated alpha before drawing the core stroke.
- **Material Shape Cursors & Peer Badges**:
  - In `WhiteboardCanvas.kt`, create deterministic hash mappings from `peerId` to a vibrant M3 palette and an expressive shape (Circle, Rounded Square, Diamond, Triangle, 8-point Star).
  - Render the custom Material shape as the peer's pointer position on canvas alongside the peer's name tag.
- **Ponytail Audit Cleanups**:
  - Delete `app/src/main/res/font/google_sans_flex.ttf`.
  - Delete duplicate `app/src/main/AndroidManifest.xml`.
  - Remove `androidx.fragment:fragment-ktx:1.8.5` from `app/build.gradle.kts`.
  - Remove unused transitions (`enterSlideLeft`, `exitSlideRight`, `enterSlideRight`, `exitSlideLeft`, `springSnappy`, `springGentle`) in `BrevityMotion.kt`.
  - Replace `ArrayList.removeAt(0)` in `MutationHistory.kt` with stdlib `ArrayDeque`.

## Testing Decisions

- Test that receiving `BoardSync` locks the peer's active board to the host board and updates `uiState`.
- Test that stroke packets arriving for a non-active or mismatched board ID are discarded.
- Test that `MutationHistory` maintains undo/redo correctness with `ArrayDeque`.
- Test that peer cursor shape and color assignment is deterministic across recompositions.
- Run JVM unit tests (`./gradlew :app:testDebugUnitTest`) to verify all seams remain green.
