# ADR 0003: 100-Peer Offline Relay Bridge, Delegated Student Sheets, Synchronized Quizzes, and Adaptive Host Cockpit

## Status
Accepted

## Context
1. **Large Classroom Scale (Up to 100 Peers)**: Standard Wi-Fi Direct and Bluetooth Low Energy (BLE) P2P stacks (such as Google Nearby Connections alone) encounter hardware constraints limiting concurrent connections to ~8–15 direct links per radio chip. Supporting a lecture hall of up to 100 students completely offline without institutional Wi-Fi infrastructure requires a scalable bridging and network topology.
2. **Desktop Host with Android Bridge**: Instructors commonly run the primary presentation whiteboard from a desktop or laptop (Windows, macOS, Linux). Client students are predominantly on Android tablets or phones. Bridging desktop socket networking to mobile peers via a dedicated Android phone without an external router is a unique architectural challenge.
3. **Bandwidth Congestion in 100-Peer Mesh**: If 100 active students broadcast full-duplex drawing strokes to each other, network throughput collapses ($O(N^2)$ traffic). Individual student activities must be isolated and transmitted selectively.
4. **Teacher Delegation & Individual Accountability**: Teachers need the ability to assign individual worksheets where a question is posed and students independently submit their work, while the teacher observes each student's progress in real time without allowing peer copying.
5. **Interactive Formative Assessment (Mentimeter Style)**: Classroom engagement requires low-latency, synchronized quizzes and polls with countdown timers, animated response aggregation, and leaderboards.
6. **Presence & Focus Telemetry**: Teachers need immediate visibility into student focus (whether the whiteboard app is active in the foreground, backgrounded, or offline).
7. **Screen Estate Asymmetry**: Desktop systems possess high-resolution wide displays that should provide comprehensive command centers rather than mimicking the phone/tablet layout.

## Decision

1. **Hardware Bridge & Local-Only Hotspot (LOHS) 100-Peer Architecture**:
   - Establish an **Android Relay Bridge**: The Host (Desktop) connects to an Android Relay device (via TCP socket over local Wi-Fi, USB reverse tethering, or ADB port forwarding).
   - The Android Relay activates an offline Local-Only Hotspot (`WifiManager.startLocalOnlyHotspot()`) requiring zero internet or external routers.
   - Student devices connect directly to this local AP over high-bandwidth TCP/UDP sockets (supporting 100+ clients).
   - In environments where direct AP association is constrained, Nearby Connections mesh relaying acts as a secondary ad-hoc fallback.

2. **Scoped Delegated Sheets & Unidirectional In-Flight Streaming**:
   - Introduce `WhiteboardPacket.DelegateSheet` and `WhiteboardPacket.SheetSubmission`.
   - Each student is assigned an isolated sub-sheet (`sheet_<studentId>`). Writing permissions are restricted to that specific student and the Host.
   - Student strokes on private sheets are routed strictly to the Host ($O(N)$ traffic instead of $O(N^2)$).
   - The Host aggregates incoming sheet states into a live **Omniview Grid Matrix** showing real-time thumbnail previews of each student's active work.
   - Students tap "Submit Sheet", which transitions their sheet to a read-only locked state with a submission timestamp.

3. **Synchronized Quiz & Live Poll Engine**:
   - Introduce `WhiteboardPacket.QuizBroadcast`, `WhiteboardPacket.QuizAnswer`, and `WhiteboardPacket.QuizClose`.
   - The Host defines questions (multiple choice, true/false, numeric, poll), options, and a countdown timer.
   - When launched, students receive a synchronized Material 3 Expressive bottom sheet / card overlay with an active animated timer countdown.
   - Responses stream in real-time to the Host, which dynamically computes choice distributions, percentage breakdowns, and a gamified leaderboard.

4. **Lifecycle-Aware Engagement Telemetry**:
   - Track application lifecycle events on mobile (`ON_PAUSE`, `ON_STOP`, `ON_RESUME`) and window focus on desktop.
   - Transmit `WhiteboardPacket.EngagementHeartbeat` containing `EngagementState` (`WATCHING`, `AWAY`, `OFFLINE`).
   - The Host dashboard displays presence pills (🟢 Watching, 🟠 Away, ⚪ Offline) next to every connected participant.

5. **Adaptive Multi-Pane Host Cockpit**:
   - On Desktop and tablet landscape displays (> 840dp / 1200dp), provide an integrated dual/triple-pane layout:
     - Left / Center: Interactive Infinite Whiteboard Master Canvas.
     - Right / Dock: Expandable Command Center featuring Student Omniview Grid, Quiz Control Deck, and Relay Diagnostics.

## Consequences
- Guaranteed zero-internet, offline operation supporting up to 100 connected students with smooth stroke latencies.
- Complete privacy between students during individual tasks, eliminating visual cheating.
- Bandwidth utilization scaled from $O(N^2)$ to $O(N)$ by keeping student worksheet strokes point-to-point (student $\rightarrow$ host).
- Full formative assessment suite (quizzes, polls, timer countdowns) embedded without third-party cloud services.
- Deterministic attentiveness reporting providing instructors with actionable classroom insights.
- Clear technical differentiation and systemic novelty suitable for formal academic presentation and intellectual property evaluation.
