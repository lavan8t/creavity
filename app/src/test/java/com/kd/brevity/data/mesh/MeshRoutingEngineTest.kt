package com.kd.brevity.data.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshRoutingEngineTest {
    private val testSecret = "mesh-board-secret-key"

    @Test
    fun meshEnvelope_serializationRoundtrip() {
        val original = MeshEnvelope(
            packetId = 123456789L,
            originNodeId = "nodeA",
            senderNodeId = "nodeB",
            ttl = 10,
            hopCount = 2,
            roleToken = "sample-token",
            isMutation = true,
            payload = "Hello Whiteboard Mesh".toByteArray()
        )

        val bytes = original.toByteArray()
        val restored = MeshEnvelope.fromByteArray(bytes)

        assertNotNull(restored)
        assertEquals(original, restored)
    }

    @Test
    fun antiLoop_duplicatePacketsAreDropped() {
        val engine = MeshRoutingEngine(nodeId = "nodeRelay", maxDegree = 6)
        engine.addNeighbor("node1")
        engine.addNeighbor("node2")

        val envelope = MeshEnvelope(
            packetId = 9999L,
            originNodeId = "nodeOrigin",
            senderNodeId = "node1",
            ttl = 5,
            payload = byteArrayOf(1, 2, 3)
        )

        val firstResult = engine.processInbound(envelope)
        assertTrue(firstResult is InboundResult.DeliverAndForward)

        val duplicateResult = engine.processInbound(envelope)
        assertEquals(InboundResult.DroppedDuplicate, duplicateResult)
    }

    @Test
    fun multiHop_decrementsTtlAndExcludesSender() {
        val engine = MeshRoutingEngine(nodeId = "nodeRelay", maxDegree = 6)
        engine.addNeighbor("node1")
        engine.addNeighbor("node2")
        engine.addNeighbor("node3")

        val envelope = MeshEnvelope(
            packetId = 1001L,
            originNodeId = "nodeOrigin",
            senderNodeId = "node1", // Came from node1
            ttl = 8,
            hopCount = 1,
            payload = byteArrayOf(5, 6)
        )

        val result = engine.processInbound(envelope)
        assertTrue(result is InboundResult.DeliverAndForward)
        val forwardResult = result as InboundResult.DeliverAndForward

        assertEquals(7.toByte(), forwardResult.envelope.ttl)
        assertEquals(2.toByte(), forwardResult.envelope.hopCount)
        assertEquals("nodeRelay", forwardResult.envelope.senderNodeId)

        // Sender "node1" must be excluded; should forward to node2 and node3
        assertFalse(forwardResult.targetNeighbors.contains("node1"))
        assertTrue(forwardResult.targetNeighbors.contains("node2"))
        assertTrue(forwardResult.targetNeighbors.contains("node3"))
    }

    @Test
    fun boundedDegree_enforcesMaxNeighbors() {
        val engine = MeshRoutingEngine(nodeId = "nodeA", maxDegree = 3)
        assertTrue(engine.addNeighbor("n1"))
        assertTrue(engine.addNeighbor("n2"))
        assertTrue(engine.addNeighbor("n3"))
        assertFalse(engine.addNeighbor("n4")) // Exceeds max degree 3
        assertTrue(engine.isAtCapacity())
    }

    @Test
    fun unauthorizedViewerMutation_isDroppedByRelay() {
        val engine = MeshRoutingEngine(nodeId = "nodeRelay", secretKey = testSecret)
        engine.addNeighbor("node1")
        engine.addNeighbor("node2")

        val viewerTicket = MeshAccessController.createTicket(testSecret, "b1", "viewerUser", MeshRole.VIEWER)
        engine.registerTicket(viewerTicket)

        // Viewer tries to inject a mutation packet
        val mutationEnvelope = MeshEnvelope(
            packetId = 8888L,
            originNodeId = "viewerUser",
            senderNodeId = "node1",
            ttl = 6,
            isMutation = true,
            roleToken = viewerTicket.signature,
            payload = byteArrayOf(9, 9, 9)
        )

        val result = engine.processInbound(mutationEnvelope)
        assertEquals(InboundResult.DroppedUnauthorized, result)
    }
}
