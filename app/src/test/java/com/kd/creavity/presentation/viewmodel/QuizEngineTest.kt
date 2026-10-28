package com.kd.creavity.presentation.viewmodel

import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.data.nearby.PeerTransport
import com.kd.creavity.data.storage.AppSettingsManager
import com.kd.creavity.data.storage.BoardManager
import com.kd.creavity.domain.model.ActiveQuiz
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.QuizType
import com.kd.creavity.presentation.ui.BoardAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class QuizEngineTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class TestTransport(override val localNodeId: String) : PeerTransport {
        override val meshRoutingEngine = MeshRoutingEngine(localNodeId)
        override val isAdvertising = MutableStateFlow(false)
        override val isDiscovering = MutableStateFlow(false)
        override val discoveredPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
        override val connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
        val sentPackets = mutableListOf<WhiteboardPacket>()
        val incoming = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 64)
        override val incomingPackets: SharedFlow<WhiteboardPacket> = incoming

        override fun startAdvertising(deviceName: String, roomCode: String) {}
        override fun stopAdvertising() {}
        override fun startDiscovery() {}
        override fun stopDiscovery() {}
        override fun connectToPeer(endpointId: String, localDisplayName: String) {}
        override fun disconnectPeer(endpointId: String) {}
        override fun disconnectAll() {}
        override fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {
            sentPackets.add(packet)
        }
        override fun sendPacketToPeer(endpointId: String, packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {
            sentPackets.add(packet)
        }
    }

    @Test
    fun `host starts quiz and broadcasts QuizStart packet`() {
        val hostTransport = TestTransport("host_node")
        val hostVm = WhiteboardViewModel(
            peerTransport = hostTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val quiz = ActiveQuiz(
            quizId = "quiz_101",
            questionText = "What is 2 + 2?",
            options = listOf("3", "4", "5"),
            correctOptionIndex = 1,
            durationSeconds = 20,
            type = QuizType.TIMED_QUIZ
        )

        hostVm.dispatch(BoardAction.StartQuiz(quiz))

        assertEquals(quiz, hostVm.uiState.value.activeQuiz)
        assertEquals(1, hostTransport.sentPackets.size)
        val packet = hostTransport.sentPackets.first() as WhiteboardPacket.QuizStart
        assertEquals("quiz_101", packet.quizId)
        assertEquals(1, packet.quizType)
        assertEquals("What is 2 + 2?", packet.question)
        assertEquals(3, packet.options.size)
        assertEquals(20, packet.durationSeconds)
    }

    @Test
    fun `student receives QuizStart and can submit answer`() {
        val studentTransport = TestTransport("student_node_1")
        val studentVm = WhiteboardViewModel(
            peerTransport = studentTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val startPacket = WhiteboardPacket.QuizStart(
            quizId = "quiz_202",
            quizType = 0, // Poll
            question = "Favorite color?",
            options = listOf("Red", "Blue", "Green"),
            durationSeconds = 0,
            startTimestampMs = 1000L
        )

        studentVm.handleIncomingPacket(startPacket)

        val active = studentVm.uiState.value.activeQuiz
        assertNotNull(active)
        assertEquals("quiz_202", active?.quizId)
        assertEquals("Favorite color?", active?.questionText)

        // Student submits answer for Option B (index 1)
        studentVm.dispatch(BoardAction.AnswerQuiz(quizId = "quiz_202", selectedOptionIndex = 1))
        assertEquals(1, studentVm.uiState.value.userQuizAnswer)

        val answerPacket = studentTransport.sentPackets.filterIsInstance<WhiteboardPacket.QuizAnswer>().firstOrNull()
        assertNotNull(answerPacket)
        assertEquals("quiz_202", answerPacket?.quizId)
        assertEquals("student_node_1", answerPacket?.studentId)
        assertEquals(1, answerPacket?.selectedOptionIndex)
    }

    @Test
    fun `host aggregates student answers and calculates response statistics and correctness`() {
        val hostTransport = TestTransport("host_node")
        val hostVm = WhiteboardViewModel(
            peerTransport = hostTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val peer1 = PeerDevice(endpointId = "student_1", name = "Alice")
        val peer2 = PeerDevice(endpointId = "student_2", name = "Bob")
        hostVm._uiState.value = hostVm._uiState.value.copy(
            connectedPeers = listOf(peer1, peer2)
        )

        val quiz = ActiveQuiz(
            quizId = "quiz_303",
            questionText = "Capital of France?",
            options = listOf("London", "Paris", "Berlin"),
            correctOptionIndex = 1,
            durationSeconds = 30,
            type = QuizType.TIMED_QUIZ,
            startedAtMs = 5000L
        )
        hostVm.dispatch(BoardAction.StartQuiz(quiz))

        // Alice answers Paris (index 1, correct)
        hostVm.handleIncomingPacket(
            WhiteboardPacket.QuizAnswer(
                quizId = "quiz_303",
                studentId = "student_1",
                selectedOptionIndex = 1,
                answerTimestampMs = 7000L
            )
        )

        // Bob answers London (index 0, incorrect)
        hostVm.handleIncomingPacket(
            WhiteboardPacket.QuizAnswer(
                quizId = "quiz_303",
                studentId = "student_2",
                selectedOptionIndex = 0,
                answerTimestampMs = 8500L
            )
        )

        val submissions = hostVm.uiState.value.quizSubmissions
        assertEquals(2, submissions.size)

        val aliceSub = submissions.find { it.studentId == "student_1" }
        assertNotNull(aliceSub)
        assertEquals("Alice", aliceSub?.studentName)
        assertEquals(1, aliceSub?.selectedOptionIndex)
        assertTrue(aliceSub!!.isCorrect)
        assertEquals(2000L, aliceSub.responseTimeMs)

        val bobSub = submissions.find { it.studentId == "student_2" }
        assertNotNull(bobSub)
        assertEquals("Bob", bobSub?.studentName)
        assertEquals(0, bobSub?.selectedOptionIndex)
        assertFalse(bobSub!!.isCorrect)
    }

    @Test
    fun `ending quiz reveals results and marks ended`() {
        val hostTransport = TestTransport("host_node")
        val hostVm = WhiteboardViewModel(
            peerTransport = hostTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val quiz = ActiveQuiz(
            quizId = "quiz_404",
            questionText = "Test",
            options = listOf("A", "B"),
            correctOptionIndex = 0
        )
        hostVm.dispatch(BoardAction.StartQuiz(quiz))
        assertFalse(hostVm.uiState.value.activeQuiz!!.isEnded)

        hostVm.dispatch(BoardAction.EndQuiz(quizId = "quiz_404", correctOptionIndex = 0))
        assertTrue(hostVm.uiState.value.activeQuiz!!.isEnded)

        val endPacket = hostTransport.sentPackets.filterIsInstance<WhiteboardPacket.QuizEnd>().firstOrNull()
        assertNotNull(endPacket)
        assertEquals("quiz_404", endPacket?.quizId)
        assertEquals(0, endPacket?.correctOptionIndex)
    }
}
