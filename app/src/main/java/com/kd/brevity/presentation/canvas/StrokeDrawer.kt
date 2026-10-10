package com.kd.brevity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStrokeStyle
import com.kd.brevity.domain.math.PathUtils
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.domain.model.Point
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object StrokeDrawer {

    fun DrawScope.drawStyledStroke(
        points: List<Point>,
        color: Color,
        width: Float,
        tool: DrawingTool,
        cachedPath: Path? = null
    ) {
        if (points.size < 2) return

        when (tool) {
            DrawingTool.PEN -> {
                if (hasDynamicPressure(points)) {
                    drawPressureSensitiveStroke(points, color, width)
                } else {
                    val path = cachedPath ?: PathUtils.createSmoothPath(points)
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
                drawPressureSensitiveStroke(points, color.copy(alpha = 0.85f), width * 2.5f)
            }
            DrawingTool.PENCIL -> {
                drawRoughPencilStroke(points, color.copy(alpha = 0.75f), width * 0.7f)
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
                if (points.isNotEmpty()) {
                    val last = points.last()
                    // Glowing outer laser halo
                    drawCircle(Color(0xFFFF1744).copy(alpha = 0.35f), radius = (width * 2.8f).coerceAtLeast(8f), center = Offset(last.x, last.y))
                    drawCircle(Color(0xFFFF5252).copy(alpha = 0.7f), radius = (width * 1.6f).coerceAtLeast(5f), center = Offset(last.x, last.y))
                    drawCircle(Color.White, radius = (width * 0.75f).coerceAtLeast(2.5f), center = Offset(last.x, last.y))
                    if (points.size >= 2) {
                        val path = cachedPath ?: PathUtils.createSmoothPath(points.takeLast(16))
                        drawPath(
                            path = path,
                            color = Color(0xFFFF1744).copy(alpha = 0.65f),
                            style = CanvasStrokeStyle(
                                width = (width * 1.4f).coerceAtLeast(3f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
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
        var minP = 1.0f
        var maxP = 1.0f
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

    private fun DrawScope.drawMultiLayerPenStroke(
        points: List<Point>,
        color: Color,
        width: Float
    ) {
        val baseWidth = width.coerceAtLeast(1.0f)
        val layerOffsets = listOf(-0.35f, 0f, 0.35f)

        layerOffsets.forEachIndexed { offsetIndex, layerFactor ->
            val layerOffset = layerFactor * (baseWidth * 0.4f)
            val path = Path()
            path.moveTo(points.first().x + layerOffset, points.first().y + layerOffset)

            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2f + layerOffset
                val midY = (prev.y + curr.y) / 2f + layerOffset
                path.quadraticTo(prev.x + layerOffset, prev.y + layerOffset, midX, midY)
            }
            path.lineTo(points.last().x + layerOffset, points.last().y + layerOffset)

            val widthFactor = 1f - (abs(offsetIndex - 1) * 0.25f)
            val strokeWidth = baseWidth * (0.6f + 0.4f * widthFactor)
            val alphaMod = (0.55f + 0.25f * widthFactor).coerceIn(0f, 1f)

            drawPath(
                path = path,
                color = color.copy(alpha = color.alpha * alphaMod),
                style = CanvasStrokeStyle(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Final crisp center core line
        val mainPath = PathUtils.createSmoothPath(points)
        drawPath(
            path = mainPath,
            color = color,
            style = CanvasStrokeStyle(
                width = baseWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    private fun DrawScope.drawRoughPencilStroke(
        points: List<Point>,
        color: Color,
        width: Float
    ) {
        val baseWidth = width.coerceAtLeast(1.0f)
        val jitter = (baseWidth * 0.35f).coerceAtLeast(0.5f)
        val random = Random(points.first().timestampMs.toInt())

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            val offsetStart = Offset(
                start.x + (random.nextFloat() - 0.5f) * jitter,
                start.y + (random.nextFloat() - 0.5f) * jitter
            )
            val offsetEnd = Offset(
                end.x + (random.nextFloat() - 0.5f) * jitter,
                end.y + (random.nextFloat() - 0.5f) * jitter
            )
            drawLine(
                color = color,
                start = offsetStart,
                end = offsetEnd,
                strokeWidth = baseWidth * 0.85f,
                cap = StrokeCap.Round
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
