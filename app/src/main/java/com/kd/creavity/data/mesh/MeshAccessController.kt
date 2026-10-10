package com.kd.creavity.data.mesh

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object MeshAccessController {
    private const val ALGORITHM = "HmacSHA256"

    fun generateSignature(
        secretKey: String,
        boardId: String,
        deviceId: String,
        role: MeshRole,
        expiresAt: Long
    ): String {
        return try {
            val hmac = Mac.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            hmac.init(keySpec)
            val data = "$boardId:$deviceId:${role.name}:$expiresAt"
            val hash = hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    fun createTicket(
        secretKey: String,
        boardId: String,
        deviceId: String,
        role: MeshRole,
        durationMs: Long = 86_400_000L // 24 hours default
    ): PermissionTicket {
        val expiresAt = if (durationMs != 0L) System.currentTimeMillis() + durationMs else 0L
        val sig = generateSignature(secretKey, boardId, deviceId, role, expiresAt)
        return PermissionTicket(
            deviceId = deviceId,
            boardId = boardId,
            role = role,
            expiresAt = expiresAt,
            signature = sig
        )
    }

    fun validateTicket(secretKey: String, ticket: PermissionTicket): Boolean {
        if (ticket.signature.isBlank()) return false
        if (ticket.expiresAt > 0 && System.currentTimeMillis() > ticket.expiresAt) return false
        val expected = generateSignature(
            secretKey = secretKey,
            boardId = ticket.boardId,
            deviceId = ticket.deviceId,
            role = ticket.role,
            expiresAt = ticket.expiresAt
        )
        return expected.equals(ticket.signature, ignoreCase = true)
    }

    fun canWrite(ticket: PermissionTicket): Boolean {
        return ticket.role == MeshRole.HOST || ticket.role == MeshRole.EDITOR
    }

    fun canWrite(secretKey: String, ticket: PermissionTicket): Boolean {
        return canWrite(ticket) && validateTicket(secretKey, ticket)
    }
}
