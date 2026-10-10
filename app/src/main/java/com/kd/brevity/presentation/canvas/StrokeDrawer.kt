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
        if (points.isEmpty()) return
        if (points.size == 1 && tool != DrawingTool.LASER) {
            val p = points.first()
            drawCircle(color, radius = (width * 0.5f).coerceAtLeast(1.5f), center = Offset(p.x, p.y))
            return
        }

        when (tool) {
            DrawingTool.PEN -> {
                if (cachedPath != null && points.size > 1 && !hasDynamicPressure(points)) {
                    drawPath(
                        path = cachedPath,
                        color = color,
                        style = CanvasStrokeStyle(
                            width = width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else {
                    drawProfessionalApplePen(points, color, width)
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
                if (cachedPath != null && points.size > 1) {
                    drawPath(
                        path = cachedPath,
                        color = color.copy(alpha = 0.85f),
                        style = CanvasStrokeStyle(
                            width = width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else {
                    drawProfessionalApplePencil(points, color, width)
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

    private fun DrawScope.drawProfessionalApplePen(
        points: List<Point>,
        color: Color,
        baseWidth: Float
    ) {
        if (points.isEmpty()) return
        if (points.size == 1) {
            val p = points.first()
            drawCircle(color, radius = (baseWidth * 0.5f).coerceAtLeast(1f), center = Offset(p.x, p.y))
            return
        }

        val widths = FloatArray(points.size)
        var smoothedWidth = baseWidth

        for (i in points.indices) {
            val curr = points[i]
            val prev = if (i > 0) points[i - 1] else curr
            val dt = (curr.timestampMs - prev.timestampMs).coerceIn(1L, 160L).toFloat()
            val dx = curr.x - prev.x
            val dy = curr.y - prev.y
            val dist = sqrt(dx * dx + dy * dy)
            val speed = dist / dt

            val speedFactor = 1.0f - (speed * 0.12f).coerceIn(-0.35f, 0.45f)
            val pressureFactor = 0.45f + 0.55f * curr.pressure.coerceIn(0.1f, 1.5f)

            val targetWidth = (baseWidth * speedFactor * pressureFactor).coerceIn(baseWidth * 0.35f, baseWidth * 1.8f)
            smoothedWidth = smoothedWidth * 0.65f + targetWidth * 0.35f
            widths[i] = smoothedWidth
        }

        for (i in 0 until points.size - 1) {
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

    private fun DrawScope.drawProfessionalApplePencil(
        points: List<Point>,
        color: Color,
        baseWidth: Float
    ) {
        if (points.isEmpty()) return
        if (points.size == 1) {
            val p = points.first()
            drawCircle(color.copy(alpha = 0.7f), radius = (baseWidth * 0.45f).coerceAtLeast(1f), center = Offset(p.x, p.y))
            return
        }

        val avgTilt = points.map { it.tilt }.average().toFloat()
        val tiltRatio = (avgTilt / 1.57f).coerceIn(0f, 1f)
        val tiltSpread = 1f + 2.5f * tiltRatio
        val effectiveWidth = (baseWidth * tiltSpread).coerceAtLeast(1f)
        val baseAlpha = (color.alpha * (0.85f - 0.35f * tiltRatio)).coerceIn(0.2f, 0.95f)

        val seed = points.first().timestampMs.toInt()
        val random = Random(seed)

        val jitterAmount = (effectiveWidth * 0.35f).coerceAtLeast(0.6f)
        val passes = if (tiltRatio > 0.25f) 3 else 2

        for (pass in 0 until passes) {
            val passAlpha = (baseAlpha / passes.toFloat()) * (0.7f + random.nextFloat() * 0.5f)
            val passWidth = effectiveWidth * (0.6f + random.nextFloat() * 0.4f)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val jx0 = (random.nextFloat() - 0.5f) * jitterAmount
                val jy0 = (random.nextFloat() - 0.5f) * jitterAmount
                val jx1 = (random.nextFloat() - 0.5f) * jitterAmount
                val jy1 = (random.nextFloat() - 0.5f) * jitterAmount

                drawLine(
                    color = color.copy(alpha = passAlpha.coerceIn(0.05f, 1f)),
                    start = Offset(p0.x + jx0, p0.y + jy0),
                    end = Offset(p1.x + jx1, p1.y + jy1),
                    strokeWidth = passWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val segPressure = (p0.pressure + p1.pressure) / 2f
            val coreWidth = (baseWidth * 0.7f * (0.5f + 0.5f * segPressure)).coerceAtLeast(1f)
            val coreAlpha = (color.alpha * (0.65f + 0.35f * segPressure)).coerceIn(0.1f, 1f)

            drawLine(
                color = color.copy(alpha = coreAlpha),
                start = Offset(p0.x, p0.y),
                end = Offset(p1.x, p1.y),
                strokeWidth = coreWidth,
                cap = StrokeCap.Round
            )
        }
    }

    fun DrawScope.drawGlowingLaserStroke(
        points: List<Point>,
        baseWidth: Float
    ) {
        if (points.isEmpty()) return
        val now = android.os.SystemClock.uptimeMillis()
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
