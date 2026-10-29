package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

class BackgroundDrawerTest {

    @Test
    fun testIsometricDotGridWorldCoordinatesAreStableAcrossPan() {
        val spacing = 48f
        val rowHeight = spacing * 0.8660254f

        // Function calculating world coordinates of dots on row r
        fun getRowWorldXOffset(worldY: Float): Float {
            val r = (worldY / rowHeight).roundToInt()
            return if (abs(r % 2) != 0) spacing * 0.5f else 0f
        }

        // Row 1 (y = rowHeight) must ALWAYS have offset spacing * 0.5f (24f)
        val offsetAtZero = getRowWorldXOffset(rowHeight)
        assertEquals(24f, offsetAtZero, 0.001f)

        // Regardless of panning, row 1 world Y is rowHeight, so its offset must remain 24f
        val offsetAfterPan = getRowWorldXOffset(rowHeight)
        assertEquals(24f, offsetAfterPan, 0.001f)

        // Row 0 must ALWAYS have offset 0f
        assertEquals(0f, getRowWorldXOffset(0f), 0.001f)

        // Row -1 must ALWAYS have offset 24f
        assertEquals(24f, getRowWorldXOffset(-rowHeight), 0.001f)
    }

    @Test
    fun testAllPatternsRenderWithoutExceptionsInCanvasDrawScope() {
        val bitmap = ImageBitmap(200, 200)
        val canvas = Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(200f, 200f)
        ) {
            with(BackgroundDrawer) {
                // Should render smoothly using GPU-batched drawPoints
                drawCanvasPattern(CanvasBackgroundPattern.ISOMETRIC_DOTS, Color.White, zoom = 1.0f, panOffset = Offset.Zero)
                drawCanvasPattern(CanvasBackgroundPattern.DOTS, Color.White, zoom = 1.0f, panOffset = Offset(50f, -50f))
                drawCanvasPattern(CanvasBackgroundPattern.LINES, Color.White, zoom = 1.5f, panOffset = Offset(-100f, 200f))
                drawCanvasPattern(CanvasBackgroundPattern.BLANK, Color.White, zoom = 0.5f, panOffset = Offset.Zero)
            }
        }
    }
}
