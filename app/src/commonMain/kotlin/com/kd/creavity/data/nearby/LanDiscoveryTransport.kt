package com.kd.creavity.data.nearby

import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.mesh.InboundResult
import com.kd.creavity.data.mesh.MeshEnvelope
import com.kd.creavity.data.mesh.MeshRole
import com.kd.creavity.data.mesh.MeshRoutingEngine
import com.kd.creavity.data.mesh.PermissionTicket
import com.kd.creavity.domain.model.PeerDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface MulticastLockHelper {
    fun acquire()
    fun release()
}

/**
 * Pure Kotlin/JVM cross-platform LAN socket mesh transport.
 * Discovers peers via UDP beacon on 53535 and streams whiteboard frames over TCP on 53536+.
 */
class LanDiscoveryTransport(
    override val localNodeId: String = UUID.randomUUID().toString().take(8),
    override val meshRoutingEngine: MeshRoutingEngine = MeshRoutingEngine(localNodeId),
    private val multicastLock: MulticastLockHelper? = null
) : PeerTransport {

    companion object {
        const val DISCOVERY_PORT = 53535
        const val DEFAULT_TCP_PORT = 53536
        private const val BEACON_MAGIC = "CREAVITY_BEACON"
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var advertisingJob: Job? = null
    private var discoveryJob: Job? = null
    private var serverJob: Job? = null

    private var serverSocket: ServerSocket? = null
    private var boundTcpPort = DEFAULT_TCP_PORT

    private val connectedSockets = ConcurrentHashMap<String, Socket>()
    private val discoveredPeersMap = ConcurrentHashMap<String, DiscoveredLanPeer>()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    override val discoveredPeers: StateFlow<List<PeerDevice>> = _discoveredEndpoints.asStateFlow()

    private val _connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    override val connectedPeers: StateFlow<List<PeerDevice>> = _connectedPeers.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 128)
    override val incomingPackets: SharedFlow<WhiteboardPacket> = _incomingPackets.asSharedFlow()

    private data class DiscoveredLanPeer(
        val endpointId: String,
        val name: String,
        val ip: String,
        val port: Int,
        val roomCode: String,
        val role: MeshRole,
        val lastSeenMs: Long
    )

    override fun startAdvertising(deviceName: String, roomCode: String) {
        stopAdvertising()
        ensureServerStarted()

        advertisingJob = scope.launch {
            _isAdvertising.value = true
            var udpSocket: DatagramSocket? = null
            try {
                udpSocket = DatagramSocket().apply { broadcast = true }
                val targetAddr = InetAddress.getByName("255.255.255.255")

                while (isActive) {
                    val beacon = "$BEACON_MAGIC\n$localNodeId\n$deviceName\n$roomCode\n$boundTcpPort\nHOST"
                    val bytes = beacon.toByteArray(StandardCharsets.UTF_8)
                    val packet = DatagramPacket(bytes, bytes.size, targetAddr, DISCOVERY_PORT)
                    try {
                        udpSocket.send(packet)
                    } catch (_: Exception) {}
                    delay(1500)
                }
            } catch (_: Exception) {
            } finally {
                udpSocket?.close()
                _isAdvertising.value = false
            }
        }
    }

    override fun stopAdvertising() {
        advertisingJob?.cancel()
        advertisingJob = null
        _isAdvertising.value = false
    }

    override fun startDiscovery() {
        stopDiscovery()
        multicastLock?.acquire()

        discoveryJob = scope.launch {
            _isDiscovering.value = true
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(DISCOVERY_PORT))
                    broadcast = true
                }
                val buffer = ByteArray(2048)

                // Periodic prune job
                launch {
                    while (isActive) {
                        delay(3000)
                        val now = System.currentTimeMillis()
                        var removed = false
                        discoveredPeersMap.entries.removeIf { (_, peer) ->
                            if (now - peer.lastSeenMs > 7000) {
                                removed = true
                                true
                            } else false
                        }
                        if (removed) updateDiscoveredEndpoints()
                    }
                }

                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val text = String(packet.data, 0, packet.length, StandardCharsets.UTF_8)
                    val lines = text.split("\n")
                    if (lines.size >= 6 && lines[0] == BEACON_MAGIC) {
                        val senderNodeId = lines[1].trim()
                        if (senderNodeId == localNodeId) continue

                        val name = lines[2].trim()
                        val roomCode = lines[3].trim()
                        val remotePort = lines[4].trim().toIntOrNull() ?: DEFAULT_TCP_PORT
                        val roleStr = lines[5].trim()
                        val ip = packet.address.hostAddress ?: continue
                        val endpointId = "lan_${ip}_$remotePort"

                        val role = if (roleStr.equals("HOST", ignoreCase = true)) MeshRole.HOST else MeshRole.VIEWER
                        discoveredPeersMap[endpointId] = DiscoveredLanPeer(
                            endpointId = endpointId,
                            name = name,
                            ip = ip,
                            port = remotePort,
                            roomCode = roomCode,
                            role = role,
                            lastSeenMs = System.currentTimeMillis()
                        )
                        updateDiscoveredEndpoints()
                    }
                }
            } catch (_: Exception) {
            } finally {
                socket?.close()
                multicastLock?.release()
                _isDiscovering.value = false
            }
        }
    }

    override fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
        multicastLock?.release()
        discoveredPeersMap.clear()
        updateDiscoveredEndpoints()
        _isDiscovering.value = false
    }

    private fun ensureServerStarted() {
        if (serverSocket != null) return

        serverJob = scope.launch {
            var port = DEFAULT_TCP_PORT
            while (port < DEFAULT_TCP_PORT + 10) {
                try {
                    serverSocket = ServerSocket(port)
                    boundTcpPort = port
                    break
                } catch (_: Exception) {
                    port++
                }
            }

            val ss = serverSocket ?: return@launch
            while (isActive) {
                try {
                    val client = ss.accept()
                    val remoteIp = client.inetAddress.hostAddress ?: "unknown"
                    val endpointId = "lan_${remoteIp}_${client.port}"

                    connectedSockets[endpointId] = client
                    meshRoutingEngine.addNeighbor(endpointId)
                    updateConnectedPeers()

                    startSocketReader(endpointId, client)

                    // Request state sync
                    sendDirectPacket(client, WhiteboardPacket.SyncRequest)
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    override fun connectToPeer(endpointId: String, localDisplayName: String) {
        val peer = discoveredPeersMap[endpointId] ?: return
        if (connectedSockets.containsKey(endpointId)) return

        scope.launch {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(peer.ip, peer.port), 3000)
                connectedSockets[endpointId] = socket
                meshRoutingEngine.addNeighbor(endpointId)
                updateConnectedPeers()

                startSocketReader(endpointId, socket)

                // Request initial sync
                sendDirectPacket(socket, WhiteboardPacket.SyncRequest)
            } catch (_: Exception) {
                connectedSockets.remove(endpointId)
                meshRoutingEngine.removeNeighbor(endpointId)
                updateConnectedPeers()
            }
        }
    }

    private fun startSocketReader(endpointId: String, socket: Socket) {
        scope.launch {
            try {
                val dis = DataInputStream(socket.getInputStream())
                while (isActive && !socket.isClosed) {
                    val length = dis.readInt()
                    if (length <= 0 || length > 10 * 1024 * 1024) break
                    val bytes = ByteArray(length)
                    dis.readFully(bytes)

                    val envelope = runCatching { MeshEnvelope.fromByteArray(bytes) }.getOrNull()
                    if (envelope != null) {
                        when (val result = meshRoutingEngine.processInbound(envelope)) {
                            is InboundResult.DeliverOnly -> {
                                val packet = PacketSerializer.deserialize(envelope.payload)
                                if (packet != null) _incomingPackets.emit(packet)
                            }
                            is InboundResult.DeliverAndForward -> {
                                val packet = PacketSerializer.deserialize(envelope.payload)
                                if (packet != null) _incomingPackets.emit(packet)
                                val forwardBytes = result.envelope.toByteArray()
                                for (targetId in result.targetNeighbors) {
                                    connectedSockets[targetId]?.let { s ->
                                        scope.launch { sendDirectBytes(s, forwardBytes) }
                                    }
                                }
                            }
                            else -> {}
                        }
                    } else {
                        val packet = PacketSerializer.deserialize(bytes)
                        if (packet != null) _incomingPackets.emit(packet)
                    }
                }
            } catch (_: Exception) {
            } finally {
                disconnectPeer(endpointId)
            }
        }
    }

    override fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {
        val payload = PacketSerializer.serialize(packet)
        val (envelope, targets) = meshRoutingEngine.createOutbound(payload, isMutation)
        val envelopeBytes = envelope.toByteArray()

        for (targetId in targets) {
            val socket = connectedSockets[targetId] ?: continue
            scope.launch {
                sendDirectBytes(socket, envelopeBytes)
            }
        }
    }

    override fun sendPacketToPeer(endpointId: String, packet: WhiteboardPacket, isMutation: Boolean, ticket: Long) {
        val socket = connectedSockets[endpointId] ?: return
        val payload = PacketSerializer.serialize(packet)
        val (envelope, _) = meshRoutingEngine.createOutbound(payload, isMutation)
        val envelopeBytes = envelope.toByteArray()
        scope.launch {
            sendDirectBytes(socket, envelopeBytes)
        }
    }

    private fun sendDirectPacket(socket: Socket, packet: WhiteboardPacket) {
        val bytes = PacketSerializer.serialize(packet)
        sendDirectBytes(socket, bytes)
    }

    private fun sendDirectBytes(socket: Socket, bytes: ByteArray) {
        try {
            val dos = DataOutputStream(socket.getOutputStream())
            dos.writeInt(bytes.size)
            dos.write(bytes)
            dos.flush()
        } catch (_: Exception) {}
    }

    fun forwardEnvelope(envelope: MeshEnvelope, targetEndpoints: Set<String>) {
        val forwardBytes = envelope.toByteArray()
        for (targetId in targetEndpoints) {
            connectedSockets[targetId]?.let { s ->
                scope.launch { sendDirectBytes(s, forwardBytes) }
            }
        }
    }

    override fun disconnectPeer(endpointId: String) {
        connectedSockets.remove(endpointId)?.let {
            try { it.close() } catch (_: Exception) {}
        }
        meshRoutingEngine.removeNeighbor(endpointId)
        updateConnectedPeers()
    }

    override fun disconnectAll() {
        stopAdvertising()
        stopDiscovery()
        serverJob?.cancel()
        serverJob = null
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null

        connectedSockets.values.forEach {
            try { it.close() } catch (_: Exception) {}
        }
        connectedSockets.clear()
        discoveredPeersMap.clear()
        updateConnectedPeers()
        updateDiscoveredEndpoints()
    }

    private fun updateDiscoveredEndpoints() {
        _discoveredEndpoints.value = discoveredPeersMap.values.map {
            PeerDevice(endpointId = it.endpointId, name = it.name, role = it.role)
        }
    }

    private fun updateConnectedPeers() {
        _connectedPeers.value = connectedSockets.keys.map { endpointId ->
            val name = discoveredPeersMap[endpointId]?.name ?: "Peer $endpointId"
            PeerDevice(endpointId = endpointId, name = name, isConnected = true)
        }
    }
}
