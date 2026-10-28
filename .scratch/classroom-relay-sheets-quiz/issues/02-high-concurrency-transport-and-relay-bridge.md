# Ticket 02: High-Concurrency Transport & Android Local-Only Hotspot Relay Bridge

Status: resolved
Blocked by: 01

## What it delivers
- Scale `LanDiscoveryTransport` socket listener and connection registry:
  - Increase concurrent client connection capacity to 100 peers.
  - Employ bounded thread/coroutine pools for peer socket streams without thread starvation.
  - Implement selective routing: forward student sheet strokes only to Host node instead of full mesh broadcast.
- Android Relay Bridge in `androidMain`:
  - `LocalOnlyHotspotBridgeManager`: initiates `WifiManager.startLocalOnlyHotspot()` providing an unmetered, zero-internet offline access point for student tablets.
  - Bridges incoming socket connections between Desktop Host TCP socket and local hotspot clients.
- Handle disconnection cleanup and graceful socket recycling.

## Verification
- Unit tests in `LanDiscoveryTransportTest.kt` verifying concurrent peer registrations up to large counts without memory leaks.
- Unit tests verifying packet forwarding selectively routes directed packets to host.
- `./gradlew :app:testDebugUnitTest` passes.
