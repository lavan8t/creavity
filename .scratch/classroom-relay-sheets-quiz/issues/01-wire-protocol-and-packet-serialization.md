# Ticket 01: Wire Protocol Extensions for Classroom Scale, Sheets, Quizzes, and Telemetry

Status: resolved
Blocked by: none

## What it delivers
- Extend `WhiteboardPacket` sealed class with classroom collaboration packets:
  - `WhiteboardPacket.DelegateSheet(sheetId, title, questionText, assignedToAll, targetStudentId)`
  - `WhiteboardPacket.StudentSheetStroke(sheetId, studentId, strokePacketBytes)`
  - `WhiteboardPacket.SheetSubmission(sheetId, studentId, timestampMs)`
  - `WhiteboardPacket.QuizStart(quizId, quizType, question, options, durationSeconds, startTimestampMs)`
  - `WhiteboardPacket.QuizAnswer(quizId, studentId, selectedOptionIndex, answerTimestampMs)`
  - `WhiteboardPacket.QuizEnd(quizId, correctOptionIndex)`
  - `WhiteboardPacket.EngagementHeartbeat(nodeId, stateIndex, timestampMs)`
  - `WhiteboardPacket.RelayBridgeAnnounce(relayNodeId, ssid, passphrase, ipAddress, port)`
- Implement binary serialization and deserialization in `PacketSerializer.kt` with opcodes `0x20` through `0x27`.
- Unit tests verifying serialization round-trip for every new packet type and payload validation.

## Verification
- `PacketSerializerTest.kt` unit tests covering all new classroom packets.
- `./gradlew :app:testDebugUnitTest` passes.
