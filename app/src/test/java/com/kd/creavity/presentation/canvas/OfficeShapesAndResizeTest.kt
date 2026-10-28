package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Rect
import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import com.kd.creavity.domain.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficeShapesAndResizeTest {

    @Test
    fun testStrokeDefaultRoughnessIsClean() {
        val defaultStroke = Stroke()
        assertEquals(
            "Default shape roughness must be CLEAN for professional office-grade vectors",
            RoughnessStyle.CLEAN,
            defaultStroke.roughness
        )
    }

    @Test
    fun testCleanPathGenerationForOfficeShapes() {
        val points = listOf(Point(100f, 100f), Point(300f, 250f))

        // Rectangle
        val rectPath = PathUtils.createToolPath(DrawingTool.RECTANGLE, points)
        assertNotNull(rectPath)

        // Circle / Oval
        val circlePath = PathUtils.createToolPath(DrawingTool.CIRCLE, points)
        assertNotNull(circlePath)

        // Triangle
        val trianglePath = PathUtils.createToolPath(DrawingTool.TRIANGLE, points)
        assertNotNull(trianglePath)

        // Cylinder
        val cylinderPath = PathUtils.createToolPath(DrawingTool.CYLINDER, points)
        assertNotNull(cylinderPath)

        // Star
        val starPath = PathUtils.createToolPath(DrawingTool.STAR, points)
        assertNotNull(starPath)
    }

    @Test
    fun testEightPointResizeHandleHitDetection() {
        val controller = CanvasGestureController(
            onTransform = { _, _, _ -> },
            onTransformStateChanged = { _, _ -> },
            onStrokeCompleted = {},
            onStrokeBroadcastStart = { _, _, _, _, _, _ -> },
            onStrokeBroadcastPoint = { _, _ -> },
            onStrokeBroadcastEnd = {},
            onDeleteStroke = {},
            onDeleteText = {},
            onDeleteImage = {},
            onElementsMoved = { _, _, _ -> },
            onSelectionChanged = { _, _, _ -> },
            onTapForText = {},
            requestInvalidate = {}
        )

        val bounds = Rect(100f, 100f, 300f, 300f)

        // Hit TOP_LEFT
        val handleTL = controller.hitTestResizeHandle(Point(102f, 98f), bounds, 15f)
        assertEquals(ResizeHandle.TOP_LEFT, handleTL)

        // Hit TOP (center)
        val handleTop = controller.hitTestResizeHandle(Point(200f, 101f), bounds, 15f)
        assertEquals(ResizeHandle.TOP, handleTop)

        // Hit TOP_RIGHT
        val handleTR = controller.hitTestResizeHandle(Point(299f, 102f), bounds, 15f)
        assertEquals(ResizeHandle.TOP_RIGHT, handleTR)

        // Hit RIGHT
        val handleRight = controller.hitTestResizeHandle(Point(301f, 200f), bounds, 15f)
        assertEquals(ResizeHandle.RIGHT, handleRight)

        // Hit BOTTOM_RIGHT
        val handleBR = controller.hitTestResizeHandle(Point(298f, 298f), bounds, 15f)
        assertEquals(ResizeHandle.BOTTOM_RIGHT, handleBR)

        // Hit BOTTOM (center)
        val handleBottom = controller.hitTestResizeHandle(Point(200f, 299f), bounds, 15f)
        assertEquals(ResizeHandle.BOTTOM, handleBottom)

        // Hit BOTTOM_LEFT
        val handleBL = controller.hitTestResizeHandle(Point(101f, 301f), bounds, 15f)
        assertEquals(ResizeHandle.BOTTOM_LEFT, handleBL)

        // Hit LEFT (center)
        val handleLeft = controller.hitTestResizeHandle(Point(99f, 200f), bounds, 15f)
        assertEquals(ResizeHandle.LEFT, handleLeft)

        // Miss inside center
        val handleMiss = controller.hitTestResizeHandle(Point(200f, 200f), bounds, 15f)
        assertEquals(null, handleMiss)
    }
}
