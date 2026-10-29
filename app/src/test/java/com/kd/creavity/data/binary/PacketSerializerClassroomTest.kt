package com.kd.creavity.data.binary

import org.junit.Assert.assertEquals
import org.junit.Test

class PacketSerializerClassroomTest {

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
