package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class RoughShapePaths(
    val strokePaths: List<Path>,
    val fillPath: Path?,
    val hachurePaths: List<Path>
)

/**
 * Native Rough Shape Generator (Rough.js algorithm adapted for Jetpack Compose Canvas / Skia).
 * Generates natural hand-drawn wobbly outlines and cross-hatch shading.
 */
object RoughShapeGenerator {

    private val cache = mutableMapOf<String, RoughShapePaths>()

    fun getRoughnessFactor(style: RoughnessStyle): Float = when (style) {
        RoughnessStyle.CLEAN -> 0.0f
        RoughnessStyle.SKETCHY -> 1.3f
        RoughnessStyle.CARTOON -> 2.6f
    }

    fun getOrCreateRoughShape(
        strokeId: String,
        tool: DrawingTool,
        points: List<Point>,
        strokeWidth: Float,
        roughness: RoughnessStyle = RoughnessStyle.SKETCHY,
        fillStyle: FillStyle = FillStyle.TRANSPARENT
    ): RoughShapePaths {
        if (points.size < 2) {
            return RoughShapePaths(emptyList(), null, emptyList())
        }

        val cacheKey = "$strokeId-${tool.name}-${points.first().x},${points.first().y}-${points.last().x},${points.last().y}-${roughness.name}-${fillStyle.name}-$strokeWidth"
        cache[cacheKey]?.let { return it }

        val seed = strokeId.hashCode().toLong()
        val random = Random(seed)
        val rFactor = getRoughnessFactor(roughness)

        val result: RoughShapePaths = when (tool) {
            DrawingTool.RECTANGLE -> createRoughRectangle(points.first(), points.last(), strokeWidth, rFactor, fillStyle, random)
            DrawingTool.CIRCLE -> createRoughEllipse(points.first(), points.last(), strokeWidth, rFactor, fillStyle, random)
            DrawingTool.TRIANGLE -> createRoughTriangle(points.first(), points.last(), strokeWidth, rFactor, fillStyle, random)
            DrawingTool.DIAMOND -> createRoughDiamond(points.first(), points.last(), strokeWidth, rFactor, fillStyle, random)
            DrawingTool.CYLINDER -> createRoughCylinder(points.first(), points.last(), strokeWidth, rFactor, fillStyle, random)
            DrawingTool.STAR -> createRoughStar(points.first(), points.last(), strokeWidth, rFactor, fillStyle, random)
            DrawingTool.LINE -> createRoughLineShape(points.first(), points.last(), strokeWidth, rFactor, random)
            DrawingTool.ARROW -> createRoughArrowShape(points.first(), points.last(), strokeWidth, rFactor, random)
            else -> createRoughLineShape(points.first(), points.last(), strokeWidth, rFactor, random)
        }

        // Limit cache size to prevent memory leaks across long sessions
        if (cache.size > 2000) {
            val toRemove = cache.keys.take(500)
            toRemove.forEach { cache.remove(it) }
        }

        cache[cacheKey] = result
        return result
    }

    /**
     * Generates a multi-pass wobbly line between two points.
     */
    fun createRoughLine(
        p1: Offset,
        p2: Offset,
        roughness: Float,
        random: Random,
        overshoot: Float = 0f
    ): List<Path> {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val length = sqrt(dx * dx + dy * dy)
        if (length < 1f) return emptyList()

        if (roughness <= 0.05f) {
            // Clean geometric line
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
            }
            return listOf(path)
        }

        val passes = 2
        val paths = mutableListOf<Path>()
        val angle = atan2(dy, dx)

