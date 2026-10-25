package com.kd.creavity.data.nearby

import android.content.Context
import android.net.wifi.WifiManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.gms.nearby.connection.Payload
import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.mesh.InboundResult
import com.kd.creavity.data.mesh.MeshEnvelope
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.data.mesh.PermissionTicket
import com.kd.creavity.domain.model.PeerDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

class NearbyConnectionsManager(context: Context) : PeerTransport {
    private val appContext = context.applicationContext
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(appContext)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val SERVICE_ID = "com.kd.creavity.SERVICE_ID"
    private val STRATEGY = Strategy.P2P_CLUSTER

    private val multicastLockHelper = object : MulticastLockHelper {
        private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        private val lock: WifiManager.MulticastLock? = wifiManager?.createMulticastLock("creavity_multicast_lock")?.apply {
            setReferenceCounted(true)
        }

        override fun acquire() {
            runCatching {
                if (lock?.isHeld != true) {
                    lock?.acquire()
                }
            }
        }

        override fun release() {
            runCatching {
                if (lock?.isHeld == true) {
                    lock?.release()
                }
            }
        }
    }

    override val localNodeId: String = UUID.randomUUID().toString()
    override val meshRoutingEngine: MeshRoutingEngine = MeshRoutingEngine(nodeId = localNodeId, maxDegree = 6)
    val lanTransport = LanDiscoveryTransport(localNodeId, meshRoutingEngine, multicastLock = multicastLockHelper)

    private val _discoveredNearbyEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _discoveredEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    override val discoveredPeers: StateFlow<List<PeerDevice>> = _discoveredEndpoints.asStateFlow()

    private val _connectedNearbyPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    override val connectedPeers: StateFlow<List<PeerDevice>> = _connectedPeers.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 128)
    override val incomingPackets: SharedFlow<WhiteboardPacket> = _incomingPackets.asSharedFlow()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    init {
        lanTransport.discoveredPeers.onEach { lanPeers ->
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
            val bytes = payload.asBytes() ?: return
            val envelope = MeshEnvelope.fromByteArray(bytes) ?: return

            when (val res = meshRoutingEngine.processInbound(envelope)) {
                is InboundResult.DeliverOnly -> {
                    PacketSerializer.deserialize(res.envelope.payload)?.let { packet ->
                        _incomingPackets.tryEmit(packet)
                    }
                }
                is InboundResult.DeliverAndForward -> {
                    PacketSerializer.deserialize(res.envelope.payload)?.let { packet ->
                        _incomingPackets.tryEmit(packet)
                    }
                    forwardPayload(res.envelope, res.targetNeighbors.toSet())
                }
                else -> {}
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private fun forwardPayload(envelope: MeshEnvelope, nextHops: Set<String>) {
        val lanHops = nextHops.filter { it.startsWith("lan_") }
        if (lanHops.isNotEmpty()) {
            lanTransport.forwardEnvelope(envelope, lanHops.toSet())
        }

        val nearbyHops = nextHops.filterNot { it.startsWith("lan_") }
        if (nearbyHops.isNotEmpty()) {
            val payload = Payload.fromBytes(envelope.toByteArray())
            connectionsClient.sendPayload(nearbyHops, payload)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                val peer = _discoveredNearbyEndpoints.value.find { it.endpointId == endpointId }
                    ?: PeerDevice(endpointId = endpointId, name = "Nearby Peer")

                meshRoutingEngine.addNeighbor(endpointId)
                _connectedNearbyPeers.value = _connectedNearbyPeers.value + peer
                _discoveredNearbyEndpoints.value = _discoveredNearbyEndpoints.value.filter { it.endpointId != endpointId }
                mergeConnectedPeers()
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
            if (_discoveredNearbyEndpoints.value.none { it.endpointId == endpointId }) {
                _discoveredNearbyEndpoints.value = _discoveredNearbyEndpoints.value + peer
                mergeDiscoveredPeers()
            }
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredNearbyEndpoints.value = _discoveredNearbyEndpoints.value.filter { it.endpointId != endpointId }
            mergeDiscoveredPeers()
        }
    }

    override fun startAdvertising(deviceName: String, roomCode: String) {
        lanTransport.startAdvertising(deviceName, roomCode)

        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            deviceName,
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            _isAdvertising.value = true
        }.addOnFailureListener {
            _isAdvertising.value = true
        }
    }

    override fun stopAdvertising() {
        lanTransport.stopAdvertising()
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }

    override fun startDiscovery() {
        lanTransport.startDiscovery()

        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            options
        ).addOnSuccessListener {
            _isDiscovering.value = true
        }.addOnFailureListener {
            _isDiscovering.value = true
        }
    }

    override fun stopDiscovery() {
        lanTransport.stopDiscovery()
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
    }

    override fun connectToPeer(endpointId: String, localDisplayName: String) {
        if (endpointId.startsWith("lan_")) {
            lanTransport.connectToPeer(endpointId, localDisplayName)
        } else {
            val name = localDisplayName.ifBlank { "Android Peer" }
            connectionsClient.requestConnection(name, endpointId, connectionLifecycleCallback)
        }
    }

    override fun disconnectPeer(endpointId: String) {
        if (endpointId.startsWith("lan_")) {
            lanTransport.disconnectPeer(endpointId)
        } else {
            connectionsClient.disconnectFromEndpoint(endpointId)
            _connectedNearbyPeers.value = _connectedNearbyPeers.value.filter { it.endpointId != endpointId }
            mergeConnectedPeers()
            meshRoutingEngine.removeNeighbor(endpointId)
        }
    }

    override fun disconnectAll() {
        lanTransport.disconnectAll()
        connectionsClient.stopAllEndpoints()
        meshRoutingEngine.clear()
        _connectedNearbyPeers.value = emptyList()
        _discoveredNearbyEndpoints.value = emptyList()
        _connectedPeers.value = emptyList()
        _discoveredEndpoints.value = emptyList()
        _isAdvertising.value = false
        _isDiscovering.value = false
    }

    override fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {
        lanTransport.sendPacketToAll(packet, isMutation, ticket)

        val connectedList = _connectedNearbyPeers.value
        if (connectedList.isEmpty()) return
        val rawBytes = PacketSerializer.serialize(packet)
        val (envelope, targets) = meshRoutingEngine.createOutbound(rawBytes, isMutation, null)
        val nearbyTargets = targets.filterNot { it.startsWith("lan_") }
        if (nearbyTargets.isEmpty()) return
        val payload = Payload.fromBytes(envelope.toByteArray())
        connectionsClient.sendPayload(nearbyTargets, payload)
    }

    override fun sendPacketToPeer(endpointId: String, packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {
        if (endpointId.startsWith("lan_")) {
            lanTransport.sendPacketToPeer(endpointId, packet, isMutation, ticket)
            return
        }
        val rawBytes = PacketSerializer.serialize(packet)
        val (envelope, _) = meshRoutingEngine.createOutbound(rawBytes, isMutation, null)
        val payload = Payload.fromBytes(envelope.toByteArray())
        connectionsClient.sendPayload(listOf(endpointId), payload)
    }
}
