package com.kd.creavity.presentation.canvas.renderers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.kd.creavity.domain.model.Point
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, mathematically rigorous broad-edge chisel nib calligraphy renderer.
 *
 * Produces authentic thick-and-thin lettering:
 * - Broad-edge nib model oriented at 45° angle.
 * - Direction-dependent width: strokes perpendicular to nib are broad, strokes parallel to nib are hairlines.
 * - Pressure sensitivity gently modulates nib width.
 * - Constructs a single continuous closed outline Path with smooth joins and clean chisel caps.
 * - Zero gaps, zero overlapping transparency seams, zero disconnected polygon segments.
 */
object CalligraphyStrokeRenderer {

    // Standard Western / italic chisel nib angle: 45 degrees
    private const val DEFAULT_NIB_ANGLE_RAD = (Math.PI / 4.0).toFloat()
    private val COS_ANGLE = cos(DEFAULT_NIB_ANGLE_RAD)
    private val SIN_ANGLE = sin(DEFAULT_NIB_ANGLE_RAD)

    /**
     * Builds a closed vector outline Path representing the broad-edge calligraphy stroke.
     */
    fun createCalligraphyPath(
        points: List<Point>,
        baseWidth: Float,
        nibAngleRad: Float = DEFAULT_NIB_ANGLE_RAD
    ): Path {
        val path = Path()
        if (points.isEmpty()) return path

        val cosA = if (nibAngleRad == DEFAULT_NIB_ANGLE_RAD) COS_ANGLE else cos(nibAngleRad)
        val sinA = if (nibAngleRad == DEFAULT_NIB_ANGLE_RAD) SIN_ANGLE else sin(nibAngleRad)

        // Single tap chisel imprint
        if (points.size == 1) {
            val p = points.first()
            val w = (baseWidth * (0.80f + 0.35f * p.pressure.coerceIn(0.2f, 1.4f))).coerceAtLeast(2f)
            val halfW = w / 2f
            val nx = cosA * halfW
            val ny = -sinA * halfW
            val perpX = -ny * 0.25f
            val perpY = nx * 0.25f

            path.moveTo(p.x - nx, p.y - ny)
            path.lineTo(p.x + perpX, p.y + perpY)
            path.lineTo(p.x + nx, p.y + ny)
            path.lineTo(p.x - perpX, p.y - perpY)
            path.close()
            return path
        }

        val count = points.size
        val leftPoints = ArrayList<Offset>(count)
        val rightPoints = ArrayList<Offset>(count)

        // 1. Calculate left and right nib edges for each sampled point
        for (i in 0 until count) {
            val pt = points[i]
            val w = (baseWidth * (0.80f + 0.35f * pt.pressure.coerceIn(0.2f, 1.4f))).coerceAtLeast(2f)
            val halfW = w / 2f
            val nx = cosA * halfW
            val ny = -sinA * halfW

            leftPoints.add(Offset(pt.x - nx, pt.y - ny))
            rightPoints.add(Offset(pt.x + nx, pt.y + ny))
        }

        // 2. Start at first left edge point
        path.moveTo(leftPoints[0].x, leftPoints[0].y)

        // 3. Trace left contour with smooth quadratic Bezier midpoints
        if (count == 2) {
            path.lineTo(leftPoints[1].x, leftPoints[1].y)
        } else {
            for (i in 0 until count - 1) {
                val pCurr = leftPoints[i]
                val pNext = leftPoints[i + 1]
                val midX = (pCurr.x + pNext.x) / 2f
                val midY = (pCurr.y + pNext.y) / 2f
                if (i == 0) {
                    path.lineTo(midX, midY)
                } else {
                    path.quadraticTo(pCurr.x, pCurr.y, midX, midY)
                }
            }
            path.lineTo(leftPoints.last().x, leftPoints.last().y)
        }

        // 4. Chisel end cap: connect to right contour
        path.lineTo(rightPoints.last().x, rightPoints.last().y)

        // 5. Trace right contour backwards with smooth quadratic Bezier midpoints
        if (count == 2) {
            path.lineTo(rightPoints[0].x, rightPoints[0].y)
        } else {
            for (i in count - 1 downTo 1) {
                val pCurr = rightPoints[i]
                val pPrev = rightPoints[i - 1]
                val midX = (pCurr.x + pPrev.x) / 2f
                val midY = (pCurr.y + pPrev.y) / 2f
                path.quadraticTo(pCurr.x, pCurr.y, midX, midY)
            }
            path.lineTo(rightPoints[0].x, rightPoints[0].y)
        }

        // 6. Close back to start of left contour
        path.close()
        return path
    }

    /**
     * Renders a calligraphy stroke on the given DrawScope.
     */
    fun DrawScope.drawCalligraphy(
        points: List<Point>,
        color: Color,
        baseWidth: Float,
        cachedPath: Path? = null
    ) {
        if (points.isEmpty()) return

        val path = cachedPath ?: createCalligraphyPath(points, baseWidth)
        drawPath(
            path = path,
            color = color
        )
    }
}
