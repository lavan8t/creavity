package com.kd.creavity.data.nearby

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class LanDiscoveryTransportTest {

    private class TestLockHelper : MulticastLockHelper {
        val acquireCount = AtomicInteger(0)
        val releaseCount = AtomicInteger(0)

        override fun acquire() {
            acquireCount.incrementAndGet()
        }

        override fun release() {
            releaseCount.incrementAndGet()
        }
    }

    @Test
    fun testAdvertisingLifecycleAndLock() {
        val lock = TestLockHelper()
        val transport = LanDiscoveryTransport(
            localNodeId = "testnode1",
            multicastLock = lock
        )

        assertFalse(transport.isAdvertising.value)
        transport.startAdvertising(deviceName = "Test Host", roomCode = "ROOM123")
        assertTrue(transport.isAdvertising.value)
        assertTrue(lock.acquireCount.get() >= 1)

        transport.stopAdvertising()
        assertFalse(transport.isAdvertising.value)
        assertTrue(lock.releaseCount.get() >= 1)
        transport.disconnectAll()
    }

    @Test
    fun testDiscoveryLifecycleAndLock() {
        val lock = TestLockHelper()
        val transport = LanDiscoveryTransport(
            localNodeId = "testnode2",
            multicastLock = lock
        )

        assertFalse(transport.isDiscovering.value)
        transport.startDiscovery()
        Thread.sleep(500)
        assertTrue(transport.isDiscovering.value)
        assertTrue(lock.acquireCount.get() >= 1)

        transport.stopDiscovery()
        assertFalse(transport.isDiscovering.value)
        assertTrue(lock.releaseCount.get() >= 1)
        transport.disconnectAll()
    }

    @Test
    fun testConstants() {
        assertEquals(53535, LanDiscoveryTransport.DISCOVERY_PORT)
        assertEquals(53536, LanDiscoveryTransport.DEFAULT_TCP_PORT)
        assertEquals("224.0.0.167", LanDiscoveryTransport.MULTICAST_GROUP)
    }
}
