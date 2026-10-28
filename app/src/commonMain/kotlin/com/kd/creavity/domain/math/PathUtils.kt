package com.kd.creavity.domain.math

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object PathUtils {
    /**
     * Filters out sub-pixel jitter and applies a Gaussian smoothing kernel
     * to eliminate hand tremble and touch sensor noise.
     */
    fun smoothAndSimplifyPoints(points: List<Point>, minDistance: Float = 2.0f): List<Point> {
        if (points.size <= 2) return points

        // 1. Distance filter: discard micro-jitter points that are too close
        val filtered = ArrayList<Point>(points.size)
        filtered.add(points.first())
        var lastPt = points.first()
        val minDistSq = minDistance * minDistance

        for (i in 1 until points.size - 1) {
            val pt = points[i]
            val dx = pt.x - lastPt.x
            val dy = pt.y - lastPt.y
            if (dx * dx + dy * dy >= minDistSq) {
                filtered.add(pt)
                lastPt = pt
            }
        }
        filtered.add(points.last())
        if (filtered.size <= 2) return filtered

        // 2. Laplacian smoothing pass for organic, fluid ink curves
        val smoothed = ArrayList<Point>(filtered.size)
        smoothed.add(filtered.first())
        for (i in 1 until filtered.size - 1) {
            val pPrev = filtered[i - 1]
            val pCurr = filtered[i]
            val pNext = filtered[i + 1]
            val sx = pPrev.x * 0.2f + pCurr.x * 0.6f + pNext.x * 0.2f
            val sy = pPrev.y * 0.2f + pCurr.y * 0.6f + pNext.y * 0.2f
            val sp = pPrev.pressure * 0.2f + pCurr.pressure * 0.6f + pNext.pressure * 0.2f
            smoothed.add(pCurr.copy(x = sx, y = sy, pressure = sp))
        }
        smoothed.add(filtered.last())
        return smoothed
    }

    /**
     * Builds a smooth Compose Path from a list of continuous touch points
     * using quadratic Bezier curves for low-latency, smooth drawing.
     */
    fun createSmoothPath(points: List<Point>): Path {
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
     * Generates vector path based on the selected drawing tool (Pen, Calligraphy, Highlighter, Line, Arrow, Rectangle, Circle).
     */
    fun createToolPath(tool: DrawingTool, points: List<Point>, strokeWidth: Float = 6f): Path {
        if (points.isEmpty()) return Path()
        if (tool == DrawingTool.CALLIGRAPHY) {
            return com.kd.creavity.presentation.canvas.renderers.CalligraphyStrokeRenderer.createCalligraphyPath(points, strokeWidth)
        }
        if (points.size <= 1 || tool == DrawingTool.PEN || tool == DrawingTool.MARKER || tool == DrawingTool.BRUSH || tool == DrawingTool.PENCIL || tool == DrawingTool.HIGHLIGHTER || tool == DrawingTool.ERASER || tool == DrawingTool.LASER) {
            return createSmoothPath(points)
        }

        val start = points.first()
        val end = points.last()
        val path = Path()

        when (tool) {
            DrawingTool.LINE -> {
                path.moveTo(start.x, start.y)
                path.lineTo(end.x, end.y)
            }
            DrawingTool.ARROW -> {
                path.moveTo(start.x, start.y)
                path.lineTo(end.x, end.y)
                val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
                val arrowLength = 28.0
                val arrowAngle = Math.PI / 6.0
                val x1 = (end.x - arrowLength * cos(angle - arrowAngle)).toFloat()
                val y1 = (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
                val x2 = (end.x - arrowLength * cos(angle + arrowAngle)).toFloat()
                val y2 = (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
                path.moveTo(end.x, end.y)
                path.lineTo(x1, y1)
                path.moveTo(end.x, end.y)
                path.lineTo(x2, y2)
            }
            DrawingTool.RECTANGLE -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                path.addRect(Rect(left, top, right, bottom))
            }
            DrawingTool.CIRCLE -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                path.addOval(Rect(left, top, right, bottom))
            }
            DrawingTool.TRIANGLE -> {
                path.moveTo(start.x, end.y)
                path.lineTo((start.x + end.x) / 2f, start.y)
                path.lineTo(end.x, end.y)
                path.close()
            }
            DrawingTool.DIAMOND -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                val midX = (left + right) / 2f
                val midY = (top + bottom) / 2f
                path.moveTo(midX, top)
                path.lineTo(right, midY)
                path.lineTo(midX, bottom)
                path.lineTo(left, midY)
                path.close()
            }
            DrawingTool.CYLINDER -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                val h = bottom - top
                val capH = kotlin.math.min(h * 0.25f, 40f).coerceAtLeast(6f)
                path.addOval(Rect(left, top, right, top + capH))
                path.moveTo(left, top + capH / 2f)
                path.lineTo(left, bottom - capH / 2f)
                path.quadraticTo((left + right) / 2f, bottom, right, bottom - capH / 2f)
                path.lineTo(right, top + capH / 2f)
            }
            DrawingTool.STAR -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val rOuter = kotlin.math.min(right - left, bottom - top) / 2f
                val rInner = rOuter * 0.40f
                val step = Math.PI / 5.0
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) rOuter else rInner
                    val angle = -Math.PI / 2.0 + i * step
                    val x = (cx + r * kotlin.math.cos(angle)).toFloat()
                    val y = (cy + r * kotlin.math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
            else -> return createSmoothPath(points)
        }
        return path
    }

    /**
     * Ray-casting algorithm to test if a point (px, py) is inside a polygon defined by a list of points.
     */
    fun isPointInPolygon(point: Point, polygon: List<Point>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]
            if ((pi.y > point.y) != (pj.y > point.y) &&
                (point.x < (pj.x - pi.x) * (point.y - pi.y) / (pj.y - pi.y) + pi.x)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /**
     * Checks if any points in a stroke are inside a polygon.
     */
    fun isStrokeInPolygon(strokePoints: List<Point>, polygon: List<Point>): Boolean {
        if (strokePoints.isEmpty() || polygon.size < 3) return false
        return strokePoints.any { isPointInPolygon(it, polygon) }
    }
}
