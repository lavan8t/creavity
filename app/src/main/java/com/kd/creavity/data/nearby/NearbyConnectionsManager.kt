package com.kd.creavity.data.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.domain.model.PeerDevice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.kd.creavity.data.mesh.InboundResult
import com.kd.creavity.data.mesh.MeshEnvelope
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.data.mesh.PermissionTicket
import java.util.UUID
import kotlinx.coroutines.flow.asStateFlow

class NearbyConnectionsManager(context: Context) {
    private val appContext = context.applicationContext
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(appContext)

    private val SERVICE_ID = "com.kd.creavity.SERVICE_ID"
    private val STRATEGY = Strategy.P2P_CLUSTER

    val localNodeId: String = UUID.randomUUID().toString()
    val meshRoutingEngine = MeshRoutingEngine(nodeId = localNodeId, maxDegree = 6)

    private val _discoveredEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    val discoveredEndpoints: StateFlow<List<PeerDevice>> = _discoveredEndpoints.asStateFlow()

    private val _connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    val connectedPeers: StateFlow<List<PeerDevice>> = _connectedPeers.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 128)
    val incomingPackets: SharedFlow<WhiteboardPacket> = _incomingPackets.asSharedFlow()

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    val envelope = MeshEnvelope.fromByteArray(bytes)
                    if (envelope != null) {
                        when (val result = meshRoutingEngine.processInbound(envelope)) {
                            is InboundResult.DeliverAndForward -> {
                                PacketSerializer.deserialize(result.envelope.payload)?.let { packet ->
                                    _incomingPackets.tryEmit(packet)
                                }
                                val forwardPayload = Payload.fromBytes(result.envelope.toByteArray())
                                connectionsClient.sendPayload(result.targetNeighbors, forwardPayload)
                            }
                            is InboundResult.DeliverOnly -> {
                                PacketSerializer.deserialize(result.envelope.payload)?.let { packet ->
                                    _incomingPackets.tryEmit(packet)
                                }
                            }
                            is InboundResult.DroppedDuplicate -> {}
                            is InboundResult.DroppedTtlExpired -> {}
                            is InboundResult.DroppedUnauthorized -> {}
                        }
                    } else {
                        PacketSerializer.deserialize(bytes)?.let { packet ->
                            _incomingPackets.tryEmit(packet)
                        }
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Automatically accept incoming connection requests for zero-friction collaboration
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                val currentList = _discoveredEndpoints.value
                val peer = currentList.find { it.endpointId == endpointId }
                    ?: PeerDevice(endpointId = endpointId, name = "Peer $endpointId")

                _discoveredEndpoints.value = _discoveredEndpoints.value.filter { it.endpointId != endpointId }
                _connectedPeers.value = _connectedPeers.value.filter { it.endpointId != endpointId } + peer.copy(isConnected = true)
                meshRoutingEngine.addNeighbor(endpointId)

                // Trigger immediate initial state synchronization
                val syncBytes = PacketSerializer.serialize(WhiteboardPacket.SyncRequest)
                connectionsClient.sendPayload(endpointId, Payload.fromBytes(syncBytes))
            }
        }

        override fun onDisconnected(endpointId: String) {
            _connectedPeers.value = _connectedPeers.value.filter { it.endpointId != endpointId }
            meshRoutingEngine.removeNeighbor(endpointId)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val peer = PeerDevice(endpointId = endpointId, name = info.endpointName)
            if (_connectedPeers.value.none { it.endpointId == endpointId } && _discoveredEndpoints.value.none { it.endpointId == endpointId }) {
                _discoveredEndpoints.value = _discoveredEndpoints.value + peer
            }
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredEndpoints.value = _discoveredEndpoints.value.filter { it.endpointId != endpointId }
        }
    }

    fun startAdvertising(deviceName: String) {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            deviceName,
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            _isAdvertising.value = true
        }.addOnFailureListener {
            _isAdvertising.value = false
        }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }

    fun startDiscovery() {
        val options = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            options
        ).addOnSuccessListener {
            _isDiscovering.value = true
        }.addOnFailureListener {
            _isDiscovering.value = false
        }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
    }

    fun connectToPeer(endpointId: String, localName: String) {
        connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback)
    }

    fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean = false, ticket: PermissionTicket? = null) {
        val connectedList = _connectedPeers.value
        if (connectedList.isEmpty()) return
        val rawBytes = PacketSerializer.serialize(packet)
        val (envelope, targets) = meshRoutingEngine.createOutbound(rawBytes, isMutation, ticket)
        if (targets.isEmpty()) return
        val payload = Payload.fromBytes(envelope.toByteArray())
        connectionsClient.sendPayload(targets, payload)
    }

    fun disconnectFromPeer(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
        _connectedPeers.value = _connectedPeers.value.filter { it.endpointId != endpointId }
        meshRoutingEngine.removeNeighbor(endpointId)
    }

    fun stopAllEndpoints() {
        connectionsClient.stopAllEndpoints()
        meshRoutingEngine.clear()
        _connectedPeers.value = emptyList()
        _discoveredEndpoints.value = emptyList()
        _isAdvertising.value = false
        _isDiscovering.value = false
    }
}