        for (pass in 0 until passes) {
            val path = Path()
            val startJitterX = (random.nextFloat() - 0.5f) * roughness * 2.2f
            val startJitterY = (random.nextFloat() - 0.5f) * roughness * 2.2f
            val endJitterX = (random.nextFloat() - 0.5f) * roughness * 2.2f
            val endJitterY = (random.nextFloat() - 0.5f) * roughness * 2.2f

            val effectiveStart = Offset(
                p1.x - overshoot * cos(angle) + startJitterX,
                p1.y - overshoot * sin(angle) + startJitterY
            )
            val effectiveEnd = Offset(
                p2.x + overshoot * cos(angle) + endJitterX,
                p2.y + overshoot * sin(angle) + endJitterY
            )

            path.moveTo(effectiveStart.x, effectiveStart.y)

            // Random midpoint deflection for organic bow/hand tremor
            val midDist = length * (0.35f + random.nextFloat() * 0.3f)
            val midX = p1.x + midDist * cos(angle) - sin(angle) * (random.nextFloat() - 0.5f) * roughness * 3.5f
            val midY = p1.y + midDist * sin(angle) + cos(angle) * (random.nextFloat() - 0.5f) * roughness * 3.5f

            path.quadraticTo(midX, midY, effectiveEnd.x, effectiveEnd.y)
            paths.add(path)
        }

