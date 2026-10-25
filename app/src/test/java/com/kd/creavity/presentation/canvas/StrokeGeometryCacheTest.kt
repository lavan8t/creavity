package com.kd.creavity.presentation.canvas

import androidx.compose.ui.graphics.Path
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeGeometryCacheTest {

    private val cache = StrokeGeometryCache()
    private var builds = 0

    private fun pathFor(stroke: Stroke, isMoving: Boolean = false): Path =
        cache.pathFor(stroke, isMoving) {
            builds++
            Path()
        }

    private fun stroke(vararg coords: Pair<Float, Float>) = Stroke(
        id = "stroke-1",
        points = coords.mapIndexed { i, (x, y) -> Point(x, y, 1f, i.toLong()) },
        color = 0xFF000000L,
        strokeWidth = 6f,
        tool = DrawingTool.PEN
    )

    @Test
    fun `appending a point rebuilds the geometry`() {
        pathFor(stroke(10f to 10f))
        pathFor(stroke(10f to 10f, 20f to 20f))
        pathFor(stroke(10f to 10f, 20f to 20f, 30f to 30f))

        assertEquals("each point changes the drawn shape", 3, builds)
        assertEquals("superseded geometry is evicted, not accumulated", 1, cache.pathCount())
    }

    @Test
    fun `translating a stroke without changing its point count rebuilds the geometry`() {
        pathFor(stroke(10f to 10f, 20f to 20f))
        pathFor(stroke(110f to 110f, 120f to 120f), isMoving = true)
        pathFor(stroke(210f to 210f, 220f to 220f), isMoving = true)

        assertEquals("a drag moves the shape under a constant point count", 3, builds)
    }

    @Test
    fun `repeat frames of an unchanged stroke reuse the cached geometry`() {
        val settled = stroke(10f to 10f, 20f to 20f)
        repeat(5) { pathFor(settled) }

        assertEquals("caching must still pay for itself", 1, builds)
    }

    @Test
    fun `removing a stroke drops its cached geometry`() {
        pathFor(stroke(10f to 10f, 20f to 20f))
        cache.removeStroke("stroke-1")

        assertEquals(0, cache.pathCount())
        assertEquals(0, cache.boundsCount())
    }
}
