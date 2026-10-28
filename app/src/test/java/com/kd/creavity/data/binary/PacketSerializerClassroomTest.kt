package com.kd.creavity.data.binary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketSerializerClassroomTest {

    @Test
    fun `DelegateSheet round trip`() {
        val original = WhiteboardPacket.DelegateSheet(
            sheetId = "sheet_123",
            title = "Math Quiz 1",
            questionText = "Solve for x: 2x + 5 = 15",
            assignedToAll = true,
            targetStudentId = ""
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.DelegateSheet

        assertEquals(original, deserialized)
    }

    @Test
    fun `StudentSheetStroke round trip`() {
        val strokeData = byteArrayOf(1, 2, 3, 4, 5)
        val original = WhiteboardPacket.StudentSheetStroke(
            sheetId = "sheet_456",
            studentId = "student_alice",
            strokePacketBytes = strokeData
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.StudentSheetStroke

        assertEquals(original.sheetId, deserialized?.sheetId)
        assertEquals(original.studentId, deserialized?.studentId)
        assertTrue(original.strokePacketBytes.contentEquals(deserialized?.strokePacketBytes))
    }

    @Test
    fun `SheetSubmission round trip`() {
        val original = WhiteboardPacket.SheetSubmission(
            sheetId = "sheet_789",
            studentId = "student_bob",
            timestampMs = 1730000000000L
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.SheetSubmission

        assertEquals(original, deserialized)
    }

    @Test
    fun `QuizStart round trip`() {
        val original = WhiteboardPacket.QuizStart(
            quizId = "quiz_001",
            quizType = 1, // 0 = Poll, 1 = Timed Quiz
            question = "What is the capital of France?",
            options = listOf("London", "Berlin", "Paris", "Madrid"),
            durationSeconds = 30,
            startTimestampMs = 1730000050000L
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.QuizStart

        assertEquals(original, deserialized)
    }

    @Test
    fun `QuizAnswer round trip`() {
        val original = WhiteboardPacket.QuizAnswer(
            quizId = "quiz_001",
            studentId = "student_alice",
            selectedOptionIndex = 2,
            answerTimestampMs = 1730000055000L
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.QuizAnswer

        assertEquals(original, deserialized)
    }

    @Test
    fun `QuizEnd round trip`() {
        val original = WhiteboardPacket.QuizEnd(
            quizId = "quiz_001",
            correctOptionIndex = 2
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.QuizEnd

        assertEquals(original, deserialized)
    }

    @Test
    fun `EngagementHeartbeat round trip`() {
        val original = WhiteboardPacket.EngagementHeartbeat(
            nodeId = "peer_charlie",
            stateIndex = 0, // 0 = WATCHING, 1 = NOT_WATCHING
            timestampMs = 1730000099000L
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.EngagementHeartbeat

        assertEquals(original, deserialized)
    }

    @Test
    fun `RelayBridgeAnnounce round trip`() {
        val original = WhiteboardPacket.RelayBridgeAnnounce(
            relayNodeId = "relay_node_9",
            ssid = "CreaVITy_Bridge_AP",
            passphrase = "creavity_secret",
            ipAddress = "192.168.49.1",
            port = 53536
        )
        val bytes = PacketSerializer.serialize(original)
        val deserialized = PacketSerializer.deserialize(bytes) as? WhiteboardPacket.RelayBridgeAnnounce

        assertEquals(original, deserialized)
    }
}
