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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NearbyConnectionsManager(context: Context) {
    private val appContext = context.applicationContext
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(appContext)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val SERVICE_ID = "com.kd.creavity.SERVICE_ID"
    private val STRATEGY = Strategy.P2P_CLUSTER

    val localNodeId: String = UUID.randomUUID().toString()
    val meshRoutingEngine = MeshRoutingEngine(nodeId = localNodeId, maxDegree = 6)
    val lanTransport = LanDiscoveryTransport(context, localNodeId, meshRoutingEngine)

    private val _discoveredNearbyEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _discoveredEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    val discoveredEndpoints: StateFlow<List<PeerDevice>> = _discoveredEndpoints.asStateFlow()

    private val _connectedNearbyPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    val connectedPeers: StateFlow<List<PeerDevice>> = _connectedPeers.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 128)
    val incomingPackets: SharedFlow<WhiteboardPacket> = _incomingPackets.asSharedFlow()

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    init {
        lanTransport.discoveredEndpoints.onEach { lanPeers ->
            mergeDiscoveredPeers(lanPeers)
        }.launchIn(coroutineScope)

        lanTransport.connectedPeers.onEach { lanPeers ->
            mergeConnectedPeers(lanPeers)
        }.launchIn(coroutineScope)

        lanTransport.incomingPackets.onEach { packet ->
            _incomingPackets.tryEmit(packet)
        }.launchIn(coroutineScope)
    }

    private fun mergeDiscoveredPeers(lanPeers: List<PeerDevice> = emptyList()) {
        val currentNearby = _discoveredNearbyEndpoints.value
        val combined = (currentNearby + lanPeers).distinctBy { it.endpointId }
        val connectedIds = _connectedPeers.value.map { it.endpointId }.toSet()
        _discoveredEndpoints.value = combined.filter { it.endpointId !in connectedIds }
    }

    private fun mergeConnectedPeers(lanPeers: List<PeerDevice> = emptyList()) {
        val currentNearby = _connectedNearbyPeers.value
        val combined = (currentNearby + lanPeers).distinctBy { it.endpointId }
        _connectedPeers.value = combined
        mergeDiscoveredPeers()
    }

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

                _discoveredNearbyEndpoints.value = _discoveredNearbyEndpoints.value.filter { it.endpointId != endpointId }
                _connectedNearbyPeers.value = _connectedNearbyPeers.value.filter { it.endpointId != endpointId } + peer.copy(isConnected = true)
                mergeConnectedPeers()
                meshRoutingEngine.addNeighbor(endpointId)

                // Trigger immediate initial state synchronization
                val syncBytes = PacketSerializer.serialize(WhiteboardPacket.SyncRequest)
                connectionsClient.sendPayload(endpointId, Payload.fromBytes(syncBytes))
            }
        }

        override fun onDisconnected(endpointId: String) {
            _connectedNearbyPeers.value = _connectedNearbyPeers.value.filter { it.endpointId != endpointId }
            mergeConnectedPeers()
            meshRoutingEngine.removeNeighbor(endpointId)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val peer = PeerDevice(endpointId = endpointId, name = info.endpointName)
            _discoveredNearbyEndpoints.value = _discoveredNearbyEndpoints.value.filter { it.endpointId != endpointId } + peer
            mergeDiscoveredPeers()
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredNearbyEndpoints.value = _discoveredNearbyEndpoints.value.filter { it.endpointId != endpointId }
            mergeDiscoveredPeers()
        }
    }

    fun startAdvertising(deviceName: String) {
        lanTransport.startAdvertising(deviceName)
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            deviceName,
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            _isAdvertising.value = true
        }.addOnFailureListener {
            _isAdvertising.value = true // LAN advertising is still active
        }
    }

    fun stopAdvertising() {
        lanTransport.stopAdvertising()
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }

    fun startDiscovery() {
        lanTransport.startDiscovery()
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
            _isDiscovering.value = true // LAN discovery is still active
        }
    }

    fun stopDiscovery() {
        lanTransport.stopDiscovery()
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
    }

    fun connectToPeer(endpointId: String, localName: String) {
        if (endpointId.startsWith("lan_")) {
            lanTransport.connectToPeer(endpointId)
        } else {
            connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback)
        }
    }

    fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean = false, ticket: PermissionTicket? = null) {
        lanTransport.sendPacketToAll(packet, isMutation, ticket)

        val connectedList = _connectedNearbyPeers.value
        if (connectedList.isEmpty()) return
        val rawBytes = PacketSerializer.serialize(packet)
        val (envelope, targets) = meshRoutingEngine.createOutbound(rawBytes, isMutation, ticket)
        val nearbyTargets = targets.filterNot { it.startsWith("lan_") }
        if (nearbyTargets.isEmpty()) return
        val payload = Payload.fromBytes(envelope.toByteArray())
        connectionsClient.sendPayload(nearbyTargets, payload)
    }

    fun disconnectFromPeer(endpointId: String) {
        if (endpointId.startsWith("lan_")) {
            lanTransport.disconnectPeer(endpointId)
        } else {
            connectionsClient.disconnectFromEndpoint(endpointId)
            _connectedNearbyPeers.value = _connectedNearbyPeers.value.filter { it.endpointId != endpointId }
            mergeConnectedPeers()
            meshRoutingEngine.removeNeighbor(endpointId)
        }
    }

    fun stopAllEndpoints() {
        lanTransport.stopAll()
        connectionsClient.stopAllEndpoints()
        meshRoutingEngine.clear()
        _connectedNearbyPeers.value = emptyList()
        _discoveredNearbyEndpoints.value = emptyList()
        _connectedPeers.value = emptyList()
        _discoveredEndpoints.value = emptyList()
        _isAdvertising.value = false
        _isDiscovering.value = false
    }
}
