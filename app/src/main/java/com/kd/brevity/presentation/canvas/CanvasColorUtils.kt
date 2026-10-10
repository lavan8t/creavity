package com.kd.brevity.presentation.canvas

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object CanvasColorUtils {
    /**
     * Adapts stroke colors dynamically between Light and Dark canvas themes:
     * - In Light theme: White/near-white strokes invert to dark ink (#1B1B1F) so they are crisp and visible.
     * - In Dark theme: Black/near-black strokes invert to light ink (#FBF8F6) so they are crisp and visible.
     * - Chromatic colors (Red, Blue, Green, Orange, Purple) are preserved.
     * - High-luminance colors like bright yellow are adjusted slightly in Light theme for contrast.
     */
    fun getAdaptiveStrokeColor(originalColor: Color, isDarkCanvas: Boolean): Color {
        val r = originalColor.red
        val g = originalColor.green
        val b = originalColor.blue
        val a = originalColor.alpha
        val maxC = maxOf(r, g, b)
        val minC = minOf(r, g, b)
        val delta = maxC - minC
        val isGrayscale = delta < 0.15f
        val lum = originalColor.luminance()

        return if (!isDarkCanvas) {
            when {
                isGrayscale && lum > 0.70f -> Color(0xFF1B1B1F).copy(alpha = a)
                !isGrayscale && lum > 0.72f -> Color((r * 0.70f).coerceIn(0f, 1f), (g * 0.70f).coerceIn(0f, 1f), (b * 0.70f).coerceIn(0f, 1f), a)
                else -> originalColor
            }
        } else {
            when {
                isGrayscale && lum < 0.25f && a > 0.4f -> Color(0xFFFBF8F6).copy(alpha = a)
                else -> originalColor
            }
        }
    }
}
