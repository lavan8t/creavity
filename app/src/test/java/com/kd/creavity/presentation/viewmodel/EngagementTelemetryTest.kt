package com.kd.creavity.presentation.viewmodel

import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.domain.model.EngagementState
import com.kd.creavity.domain.model.PeerDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class EngagementTelemetryTest {

    @Test
    fun `engagement state enum has watching, not watching, and offline`() {
        assertEquals(3, EngagementState.entries.size)
        assertEquals(EngagementState.WATCHING, EngagementState.entries[0])
        assertEquals(EngagementState.NOT_WATCHING, EngagementState.entries[1])
        assertEquals(EngagementState.OFFLINE, EngagementState.entries[2])
    }

    @Test
    fun `peer device holds engagement state defaulting to watching`() {
        val peer = PeerDevice(endpointId = "peer_1", name = "Alice")
        assertEquals(EngagementState.WATCHING, peer.engagementState)

        val awayPeer = peer.copy(engagementState = EngagementState.NOT_WATCHING)
        assertEquals(EngagementState.NOT_WATCHING, awayPeer.engagementState)
    }

    @Test
    fun `engagement heartbeat packet translates to proper engagement state`() {
        val watchingPacket = WhiteboardPacket.EngagementHeartbeat("peer_1", 0, 1000L)
        val awayPacket = WhiteboardPacket.EngagementHeartbeat("peer_1", 1, 1000L)
        val offlinePacket = WhiteboardPacket.EngagementHeartbeat("peer_1", 2, 1000L)

        fun toState(stateIndex: Int): EngagementState = when (stateIndex) {
            0 -> EngagementState.WATCHING
            1 -> EngagementState.NOT_WATCHING
            else -> EngagementState.OFFLINE
        }

        assertEquals(EngagementState.WATCHING, toState(watchingPacket.stateIndex))
        assertEquals(EngagementState.NOT_WATCHING, toState(awayPacket.stateIndex))
        assertEquals(EngagementState.OFFLINE, toState(offlinePacket.stateIndex))
    }
}
