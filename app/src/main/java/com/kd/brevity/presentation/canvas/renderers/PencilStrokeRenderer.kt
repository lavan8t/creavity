package com.kd.brevity.presentation.canvas.renderers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStrokeStyle
import com.kd.brevity.domain.model.Point
import kotlin.math.hypot

/**
 * Genuine graphite pencil engine with authentic graphite grain, paper tooth,
 * pressure-dependent tonal darkness, tilt-based shading, and soft tapers.
 *
 * Deterministic texture placement:
 * Seeded by stroke timestamp and coordinate geometry so recomposition, redrawing,
 * zooming, and exporting never flicker or change randomly.
 */
object PencilStrokeRenderer {

    /**
     * Fast deterministic hash for stable, jitter-free pseudo-random values.
     * Returns a float in [-0.5f, 0.5f].
     */
    private fun deterministicNoise(seed: Long, index: Int, pass: Int): Float {
        var h = seed xor (index.toLong() * 374761393L) xor (pass.toLong() * 668265263L)
        h = (h xor (h ushr 16)) * 0x85ebca6bL
        h = (h xor (h ushr 13)) * 0xc2b2ae35L
        h = h xor (h ushr 16)
        val intVal = (h and 0xFFFFL).toInt()
        return (intVal / 65535f) - 0.5f
    }

    fun DrawScope.drawPencil(
        points: List<Point>,
        color: Color,
        baseWidth: Float,
        cachedPath: Path? = null
    ) {
        if (points.isEmpty()) return

        // Single tap dot with soft graphite tooth
        if (points.size == 1) {
            val p = points.first()
            val press = p.pressure.coerceIn(0.2f, 1.3f)
            val dotRadius = (baseWidth * 0.45f * press).coerceAtLeast(1.2f)
            val dotAlpha = (color.alpha * (0.50f + 0.45f * press)).coerceIn(0.1f, 1f)
            drawCircle(
                color = color.copy(alpha = dotAlpha),
                radius = dotRadius,
                center = Offset(p.x, p.y)
            )
            return
        }

        // Tilt detection for soft graphite shading
        var sumTilt = 0f
        var tiltCount = 0
        for (p in points) {
            if (p.tilt > 0f) {
                sumTilt += p.tilt
                tiltCount++
            }
        }
        val avgTilt = if (tiltCount > 0) sumTilt / tiltCount else 0f
        val tiltRatio = (avgTilt / 1.57f).coerceIn(0f, 1f)
        val tiltSpread = 1.0f + 2.2f * tiltRatio
        val effectiveWidth = (baseWidth * tiltSpread).coerceAtLeast(1.5f)

        // Seed derived from first and last points and timestamps for 100% stable determinism
        val pFirst = points.first()
        val pLast = points.last()
        val seed = (pFirst.timestampMs and 0xFFFFFFFFL) xor
                (pFirst.x.toBits().toLong() shl 16) xor
                (pLast.y.toBits().toLong())

        val pointCount = points.size

        // Precompute per-point width, pressure, and taper factors
        val widths = FloatArray(pointCount)
        val alphas = FloatArray(pointCount)

        for (i in 0 until pointCount) {
            val pt = points[i]
            val press = pt.pressure.coerceIn(0.15f, 1.4f)

            // Natural taper at stroke head and tail (first/last 2 points)
            val headTaper = if (i < 3) (i + 1) / 3.5f else 1.0f
            val tailTaper = if (i >= pointCount - 3) (pointCount - i) / 3.5f else 1.0f
            val taper = minOf(headTaper, tailTaper)

            // Pressure modulation: width and darkness
            val widthFactor = (0.75f + 0.35f * press) * taper
            val alphaFactor = (0.40f + 0.55f * press) * (1.0f - 0.25f * tiltRatio) * taper

            widths[i] = (effectiveWidth * widthFactor).coerceAtLeast(1f)
            alphas[i] = (color.alpha * alphaFactor).coerceIn(0.15f, 0.95f)
        }

        // Pass 1: Graphite tooth & edge irregularity (soft micro-tooth caught on paper)
        val toothJitter = (effectiveWidth * 0.22f).coerceIn(0.4f, 2.5f)
        val toothAlpha = (color.alpha * 0.25f * (1.0f + 0.3f * tiltRatio)).coerceIn(0.08f, 0.45f)
        val toothWidth = effectiveWidth * (1.1f + 0.4f * tiltRatio)

        for (i in 0 until pointCount - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val dx = p1.x - p0.x
            val dy = p1.y - p0.y
            val dist = hypot(dx, dy)
            if (dist < 0.1f) continue

            // Normal vector for transverse tooth displacement
            val nx = -dy / dist
            val ny = dx / dist

            val j0 = deterministicNoise(seed, i, 1) * toothJitter
            val j1 = deterministicNoise(seed, i + 1, 1) * toothJitter

            drawLine(
                color = color.copy(alpha = toothAlpha),
                start = Offset(p0.x + nx * j0, p0.y + ny * j0),
                end = Offset(p1.x + nx * j1, p1.y + ny * j1),
                strokeWidth = toothWidth,
                cap = StrokeCap.Round
            )
        }

        // Pass 2: Main graphite core with pressure-modulated width and opacity
        for (i in 0 until pointCount - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val segWidth = (widths[i] + widths[i + 1]) / 2f
            val segAlpha = (alphas[i] + alphas[i + 1]) / 2f

            drawLine(
                color = color.copy(alpha = segAlpha),
                start = Offset(p0.x, p0.y),
                end = Offset(p1.x, p1.y),
                strokeWidth = segWidth,
                cap = StrokeCap.Round
            )
        }

        // Pass 3: Fine graphite grain striae (internal crystal grain texture)
        // High pressure fills grain densely; light pressure exposes paper grain valleys
        val grainPasses = if (tiltRatio > 0.3f) 2 else 1
        for (pass in 0 until grainPasses) {
            for (i in 0 until pointCount - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val dx = p1.x - p0.x
                val dy = p1.y - p0.y
                val dist = hypot(dx, dy)
                if (dist < 0.2f) continue

                val nx = -dy / dist
                val ny = dx / dist

                val grainShift = deterministicNoise(seed, i, 10 + pass) * (widths[i] * 0.35f)
                val grainAlpha = (alphas[i] * 0.55f).coerceIn(0.1f, 0.75f)
                val grainWidth = (widths[i] * 0.45f).coerceAtLeast(0.8f)

                drawLine(
                    color = color.copy(alpha = grainAlpha),
                    start = Offset(p0.x + nx * grainShift, p0.y + ny * grainShift),
                    end = Offset(p1.x + nx * grainShift, p1.y + ny * grainShift),
                    strokeWidth = grainWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
