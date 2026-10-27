# ADR 0001: Collaborative Board Locking, Peer Presence Styling, and IP Simplification

## Status
Accepted

## Context
1. **Manual Direct IP Entry**: Users were exposed to a "Same-Network Connect" card with raw IP inputs and port syntax (`lan_ip_port`), which duplicated automatic zero-config discovery and caused confusion.
2. **Board Desynchronization**: Peers could switch boards independently or send edits intended for a previous board, violating collaborative whiteboard expectations where all attendees look at and edit the same canvas.
3. **Presence Feedback**: During collaborative drawing, remote strokes blended with local strokes without distinct visual energy, and peer pointers lacked expressive visual distinction.
4. **Maintenance & Bloat**: Redundant font files, duplicate manifests, and unused dependencies added noise and size.

## Decision
1. **Remove Direct IP Input**: Completely remove the manual IP connection card from `PeerConnectScreen` and remove `autoConnectSameBoard` preference and auto-join loop. All connections rely on clean peer discovery and explicit tap-to-connect.
2. **Authoritative Host Board Locking**:
   - As soon as a peer connects, the host's active board is immediately synchronized and selected by the peer.
   - Non-host peers cannot switch boards, rename boards, or navigate to boards library while connected; only the host can select or change the active board.
   - All mutations and sync packets are strictly filtered and scoped to the host-owned board.
3. **Live Peer Stroke Glow**:
   - While a remote peer draws (`remoteStrokesMap`), render an outer radiant glow around the in-flight stroke based on the stroke's color.
   - Settle into the clean committed vector stroke on `StrokeEnd`.
4. **Expressive Material Shape Cursors & Color Badges**:
   - Peer badges receive a deterministic vivid color tag based on `peerId`.
   - Peer cursors are rendered using shapes from the Material shape family (Circle, Rounded Square, Diamond, Triangle, Star/Polygon) determined by `peerId`.
5. **Ponytail Audit Cleanups**:
   - Delete duplicate font asset `app/src/main/res/font/google_sans_flex.ttf`.
   - Delete duplicate `app/src/main/AndroidManifest.xml`.
   - Remove unused `androidx.fragment:fragment-ktx` dependency.
   - Remove dead transitions in `BrevityMotion.kt`.
   - Replace O(N) `ArrayList.removeAt(0)` with O(1) `ArrayDeque.removeFirst()` in `MutationHistory.kt`.

## Consequences
- Single source of truth for the collaborative canvas: zero board divergence.
- Cleaner, intuitive peer connect experience without raw networking technicalities.
- Visually clear and distinct real-time multi-user drawing.
- Reduced app binary size and codebase maintenance overhead.
