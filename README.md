# CreaVITy

> **CreaVITy** is a modern, high-performance offline collaborative digital whiteboard for Android, built with Jetpack Compose, Material 3 Expressive, and Google Jetpack Ink.

---

## Highlights & Features

- **Realistic Drawing Engines**
  - **Ballpoint Pen**: Realistic ballpoint ink physics with velocity dampening and controlled pressure sensitivity ($0.88\times$ to $1.12\times$).
  - **Graphite Pencil**: Authentic graphite texture with paper tooth, grain striae, pressure-dependent darkness, and stylus tilt-based shading. Deterministic noise ensures zero flickering across zooms and exports.
  - **Broad-Edge Calligraphy**: $45^\circ$ chisel nib geometry producing thick-and-thin lettering with smooth Bezier contour ribbons.
  - **Other Tools**: Marker, Brush, Highlighter, Eraser, Shapes (Line, Arrow, Rectangle, Circle, Triangle), Sticky Notes, Text, and an Ephemeral Glowing Laser pointer.

- **Offline Mesh Collaboration**
  - Instant peer-to-peer collaboration over Bluetooth LE and Wi-Fi Direct.
  - Quick Share-inspired UI for nearby peer discovery and host/join pairing.
  - Binary packet serialization for real-time synchronization.

- **Smart Workspace & Library**
  - Interactive grid and list views with multi-selection, batch favoriting, and bulk management.
  - Folder organization featuring 2x2 miniature board previews and shared element spatial transitions.

- **Adaptive Theming & Material 3 Expressive**
  - 100% Material You dynamic theming with System, Light, and Dark modes.
  - Adaptive canvas ink contrast inversion so dark ink translates gracefully to light mode.
  - Bottom toolbar with expandable Material 3 Floating Action Button (FAB) menu.

- **High-Performance Architecture**
  - GPU hardware-accelerated drawing pipelines with vector path caching.
  - Full R8 / ProGuard minification, class repackaging, and resource shrinking.
  - Seamless predictive back navigation.
  - Export to PDF, high-res PNG, vector SVG, and lossless JSON backups.

---

## Tech Stack

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose & Material 3 Expressive
- **Ink & Rendering**: Google Jetpack Ink 1.0.0 (`androidx.ink`), AndroidX Graphics Core & Path
- **Architecture**: MVI / Unidirectional Data Flow (StateFlow, Coroutines)
- **Networking**: Google Nearby Connections P2P API
- **Build System**: Gradle with Kotlin DSL, Version Catalogs, R8 Full Mode

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/lavan8t/creavity.git
cd creavity

# Build debug APK
./gradlew assembleDebug

# Build optimized release APK with R8 obfuscation
./gradlew assembleRelease
```
