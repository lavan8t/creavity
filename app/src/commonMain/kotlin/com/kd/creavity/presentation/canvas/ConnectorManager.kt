package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import kotlin.math.sqrt

enum class AnchorDirection {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    CENTER
}

data class ShapeAnchor(
    val id: String,
    val elementId: String,
    val position: Offset,
    val direction: AnchorDirection
)

data class SnapResult(
    val points: List<Point>,
    val startAnchorId: String?,
    val endAnchorId: String?
)

object ConnectorManager {

    private val CONNECTABLE_TOOLS = setOf(
        DrawingTool.RECTANGLE,
        DrawingTool.CIRCLE,
        DrawingTool.TRIANGLE,
        DrawingTool.DIAMOND,
        DrawingTool.STAR
    )

    fun computeAnchorsForStroke(stroke: Stroke): List<ShapeAnchor> {
        if (stroke.tool !in CONNECTABLE_TOOLS || stroke.points.size < 2) {
            return emptyList()
        }

        val minX = stroke.points.minOf { it.x }
        val maxX = stroke.points.maxOf { it.x }
        val minY = stroke.points.minOf { it.y }
        val maxY = stroke.points.maxOf { it.y }

        val midX = (minX + maxX) / 2f
        val midY = (minY + maxY) / 2f

        return listOf(
            ShapeAnchor("${stroke.id}:${AnchorDirection.NORTH.name}", stroke.id, Offset(midX, minY), AnchorDirection.NORTH),
            ShapeAnchor("${stroke.id}:${AnchorDirection.SOUTH.name}", stroke.id, Offset(midX, maxY), AnchorDirection.SOUTH),
            ShapeAnchor("${stroke.id}:${AnchorDirection.EAST.name}", stroke.id, Offset(maxX, midY), AnchorDirection.EAST),
            ShapeAnchor("${stroke.id}:${AnchorDirection.WEST.name}", stroke.id, Offset(minX, midY), AnchorDirection.WEST),
            ShapeAnchor("${stroke.id}:${AnchorDirection.CENTER.name}", stroke.id, Offset(midX, midY), AnchorDirection.CENTER)
        )
    }

    fun computeAnchorsForStickyNote(note: StickyNote): List<ShapeAnchor> {
        val minX = note.x
        val maxX = note.x + note.width
        val minY = note.y
        val maxY = note.y + note.height

        val midX = (minX + maxX) / 2f
        val midY = (minY + maxY) / 2f

        return listOf(
            ShapeAnchor("${note.id}:${AnchorDirection.NORTH.name}", note.id, Offset(midX, minY), AnchorDirection.NORTH),
            ShapeAnchor("${note.id}:${AnchorDirection.SOUTH.name}", note.id, Offset(midX, maxY), AnchorDirection.SOUTH),
            ShapeAnchor("${note.id}:${AnchorDirection.EAST.name}", note.id, Offset(maxX, midY), AnchorDirection.EAST),
            ShapeAnchor("${note.id}:${AnchorDirection.WEST.name}", note.id, Offset(minX, midY), AnchorDirection.WEST),
            ShapeAnchor("${note.id}:${AnchorDirection.CENTER.name}", note.id, Offset(midX, midY), AnchorDirection.CENTER)
        )
    }

    fun computeAllAnchors(
        strokes: List<Stroke>,
        stickyNotes: List<StickyNote> = emptyList()
    ): List<ShapeAnchor> {
        val anchors = mutableListOf<ShapeAnchor>()
        strokes.forEach { stroke ->
            anchors.addAll(computeAnchorsForStroke(stroke))
        }
        stickyNotes.forEach { note ->
            anchors.addAll(computeAnchorsForStickyNote(note))
        }
        return anchors
    }

    fun findClosestAnchor(
        point: Offset,
        anchors: List<ShapeAnchor>,
        threshold: Float = 28f
    ): ShapeAnchor? {
        var closest: ShapeAnchor? = null
        var minDistance = threshold

        for (anchor in anchors) {
            val dx = anchor.position.x - point.x
            val dy = anchor.position.y - point.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist <= minDistance) {
                minDistance = dist
                closest = anchor
            }
        }
        return closest
    }

    fun snapEndpoints(
        points: List<Point>,
        anchors: List<ShapeAnchor>,
        threshold: Float = 28f
    ): SnapResult {
        if (points.size < 2) return SnapResult(points, null, null)

        val firstPt = points.first()
        val lastPt = points.last()

        val startAnchor = findClosestAnchor(Offset(firstPt.x, firstPt.y), anchors, threshold)
        // If end anchor is checked, exclude the start anchor so arrow doesn't snap both ends to same point
        val endAnchor = findClosestAnchor(
            Offset(lastPt.x, lastPt.y),
            if (startAnchor != null) anchors.filter { it.id != startAnchor.id } else anchors,
            threshold
        )

        val newStart = if (startAnchor != null) {
            firstPt.copy(x = startAnchor.position.x, y = startAnchor.position.y)
        } else firstPt

        val newEnd = if (endAnchor != null) {
            lastPt.copy(x = endAnchor.position.x, y = endAnchor.position.y)
        } else lastPt

        val resultPoints = listOf(newStart, newEnd)
        return SnapResult(
            points = resultPoints,
            startAnchorId = startAnchor?.id,
            endAnchorId = endAnchor?.id
        )
    }

    /**
     * When elements are moved, recalculate connected arrow/line endpoints so they remain magnetically bound.
     */
    fun rerouteConnectedStrokes(
        allStrokes: List<Stroke>,
        movedStrokes: List<Stroke>,
        stickyNotes: List<StickyNote> = emptyList()
    ): List<Stroke> {
        val allAnchors = computeAllAnchors(allStrokes, stickyNotes).associateBy { it.id }

        return allStrokes.map { stroke ->
            if (stroke.tool != DrawingTool.ARROW && stroke.tool != DrawingTool.LINE) {
                return@map stroke
            }
            if (stroke.connectedStartAnchor == null && stroke.connectedEndAnchor == null) {
                return@map stroke
            }
            if (stroke.points.size < 2) return@map stroke

            var newStart = stroke.points.first()
            var newEnd = stroke.points.last()

            if (stroke.connectedStartAnchor != null) {
                allAnchors[stroke.connectedStartAnchor]?.let { anchor ->
                    newStart = newStart.copy(x = anchor.position.x, y = anchor.position.y)
                }
            }

            if (stroke.connectedEndAnchor != null) {
                allAnchors[stroke.connectedEndAnchor]?.let { anchor ->
                    newEnd = newEnd.copy(x = anchor.position.x, y = anchor.position.y)
                }
            }

            stroke.copy(points = listOf(newStart, newEnd))
        }
    }
}
