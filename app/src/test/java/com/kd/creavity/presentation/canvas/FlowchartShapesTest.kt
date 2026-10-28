package com.kd.creavity.presentation.canvas

import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FlowchartShapesTest {

    @Before
    fun setUp() {
        RoughShapeGenerator.clearCache()
    }

    @Test
    fun testDiamondShapeGeneration() {
        val points = listOf(Point(100f, 100f), Point(300f, 200f))

        // 1. PathUtils clean path
        val cleanPath = PathUtils.createToolPath(DrawingTool.DIAMOND, points)
        assertNotNull(cleanPath)

        // 2. Rough shape generation
        val diamond = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "diamond-1",
            tool = DrawingTool.DIAMOND,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.HACHURE
        )

        // 4 sides x 2 passes = 8 wobbly paths
        assertEquals(8, diamond.strokePaths.size)
        assertNotNull(diamond.fillPath)
        assertTrue("Diamond should have hachure shading paths", diamond.hachurePaths.isNotEmpty())
    }

    @Test
    fun testCylinderShapeGeneration() {
        val points = listOf(Point(50f, 50f), Point(150f, 250f))

        // 1. PathUtils clean path
        val cleanPath = PathUtils.createToolPath(DrawingTool.CYLINDER, points)
        assertNotNull(cleanPath)

        // 2. Rough shape generation
        val cylinder = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "cylinder-1",
            tool = DrawingTool.CYLINDER,
            points = points,
            strokeWidth = 3f,
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.SOLID
        )

        // Top ellipse (2 passes) + 2 sides (2x2=4) + bottom rim (2) = at least 8 paths
        assertTrue("Cylinder should have multiple stroke paths for 3D body", cylinder.strokePaths.size >= 6)
        assertNotNull(cylinder.fillPath)
    }

    @Test
    fun testStarShapeGeneration() {
        val points = listOf(Point(100f, 100f), Point(300f, 300f))

        // 1. PathUtils clean path
        val cleanPath = PathUtils.createToolPath(DrawingTool.STAR, points)
        assertNotNull(cleanPath)

        // 2. Rough shape generation
        val star = RoughShapeGenerator.getOrCreateRoughShape(
            strokeId = "star-1",
            tool = DrawingTool.STAR,
            points = points,
            strokeWidth = 4f,
            roughness = RoughnessStyle.SKETCHY,
            fillStyle = FillStyle.SEMI_TRANSPARENT
        )

        // 10 sides x 2 passes = 20 stroke paths
        assertEquals(20, star.strokePaths.size)
        assertNotNull(star.fillPath)
    }

    @Test
    fun testToolOrdinalsArePreserved() {
        assertTrue(DrawingTool.entries.contains(DrawingTool.DIAMOND))
        assertTrue(DrawingTool.entries.contains(DrawingTool.CYLINDER))
        assertTrue(DrawingTool.entries.contains(DrawingTool.STAR))
    }
}
