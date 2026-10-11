# Compose Multiplatform Migration Design Specification

## 1. Overview and Objectives

This project migrates CreaVITy from an asymmetric architecture (Jetpack Compose Android app + WinUI 3 C# desktop app) to a unified **Compose Multiplatform (CMP)** application targeting Android and Windows Desktop (JVM).

### Objectives
- **Zero duplicated UI**: 100% of the UI (whiteboard canvas, toolbars, screens, dialogs, cards, themes, animations) resides in `commonMain` and executes identically on Android and Windows Desktop.
- **Shared logic and models**: Domain models, ViewModels, mutation history, state machines, and binary packet serializers reside in `commonMain`.
- **Dedicated platform networking**:
  - Android: Cross-platform TCP/UDP LAN mesh + Google Play Services Nearby Connections.
  - Windows Desktop: Pure Kotlin/JVM TCP/UDP LAN mesh (`java.net.DatagramSocket` discovery and `java.net.ServerSocket`/`Socket` peer connections).
- **Typography**: Google Sans Flex typography tokens across both platforms.

---

## 2. Architecture and Source Set Structure

The `:app` module is reconfigured into a Kotlin Multiplatform module applying `org.jetbrains.kotlin.multiplatform`, `org.jetbrains.compose`, and `com.android.application`.

```
app/
├── src/
│   ├── commonMain/kotlin/com/kd/creavity/
│   │   ├── domain/               # Domain models, math, data structures
│   │   ├── presentation/
│   │   │   ├── canvas/           # WhiteboardCanvas, renderers, color utils
│   │   │   ├── ui/               # All screens, components, symbols, dialogs
│   │   │   └── viewmodel/        # WhiteboardViewModel, UiState, SyncHandler
│   │   ├── data/
│   │   │   ├── binary/           # PacketSerializer, packet hierarchy
│   │   │   └── nearby/           # PeerTransport interface, shared mesh logic
│   │   └── ui/theme/             # Google Sans Flex, M3 tokens, color schemes
│   │
│   ├── androidMain/kotlin/com/kd/creavity/
│   │   ├── MainActivity.kt       # Android Activity launcher
│   │   └── data/nearby/          # Android Nearby Connections + LAN transport
│   │
│   └── desktopMain/kotlin/com/kd/creavity/
│       ├── main.kt               # Desktop Window launcher
│       └── data/nearby/          # Windows JVM LAN socket mesh transport
```

---

## 3. Platform Abstractions

Where Android-specific APIs were previously called directly:

1. **File Pickers**:
   - `rememberFilePickerLauncher(onResult: (PlatformFile?) -> Unit)` expect/actual composable helper.
   - Android: Wraps `ActivityResultContracts.GetContent()` / `OpenDocument()`.
   - Desktop: Wraps `java.awt.FileDialog` running on Swing dispatcher.

2. **Typography**:
   - Google Sans Flex font definition packaged in Compose resources / font family provider with system fallback (`Segoe UI` on Windows, `Roboto` on Android).

3. **Peer Transport**:
   - `PeerTransport` interface in `commonMain`:
     ```kotlin
     interface PeerTransport {
         val discoveredPeers: StateFlow<List<PeerDevice>>
         val connectedPeers: StateFlow<List<PeerDevice>>
         val incomingPackets: SharedFlow<WhiteboardPacket>
         fun startAdvertising(deviceName: String, roomCode: String)
         fun stopAdvertising()
         fun startDiscovery()
         fun stopDiscovery()
         fun connectToPeer(peerId: String)
         fun disconnectPeer(peerId: String)
         fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean = false, ticket: Long = 0L)
     }
     ```
   - Android implements `PeerTransport` using unified LAN sockets and Google Play Nearby Connections.
   - Desktop implements `PeerTransport` using pure JVM LAN sockets (`DatagramSocket` + `ServerSocket`).

---

## 4. Desktop Entry Point and Packaging

- Entry point: `app/src/desktopMain/kotlin/com/kd/creavity/Main.kt`.
- Uses `androidx.compose.ui.window.Window` with a default size of 1280x800 and title "CreaVITy".
- Gradle tasks:
  - `./gradlew :app:run` runs the Windows desktop application in development.
  - `./gradlew :app:packageMsi` / `:app:packageExe` produces native Windows packages.

---

## 5. Verification Plan

1. **Gradle Build Configuration**:
   - Run `./gradlew :app:tasks` to verify Kotlin Multiplatform and Compose Desktop targets are configured.
2. **Desktop Compilation & Execution**:
   - Run `./gradlew :app:compileKotlinDesktop` to verify shared UI and desktop source sets compile without errors.
3. **Android Compilation**:
   - Run `./gradlew :app:compileDebugKotlin` to verify Android target compiles without errors.
4. **Interactive Verification**:
   - Run desktop app and verify canvas drawing, tools, and theme rendering.
