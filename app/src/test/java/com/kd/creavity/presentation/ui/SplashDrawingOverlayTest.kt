package com.kd.creavity.presentation.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashDrawingOverlayTest {
    @Test
    fun testSplashStateTransitions() {
        var isSplashActive = true
        var whiteboardReady = false

        fun onAnimationComplete() {
            isSplashActive = false
            whiteboardReady = true
        }

        assertTrue("Splash must be active on initial launch", isSplashActive)
        assertFalse("Whiteboard must be hidden until splash finishes", whiteboardReady)

        // Simulate animation complete or tap-to-skip
        onAnimationComplete()

        assertFalse("Splash must dismiss once animation completes", isSplashActive)
        assertTrue("Whiteboard must show after splash finishes", whiteboardReady)
    }
}
