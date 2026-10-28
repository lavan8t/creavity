package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartGuideEngineTest {

    @Test
    fun testVerticalLeftAlignmentDetection() {
        val target = Rect(left = 102f, top = 200f, right = 202f, bottom = 300f)
        val other = Rect(left = 100f, top = 50f, right = 250f, bottom = 150f)

        // Threshold 8f -> difference is 2f, so it should snap
        val result = SmartGuideEngine.computeAlignmentGuides(
            targetRect = target,
            candidateRects = listOf(other),
            threshold = 8f
        )

        assertEquals(-2f, result.snapOffset.x, 0.01f)
        assertTrue(result.guides.any { it.isVertical && it.position == 100f })
    }

    @Test
    fun testHorizontalCenterAlignmentDetection() {
        // Target center Y = 203f (height 100f)
        val target = Rect(left = 100f, top = 153f, right = 200f, bottom = 253f)
        // Other center Y = 200f (height 60f: top 170f, bottom 230f)
        val other = Rect(left = 400f, top = 170f, right = 500f, bottom = 230f)

        val result = SmartGuideEngine.computeAlignmentGuides(
            targetRect = target,
            candidateRects = listOf(other),
            threshold = 8f
        )

        assertEquals(-3f, result.snapOffset.y, 0.01f)
        assertTrue(result.guides.any { !it.isVertical && it.position == 200f })
    }

    @Test
    fun testThresholdRejection() {
        val target = Rect(left = 120f, top = 200f, right = 220f, bottom = 300f)
        val other = Rect(left = 100f, top = 50f, right = 200f, bottom = 150f)

        // Diff is 20f > 8f threshold
        val result = SmartGuideEngine.computeAlignmentGuides(
            targetRect = target,
            candidateRects = listOf(other),
            threshold = 8f
        )

        assertEquals(0f, result.snapOffset.x, 0.01f)
        assertEquals(0f, result.snapOffset.y, 0.01f)
        assertTrue(result.guides.isEmpty())
    }

    @Test
    fun testEmptyCandidatesReturnsZeroOffset() {
        val target = Rect(10f, 10f, 50f, 50f)
        val result = SmartGuideEngine.computeAlignmentGuides(target, emptyList())
        assertEquals(0f, result.snapOffset.x, 0.01f)
        assertEquals(0f, result.snapOffset.y, 0.01f)
        assertTrue(result.guides.isEmpty())
    }
}
