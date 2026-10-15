package com.kd.creavity.data.nearby

import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.domain.model.PeerDevice
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PeerTransport {
    val localNodeId: String
    val meshRoutingEngine: MeshRoutingEngine
    val isAdvertising: StateFlow<Boolean>
    val isDiscovering: StateFlow<Boolean>
    val discoveredPeers: StateFlow<List<PeerDevice>>
    val connectedPeers: StateFlow<List<PeerDevice>>
    val incomingPackets: SharedFlow<WhiteboardPacket>

    fun startAdvertising(deviceName: String, roomCode: String)
    fun stopAdvertising()
    fun startDiscovery()
    fun stopDiscovery()
    fun connectToPeer(endpointId: String, localDisplayName: String = "")
    fun disconnectPeer(endpointId: String)
    fun disconnectAll()
    fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean = false, ticket: Long = 0L)
    fun sendPacketToPeer(endpointId: String, packet: WhiteboardPacket, isMutation: Boolean = false, ticket: Long = 0L)
}
