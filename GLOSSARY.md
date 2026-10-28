# Domain Glossary

Canonical vocabulary for CreaVITy domain concepts.

## Collaboration & Session

- **Host**: The collaborator who starts advertising or initiates the room session. The host maintains authoritative board selection.
- **Peer**: A collaborator joined to the session. A peer's canvas is locked to the host-owned board and follows the host.
- **Host-Owned Board**: The specific board currently selected by the host. Edits during a collaboration session are strictly scoped to this board; incoming edits for other boards are discarded.
- **In-Flight Stroke**: A stroke being streamed incrementally (`StrokeStart` → `StrokePoint`...) before `StrokeEnd`. Rendered live with a luminous glow matching the peer's stroke color.
- **Committed Stroke**: A finalized stroke whose points are fully received and merged into the active page document.
- **Peer Presence**: The ephemeral state of a remote collaborator including canvas coordinates, active drawing state, assigned Material cursor shape, and color badge.
- **LAN Discovery**: Zero-configuration discovery using UDP beacons/multicast and Android Nearby Connections, bypassing manual IP entry.
- **Exclusive Discovery**: State constraint where broadcasting (Host) and scanning (Join) are mutually exclusive. Initiating either immediately transforms the button into a "Cancel" action and greys out/disables the other with 0ms UI delay.
- **Hardware Readiness Gate**: Verification that Bluetooth, Wi-Fi, and Nearby Devices permissions are active before initiating discovery, prompting user enablement if disabled.
- **Board Viewport State**: The persistent canvas zoom and pan offset (`panOffset: Offset`, `zoom: Float`) saved per board, restored upon reopening or returning from library.
- **Historical Digitizer Ingestion**: Ingesting intermediate sub-frame touch points from `PointerInputChange.historical` to preserve 120Hz–480Hz digitizer sampling rates between Compose render frames.
- **Stylus Palm Rejection**: Suppressing single-finger touches as palm contact while a stylus pointer (`PointerType.Stylus` or `PointerType.Eraser`) is active, reserving finger input exclusively for two-finger pan/zoom.
- **Calibrated Pressure Curve**: A non-linear power curve applied to raw digitizer pressure to deliver immediate touch sensitivity on light contact and proportional width modulation under firmer force.

## Classroom & Scale (100-Peer Infrastructure)

- **Android Relay Bridge**: An Android device connected to the Host (Desktop/Laptop) that creates an offline local network and bridges socket traffic and Nearby Connections payloads between the Host and up to 100 client devices.
- **Local-Only Hotspot (LOHS) Bridge**: A zero-configuration, completely offline Wi-Fi access point created programmatically by Android's `WifiManager.startLocalOnlyHotspot()` without requiring an external router or internet connectivity.
- **Student Sheet**: An isolated, delegated workspace canvas assigned to an individual student (`sheet_<studentId>`). Strokes on a student sheet are synchronized privately with the Host rather than broadcast to all participants.
- **Host Omniview**: A command dashboard on the Host displaying a synchronized grid matrix of all active student sheets with live thumbnails, enabling real-time inspection, teacher intervention, and submission tracking.
- **Synchronized Quiz / Poll Engine**: A server-authoritative assessment protocol broadcasting question payloads, option sets, and countdown timers, rendering an interactive Material 3 Expressive overlay and streaming responses into live statistical distributions.
- **Engagement Telemetry**: Real-time tracking of participant app lifecycle and window focus (`Watching`, `Away / Not Watching`, `Offline`) communicated through lifecycle state transitions and keepalive heartbeats.
- **Adaptive Host Cockpit**: A responsive multi-pane layout for desktop displays and expanded screen widths (> 840dp) combining the master canvas, student matrix, quiz controls, and network topology diagnostics into an integrated workstation.

## Diagramming & Sketch Mechanics (Excalidraw Engine)

- **Rough Engine (Hand-Drawn Jitter)**: Algorithmic multi-pass line displacement producing natural, human-like sketchiness with configurable roughness levels (`CLEAN`, `SKETCHY`, `CARTOON`).
- **Hachure & Cross-Hatch Fill**: Stylized hand-drawn shading using parameterized parallel or cross-hatched stroke lines rather than flat color fills.
- **Magnetic Connector & Anchor Snap**: Semantic bindings between arrows/connectors and bounding anchors (North, South, East, West, Center) of target shapes that dynamically update geometry when shapes are dragged.
- **Shape-Embedded Text**: Text elements intrinsically bound and centered inside flowchart nodes (rectangles, diamonds, cylinders, ellipses) that move, scale, and delete together.
- **Smart Alignment Guides**: Ephemeral visual axes in `MaterialTheme.colorScheme.primary` that appear during element transformation to magnetically snap collinear edges and centroids.
- **Floating Contextual Properties Dock**: A responsive, floating Material 3 `ElevatedCard` (`border = null`) anchored to current selections providing one-tap adjustments for fill, roughness, stroke width, color, and layer hierarchy.
- **Canvas Vector Export**: Deterministic export pipelines rendering canvas selections or full boards into transparent/solid PNGs, clean SVG vectors, and OS clipboard bitmaps.

## View-Only, Version History & Taxonomy

- **Read-Only Viewport Gate**: Strict suppression of all drawing toolbars, floating property docks, element creation menus, and shape handlers when `isCanvasReadOnly` is true, replacing them with a non-intrusive Material 3 Expressive status indicator pill while preserving zoom, pan, and laser pointer navigation.
- **Board Version Snapshot**: An immutable historical capture of board state (strokes, text elements, image elements, sticky notes, flowchart shapes, and connectors) tagged with an author, timestamp, snapshot name, and element metrics, enabling non-destructive timeline review and rollback.
- **Board Ownership Scope**: Taxonomy partitioning boards into `Owned by me` (created on local peer profile) versus `Shared with me` (synchronized from remote collaborators/hosts) surfaced through borderless Material 3 Expressive filter chips.
- **Label-Based Taxonomy**: Flat, multi-select tag filtering architecture replacing legacy hierarchical folders, allowing flexible categorization and discovery across arbitrarily labeled boards.

## Office-Grade Shapes & Identity

- **Office-Grade Shapes**: Clean geometric vector shapes (Rectangle, Oval/Circle, Triangle, Line, Arrow, Star, Cylinder) rendered with crisp anti-aliased paths (`RoughnessStyle.CLEAN` by default) matching Microsoft Word, PowerPoint, and Google Slides fidelity.
- **Eight-Point Bounding Box Resizing**: Interactive transformation handles positioned at the 8 perimeter points (NW, N, NE, E, SE, S, SW, W) of selected shapes and elements, allowing proportional and directional resizing.
- **Two-Word Fusion Board Name**: Automatically generated memorable titles combining evocative adjectives and canvas/nature nouns (e.g. "Cosmic Canvas", "Solar Drift", "Amber Horizon") replacing generic placeholders.


