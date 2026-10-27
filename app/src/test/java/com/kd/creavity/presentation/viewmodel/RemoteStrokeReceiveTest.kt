package com.kd.creavity.presentation.viewmodel

import androidx.compose.ui.graphics.Path
import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.mesh.InboundResult
import com.kd.creavity.data.mesh.MeshEnvelope
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.data.nearby.PeerTransport
import com.kd.creavity.data.storage.AppSettingsManager
import com.kd.creavity.data.storage.BoardManager
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.presentation.canvas.StrokeGeometryCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Feedback loop for "peer cursor arrives but the stroke never draws".
 *
 * Replays a real sender's packets: serialize -> MeshEnvelope -> receiver routing -> deserialize -> handleIncomingPacket,
 * then asserts on what the receiver renders.
 */
class RemoteStrokeReceiveTest {

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

    private class FakeTransport : PeerTransport {
        override val localNodeId: String = "receiver-node"
        override val meshRoutingEngine = MeshRoutingEngine(localNodeId)
        override val isAdvertising = MutableStateFlow(false)
        override val isDiscovering = MutableStateFlow(false)
        override val discoveredPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
        override val connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
        val packets = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 256)
        override val incomingPackets: SharedFlow<WhiteboardPacket> = packets

        override fun startAdvertising(deviceName: String, roomCode: String) {}
        override fun stopAdvertising() {}
        override fun startDiscovery() {}
        override fun stopDiscovery() {}
        override fun connectToPeer(endpointId: String, localDisplayName: String) {}
        override fun disconnectPeer(endpointId: String) {}
        override fun disconnectAll() {}
        override fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {}
        override fun sendPacketToPeer(endpointId: String, packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {}
    }

    /** Receiver wired to a real sender engine, mirroring the socket reader's unpacking. */
    private class Replay(tmp: TemporaryFolder) {
        val transport = FakeTransport()
        val vm = WhiteboardViewModel(
            peerTransport = transport,
            boardManager = BoardManager(tmp.newFolder()),
            appSettings = AppSettingsManager(tmp.newFile())
        )
        private val senderEngine = MeshRoutingEngine("sender-node")

        fun deliver(packet: WhiteboardPacket) {
            val (envelope, _) = senderEngine.createOutbound(PacketSerializer.serialize(packet), false)
            val inbound = transport.meshRoutingEngine.processInbound(envelope)
            val delivered: MeshEnvelope = when (inbound) {
                is InboundResult.DeliverOnly -> inbound.envelope
                is InboundResult.DeliverAndForward -> inbound.envelope
                else -> throw AssertionError("packet dropped in routing: $inbound")
            }
            val received = PacketSerializer.deserialize(delivered.payload)
                ?: throw AssertionError("packet failed to deserialize: $packet")
            vm.handleIncomingPacket(received)
        }

        fun start(id: String, point: Point) = deliver(
            WhiteboardPacket.StrokeStart(id, 0xFF1B1B1FL, 6f, false, point, DrawingTool.PEN)
        )

        fun point(id: String, point: Point) = deliver(WhiteboardPacket.StrokePoint(id, point))

        fun end(id: String) = deliver(WhiteboardPacket.StrokeEnd(id))
    }

    @Test
    fun `peer stroke start and points reach the receiver canvas`() {
        val replay = Replay(tmp)
        val id = "stroke-1"

        replay.start(id, Point(10f, 10f, 1f, 1_000L))
        repeat(5) { i ->
            replay.point(id, Point(10f + i, 20f + i, 1f, 2_000L + i))
        }

        val strokes = replay.vm.uiState.value.strokes
        assertEquals("stroke must be visible before StrokeEnd", 1, strokes.size)
        assertEquals("all peer points must be appended", 6, strokes[0].points.size)
    }

    @Test
    fun `ending a peer stroke commits it exactly once`() {
        val replay = Replay(tmp)
        val id = "stroke-3"

        replay.start(id, Point(10f, 10f, 1f, 1_000L))
        repeat(3) { i -> replay.point(id, Point(20f + i, 30f + i, 1f, 2_000L + i)) }
        replay.end(id)
        val strokes = replay.vm.uiState.value.strokes.filter { it.id == id }
        assertEquals("one id must resolve to one element in the page", 1, strokes.size)
        assertEquals("the committed stroke keeps every point", 4, strokes.single().points.size)
        assertTrue("nothing may stay in flight after StrokeEnd", replay.vm.remoteStrokesMap.isEmpty())
    }

