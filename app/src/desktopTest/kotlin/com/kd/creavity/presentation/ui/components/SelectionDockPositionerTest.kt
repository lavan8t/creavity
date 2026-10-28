package com.kd.creavity.presentation.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionDockPositionerTest {

    @Test
    fun testDockPositionsDirectlyAboveSelectionBounds() {
        val bounds = Rect(200f, 300f, 400f, 500f)
        val pos = SelectionDockPositioner.calculateDockPosition(
            selectionBounds = bounds,
            zoom = 1.0f,
            panOffset = Offset.Zero,
            canvasSize = IntSize(1920, 1080),
            dockWidth = 400,
            dockHeight = 60,
            spacing = 16f
        )
        // Center X = 300, dock starts at 300 - 200 = 100
        assertEquals(100, pos.x)
        // Top = 300, dock placed above: 300 - 60 - 16 = 224
        assertEquals(224, pos.y)
    }

    @Test
    fun testDockFlipsBelowWhenNearTopEdge() {
        val bounds = Rect(200f, 20f, 400f, 100f)
        val pos = SelectionDockPositioner.calculateDockPosition(
            selectionBounds = bounds,
            zoom = 1.0f,
            panOffset = Offset.Zero,
            canvasSize = IntSize(1920, 1080),
            dockWidth = 400,
            dockHeight = 60,
            spacing = 16f
        )
        // Top edge is 20, placing above would be 20 - 60 - 16 = -56 (< 72), so flips below: bottom (100) + 16 = 116
        assertEquals(116, pos.y)
    }

    @Test
    fun testDockClampedInsideLeftAndRightViewport() {
        // Selection is way off the left edge (-200, 300, -50, 400)
        val boundsLeft = Rect(-200f, 300f, -50f, 400f)
        val posLeft = SelectionDockPositioner.calculateDockPosition(
            selectionBounds = boundsLeft,
            zoom = 1.0f,
            panOffset = Offset.Zero,
            canvasSize = IntSize(1920, 1080),
            dockWidth = 400,
            dockHeight = 60
        )
        assertTrue("X should be clamped to at least 16", posLeft.x >= 16)

        // Selection is way off the right edge (1900, 300, 2100, 400)
        val boundsRight = Rect(1900f, 300f, 2100f, 400f)
        val posRight = SelectionDockPositioner.calculateDockPosition(
            selectionBounds = boundsRight,
            zoom = 1.0f,
            panOffset = Offset.Zero,
            canvasSize = IntSize(1920, 1080),
            dockWidth = 400,
            dockHeight = 60
        )
        assertTrue("X should not exceed canvas width minus dock width minus 16", posRight.x <= 1920 - 400 - 16)
    }
}
