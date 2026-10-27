# Ticket 05: Expressive Material Shape Cursors and Deterministic Peer Badges

Status: resolved
Blocked by: 03

## What it delivers
- In `WhiteboardCanvas.kt`, create a deterministic peer style resolver that maps each `peerId` to:
  1. A vibrant, distinct M3 color (e.g. Coral, Sapphire, Emerald, Amber, Violet, Rose, Cyan).
  2. A distinct Material shape (Circle, Rounded Square, Diamond, Triangle, Star).
- Render each peer's floating cursor using their assigned Material shape at their live pointer position, accompanied by their color-coded badge.

## Verification
- Peer cursors render with distinct Material shapes and consistent color tags across frames.
- `./gradlew :app:testDebugUnitTest` passes.