    @Test
    fun `onStrokeCompleted does not duplicate a stroke already in the page`() {
        val replay = Replay(tmp)
        val id = "stroke-dup"

        val stroke = Stroke(
            id = id,
            points = listOf(Point(10f, 10f, 1f, 1L)),
            color = 0xFF000000L,
            strokeWidth = 6f,
            tool = DrawingTool.PEN,
            isEraser = false,
            author = "Peer"
        )

        // First commit simulates updateRemoteStrokes having merged the in-flight stroke into the page.
        // Second commit simulates StrokeEnd calling onStrokeCompleted again — must replace, not append.
        replay.vm.onStrokeCompleted(stroke)
        replay.vm.onStrokeCompleted(stroke)

        val strokes = replay.vm.uiState.value.strokes.filter { it.id == id }
        assertEquals("re-committing an in-flight stroke must not duplicate it", 1, strokes.size)
    }
    @Test
    fun `growing peer stroke invalidates its cached geometry`() {
        val replay = Replay(tmp)
        val id = "stroke-2"
        val cache = StrokeGeometryCache()
        var rebuilds = 0

        fun render() {
            val stroke = replay.vm.uiState.value.strokes.single { it.id == id }
            cache.pathFor(stroke, isMoving = false) {
                rebuilds++
                Path()
            }
        }

        replay.start(id, Point(10f, 10f, 1f, 1_000L))
        render()
        repeat(2) { i ->
            replay.point(id, Point(20f + i, 30f + i, 1f, 2_000L + i))
            render()
        }

        assertEquals(
            "the drawer must rebuild geometry as the peer's points stream in, " +
                "otherwise it keeps replaying the 1-point path and the ink never grows past the first dot",
            3,
            rebuilds
        )
        assertEquals("only the latest geometry may be retained", 1, cache.pathCount())
    }

    @Test
    fun `incoming BoardSync switches peer board and locks board switching`() {
        val replay = Replay(tmp)
        replay.transport.connectedPeers.value = listOf(
            PeerDevice("host-1", "Host User", role = com.kd.creavity.data.mesh.MeshRole.HOST)
        )
        replay.deliver(
            WhiteboardPacket.BoardSync(
                boardId = "host-board-99",
                title = "Host Master Board",
                backgroundColor = 0xFFFFFFFFL,
                isReadOnly = false,
                strokes = emptyList(),
                textElements = emptyList()
            )
        )

        assertEquals("host-board-99", replay.vm.uiState.value.currentBoard.id)
        assertEquals("Host Master Board", replay.vm.uiState.value.currentBoard.title)
        assertTrue("Board switching must be locked for peer in active session", replay.vm.uiState.value.isBoardSwitchingLocked)

        // Attempting to switch board as non-host must be ignored
        val rogueBoard = com.kd.creavity.domain.model.Board(id = "rogue-board-1", title = "Rogue Board")
        replay.vm.switchBoard(rogueBoard)
        assertEquals("host-board-99", replay.vm.uiState.value.currentBoard.id)
    }

    @Test
    fun `stroke packet for mismatched board is discarded`() {
        val replay = Replay(tmp)
        replay.transport.connectedPeers.value = listOf(
            PeerDevice("host-1", "Host User", role = com.kd.creavity.data.mesh.MeshRole.HOST)
        )
        // Receive board sync for host board
        replay.deliver(
            WhiteboardPacket.BoardSync(
                boardId = "host-board-100",
                title = "Host Board",
                backgroundColor = 0xFFFFFFFFL,
                isReadOnly = false,
                strokes = emptyList(),
                textElements = emptyList()
            )
        )
        // Set local board to something else to simulate race / mismatched board
        replay.vm.hostOwnedBoardId = "host-board-100"

        // Send a stroke when current board ID doesn't match hostOwnedBoardId
        val wrongBoardState = replay.vm.uiState.value.currentBoard.copy(id = "wrong-board")
        replay.vm.onStrokeCompleted(com.kd.creavity.domain.model.Stroke(id = "local-1"))
        // Check that packets for wrong board are ignored in handleIncomingPacket
        replay.deliver(
            WhiteboardPacket.StrokeStart(
                strokeId = "peer-stroke-mismatch",
                color = 0xFF000000L,
                strokeWidth = 5f,
                isEraser = false,
                point = Point(50f, 50f, 1f, 1000L)
            )
        )
        // Because hostOwnedBoardId is host-board-100 and current board is host-board-100,
        // it was accepted. If we force hostOwnedBoardId mismatch:
        replay.vm.hostOwnedBoardId = "some-other-board"
        replay.deliver(
            WhiteboardPacket.StrokeStart(
                strokeId = "rejected-stroke",
                color = 0xFF000000L,
                strokeWidth = 5f,
                isEraser = false,
                point = Point(100f, 100f, 1f, 2000L)
            )
        )
        assertTrue(replay.vm.uiState.value.strokes.none { it.id == "rejected-stroke" })
    }
}
