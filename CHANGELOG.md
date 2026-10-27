# Changelog

All notable changes to CreaVITy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-10-28

### Added
- **Host-Authoritative Board Locking**:
  - Automatically synchronizes joining peers to the host's active board via `WhiteboardPacket.BoardSync` upon connection.
  - Locks board switching, creation, renaming, duplicating, and deletion for non-host participants during live collaborative sessions.
  - Discards out-of-sync packets for non-active boards to prevent cross-board data bleed.
- **Peer In-Flight Stroke Glow & Expressive Cursors**:
  - Multi-layered radiant aura and bloom pass rendered live around in-flight peer strokes based on their chosen color.
  - Deterministic Material expressive shape cursors (Circle, Rounded Square, Diamond, Triangle, Star) and vibrant collaborator presence badges.
- **Material 3 Expressive LoadingIndicator**:
  - Integrated official Material 3 Expressive `LoadingIndicator` featuring animated shape morphing across `MaterialShapes` (SoftBurst, Cookie9Sided, Pentagon, Pill, Sunny, Oval) during radar and peer discovery.
- **Instant Discovery Mutual Exclusivity & Cancel Affordance**:
  - Tapping "Host" (broadcasting) or "Join" (scanning) synchronously marks the active button as "Cancel" and greys out the other with 0ms UI delay, preventing concurrent state conflicts.
  - Added Bluetooth, Wi-Fi, and Nearby Devices readiness checks prompting users to enable required radios when disabled.
- **Persistent Board Viewport (Last Panned View)**:
  - Preserves and restores each board's last panned view (`panOffset` and `zoom`) across library navigation and board switching, eliminating accidental resets to `(0, 0)`.
- **High-Frequency Digitizer Touch Sensitivity & Stylus Support**:
  - Ingests intermediate sub-frame micro-points from `PointerInputChange.historical` to preserve 120Hz–480Hz hardware digitizer sampling rates without angular corner cutting.
  - Strict stylus palm rejection: suppresses accidental single-finger touches while stylus is active, reserving two-finger gestures for canvas pan and pinch-to-zoom.
  - Calibrated power-law pressure transfer curve (`rawPressure ^ 0.65`) providing responsive low-force sensitivity for light sketching and handwriting.

### Removed
- Removed manual direct IP entry card and port inputs (`lan_ip_port`) in favor of zero-configuration local discovery.
- Removed deprecated `autoConnectSameBoard` preference and background polling loop.

### Optimized
- Deleted redundant 4.15MB duplicate font asset (`google_sans_flex.ttf`) and duplicate Android manifest.
- Removed unused `androidx.fragment:fragment-ktx` dependency.
- Optimized `MutationHistory` undo/redo with Kotlin stdlib `ArrayDeque`.
- Pruned dead transitions from `BrevityMotion.kt`.

## [1.1.0] - 2026-10-26

### Added
- **Real-Time Image Sync**: Added full image synchronization across devices via LAN and mesh networking.
- **Board-Scoped Collaboration**: Edits, strokes, and sync packets are strictly scoped to the host-owned board.
- **Windows Desktop UX**: Right-click drag pans canvas; right-click tap clears selection. Keyboard shortcuts (Ctrl+Z, Ctrl+Y, tool hotkeys).

### Fixed
- Resolved SubcomposeLayout intrinsic measurement crash in TopAppBar overflow menu.
- Resolved peer presence canvas layout crash.
- Migrated deprecated Material symbols to AutoMirrored variants.

## [1.0.0] - 2026-10-25

### Initial Release
- Infinite collaborative whiteboard for Android and Desktop built with Jetpack Compose, Material 3, and Kotlin Multiplatform.
- Offline peer-to-peer mesh synchronization with Nearby Connections and LAN multicast.
- Multi-page board support, drawing tools, sticky notes, and text elements.
