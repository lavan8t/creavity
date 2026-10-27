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
