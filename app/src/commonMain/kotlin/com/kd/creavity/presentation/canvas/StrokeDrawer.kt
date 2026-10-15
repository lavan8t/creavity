package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStrokeStyle
import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.presentation.canvas.renderers.CalligraphyStrokeRenderer
import com.kd.creavity.presentation.canvas.renderers.PenStrokeRenderer
import com.kd.creavity.presentation.canvas.renderers.PencilStrokeRenderer
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object StrokeDrawer {

    fun DrawScope.drawStyledStroke(
        points: List<Point>,
        color: Color,
        width: Float,
        tool: DrawingTool,
        cachedPath: Path? = null
    ) {
        if (points.isEmpty()) return
        if (points.size == 1 && tool != DrawingTool.LASER && tool != DrawingTool.CALLIGRAPHY && tool != DrawingTool.PENCIL && tool != DrawingTool.PEN) {
            val p = points.first()
            drawCircle(color, radius = (width * 0.5f).coerceAtLeast(1.5f), center = Offset(p.x, p.y))
            return
        }

        when (tool) {
            DrawingTool.PEN -> {
                PenStrokeRenderer.run {
                    drawPen(points, color, width, cachedPath)
                }
            }
            DrawingTool.PENCIL -> {
                PencilStrokeRenderer.run {
                    drawPencil(points, color, width, cachedPath)
                }
            }
            DrawingTool.CALLIGRAPHY -> {
                CalligraphyStrokeRenderer.run {
                    drawCalligraphy(points, color, width, cachedPath)
                }
            }
            DrawingTool.MARKER -> {
                val path = cachedPath ?: PathUtils.createSmoothPath(points)
                drawPath(
                    path = path,
                    color = color,
                    style = CanvasStrokeStyle(
                        width = width * 2.2f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            DrawingTool.BRUSH -> {
                if (cachedPath != null && points.size > 1) {
                    drawPath(
                        path = cachedPath,
                        color = color.copy(alpha = 0.85f),
                        style = CanvasStrokeStyle(
                            width = width * 2.0f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else {
                    drawPressureSensitiveStroke(points, color.copy(alpha = 0.85f), width * 2.5f)
                }
            }
            DrawingTool.HIGHLIGHTER -> {
                val path = cachedPath ?: PathUtils.createSmoothPath(points)
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.38f),
                    style = CanvasStrokeStyle(
                        width = width * 3.2f,
                        cap = StrokeCap.Square,
                        join = StrokeJoin.Bevel
                    )
                )
            }
            DrawingTool.ARROW -> {
                if (points.size >= 2) {
                    drawArrow(points.first(), points.last(), color, width)
                }
            }
            DrawingTool.TRIANGLE -> {
                if (points.size >= 2) {
                    drawTriangle(points.first(), points.last(), color, width)
                }
            }
            DrawingTool.RECTANGLE -> {
                if (points.size >= 2) {
                    drawRectangle(points.first(), points.last(), color, width)
                }
            }
            DrawingTool.CIRCLE -> {
                if (points.size >= 2) {
                    drawCircleShape(points.first(), points.last(), color, width)
                }
            }
            DrawingTool.LINE -> {
                if (points.size >= 2) {
                    val start = points.first()
                    val end = points.last()
                    drawLine(
                        color = color,
                        start = Offset(start.x, start.y),
                        end = Offset(end.x, end.y),
                        strokeWidth = width,
                        cap = StrokeCap.Round
                    )
                }
            }
            DrawingTool.LASER -> {
                drawGlowingLaserStroke(points, width)
            }
            else -> {
                val path = cachedPath ?: PathUtils.createToolPath(tool, points)
                drawPath(
                    path = path,
                    color = color,
                    style = CanvasStrokeStyle(
                        width = width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }

    private fun hasDynamicPressure(points: List<Point>): Boolean {
        if (points.isEmpty()) return false
        var minP = points[0].pressure
        var maxP = points[0].pressure
        for (p in points) {
            if (p.pressure < minP) minP = p.pressure
            if (p.pressure > maxP) maxP = p.pressure
            if (maxP - minP > 0.08f) return true
        }
        return false
    }

    private fun DrawScope.drawPressureSensitiveStroke(
        points: List<Point>,
        color: Color,
        baseWidth: Float
    ) {
        val minWidth = (baseWidth * 0.3f).coerceAtLeast(1f)
        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            val avgPressure = (start.pressure + end.pressure) / 2f
            val segmentWidth = (baseWidth * (0.35f + 0.65f * avgPressure)).coerceAtLeast(minWidth)
            val avgTilt = (start.tilt + end.tilt) / 2f
            val tiltOpacity = 0.85f + 0.15f * (avgTilt / 1.0f).coerceIn(0f, 1f)

            drawLine(
                color = color.copy(alpha = (color.alpha * tiltOpacity).coerceIn(0f, 1f)),
                start = Offset(start.x, start.y),
                end = Offset(end.x, end.y),
                strokeWidth = segmentWidth,
                cap = StrokeCap.Round
            )
        }
    }


    fun DrawScope.drawGlowingLaserStroke(
        points: List<Point>,
        baseWidth: Float
    ) {
        if (points.isEmpty()) return
        val now = System.currentTimeMillis()
        val durationMs = 1200f

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val age = (now - p1.timestampMs).toFloat() / durationMs
            if (age >= 1.0f) continue
            val life = (1.0f - age).coerceIn(0f, 1f)
            val alphaMod = life * life

            val start = Offset(p0.x, p0.y)
            val end = Offset(p1.x, p1.y)

            drawLine(
                color = Color(0xFFFF1744).copy(alpha = 0.22f * alphaMod),
                start = start,
                end = end,
                strokeWidth = (baseWidth * 4.2f).coerceAtLeast(10f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF5252).copy(alpha = 0.55f * alphaMod),
                start = start,
                end = end,
                strokeWidth = (baseWidth * 2.2f).coerceAtLeast(6f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF8A80).copy(alpha = 0.85f * alphaMod),
                start = start,
                end = end,
                strokeWidth = (baseWidth * 1.1f).coerceAtLeast(3f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.95f * alphaMod),
                start = start,
                end = end,
                strokeWidth = (baseWidth * 0.45f).coerceAtLeast(1.5f),
                cap = StrokeCap.Round
            )
        }

        val latest = points.last()
        val headAge = (now - latest.timestampMs).toFloat() / durationMs
        if (headAge < 1.0f) {
            val headLife = (1.0f - headAge).coerceIn(0f, 1f)
            val headAlpha = headLife * headLife
            val center = Offset(latest.x, latest.y)

            drawCircle(
                color = Color(0xFFFF1744).copy(alpha = 0.35f * headAlpha),
                radius = (baseWidth * 3.5f).coerceAtLeast(12f),
                center = center
            )
            drawCircle(
                color = Color(0xFFFF5252).copy(alpha = 0.75f * headAlpha),
                radius = (baseWidth * 2.0f).coerceAtLeast(7f),
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * headAlpha),
                radius = (baseWidth * 0.9f).coerceAtLeast(3.5f),
                center = center
            )
        }
    }

    fun DrawScope.drawArrow(start: Point, end: Point, color: Color, width: Float) {
        val dx = (end.x - start.x).toDouble()
        val dy = (end.y - start.y).toDouble()
        val angle = atan2(dy, dx)
        val arrowLength = (width * 3.5f).coerceAtLeast(14f)
        val arrowAngle = Math.PI / 7.0

        val arrow1 = Offset(
            (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
        )
        val arrow2 = Offset(
            (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
        )

        val arrowBase = Offset(
            (end.x - arrowLength * 0.7f * cos(angle)).toFloat(),
            (end.y - arrowLength * 0.7f * sin(angle)).toFloat()
        )

        // Draw shaft stopping at base of head
        drawLine(
            color = color,
            start = Offset(start.x, start.y),
            end = arrowBase,
            strokeWidth = width,
            cap = StrokeCap.Round
        )

        // Draw solid filled triangle arrow head
        val headPath = Path().apply {
            moveTo(end.x, end.y)
            lineTo(arrow1.x, arrow1.y)
            lineTo(arrow2.x, arrow2.y)
            close()
        }
        drawPath(headPath, color)
    }

    fun DrawScope.drawTriangle(start: Point, end: Point, color: Color, width: Float, isFilled: Boolean = false) {
        val path = Path().apply {
            moveTo(start.x, end.y)
            lineTo((start.x + end.x) / 2f, start.y)
            lineTo(end.x, end.y)
            close()
        }
        if (isFilled) {
            drawPath(path, color)
        } else {
            drawPath(path, color, style = CanvasStrokeStyle(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    fun DrawScope.drawRectangle(start: Point, end: Point, color: Color, width: Float, isFilled: Boolean = false) {
        val left = minOf(start.x, end.x)
        val top = minOf(start.y, end.y)
        val right = maxOf(start.x, end.x)
        val bottom = maxOf(start.y, end.y)
        val rect = Rect(left, top, right, bottom)
        if (isFilled) {
            drawRect(color, topLeft = rect.topLeft, size = rect.size)
        } else {
            drawRect(color, topLeft = rect.topLeft, size = rect.size, style = CanvasStrokeStyle(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    fun DrawScope.drawCircleShape(start: Point, end: Point, color: Color, width: Float, isFilled: Boolean = false) {
        val left = minOf(start.x, end.x)
        val top = minOf(start.y, end.y)
        val right = maxOf(start.x, end.x)
        val bottom = maxOf(start.y, end.y)
        val rect = Rect(left, top, right, bottom)
        if (isFilled) {
            drawOval(color, topLeft = rect.topLeft, size = rect.size)
        } else {
            drawOval(color, topLeft = rect.topLeft, size = rect.size, style = CanvasStrokeStyle(width = width))
        }
    }
}
