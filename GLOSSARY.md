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
