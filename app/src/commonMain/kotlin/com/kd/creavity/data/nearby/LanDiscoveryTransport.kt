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
import java.net.MulticastSocket
import java.net.NetworkInterface
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
        const val MULTICAST_GROUP = "224.0.0.167"
        private const val BEACON_MAGIC = "CREAVITY_BEACON"

        fun getLocalIpAddress(): String {
            try {
                val ifaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
                for (iface in ifaces) {
                    if (!iface.isUp || iface.isLoopback) continue
                    for (addr in iface.inetAddresses) {
                        if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                            return addr.hostAddress ?: ""
                        }
                    }
                }
            } catch (_: Throwable) {}
            return "127.0.0.1"
        }
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

    var activeBoardId: String = ""

    private data class DiscoveredLanPeer(
        val endpointId: String,
        val name: String,
        val ip: String,
        val port: Int,
        val roomCode: String,
        val role: MeshRole,
        val lastSeenMs: Long,
        val boardId: String = ""
    )

    override fun startAdvertising(deviceName: String, roomCode: String) {
        stopAdvertising()
        ensureServerStarted()
        multicastLock?.acquire()
        _isAdvertising.value = true

        advertisingJob = scope.launch {
            var udpSocket: DatagramSocket? = null
            var mcastSocket: MulticastSocket? = null
            try {
                udpSocket = DatagramSocket().apply { broadcast = true }
                mcastSocket = MulticastSocket().apply { timeToLive = 4 }
                val mcastAddr = InetAddress.getByName(MULTICAST_GROUP)

                while (isActive) {
                    val beacon = "$BEACON_MAGIC\n$localNodeId\n$deviceName\n$roomCode\n$boundTcpPort\nHOST\n$activeBoardId"
                    val bytes = beacon.toByteArray(StandardCharsets.UTF_8)
                    sendBeacon(bytes, udpSocket, mcastSocket, mcastAddr)
                    delay(1500)
                }
            } catch (_: Exception) {
            } finally {
                udpSocket?.close()
                mcastSocket?.close()
                multicastLock?.release()
                _isAdvertising.value = false
            }
        }
    }

    private fun sendBeacon(
        bytes: ByteArray,
        udpSocket: DatagramSocket,
        mcastSocket: MulticastSocket,
        mcastAddr: InetAddress
    ) {
        // 1. Limited broadcast
        try {
            val limited = DatagramPacket(bytes, bytes.size, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT)
            udpSocket.send(limited)
        } catch (_: Throwable) {}

        // 2. Subnet directed broadcast & multicast across all active network interfaces
        try {
            val ifaces = NetworkInterface.getNetworkInterfaces() ?: return
            for (iface in ifaces) {
                try {
                    if (!iface.isUp || iface.isLoopback) continue
                    val hasIpv4 = iface.inetAddresses.asSequence().any { it is java.net.Inet4Address }
                    if (!hasIpv4) continue

                    for (addr in iface.interfaceAddresses) {
                        val bcast = addr.broadcast
                        if (bcast != null) {
                            try {
                                val packet = DatagramPacket(bytes, bytes.size, bcast, DISCOVERY_PORT)
                                udpSocket.send(packet)
                            } catch (_: Throwable) {}
                        }
                    }

                    if (iface.supportsMulticast()) {
                        try {
                            mcastSocket.networkInterface = iface
                            val mcastPacket = DatagramPacket(bytes, bytes.size, mcastAddr, DISCOVERY_PORT)
                            mcastSocket.send(mcastPacket)
                        } catch (_: Throwable) {}
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    override fun stopAdvertising() {
        advertisingJob?.cancel()
        advertisingJob = null
        multicastLock?.release()
        _isAdvertising.value = false
    }

    override fun startDiscovery() {
        stopDiscovery()
        multicastLock?.acquire()
        _isDiscovering.value = true

        discoveryJob = scope.launch {
            var socket: MulticastSocket? = null
            val mcastAddr = InetAddress.getByName(MULTICAST_GROUP)
            val mcastGroup = InetSocketAddress(mcastAddr, DISCOVERY_PORT)

            try {
                socket = MulticastSocket(DISCOVERY_PORT).apply {
                    reuseAddress = true
                    broadcast = true
                }

                // Join multicast group on all active non-loopback network interfaces
                try {
                    val ifaces = NetworkInterface.getNetworkInterfaces()
                    if (ifaces != null) {
                        for (iface in ifaces) {
                            try {
                                if (!iface.isUp || iface.isLoopback || !iface.supportsMulticast()) continue
                                val hasIpv4 = iface.inetAddresses.asSequence().any { it is java.net.Inet4Address }
                                if (!hasIpv4) continue
                                socket.joinGroup(mcastGroup, iface)
                            } catch (_: Throwable) {}
                        }
                    }
                } catch (_: Throwable) {}

                // Send discovery wake-up ping
                try {
                    val ping = "$BEACON_MAGIC\n$localNodeId\n$localNodeId\n\n$DEFAULT_TCP_PORT\nVIEWER"
                    val pingBytes = ping.toByteArray(StandardCharsets.UTF_8)
                    socket.send(DatagramPacket(pingBytes, pingBytes.size, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT))
                } catch (_: Throwable) {}

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
                        val boardId = if (lines.size >= 7) lines[6].trim() else ""
                        val rawIp = packet.address.hostAddress ?: continue
                        val ip = rawIp.substringBefore('%')
                        val endpointId = "lan_${ip}_$remotePort"

                        val role = if (roleStr.equals("HOST", ignoreCase = true)) MeshRole.HOST else MeshRole.VIEWER
                        discoveredPeersMap[endpointId] = DiscoveredLanPeer(
                            endpointId = endpointId,
                            name = name,
                            ip = ip,
                            port = remotePort,
                            roomCode = roomCode,
                            role = role,
                            lastSeenMs = System.currentTimeMillis(),
                            boardId = boardId
                        )
                        updateDiscoveredEndpoints()
                    }
                }
            } catch (_: Throwable) {
            } finally {
                runCatching { socket?.close() }
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

    @Synchronized
    private fun ensureServerStarted() {
        if (serverSocket != null) return

        var ss: ServerSocket? = null
        var port = DEFAULT_TCP_PORT
        while (port < DEFAULT_TCP_PORT + 10) {
            try {
                ss = ServerSocket(port, 128)
                boundTcpPort = port
                break
            } catch (_: Exception) {
                port++
            }
        }
        serverSocket = ss ?: return

        serverJob = scope.launch {
            val server = serverSocket ?: return@launch
            while (isActive) {
                try {
                    val client = server.accept()
                    client.tcpNoDelay = true
                    val remoteIp = (client.inetAddress.hostAddress ?: "unknown").substringBefore('%')
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
        if (connectedSockets.containsKey(endpointId)) return
        val peer = discoveredPeersMap[endpointId]
        val (ip, port) = if (peer != null) {
            peer.ip to peer.port
        } else if (endpointId.startsWith("lan_")) {
            val parts = endpointId.removePrefix("lan_").split("_")
            val targetIp = parts.getOrNull(0) ?: return
            val targetPort = parts.getOrNull(1)?.toIntOrNull() ?: DEFAULT_TCP_PORT
            targetIp to targetPort
        } else return

        scope.launch {
            try {
                val socket = Socket()
                socket.tcpNoDelay = true
                socket.connect(InetSocketAddress(ip, port), 4000)
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
        val payload = PacketSerializer.serialize(packet)
        val (envelope, _) = meshRoutingEngine.createOutbound(payload, false)
        sendDirectBytes(socket, envelope.toByteArray())
    }

    private fun sendDirectBytes(socket: Socket, bytes: ByteArray) {
        try {
            synchronized(socket) {
                val dos = DataOutputStream(socket.getOutputStream())
                dos.writeInt(bytes.size)
                dos.write(bytes)
                dos.flush()
            }
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
            PeerDevice(endpointId = it.endpointId, name = it.name, role = it.role, boardId = it.boardId)
        }
    }

    private fun updateConnectedPeers() {
        _connectedPeers.value = connectedSockets.keys.map { endpointId ->
            val directPeer = discoveredPeersMap[endpointId]
            val peerByIp = if (directPeer == null) {
                val ip = endpointId.removePrefix("lan_").substringBefore('_')
                discoveredPeersMap.values.find { it.ip == ip }
            } else null
            val name = directPeer?.name ?: peerByIp?.name ?: "Peer ${endpointId.substringAfter("lan_")}"
            PeerDevice(endpointId = endpointId, name = name, isConnected = true)
        }
    }
}
