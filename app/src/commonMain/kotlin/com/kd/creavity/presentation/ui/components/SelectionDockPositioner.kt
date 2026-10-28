package com.kd.creavity.presentation.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Utility to calculate contextual placement for the selection properties dock
 * relative to the selected items' bounding box.
 */
object SelectionDockPositioner {

    fun calculateDockPosition(
        selectionBounds: Rect,
        zoom: Float,
        panOffset: Offset,
        canvasSize: IntSize,
        dockWidth: Int = 420,
        dockHeight: Int = 56,
        spacing: Float = 16f
    ): IntOffset {
        val screenLeft = selectionBounds.left * zoom + panOffset.x
        val screenRight = selectionBounds.right * zoom + panOffset.x
        val screenTop = selectionBounds.top * zoom + panOffset.y
        val screenBottom = selectionBounds.bottom * zoom + panOffset.y

        val centerX = (screenLeft + screenRight) / 2f
        val rawX = centerX - (dockWidth / 2f)

        // Try placing directly above the selected curve or element
        val rawYAbove = screenTop - dockHeight - spacing

        // If placing above would collide with top header (< 72px), flip directly below the selection
        val chosenY = if (rawYAbove < 72f) {
            screenBottom + spacing
        } else {
            rawYAbove
        }

        // Clamp inside canvas viewport boundaries
        val minX = 16f
        val maxX = (canvasSize.width - dockWidth - 16f).coerceAtLeast(minX)
        val clampedX = rawX.coerceIn(minX, maxX)

        val minY = 64f
        val maxY = (canvasSize.height - dockHeight - 16f).coerceAtLeast(minY)
        val clampedY = chosenY.coerceIn(minY, maxY)

        return IntOffset(clampedX.roundToInt(), clampedY.roundToInt())
    }
}
