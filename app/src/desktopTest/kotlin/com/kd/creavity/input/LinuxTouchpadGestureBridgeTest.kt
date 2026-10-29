package com.kd.creavity.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LinuxTouchpadGestureBridgeTest {

    @Test
    fun testParsePinchScaleValidLine() {
        val line = "event8    GESTURE_PINCH_UPDATE  +3.80s    2        0.00/-0.58  (    0.00/-1.85 unaccelerated)     0.63     @-0.19"
        val scale = LinuxTouchpadGestureBridge.parsePinchScale(line)
        assertNotNull("Scale must be parsed from valid libinput pinch update line", scale)
        assertEquals(0.63f, scale!!, 0.001f)
    }

    @Test
    fun testParsePinchScaleZoomIn() {
        val line = "event7 GESTURE_PINCH_UPDATE +1.20s 2 0.00/0.00 (0.00/0.00 unaccel) 1.45 @0.00"
        val scale = LinuxTouchpadGestureBridge.parsePinchScale(line)
        assertNotNull(scale)
        assertEquals(1.45f, scale!!, 0.001f)
    }

    @Test
    fun testParsePinchScaleModernFedoraFormat() {
        val line = " event7   GESTURE_PINCH_UPDATE  +1.234s\t2\t 0.00/ 0.00 ( 0.00/ 0.00mm)  1.05 @  0.00"
        val scale = LinuxTouchpadGestureBridge.parsePinchScale(line)
        assertNotNull("Scale must be parsed from modern libinput format with spaces around @", scale)
        assertEquals(1.05f, scale!!, 0.001f)
    }

    @Test
    fun testParsePinchScaleZoomOutModern() {
        val line = " event7   GESTURE_PINCH_UPDATE  +1.250s\t2\t 0.00/ 0.00 ( 0.00/ 0.00mm)  0.78 @  0.00"
        val scale = LinuxTouchpadGestureBridge.parsePinchScale(line)
        assertNotNull(scale)
        assertEquals(0.78f, scale!!, 0.001f)
    }

    @Test
    fun testParsePinchScaleNonPinchLineReturnsNull() {
        val line = "event7 POINTER_SCROLL_FINGER +2.10s vert 0.00/0.0 horiz -5.20/0.0 (finger)"
        val scale = LinuxTouchpadGestureBridge.parsePinchScale(line)
        assertNull("Non-pinch line must return null", scale)
    }
}
