package com.kd.creavity.data.mesh

import java.util.concurrent.atomic.AtomicLong

sealed class InboundResult {
    data class DeliverAndForward(val envelope: MeshEnvelope, val targetNeighbors: List<String>) : InboundResult()
    data class DeliverOnly(val envelope: MeshEnvelope) : InboundResult()
    object DroppedDuplicate : InboundResult()
    object DroppedTtlExpired : InboundResult()
    object DroppedUnauthorized : InboundResult()
}

class MeshRoutingEngine(
    val nodeId: String,
    val maxDegree: Int = 6,
    var secretKey: String? = null
) {
    private val seqCounter = AtomicLong(0)
    private val activeNeighbors = mutableSetOf<String>()

    // Anti-loop LRU cache: holds last 4096 seen packet IDs
    private val seenPacketIds = object : LinkedHashMap<Long, Long>(4096, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Long>?): Boolean {
            return size > 4096
        }
    }

    // Known participant tickets
    private val peerTickets = mutableMapOf<String, PermissionTicket>()

    @Synchronized
    fun addNeighbor(peerId: String): Boolean {
        if (peerId == nodeId) return false
        if (activeNeighbors.size >= maxDegree) return false
        return activeNeighbors.add(peerId)
    }

    @Synchronized
    fun removeNeighbor(peerId: String) {
        activeNeighbors.remove(peerId)
        peerTickets.remove(peerId)
    }

    @Synchronized
    fun getNeighbors(): List<String> = activeNeighbors.toList()

    @Synchronized
    fun registerTicket(ticket: PermissionTicket) {
        peerTickets[ticket.deviceId] = ticket
    }

    @Synchronized
    fun getTicket(deviceId: String): PermissionTicket? = peerTickets[deviceId]

    @Synchronized
    fun createOutbound(
        payload: ByteArray,
        isMutation: Boolean,
        ticket: PermissionTicket? = null
    ): Pair<MeshEnvelope, List<String>> {
        val packetId = (System.currentTimeMillis() shl 20) or (seqCounter.incrementAndGet() and 0xFFFFF)
        val envelope = MeshEnvelope(
            packetId = packetId,
            originNodeId = nodeId,
            senderNodeId = nodeId,
            ttl = 12,
            hopCount = 0,
            roleToken = ticket?.signature ?: "",
            isMutation = isMutation,
            payload = payload
        )
        seenPacketIds[packetId] = System.currentTimeMillis()
        return envelope to activeNeighbors.toList()
    }

    @Synchronized
    fun processInbound(envelope: MeshEnvelope): InboundResult {
        // 1. Drop if TTL expired
        if (envelope.ttl <= 0) {
            return InboundResult.DroppedTtlExpired
        }

        // 2. Drop if duplicate (already seen or originated by us)
        if (seenPacketIds.containsKey(envelope.packetId) || envelope.originNodeId == nodeId) {
            return InboundResult.DroppedDuplicate
        }
        seenPacketIds[envelope.packetId] = System.currentTimeMillis()

        // 3. Permission verification: Drop mutation if sender is Viewer
        if (envelope.isMutation) {
            val key = secretKey
            if (key != null) {
                val ticket = peerTickets[envelope.originNodeId]
                if (ticket != null) {
                    if (!MeshAccessController.canWrite(ticket) || !MeshAccessController.validateTicket(key, ticket)) {
                        return InboundResult.DroppedUnauthorized
                    }
                }
            }
        }

        // 4. Determine forward targets (all neighbors except sender)
        val nextTtl = (envelope.ttl - 1).toByte()
        val targets = activeNeighbors.filter { it != envelope.senderNodeId }

        if (nextTtl > 0 && targets.isNotEmpty()) {
            val forwarded = envelope.copy(
                senderNodeId = nodeId,
                ttl = nextTtl,
                hopCount = (envelope.hopCount + 1).toByte()
            )
            return InboundResult.DeliverAndForward(forwarded, targets)
        }

        return InboundResult.DeliverOnly(envelope)
    }

    @Synchronized
    fun isAtCapacity(): Boolean = activeNeighbors.size >= maxDegree

    @Synchronized
    fun clear() {
        activeNeighbors.clear()
        seenPacketIds.clear()
        peerTickets.clear()
    }
}
