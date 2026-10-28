# Ticket 06: Adaptive Desktop Command Cockpit

Status: resolved
Completed At: 2026-10-28T12:27:35+05:30
Blocked by: 03, 04, 05

## What it delivers
- In `WhiteboardScreen.kt`, implement an adaptive multi-pane layout:
  - On standard compact/medium screens (< 840dp, phones/small tablets): Whiteboard operates in standard focused view, with bottom sheets/dialogs for quizzes, worksheets, and participants.
  - On expanded/desktop screens (>= 840dp / 1200dp, Windows, macOS, Linux, large tablets):
    - Multi-pane cockpit displaying the Master Canvas on the left/center and a dedicated Command Center side-deck on the right.
- Command Center Side-Deck:
  - Tab 1: **Omniview Student Matrix** — responsive grid of all 100 student live mini-canvases with status badges (Watching, Away, Submitted).
  - Tab 2: **Quiz & Poll Deck** — real-time Mentimeter-style live animated response bars, vote count tally, timer controls, and leaderboard.
  - Tab 3: **Relay & Mesh Health** — bridge phone IP/status, connected client count (e.g. `48 / 100 Connected`), network packet rate, and latency monitor.
- Keyboard shortcuts for desktop: Quick toggle for Command Center (`Ctrl + /` or `Cmd + /`), quiz launcher (`Ctrl + Q`), and Omniview matrix (`Ctrl + M`).

## Verification
- Compose UI test / unit verification of adaptive pane visibility based on window width.
- `./gradlew :app:compileKotlinDesktop` passes.
- `./gradlew :app:testDebugUnitTest` passes.
