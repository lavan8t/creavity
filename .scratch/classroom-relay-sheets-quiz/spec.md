# Feature Spec: 100-Peer Offline Relay, Delegated Sheets, Quizzes & Adaptive Cockpit

## 1. Overview & Objectives

This specification defines the architecture, data models, network protocol, state management, and user experience for scaling CreaVITy into a formal offline interactive classroom platform. It addresses 100-peer offline networking, private delegated worksheets, interactive Mentimeter-style timed quizzes/polls, participant engagement telemetry, and an adaptive desktop command cockpit.

---

## 2. Architecture & Subsystems

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Desktop Host Machine                              │
│         (Master Whiteboard, Command Cockpit, Omniview, Quiz Deck)           │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ High-Speed TCP / Local Wi-Fi / USB Socket
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Android Relay Bridge Phone                          │
│          - Local-Only Hotspot (LOHS): High-bandwidth offline AP             │
│          - TCP / UDP Multicast Socket Server (up to 100 clients)             │
│          - Nearby Connections Mesh Fallback Relay engine                     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Offline Wi-Fi LAN / Nearby Connections
                    ┌──────────────────┼──────────────────┐
                    ▼                  ▼                  ▼
             ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
             │ Student 1    │   │ Student 2    │   │ Student 100  │
             │ Private Sheet│   │ Private Sheet│   │ Private Sheet│
             │ Quiz Modal   │   │ Quiz Modal   │   │ Quiz Modal   │
             │ Telemetry    │   │ Telemetry    │   │ Telemetry    │
             └──────────────┘   └──────────────┘   └──────────────┘
