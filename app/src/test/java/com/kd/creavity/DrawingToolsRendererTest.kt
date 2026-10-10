package com.kd.creavity

import com.kd.creavity.data.storage.BoardManager
import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.presentation.canvas.renderers.CalligraphyStrokeRenderer
import com.kd.creavity.presentation.canvas.renderers.PenStrokeRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingToolsRendererTest {

    @Test
    fun testCalligraphyPathGeneration() {
        // 1. Empty points
        val emptyPath = CalligraphyStrokeRenderer.createCalligraphyPath(emptyList(), 10f)
        assertNotNull(emptyPath)

        // 2. Single tap chisel imprint
        val singlePoint = listOf(Point(100f, 100f, pressure = 0.8f))
        val singlePath = CalligraphyStrokeRenderer.createCalligraphyPath(singlePoint, 10f)
        assertNotNull(singlePath)

        // 3. Multi-point calligraphy curve
        val multiPoints = listOf(
            Point(100f, 100f, pressure = 0.5f),
            Point(120f, 150f, pressure = 0.7f),
            Point(150f, 200f, pressure = 0.9f),
            Point(180f, 220f, pressure = 0.6f)
        )
        val multiPath = CalligraphyStrokeRenderer.createCalligraphyPath(multiPoints, 12f)
        assertNotNull(multiPath)
    }

    @Test
    fun testPenPathGeneration() {
        val points = listOf(
            Point(50f, 50f, pressure = 1.0f),
            Point(60f, 70f, pressure = 1.0f),
            Point(80f, 90f, pressure = 1.0f)
        )
        val path = PenStrokeRenderer.createPenPath(points)
        assertNotNull(path)
    }

    @Test
    fun testPathUtilsToolPath() {
        val points = listOf(
            Point(10f, 10f),
            Point(20f, 30f),
            Point(40f, 50f)
        )
        val penPath = PathUtils.createToolPath(DrawingTool.PEN, points, 5f)
        assertNotNull(penPath)

        val calligPath = PathUtils.createToolPath(DrawingTool.CALLIGRAPHY, points, 10f)
        assertNotNull(calligPath)
    }

    @Test
    fun testCalligraphyStrokeModel() {
        val stroke = Stroke(
            points = listOf(Point(10f, 10f, pressure = 0.9f, tilt = 0.5f, orientation = 1.2f)),
            tool = DrawingTool.CALLIGRAPHY
        )
        assertEquals(DrawingTool.CALLIGRAPHY, stroke.tool)
        assertEquals(0.5f, stroke.points[0].tilt)
        assertEquals(1.2f, stroke.points[0].orientation)
    }

}
