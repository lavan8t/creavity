package com.kd.creavity.presentation.viewmodel

import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.data.nearby.PeerTransport
import com.kd.creavity.data.storage.AppSettingsManager
import com.kd.creavity.data.storage.BoardManager
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
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

class WorksheetSyncTest {

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
    fun `host delegates worksheet and broadcasts packet while initializing local student sheets`() {
        val hostTransport = TestTransport("host_1")
        val hostVm = WhiteboardViewModel(
            peerTransport = hostTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val studentPeer = PeerDevice(endpointId = "student_1", name = "Alice")
        hostVm._uiState.value = hostVm._uiState.value.copy(
            connectedPeers = listOf(studentPeer)
        )

        hostVm.dispatch(
            BoardAction.DelegateWorksheet(
                title = "Calculus Quiz",
                questionText = "Compute derivative of sin(x)*cos(x)"
            )
        )

        assertEquals(1, hostTransport.sentPackets.size)
        val packet = hostTransport.sentPackets.first() as WhiteboardPacket.DelegateSheet
        assertEquals("Calculus Quiz", packet.title)
        assertEquals("Compute derivative of sin(x)*cos(x)", packet.questionText)
        assertTrue(packet.assignedToAll)

        val hostSheet = hostVm.uiState.value.studentWorksheets["student_1"]
        assertNotNull(hostSheet)
        assertEquals("Calculus Quiz", hostSheet?.title)
        assertFalse(hostSheet!!.isSubmitted)
    }

    @Test
    fun `student receives delegate packet and switches to worksheet mode`() {
        val studentTransport = TestTransport("student_1")
        val studentVm = WhiteboardViewModel(
            peerTransport = studentTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val delegatePacket = WhiteboardPacket.DelegateSheet(
            sheetId = "sheet_123",
            title = "Physics Problem",
            questionText = "Calculate terminal velocity",
            assignedToAll = true
        )

        studentVm.handleIncomingPacket(delegatePacket)

        val active = studentVm.uiState.value.activeWorksheet
        assertNotNull(active)
        assertEquals("sheet_123", active?.sheetId)
        assertEquals("Physics Problem", active?.title)
        assertEquals("Calculate terminal velocity", active?.questionText)
        assertTrue(studentVm.uiState.value.isWorksheetMode)
    }

    @Test
    fun `worksheet stroke remains isolated to worksheet and syncs to host without contaminating main canvas`() {
        val studentTransport = TestTransport("student_1")
        val studentVm = WhiteboardViewModel(
            peerTransport = studentTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        val delegatePacket = WhiteboardPacket.DelegateSheet(
            sheetId = "sheet_123",
            title = "Math 101",
            questionText = "Find x",
            assignedToAll = true
        )
        studentVm.handleIncomingPacket(delegatePacket)

        val testStroke = Stroke(
            id = "stroke_work_1",
            points = listOf(Point(10f, 10f), Point(20f, 20f)),
            color = 0xFF00FF00L,
            strokeWidth = 4f,
            tool = DrawingTool.PEN
        )

        studentVm.dispatch(BoardAction.AddStudentWorksheetStroke(testStroke))

        // Student's active worksheet has the stroke
        assertEquals(1, studentVm.uiState.value.activeWorksheet?.strokes?.size)
        // Main canvas does NOT contain the worksheet stroke
        assertTrue(studentVm.uiState.value.strokes.none { it.id == "stroke_work_1" })

        // Student sent a StudentSheetStroke packet
        val strokePacket = studentTransport.sentPackets.filterIsInstance<WhiteboardPacket.StudentSheetStroke>().firstOrNull()
        assertNotNull(strokePacket)
        assertEquals("sheet_123", strokePacket?.sheetId)
        assertEquals("student_1", strokePacket?.studentId)

        // Host receives the packet
        val hostTransport = TestTransport("host_1")
        val hostVm = WhiteboardViewModel(
            peerTransport = hostTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        hostVm.handleIncomingPacket(strokePacket!!)
        val hostStudentSheet = hostVm.uiState.value.studentWorksheets["student_1"]
        assertNotNull(hostStudentSheet)
        assertEquals(1, hostStudentSheet?.strokes?.size)
        assertEquals("stroke_work_1", hostStudentSheet?.strokes?.first()?.id)
        // Host main canvas untouched
        assertTrue(hostVm.uiState.value.strokes.none { it.id == "stroke_work_1" })
    }

    @Test
    fun `worksheet submission locks editing and marks sheet submitted on host`() {
        val studentTransport = TestTransport("student_1")
        val studentVm = WhiteboardViewModel(
            peerTransport = studentTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        studentVm.handleIncomingPacket(
            WhiteboardPacket.DelegateSheet(
                sheetId = "sheet_456",
                title = "Chemistry",
                questionText = "Balance the equation",
                assignedToAll = true
            )
        )

        studentVm.dispatch(BoardAction.SubmitWorksheet("sheet_456"))

        assertTrue(studentVm.uiState.value.activeWorksheet!!.isSubmitted)

        // Attempting to draw after submission is rejected
        val lateStroke = Stroke(
            id = "late_stroke",
            points = listOf(Point(1f, 1f)),
            color = 0xFF000000L,
            strokeWidth = 2f,
            tool = DrawingTool.PEN
        )
        studentVm.dispatch(BoardAction.AddStudentWorksheetStroke(lateStroke))
        assertEquals(0, studentVm.uiState.value.activeWorksheet!!.strokes.size)

        // Host receives submission packet
        val submissionPacket = studentTransport.sentPackets.filterIsInstance<WhiteboardPacket.SheetSubmission>().firstOrNull()
        assertNotNull(submissionPacket)

        val hostTransport = TestTransport("host_1")
        val hostVm = WhiteboardViewModel(
            peerTransport = hostTransport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )

        hostVm.handleIncomingPacket(submissionPacket!!)
        val hostSheet = hostVm.uiState.value.studentWorksheets["student_1"]
        assertNotNull(hostSheet)
        assertTrue(hostSheet!!.isSubmitted)
    }
}
