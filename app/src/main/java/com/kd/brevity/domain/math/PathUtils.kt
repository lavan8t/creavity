package com.kd.brevity.domain.math

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.domain.model.Point
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object PathUtils {
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
     * Generates vector path based on the selected drawing tool (Pen, Highlighter, Line, Arrow, Rectangle, Circle).
     */
    fun createToolPath(tool: DrawingTool, points: List<Point>): Path {
        if (points.isEmpty()) return Path()
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
