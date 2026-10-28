package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class AlignmentGuide(
    val isVertical: Boolean,
    val position: Float,
    val start: Float,
    val end: Float
)

data class GuideSnapResult(
    val snapOffset: Offset,
    val guides: List<AlignmentGuide>
)

object SmartGuideEngine {

    fun computeAlignmentGuides(
        targetRect: Rect,
        candidateRects: List<Rect>,
        threshold: Float = 8f
    ): GuideSnapResult {
        if (candidateRects.isEmpty()) {
            return GuideSnapResult(Offset.Zero, emptyList())
        }

        var snapDx = 0f
        var snapDy = 0f
        var minXDiff = threshold + 1f
        var minYDiff = threshold + 1f
        val guides = mutableListOf<AlignmentGuide>()

        val targetLeft = targetRect.left
        val targetRight = targetRect.right
        val targetCenterX = (targetLeft + targetRight) / 2f

        val targetTop = targetRect.top
        val targetBottom = targetRect.bottom
        val targetCenterY = (targetTop + targetBottom) / 2f

        // Check vertical alignments (X axis)
        for (other in candidateRects) {
            val otherLeft = other.left
            val otherRight = other.right
            val otherCenterX = (otherLeft + otherRight) / 2f

            val targetXs = listOf(targetLeft, targetCenterX, targetRight)
            val otherXs = listOf(otherLeft, otherCenterX, otherRight)

            for (tX in targetXs) {
                for (oX in otherXs) {
                    val diff = oX - tX
                    if (abs(diff) <= threshold && abs(diff) < minXDiff) {
                        minXDiff = abs(diff)
                        snapDx = diff
                        guides.removeAll { it.isVertical }
                        val minY = min(targetTop, other.top) - 20f
                        val maxY = max(targetBottom, other.bottom) + 20f
                        guides.add(AlignmentGuide(isVertical = true, position = oX, start = minY, end = maxY))
                    }
                }
            }
        }

        // Check horizontal alignments (Y axis)
        for (other in candidateRects) {
            val otherTop = other.top
            val otherBottom = other.bottom
            val otherCenterY = (otherTop + otherBottom) / 2f

            val targetYs = listOf(targetTop, targetCenterY, targetBottom)
            val otherYs = listOf(otherTop, otherCenterY, otherBottom)

            for (tY in targetYs) {
                for (oY in otherYs) {
                    val diff = oY - tY
                    if (abs(diff) <= threshold && abs(diff) < minYDiff) {
                        minYDiff = abs(diff)
                        snapDy = diff
                        guides.removeAll { !it.isVertical }
                        val minX = min(targetLeft, other.left) - 20f
                        val maxX = max(targetRight, other.right) + 20f
                        guides.add(AlignmentGuide(isVertical = false, position = oY, start = minX, end = maxX))
                    }
                }
            }
        }

        return GuideSnapResult(
            snapOffset = Offset(snapDx, snapDy),
            guides = guides
        )
    }
}