```

### 2.1 Networking & 100-Peer Bridge
- **Relay Bridge Daemon**: An Android background service and manager using `WifiManager.startLocalOnlyHotspot()` to provision an unmetered, offline Wi-Fi access point without requiring internet or cellular connectivity.
- **Desktop Connection Seam**: Desktop connects to the Relay Phone via direct socket (`DEFAULT_TCP_PORT = 53536`).
- **High-Concurrency Sockets**: `LanDiscoveryTransport` is enhanced to handle up to 100 concurrent client socket connections with coroutine-based non-blocking I/O and low-overhead thread pools.
- **Mesh Routing Efficiency**: To prevent $O(N^2)$ traffic saturation with 100 connected peers, packets are classified as `Broadcast` (e.g. Host master strokes, quizzes) or `Unicast/Directed` (e.g. Student sheet strokes routed exclusively to the Host).

### 2.2 Delegated Worksheets & Omniview
- **Worksheet Scoping**: The Host broadcasts a `DelegateSheetPacket` containing a task question, background image/prompt, and target student list.
- **Isolated State**: Each student device maintains their private sheet (`sheet_<studentId>`). Local drawing occurs normally; outbound stroke packets are tagged with the specific sheet ID and directed only to the Host.
- **Host Omniview Matrix**: The Host's UI maintains a map of `studentWorksheets: Map<String, StudentSheetState>`. Each sheet state renders an in-memory thumbnail vector path or preview.
- **Submission Workflow**: The student interface provides a "Submit Worksheet" button. When submitted, a `SheetSubmissionPacket` is transmitted to the Host, and the local student canvas transitions to a read-only locked state with a submission confirmation badge.

### 2.3 Synchronized Quiz & Live Poll Engine
- **Quiz Specification**:
  - `questionText: String`
  - `options: List<String>`
  - `correctOptionIndex: Int?` (null for opinion polls; 0-based index for graded quizzes)
  - `durationSeconds: Int` (e.g. 15s, 30s, 60s)
  - `quizType: QuizType` (`POLL`, `TIMED_QUIZ`)
- **Server-Authoritative Clock**: When the host starts a quiz, the Host emits a `QuizStartPacket` with `startTimestampMs = System.currentTimeMillis()`. Client countdown timers synchronize against this timestamp.
- **Expressive Mobile Overlay**: Students receive a floating Material 3 modal sheet displaying the question and option cards. Tapping an option locks the vote and emits `QuizAnswerPacket(studentId, selectedOption, answerTimeMs)`.
- **Live Host Aggregator & Leaderboard**: The Host calculates real-time option vote tallies, percentage bars, and speed-weighted scores ($Score = 1000 - (\Delta t \times 10)$).

### 2.4 Engagement & App Lifecycle Telemetry
- **Lifecycle Observers**: Android lifecycle events (`ON_PAUSE`, `ON_STOP`, `ON_RESUME`) and desktop focus listeners trigger status changes.
- **Telemetry States**:
  - `WATCHING`: Application is in foreground and focused.
  - `NOT_WATCHING`: App minimized, split-screened away, or device locked.
  - `OFFLINE`: Disconnected or failed 3 consecutive 3-second heartbeat pings.
- **Packet Structure**: `EngagementHeartbeatPacket(nodeId, state, timestampMs)`.

### 2.5 Adaptive Desktop Host Cockpit
- **Dual-Pane Layout**: When window width exceeds 840dp:
  - **Primary View**: The master infinite collaborative canvas.
  - **Side Command Center**:
    - **Tab 1: Student Omniview**: Searchable matrix grid of student worksheets with status chips (Watching / Not Watching / Submitted). Clicking expands the worksheet for direct teacher red-pen annotations.
    - **Tab 2: Quiz Deck**: Quiz creator, live animated response distribution bars, and winner leaderboard.
    - **Tab 3: Relay & Mesh Monitor**: Active connection list, bridge IP/hotspot info, network packet throughput graph.

---

## 3. Binary Wire Protocol Extensions

In `PacketSerializer.kt`, add the following packet types (Byte headers `0x20` to `0x27`):

| Opcode | Packet Name | Fields | Direction |
|---|---|---|---|
| `0x20` | `DelegateSheet` | `sheetId`, `title`, `questionText`, `assignedToAll`, `targetStudentId` | Host $\rightarrow$ Students |
| `0x21` | `StudentSheetStroke` | `sheetId`, `studentId`, `strokePacket` (encapsulated) | Student $\rightarrow$ Host |
| `0x22` | `SheetSubmission` | `sheetId`, `studentId`, `submissionTimestampMs` | Student $\rightarrow$ Host |
| `0x23` | `QuizStart` | `quizId`, `quizType`, `question`, `optionsCount`, `options`, `durationSec`, `startTimestampMs` | Host $\rightarrow$ Students |
| `0x24` | `QuizAnswer` | `quizId`, `studentId`, `selectedOptionIndex`, `answerTimestampMs` | Student $\rightarrow$ Host |
| `0x25` | `QuizEnd` | `quizId`, `correctOptionIndex` | Host $\rightarrow$ Students |
| `0x26` | `EngagementHeartbeat` | `nodeId`, `state` (`0 = WATCHING`, `1 = NOT_WATCHING`) | Student $\rightarrow$ Host |
| `0x27` | `RelayBridgeAnnounce`| `relayNodeId`, `hotspotSsid`, `hotspotPassword`, `ipAddress`, `port` | Relay $\rightarrow$ Host/Peers |

---

## 4. Verification & Testing Strategy

1. **Serialization & Deserialization Tests**:
   - `QuizPacketTest.kt`: Round-trip tests for `QuizStart`, `QuizAnswer`, and `QuizEnd`.
   - `SheetPacketTest.kt`: Round-trip tests for `DelegateSheet`, `StudentSheetStroke`, and `SheetSubmission`.
   - `EngagementPacketTest.kt`: Verify byte serialization of `EngagementHeartbeat`.

2. **Routing & Scoping Tests**:
   - `WorksheetRoutingTest.kt`: Verify student private worksheet strokes are only delivered to the host and rejected from peer broadcast.
   - `HighConcurrencyConnectionTest.kt`: Verify `LanDiscoveryTransport` manages socket registrations for large peer sets without memory leaks.

3. **ViewModel & State Tests**:
   - `QuizViewModelTest.kt`: Verify quiz countdown, answer aggregation, and score calculations.
   - `EngagementStateTest.kt`: Verify lifecycle pause transitions peer presence to `NOT_WATCHING`, and heartbeats keep state updated.

4. **Integration Verification**:
   - `./gradlew :app:testDebugUnitTest` passes.
   - `./gradlew :app:compileKotlinDesktop` passes.
