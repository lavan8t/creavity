package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.presentation.ui.BoardAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EmbeddedTextTest {

    @Test
    fun testStrokeWithEmbeddedTextCreation() {
        val stroke = Stroke(
            id = "shape-with-text",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(100f, 100f), Point(400f, 300f)),
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.SEMI_TRANSPARENT,
            embeddedText = "User Service Architecture"
        )

        assertEquals("User Service Architecture", stroke.embeddedText)
        assertEquals(RoughnessStyle.SKETCHY, stroke.roughness)
        assertEquals(FillStyle.SEMI_TRANSPARENT, stroke.fillStyle)
        assertEquals(0L, stroke.embeddedTextColor)
    }

    @Test
    fun testEmbeddedTextCoordinatesCenterWithinShapeBounds() {
        val stroke = Stroke(
            id = "rect-1",
            tool = DrawingTool.RECTANGLE,
            points = listOf(Point(200f, 100f), Point(600f, 300f)),
            embeddedText = "API Gateway"
        )

        val minX = stroke.points.minOf { it.x }
        val maxX = stroke.points.maxOf { it.x }
        val minY = stroke.points.minOf { it.y }
        val maxY = stroke.points.maxOf { it.y }

        val shapeW = maxX - minX
        val shapeH = maxY - minY
        val centerX = minX + shapeW / 2f
        val centerY = minY + shapeH / 2f

        assertEquals(400f, centerX, 0.01f)
        assertEquals(200f, centerY, 0.01f)
    }

    @Test
    fun testSynchronousTranslationMaintainsEmbeddedTextCentering() {
        val initialStroke = Stroke(
            id = "node-1",
            tool = DrawingTool.DIAMOND,
            points = listOf(Point(100f, 100f), Point(300f, 200f)),
            embeddedText = "Is Authenticated?"
        )

        // Translate shape by (+120f, +80f)
        val deltaX = 120f
        val deltaY = 80f
        val movedStroke = initialStroke.copy(
            points = initialStroke.points.map { it.copy(x = it.x + deltaX, y = it.y + deltaY) }
        )

        val newMinX = movedStroke.points.minOf { it.x }
        val newMaxX = movedStroke.points.maxOf { it.x }
        val newMinY = movedStroke.points.minOf { it.y }
        val newMaxY = movedStroke.points.maxOf { it.y }

        val newCenterX = newMinX + (newMaxX - newMinX) / 2f
        val newCenterY = newMinY + (newMaxY - newMinY) / 2f

        assertEquals(320f, newCenterX, 0.01f)
        assertEquals(230f, newCenterY, 0.01f)
        assertEquals("Is Authenticated?", movedStroke.embeddedText)
    }

    @Test
    fun testUpdateStrokePropertiesActionData() {
        val action = BoardAction.UpdateStrokeProperties(
            strokeId = "shape-1",
            embeddedText = "Updated Label",
            roughness = RoughnessStyle.CARTOON,
            fillStyle = FillStyle.HACHURE
        )

        assertEquals("shape-1", action.strokeId)
        assertEquals("Updated Label", action.embeddedText)
        assertEquals(RoughnessStyle.CARTOON, action.roughness)
        assertEquals(FillStyle.HACHURE, action.fillStyle)
    }
}
