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
import kotlin.math.sqrt

/**
 * Realistic ballpoint pen renderer.
 *
 * Characteristics:
 * - Relatively uniform, narrow, highly opaque ink strokes.
 * - Smooth Catmull-Rom / Bezier curves and antialiased edges.
 * - Rounded, natural beginnings, joins, and endings.
 * - Subtle, controlled changes in width with pressure (0.88x to 1.12x of base width).
 * - Speed dampening: fast cursive strokes thin out subtly while maintaining crisp body.
 * - Zero gaps between samples when drawing slowly or quickly.
 * - Clean integration with Compose DrawScope and hardware path caching.
 */
object PenStrokeRenderer {

    /**
     * Builds a smooth vector Path for the ballpoint pen stroke with quadratic Bezier smoothing.
     */
    fun createPenPath(points: List<Point>): Path {
        val path = Path()
        if (points.isEmpty()) return path

        path.moveTo(points[0].x, points[0].y)
        if (points.size == 1) {
            path.lineTo(points[0].x + 0.1f, points[0].y + 0.1f)
            return path
        }

        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }

        for (i in 1 until points.size - 1) {
            val p0 = points[i - 1]
            val p1 = points[i]
            val p2 = points[i + 1]

            val midX1 = (p0.x + p1.x) / 2f
            val midY1 = (p0.y + p1.y) / 2f
            val midX2 = (p1.x + p2.x) / 2f
            val midY2 = (p1.y + p2.y) / 2f

            if (i == 1) {
                path.lineTo(midX1, midY1)
            }
            path.quadraticTo(p1.x, p1.y, midX2, midY2)
        }

        val last = points.last()
        path.lineTo(last.x, last.y)
        return path
    }

    /**
     * Draws the ballpoint pen stroke on the DrawScope.
     */
    fun DrawScope.drawPen(
        points: List<Point>,
        color: Color,
        baseWidth: Float,
        cachedPath: Path? = null
    ) {
        if (points.isEmpty()) return

        // Single tap dot: crisp ballpoint spherical imprint
        if (points.size == 1) {
            val p = points.first()
            val press = p.pressure.coerceIn(0.4f, 1.3f)
            val r = (baseWidth * 0.5f * (0.90f + 0.15f * press)).coerceAtLeast(1.2f)
            drawCircle(
                color = color,
                radius = r,
                center = Offset(p.x, p.y)
            )
            return
        }

        val pointCount = points.size

        // If a cached vector path is provided and points are uniform, draw GPU-accelerated smooth path
        // Check if dynamic pressure variance is significant
        var minPress = points[0].pressure
        var maxPress = points[0].pressure
        for (p in points) {
            if (p.pressure < minPress) minPress = p.pressure
            if (p.pressure > maxPress) maxPress = p.pressure
        }
        val hasVariablePressure = (maxPress - minPress) > 0.12f

        if (cachedPath != null && !hasVariablePressure) {
            drawPath(
                path = cachedPath,
                color = color,
                style = CanvasStrokeStyle(
                    width = baseWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            return
        }

        // Calculate smooth velocity and pressure response per point
        val widths = FloatArray(pointCount)
        var smoothedWidth = baseWidth

        for (i in 0 until pointCount) {
            val curr = points[i]
            val prev = if (i > 0) points[i - 1] else curr
            val dt = (curr.timestampMs - prev.timestampMs).coerceIn(1L, 120L).toFloat()
            val dx = curr.x - prev.x
            val dy = curr.y - prev.y
            val dist = hypot(dx, dy)
            val speed = dist / dt

            // Subtle speed dampening (-8% to +5%)
            val speedFactor = 1.0f - (speed * 0.05f).coerceIn(-0.05f, 0.08f)
            // Subtle pressure response (0.88x to 1.12x)
            val pressFactor = 0.88f + 0.24f * curr.pressure.coerceIn(0.2f, 1.2f)

            val targetWidth = (baseWidth * speedFactor * pressFactor).coerceIn(baseWidth * 0.82f, baseWidth * 1.18f)
            smoothedWidth = smoothedWidth * 0.60f + targetWidth * 0.40f
            widths[i] = smoothedWidth
        }

        // Draw overlapping smooth segments with round caps to guarantee zero gaps
        for (i in 0 until pointCount - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midWidth = (widths[i] + widths[i + 1]) / 2f

            drawLine(
                color = color,
                start = Offset(p0.x, p0.y),
                end = Offset(p1.x, p1.y),
                strokeWidth = midWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
