package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectorManagerTest {

    @Test
    fun testComputeAnchorsForRectangle() {
        val rectStroke = Stroke(
            id = "rect-1",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(100f, 100f), Point(300f, 200f))
        )

        val anchors = ConnectorManager.computeAnchorsForStroke(rectStroke)
        assertEquals(5, anchors.size)

        val north = anchors.first { it.direction == AnchorDirection.NORTH }
        assertEquals(200f, north.position.x, 0.01f)
        assertEquals(100f, north.position.y, 0.01f)

        val south = anchors.first { it.direction == AnchorDirection.SOUTH }
        assertEquals(200f, south.position.x, 0.01f)
        assertEquals(200f, south.position.y, 0.01f)

        val east = anchors.first { it.direction == AnchorDirection.EAST }
        assertEquals(300f, east.position.x, 0.01f)
        assertEquals(150f, east.position.y, 0.01f)

        val west = anchors.first { it.direction == AnchorDirection.WEST }
        assertEquals(100f, west.position.x, 0.01f)
        assertEquals(150f, west.position.y, 0.01f)

        val center = anchors.first { it.direction == AnchorDirection.CENTER }
        assertEquals(200f, center.position.x, 0.01f)
        assertEquals(150f, center.position.y, 0.01f)
    }

    @Test
    fun testComputeAnchorsForStickyNote() {
        val note = StickyNote(
            id = "note-1",
            x = 50f,
            y = 50f,
            width = 100f,
            height = 80f,
            text = "Hello"
        )

        val anchors = ConnectorManager.computeAnchorsForStickyNote(note)
        assertEquals(5, anchors.size)

        val north = anchors.first { it.direction == AnchorDirection.NORTH }
        assertEquals(100f, north.position.x, 0.01f)
        assertEquals(50f, north.position.y, 0.01f)
    }

    @Test
    fun testProximitySnapping() {
        val rectStroke = Stroke(
            id = "rect-target",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(100f, 100f), Point(200f, 200f))
        )
        val anchors = ConnectorManager.computeAnchorsForStroke(rectStroke)

        // Point near East anchor (200, 150)
        val nearPoint = Offset(205f, 152f)
        val snapped = ConnectorManager.findClosestAnchor(nearPoint, anchors, threshold = 20f)
        assertNotNull(snapped)
        assertEquals(AnchorDirection.EAST, snapped!!.direction)

        // Point far away
        val farPoint = Offset(500f, 500f)
        val missed = ConnectorManager.findClosestAnchor(farPoint, anchors, threshold = 20f)
        assertNull(missed)
    }

    @Test
    fun testSnapEndpointsOnArrow() {
        val rect1 = Stroke(
            id = "node-1",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(100f, 100f), Point(200f, 200f)) // East at (200, 150)
        )
        val rect2 = Stroke(
            id = "node-2",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(400f, 100f), Point(500f, 200f)) // West at (400, 150)
        )

        val allAnchors = ConnectorManager.computeAllAnchors(listOf(rect1, rect2))

        val arrowPoints = listOf(
            Point(204f, 148f), // Near node-1 East
            Point(395f, 153f)  // Near node-2 West
        )

        val snapResult = ConnectorManager.snapEndpoints(arrowPoints, allAnchors, threshold = 25f)

        assertEquals("node-1:EAST", snapResult.startAnchorId)
        assertEquals("node-2:WEST", snapResult.endAnchorId)
        assertEquals(200f, snapResult.points.first().x, 0.01f)
        assertEquals(150f, snapResult.points.first().y, 0.01f)
        assertEquals(400f, snapResult.points.last().x, 0.01f)
        assertEquals(150f, snapResult.points.last().y, 0.01f)
    }

    @Test
    fun testReroutingConnectedStrokesOnShapeMove() {
        val node1 = Stroke(
            id = "node-1",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(100f, 100f), Point(200f, 200f)) // East at (200, 150)
        )
        val arrow = Stroke(
            id = "arrow-1",
            tool = DrawingTool.ARROW,
            points = listOf(Point(200f, 150f), Point(400f, 150f)),
            connectedStartAnchor = "node-1:EAST"
        )

        // Translate node1 by +50 in X and +30 in Y
        val movedNode1 = node1.copy(
            points = listOf(Point(150f, 130f), Point(250f, 230f)) // New East at (250, 180)
        )

        val rerouted = ConnectorManager.rerouteConnectedStrokes(
            allStrokes = listOf(movedNode1, arrow),
            movedStrokes = listOf(movedNode1)
        )

        val updatedArrow = rerouted.first { it.id == "arrow-1" }
        assertEquals(250f, updatedArrow.points.first().x, 0.01f)
        assertEquals(180f, updatedArrow.points.first().y, 0.01f)
        // End point unchanged
        assertEquals(400f, updatedArrow.points.last().x, 0.01f)
        assertEquals(150f, updatedArrow.points.last().y, 0.01f)
    }
}
