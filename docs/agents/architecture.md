# Architecture: collaboration pipeline

Where a stroke travels, in hop order, so a bug can be localised instead of guessed at. Tests pin the
seams named below.

## Write path (local ink, outbound)

1. `presentation/canvas/CanvasGestureController.kt` — turns pointer events into points and broadcasts
   them under one `activeStrokeId`.
2. `presentation/viewmodel/WhiteboardDispatcher.kt` — maps `OotbAction.StrokeBroadcast*` onto
   `WhiteboardViewModel.broadcastStroke*`.
3. `presentation/viewmodel/WhiteboardViewModel.kt` — `sendPacketToAll`, `isMutation` false for ink.
4. `data/nearby/PeerTransport.kt` — the transport interface; both platforms implement it.
5. `binary/PacketSerializer.kt` — packet → bytes (byte 0 is the packet type).
6. `data/mesh/MeshModels.kt` — bytes wrapped in `MeshEnvelope` (packet id, origin, ttl, payload).

## Read path (inbound ink, to pixels)

1. Transport reader: `data/nearby/LanDiscoveryTransport.kt` (sockets, `DataInputStream`) or
   `androidMain/.../NearbyConnectionsManager.kt` (Nearby payloads; it also wraps the LAN transport).
2. `data/mesh/MeshRoutingEngine.kt` — `processInbound` drops duplicates, expired ttl and unauthorised
   mutations, then returns `DeliverOnly` or `DeliverAndForward`.
3. `binary/PacketSerializer.kt` — `deserialize(envelope.payload)`.
4. `presentation/viewmodel/WhiteboardSyncHandler.kt` — `handleIncomingPacket`: strokes go into
   `remoteStrokesMap` (in flight) and are merged into the page; cursors go into `livePeerPresences`.
5. `presentation/canvas/WhiteboardCanvas.kt` — renders `uiState.strokes`; geometry comes from
   `StrokeGeometryCache`.

## State, by lifetime

| State | Lives in | Lifetime |
|---|---|---|
| Committed strokes, text, notes | `WhiteboardUiState.activePage` | document |
| Peer stroke being streamed | `WhiteboardViewModel.remoteStrokesMap` | until `StrokeEnd` |
| Peer cursors | `WhiteboardUiState.livePeerPresences` | until peer leaves |

## Debugging a sync bug

Set `CREAVITY_PACKET_TRACE=1` (or `-Dcreavity.packetTrace=1`) to append one line per packet to
`packet-trace.log` in the app storage directory: direction, type, wire size, stroke id. It separates
"never sent" from "sent but dropped in routing" from "arrived but not applied".

## Checks

- `./gradlew :app:testDebugUnitTest` — JVM unit tests, the fast loop.
- `./gradlew :app:compileKotlinDesktop` — desktop target compiles.
- CI runs both: `.github/workflows/ci.yml`.
