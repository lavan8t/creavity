package com.kd.creavity.presentation.canvas

import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoughShapeGeneratorTest {

    @Before
    fun setUp() {
        RoughShapeGenerator.clearCache()
    }

    @Test
    fun testRoughnessFactors() {
        assertEquals(0.0f, RoughShapeGenerator.getRoughnessFactor(RoughnessStyle.CLEAN), 0.001f)
        assertEquals(1.3f, RoughShapeGenerator.getRoughnessFactor(RoughnessStyle.SKETCHY), 0.001f)
        assertEquals(2.6f, RoughShapeGenerator.getRoughnessFactor(RoughnessStyle.CARTOON), 0.001f)
    }

    @Test
    fun testCleanVsSketchyLineGeneration() {
        val points = listOf(Point(10f, 10f), Point(100f, 100f))

        // CLEAN line produces exactly 1 crisp path
        val cleanShape = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "line-clean",
            tool = DrawingTool.LINE,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.CLEAN
        )
        assertEquals(1, cleanShape.strokePaths.size)
        assertNull(cleanShape.fillPath)
        assertTrue(cleanShape.hachurePaths.isEmpty())

        // SKETCHY line produces 2 wobbly passes
        val sketchyShape = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "line-sketchy",
            tool = DrawingTool.LINE,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.SKETCHY
        )
        assertEquals(2, sketchyShape.strokePaths.size)
    }

    @Test
    fun testRectangleFillAndHachure() {
        val points = listOf(Point(0f, 0f), Point(200f, 150f))

        // Transparent fill -> no hachure
        val transparentShape = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "rect-trans",
            tool = DrawingTool.RECTANGLE,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.TRANSPARENT
        )
        assertNotNull(transparentShape.fillPath)
        assertTrue(transparentShape.hachurePaths.isEmpty())
        assertTrue(transparentShape.strokePaths.isNotEmpty())

        // Hachure fill -> generates cross-hatching paths
        val hachureShape = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "rect-hachure",
            tool = DrawingTool.RECTANGLE,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.HACHURE
        )
        assertNotNull(hachureShape.fillPath)
        assertTrue("Hachure style should generate shading lines", hachureShape.hachurePaths.isNotEmpty())
    }

    @Test
    fun testEllipseShapeGeneration() {
        val points = listOf(Point(50f, 50f), Point(150f, 150f))

        val circleShape = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "circle-1",
            tool = DrawingTool.CIRCLE,
            points = points,
            strokeWidth = 3f,
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.SEMI_TRANSPARENT
        )
        assertNotNull(circleShape.fillPath)
        assertTrue(circleShape.strokePaths.isNotEmpty())
    }

    @Test
    fun testArrowShapeGeneration() {
        val points = listOf(Point(10f, 10f), Point(150f, 80f))

        val arrowShape = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "arrow-1",
            tool = DrawingTool.ARROW,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.SKETCHY
        )
        // Arrow has shaft + 2 barbs
        assertTrue(arrowShape.strokePaths.size >= 3)
        assertNotNull(arrowShape.fillPath)
    }

    @Test
    fun testShapeCachingConsistency() {
        val points = listOf(Point(20f, 30f), Point(120f, 130f))

        val shape1 = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "cache-test-1",
            tool = DrawingTool.RECTANGLE,
            points = points,
            strokeWidth = 4f
        )
        val shape2 = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "cache-test-1",
            tool = DrawingTool.RECTANGLE,
            points = points,
            strokeWidth = 4f
        )

        assertSame("Subsequent calls with identical parameters should return cached instance", shape1, shape2)
    }
}
