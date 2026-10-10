package com.kd.creavity.presentation.canvas

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlin.math.hypot

class DrawingHaptics(private val hapticFeedback: HapticFeedback?) {
    private var lastEventTime = 0L
    private var lastScreenX = 0f
    private var lastScreenY = 0f
    private var smoothedSpeed = 0f
    private var lastTickTime = 0L

    fun onStrokeStart(x: Float, y: Float, time: Long) {
        lastScreenX = x
        lastScreenY = y
        lastEventTime = time
        smoothedSpeed = 0f
        lastTickTime = time
    }

    fun onStrokeMove(x: Float, y: Float, time: Long, enabled: Boolean) {
        if (!enabled || hapticFeedback == null) return

        val dt = (time - lastEventTime).coerceAtLeast(1L)
        val dist = hypot(x - lastScreenX, y - lastScreenY)
        val instSpeed = (dist / dt) * 1000f
        smoothedSpeed = smoothedSpeed * 0.6f + instSpeed * 0.4f
        lastScreenX = x
        lastScreenY = y
        lastEventTime = time

        // Subtle paper-grain haptic ticks scaled by drawing speed
        val timeSinceLastTick = time - lastTickTime
        val tickInterval = (220L - (smoothedSpeed * 0.12f).coerceIn(0f, 170f)).toLong()
        if (smoothedSpeed > 75f && timeSinceLastTick >= tickInterval) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastTickTime = time
        }
    }

    fun performEraserHit() {
        hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun performStylusToggle() {
        hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}
