package com.kd.creavity.data.mesh

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

enum class MeshRole {
    HOST,
    EDITOR,
    VIEWER
}

data class PermissionTicket(
    val deviceId: String,
    val boardId: String,
    val role: MeshRole,
    val expiresAt: Long = 0L,
    val signature: String = ""
)

data class MeshEnvelope(
    val packetId: Long,
    val originNodeId: String,
    val senderNodeId: String,
    val ttl: Byte = 12,
    val hopCount: Byte = 0,
    val roleToken: String = "",
    val isMutation: Boolean = false,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeshEnvelope) return false
        return packetId == other.packetId &&
                originNodeId == other.originNodeId &&
                senderNodeId == other.senderNodeId &&
                ttl == other.ttl &&
                hopCount == other.hopCount &&
                roleToken == other.roleToken &&
                isMutation == other.isMutation &&
                payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = packetId.hashCode()
        result = 31 * result + originNodeId.hashCode()
        result = 31 * result + senderNodeId.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + hopCount.hashCode()
        result = 31 * result + roleToken.hashCode()
        result = 31 * result + isMutation.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    fun toByteArray(): ByteArray {
        val originBytes = originNodeId.toByteArray(StandardCharsets.UTF_8)
        val senderBytes = senderNodeId.toByteArray(StandardCharsets.UTF_8)
        val tokenBytes = roleToken.toByteArray(StandardCharsets.UTF_8)

        val totalSize = 8 + // packetId (Long)
                1 + // ttl (Byte)
                1 + // hopCount (Byte)
                1 + // isMutation (Byte 0/1)
                2 + originBytes.size + // originNodeId
                2 + senderBytes.size + // senderNodeId
                2 + tokenBytes.size + // roleToken
                4 + payload.size // payload length + bytes

        val buffer = ByteBuffer.allocate(totalSize)
        buffer.putLong(packetId)
        buffer.put(ttl)
        buffer.put(hopCount)
        buffer.put(if (isMutation) 1.toByte() else 0.toByte())

        buffer.putShort(originBytes.size.toShort())
        buffer.put(originBytes)

        buffer.putShort(senderBytes.size.toShort())
        buffer.put(senderBytes)

        buffer.putShort(tokenBytes.size.toShort())
        buffer.put(tokenBytes)

        buffer.putInt(payload.size)
        buffer.put(payload)

        return buffer.array()
    }

    companion object {
        fun fromByteArray(bytes: ByteArray): MeshEnvelope? {
            if (bytes.size < 19) return null
            return try {
                val buffer = ByteBuffer.wrap(bytes)
                val packetId = buffer.long
                val ttl = buffer.get()
                val hopCount = buffer.get()
                val isMutation = buffer.get() == 1.toByte()

                val originLen = buffer.short.toInt()
                val originBytes = ByteArray(originLen)
                buffer.get(originBytes)
                val originNodeId = String(originBytes, StandardCharsets.UTF_8)

                val senderLen = buffer.short.toInt()
                val senderBytes = ByteArray(senderLen)
                buffer.get(senderBytes)
                val senderNodeId = String(senderBytes, StandardCharsets.UTF_8)

                val tokenLen = buffer.short.toInt()
                val tokenBytes = ByteArray(tokenLen)
                buffer.get(tokenBytes)
                val roleToken = String(tokenBytes, StandardCharsets.UTF_8)

                val payloadLen = buffer.int
                val payload = ByteArray(payloadLen)
                buffer.get(payload)

                MeshEnvelope(
                    packetId = packetId,
                    originNodeId = originNodeId,
                    senderNodeId = senderNodeId,
                    ttl = ttl,
                    hopCount = hopCount,
                    roleToken = roleToken,
                    isMutation = isMutation,
                    payload = payload
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