        return paths
    }

    fun createRoughRectangle(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        fillStyle: FillStyle,
        random: Random
    ): RoughShapePaths {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)

        val tl = Offset(left, top)
        val tr = Offset(right, top)
        val br = Offset(right, bottom)
        val bl = Offset(left, bottom)

        val overshoot = if (roughness > 0.05f) min(strokeWidth * 1.5f + 2f, 8f) else 0f
        val strokePaths = mutableListOf<Path>()

        // 4 wobbly sides with subtle Excalidraw corner overshoots
        strokePaths.addAll(createRoughLine(tl, tr, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(tr, br, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(br, bl, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(bl, tl, roughness, random, overshoot))

        val fillPath = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }

        val hachurePaths = if (fillStyle == FillStyle.HACHURE) {
            createHachureLines(left, top, right, bottom, strokeWidth, roughness, random)
        } else emptyList()

        return RoughShapePaths(strokePaths, fillPath, hachurePaths)
    }

    fun createRoughEllipse(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        fillStyle: FillStyle,
        random: Random
    ): RoughShapePaths {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)

        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val rx = (right - left) / 2f
        val ry = (bottom - top) / 2f

        val strokePaths = mutableListOf<Path>()

        if (roughness <= 0.05f) {
            val path = Path().apply {
                addOval(Rect(left, top, right, bottom))
            }
            strokePaths.add(path)
        } else {
            // 2-pass hand-drawn wobbly ellipse
            val stepCount = 12
            for (pass in 0 until 2) {
                val path = Path()
                var firstPt: Offset? = null

                for (i in 0..stepCount) {
                    val theta = (i % stepCount) * (2 * Math.PI / stepCount)
                    val jitterX = (random.nextFloat() - 0.5f) * roughness * 2.0f
                    val jitterY = (random.nextFloat() - 0.5f) * roughness * 2.0f
                    val x = (cx + rx * cos(theta) + jitterX).toFloat()
                    val y = (cy + ry * sin(theta) + jitterY).toFloat()

                    if (i == 0) {
                        path.moveTo(x, y)
                        firstPt = Offset(x, y)
                    } else if (i == stepCount) {
                        firstPt?.let { path.lineTo(it.x, it.y) }
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                strokePaths.add(path)
            }
        }

        val fillPath = Path().apply {
            addOval(Rect(left, top, right, bottom))
        }

        val hachurePaths = if (fillStyle == FillStyle.HACHURE) {
            createHachureLines(left, top, right, bottom, strokeWidth, roughness, random)
        } else emptyList()

        return RoughShapePaths(strokePaths, fillPath, hachurePaths)
    }

    fun createRoughTriangle(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        fillStyle: FillStyle,
        random: Random
    ): RoughShapePaths {
        val p1 = Offset(start.x, end.y)
        val p2 = Offset((start.x + end.x) / 2f, start.y)
        val p3 = Offset(end.x, end.y)

        val overshoot = if (roughness > 0.05f) min(strokeWidth * 1.5f + 2f, 8f) else 0f
        val strokePaths = mutableListOf<Path>()

        strokePaths.addAll(createRoughLine(p1, p2, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(p2, p3, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(p3, p1, roughness, random, overshoot))

        val fillPath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        }

        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)

        val hachurePaths = if (fillStyle == FillStyle.HACHURE) {
            createHachureLines(left, top, right, bottom, strokeWidth, roughness, random)
        } else emptyList()

        return RoughShapePaths(strokePaths, fillPath, hachurePaths)
    }

    fun createRoughDiamond(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        fillStyle: FillStyle,
        random: Random
    ): RoughShapePaths {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)

        val midX = (left + right) / 2f
        val midY = (top + bottom) / 2f

        val pTop = Offset(midX, top)
        val pRight = Offset(right, midY)
        val pBottom = Offset(midX, bottom)
        val pLeft = Offset(left, midY)

        val overshoot = if (roughness > 0.05f) min(strokeWidth * 1.5f + 2f, 8f) else 0f
        val strokePaths = mutableListOf<Path>()

        strokePaths.addAll(createRoughLine(pTop, pRight, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(pRight, pBottom, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(pBottom, pLeft, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(pLeft, pTop, roughness, random, overshoot))

        val fillPath = Path().apply {
            moveTo(pTop.x, pTop.y)
            lineTo(pRight.x, pRight.y)
            lineTo(pBottom.x, pBottom.y)
            lineTo(pLeft.x, pLeft.y)
            close()
        }

        val hachurePaths = if (fillStyle == FillStyle.HACHURE) {
            createHachureLines(left, top, right, bottom, strokeWidth, roughness, random)
        } else emptyList()

        return RoughShapePaths(strokePaths, fillPath, hachurePaths)
    }

    fun createRoughCylinder(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        fillStyle: FillStyle,
        random: Random
    ): RoughShapePaths {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)
        val h = bottom - top
        val capH = min(h * 0.25f, 40f).coerceAtLeast(6f)

        val strokePaths = mutableListOf<Path>()
        val overshoot = if (roughness > 0.05f) min(strokeWidth * 1.5f + 2f, 8f) else 0f

        // Top ellipse
        val topEllipse = createRoughEllipse(Point(left, top), Point(right, top + capH), strokeWidth, roughness, FillStyle.TRANSPARENT, random)
        strokePaths.addAll(topEllipse.strokePaths)

        // Left and right vertical sides
        val sideTopLeft = Offset(left, top + capH / 2f)
        val sideBottomLeft = Offset(left, bottom - capH / 2f)
        val sideTopRight = Offset(right, top + capH / 2f)
        val sideBottomRight = Offset(right, bottom - capH / 2f)

        strokePaths.addAll(createRoughLine(sideTopLeft, sideBottomLeft, roughness, random, overshoot))
        strokePaths.addAll(createRoughLine(sideTopRight, sideBottomRight, roughness, random, overshoot))

        // Bottom curved rim
        if (roughness <= 0.05f) {
            val bottomRim = Path().apply {
                moveTo(sideBottomLeft.x, sideBottomLeft.y)
                quadraticTo((left + right) / 2f, bottom, sideBottomRight.x, sideBottomRight.y)
            }
            strokePaths.add(bottomRim)
        } else {
            for (pass in 0 until 2) {
                val rimPath = Path()
                val midJitterY = (random.nextFloat() - 0.5f) * roughness * 3f
                rimPath.moveTo(sideBottomLeft.x, sideBottomLeft.y)
                rimPath.quadraticTo((left + right) / 2f, bottom + midJitterY, sideBottomRight.x, sideBottomRight.y)
                strokePaths.add(rimPath)
            }
        }

        // Fill path enclosing the cylinder
        val fillPath = Path().apply {
            moveTo(sideTopLeft.x, sideTopLeft.y)
            quadraticTo((left + right) / 2f, top, sideTopRight.x, sideTopRight.y)
            lineTo(sideBottomRight.x, sideBottomRight.y)
            quadraticTo((left + right) / 2f, bottom, sideBottomLeft.x, sideBottomLeft.y)
            close()
        }

        val hachurePaths = if (fillStyle == FillStyle.HACHURE) {
            createHachureLines(left, top, right, bottom, strokeWidth, roughness, random)
        } else emptyList()

        return RoughShapePaths(strokePaths, fillPath, hachurePaths)
    }

    fun createRoughStar(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        fillStyle: FillStyle,
        random: Random
    ): RoughShapePaths {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)

        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val rOuter = min(right - left, bottom - top) / 2f
        val rInner = rOuter * 0.40f

        val vertices = mutableListOf<Offset>()
        val step = Math.PI / 5.0
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) rOuter else rInner
            val angle = -Math.PI / 2.0 + i * step
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            vertices.add(Offset(x, y))
        }

        val strokePaths = mutableListOf<Path>()
        val overshoot = if (roughness > 0.05f) min(strokeWidth * 1.5f + 2f, 6f) else 0f

        for (i in 0 until 10) {
            val nextIdx = (i + 1) % 10
            strokePaths.addAll(createRoughLine(vertices[i], vertices[nextIdx], roughness, random, overshoot))
        }

        val fillPath = Path().apply {
            if (vertices.isNotEmpty()) {
                moveTo(vertices[0].x, vertices[0].y)
                for (i in 1 until vertices.size) {
                    lineTo(vertices[i].x, vertices[i].y)
                }
                close()
            }
        }

        val hachurePaths = if (fillStyle == FillStyle.HACHURE) {
            createHachureLines(left, top, right, bottom, strokeWidth, roughness, random)
        } else emptyList()

        return RoughShapePaths(strokePaths, fillPath, hachurePaths)
    }

    fun createRoughLineShape(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        random: Random
    ): RoughShapePaths {
        val paths = createRoughLine(
            Offset(start.x, start.y),
            Offset(end.x, end.y),
            roughness,
            random,
            overshoot = 0f
        )
        return RoughShapePaths(paths, null, emptyList())
    }

    fun createRoughArrowShape(
        start: Point,
        end: Point,
        strokeWidth: Float,
        roughness: Float,
        random: Random
    ): RoughShapePaths {
        val dx = (end.x - start.x).toDouble()
        val dy = (end.y - start.y).toDouble()
        val angle = atan2(dy, dx)
        val arrowLength = (strokeWidth * 3.8f).coerceAtLeast(16f)
        val arrowAngle = Math.PI / 6.5

        val arrowBase = Offset(
            (end.x - arrowLength * 0.75f * cos(angle)).toFloat(),
            (end.y - arrowLength * 0.75f * sin(angle)).toFloat()
        )

        val barb1 = Offset(
            (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
        )
        val barb2 = Offset(
            (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
        )

        val strokePaths = mutableListOf<Path>()
        // Shaft
        strokePaths.addAll(createRoughLine(Offset(start.x, start.y), arrowBase, roughness, random, 0f))
        // Barbs
        strokePaths.addAll(createRoughLine(barb1, Offset(end.x, end.y), roughness, random, 0f))
        strokePaths.addAll(createRoughLine(barb2, Offset(end.x, end.y), roughness, random, 0f))

        val fillPath = Path().apply {
            moveTo(end.x, end.y)
            lineTo(barb1.x, barb1.y)
            lineTo(barb2.x, barb2.y)
            close()
        }

        return RoughShapePaths(strokePaths, fillPath, emptyList())
    }

    /**
     * Generates parallel hand-drawn cross-hatch shading lines.
     */
    fun createHachureLines(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        strokeWidth: Float,
        roughness: Float,
        random: Random,
        angleDeg: Float = -45f,
        gap: Float = 14f
    ): List<Path> {
        val width = right - left
        val height = bottom - top
        if (width < 4f || height < 4f) return emptyList()

        val paths = mutableListOf<Path>()
        val rad = Math.toRadians(angleDeg.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        val step = max(gap, strokeWidth * 2.5f)
        val span = width + height
        var d = -span

        while (d <= span) {
            val p1 = Offset(left + width / 2f + d * cosA - span * sinA, top + height / 2f + d * sinA + span * cosA)
            val p2 = Offset(left + width / 2f + d * cosA + span * sinA, top + height / 2f + d * sinA - span * cosA)

            // Clamp line roughly inside bounds
            val startX = p1.x.coerceIn(left, right)
            val startY = p1.y.coerceIn(top, bottom)
            val endX = p2.x.coerceIn(left, right)
            val endY = p2.y.coerceIn(top, bottom)

            if (abs(startX - endX) > 2f || abs(startY - endY) > 2f) {
                paths.addAll(createRoughLine(Offset(startX, startY), Offset(endX, endY), roughness * 0.7f, random, 0f))
            }

            d += step
        }

        return paths
    }

    fun clearCache() {
        cache.clear()
    }
}
