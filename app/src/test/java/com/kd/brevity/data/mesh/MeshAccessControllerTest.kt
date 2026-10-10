package com.kd.brevity.data.mesh

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshAccessControllerTest {
    private val testSecret = "breVITy-super-secret-key-12345"
    private val boardId = "board-alpha"

    @Test
    fun hostAndEditor_canWrite() {
        val hostTicket = MeshAccessController.createTicket(testSecret, boardId, "dev-1", MeshRole.HOST)
        val editorTicket = MeshAccessController.createTicket(testSecret, boardId, "dev-2", MeshRole.EDITOR)
        val viewerTicket = MeshAccessController.createTicket(testSecret, boardId, "dev-3", MeshRole.VIEWER)

        assertTrue(MeshAccessController.canWrite(testSecret, hostTicket))
        assertTrue(MeshAccessController.canWrite(testSecret, editorTicket))
        assertFalse(MeshAccessController.canWrite(testSecret, viewerTicket))
    }

    @Test
    fun invalidSecret_rejectsTicket() {
        val ticket = MeshAccessController.createTicket(testSecret, boardId, "dev-1", MeshRole.EDITOR)
        assertFalse(MeshAccessController.validateTicket("wrong-secret", ticket))
    }

    @Test
    fun tamperedRole_failsValidation() {
        val viewerTicket = MeshAccessController.createTicket(testSecret, boardId, "dev-1", MeshRole.VIEWER)
        // Adversary alters role to EDITOR without host secret
        val tamperedTicket = viewerTicket.copy(role = MeshRole.EDITOR)
        assertFalse(MeshAccessController.validateTicket(testSecret, tamperedTicket))
        assertFalse(MeshAccessController.canWrite(testSecret, tamperedTicket))
    }

    @Test
    fun expiredTicket_failsValidation() {
        // Ticket expired 10 seconds ago
        val expiredTicket = MeshAccessController.createTicket(testSecret, boardId, "dev-1", MeshRole.EDITOR, durationMs = -10_000L)
        assertFalse(MeshAccessController.validateTicket(testSecret, expiredTicket))
    }
}
