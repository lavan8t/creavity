package com.kd.creavity.presentation.canvas

import androidx.compose.ui.graphics.Path
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LiveStrokeRegressionTest {
    @Test
    fun `live point growth rebuilds geometry -- red if path frozen`() {
        val cache = StrokeGeometryCache()
        val s1 = Stroke(id = "r1", points = listOf(Point(0f, 0f), Point(10f, 10f)), color = 0L, strokeWidth = 3f, tool = DrawingTool.PEN)
        val p1 = cache.pathFor(s1, isMoving = false) { Path().apply { moveTo(0f,0f); lineTo(10f,10f) } }
        val s2 = s1.copy(points = s1.points + Point(20f, 20f))
        val p2 = cache.pathFor(s2, isMoving = false) { Path().apply { moveTo(0f,0f); lineTo(10f,10f); lineTo(20f,20f) } }
        assertNotEquals("geometry path must change when stroke points grow", System.identityHashCode(p1), System.identityHashCode(p2))
    }
}
