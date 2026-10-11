package com.kd.creavity.data.nearby

import android.content.Context
import android.net.wifi.WifiManager
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
import java.util.concurrent.ConcurrentHashMap

/**
 * Cross-platform LAN transport for P2P collaboration with Windows and other Android nodes.
 * Uses UDP beacon on 53535 and TCP streams on 53536+.
 */
class LanDiscoveryTransport(
    private val context: Context,
    val localNodeId: String,
    val meshRoutingEngine: MeshRoutingEngine
) {
    companion object {
        const val DISCOVERY_PORT = 53535
        const val DEFAULT_TCP_PORT = 53536
        private const val BEACON_MAGIC = "CREAVITY_BEACON"
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var multicastLock: WifiManager.MulticastLock? = null
    private var advertisingJob: Job? = null
    private var discoveryJob: Job? = null
    private var serverJob: Job? = null

    private var serverSocket: ServerSocket? = null
    private var boundTcpPort = DEFAULT_TCP_PORT

    private val connectedSockets = ConcurrentHashMap<String, Socket>()
    private val discoveredPeersMap = ConcurrentHashMap<String, DiscoveredLanPeer>()

    private val _discoveredEndpoints = MutableStateFlow<List<PeerDevice>>(emptyList())
    val discoveredEndpoints: StateFlow<List<PeerDevice>> = _discoveredEndpoints.asStateFlow()

    private val _connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    val connectedPeers: StateFlow<List<PeerDevice>> = _connectedPeers.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<WhiteboardPacket>(extraBufferCapacity = 128)
    val incomingPackets: SharedFlow<WhiteboardPacket> = _incomingPackets.asSharedFlow()

    private data class DiscoveredLanPeer(
        val endpointId: String,
        val name: String,
        val ip: String,
        val port: Int,
        val roomCode: String,
        val role: MeshRole,
        val lastSeenMs: Long
    )

    private fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                multicastLock = wifi?.createMulticastLock("creavity_multicast")?.apply {
                    setReferenceCounted(true)
                }
            }
            multicastLock?.acquire()
        } catch (_: Exception) {}
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {}
    }

    fun startAdvertising(deviceName: String, roomCode: String = "") {
        stopAdvertising()
        ensureServerStarted()

        advertisingJob = scope.launch {
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
            }
        }
    }

    fun stopAdvertising() {
        advertisingJob?.cancel()
        advertisingJob = null
    }

    fun startDiscovery() {
        stopDiscovery()
        acquireMulticastLock()

        discoveryJob = scope.launch {
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
                releaseMulticastLock()
            }
        }
    }

    fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
        releaseMulticastLock()
        discoveredPeersMap.clear()
        updateDiscoveredEndpoints()
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
                    val peerName = "Peer ($remoteIp)"

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

    fun connectToPeer(endpointId: String) {
        val peerInfo = discoveredPeersMap[endpointId] ?: return
        scope.launch {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(peerInfo.ip, peerInfo.port), 3000)

                connectedSockets[endpointId] = socket
                meshRoutingEngine.addNeighbor(endpointId)
                updateConnectedPeers()

                startSocketReader(endpointId, socket)

                // Immediate sync request
                sendDirectPacket(socket, WhiteboardPacket.SyncRequest)
            } catch (_: Exception) {}
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

                    handleIncomingFrame(endpointId, bytes)
                }
            } catch (_: Exception) {
            } finally {
                disconnectPeer(endpointId)
            }
        }
    }

    private fun handleIncomingFrame(senderEndpoint: String, bytes: ByteArray) {
        val envelope = MeshEnvelope.fromByteArray(bytes)
        if (envelope != null) {
            when (val result = meshRoutingEngine.processInbound(envelope)) {
                is InboundResult.DeliverAndForward -> {
                    PacketSerializer.deserialize(result.envelope.payload)?.let {
                        _incomingPackets.tryEmit(it)
                    }
                    val fwdBytes = result.envelope.toByteArray()
                    for (target in result.targetNeighbors) {
                        connectedSockets[target]?.let { s -> sendRaw(s, fwdBytes) }
                    }
                }
                is InboundResult.DeliverOnly -> {
                    PacketSerializer.deserialize(result.envelope.payload)?.let {
                        _incomingPackets.tryEmit(it)
                    }
                }
                else -> {}
            }
        } else {
            PacketSerializer.deserialize(bytes)?.let {
                _incomingPackets.tryEmit(it)
            }
        }
    }

    fun sendPacketToAll(packet: WhiteboardPacket, isMutation: Boolean = false, ticket: PermissionTicket? = null) {
        if (connectedSockets.isEmpty()) return
        val rawBytes = PacketSerializer.serialize(packet)
        val (envelope, targets) = meshRoutingEngine.createOutbound(rawBytes, isMutation, ticket)
        val envelopeBytes = envelope.toByteArray()

        for (target in targets) {
            connectedSockets[target]?.let { s -> sendRaw(s, envelopeBytes) }
        }
    }

    private fun sendDirectPacket(socket: Socket, packet: WhiteboardPacket) {
        val rawBytes = PacketSerializer.serialize(packet)
        val (envelope, _) = meshRoutingEngine.createOutbound(rawBytes, false, null)
        sendRaw(socket, envelope.toByteArray())
    }

    private fun sendRaw(socket: Socket, bytes: ByteArray) {
        try {
            val dos = DataOutputStream(socket.getOutputStream())
            dos.writeInt(bytes.size)
            dos.write(bytes)
            dos.flush()
        } catch (_: Exception) {}
    }

    fun disconnectPeer(endpointId: String) {
        connectedSockets.remove(endpointId)?.let {
            try { it.close() } catch (_: Exception) {}
        }
        meshRoutingEngine.removeNeighbor(endpointId)
        updateConnectedPeers()
    }

    fun stopAll() {
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
