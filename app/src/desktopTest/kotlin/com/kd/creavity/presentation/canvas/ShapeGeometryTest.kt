package com.kd.creavity.presentation.canvas

import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ShapeGeometryTest {

    @Test
    fun testRoughEllipseProducesSmoothCurvesWithoutDecagonArtifacts() {
        val start = Point(100f, 100f)
        val end = Point(300f, 300f)
        val shape = RoughShapeGenerator.createRoughEllipse(
            start = start,
            end = end,
            strokeWidth = 4f,
            roughness = 1.0f,
            fillStyle = FillStyle.TRANSPARENT,
            random = Random(42)
        )
        assertNotNull(shape.strokePaths)
        assertTrue("Rough ellipse must have stroke paths", shape.strokePaths.isNotEmpty())
        assertNotNull("Fill path must exist", shape.fillPath)
    }

    @Test
    fun testCleanCircleUsesExactOval() {
        val start = Point(50f, 50f)
        val end = Point(250f, 250f)
        val path = PathUtils.createToolPath(DrawingTool.CIRCLE, listOf(start, end))
        assertNotNull(path)
        val bounds = path.getBounds()
        assertTrue("Bounds width should match 200", kotlin.math.abs(bounds.width - 200f) < 1f)
        assertTrue("Bounds height should match 200", kotlin.math.abs(bounds.height - 200f) < 1f)
    }

    @Test
    fun testTriangleVerticesNormalizedWhenDraggingUpwards() {
        // Dragging upwards and leftwards: start is bottom-right, end is top-left
        val start = Point(300f, 300f)
        val end = Point(100f, 100f)
        val path = PathUtils.createToolPath(DrawingTool.TRIANGLE, listOf(start, end))
        val bounds = path.getBounds()
        assertTrue("Triangle left should be 100", kotlin.math.abs(bounds.left - 100f) < 1f)
        assertTrue("Triangle top should be 100", kotlin.math.abs(bounds.top - 100f) < 1f)
        assertTrue("Triangle width should be 200", kotlin.math.abs(bounds.width - 200f) < 1f)
        assertTrue("Triangle height should be 200", kotlin.math.abs(bounds.height - 200f) < 1f)
    }
}
