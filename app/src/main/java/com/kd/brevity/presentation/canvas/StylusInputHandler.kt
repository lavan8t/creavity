package com.kd.brevity.presentation.canvas

import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import com.kd.brevity.domain.model.Point

object StylusInputHandler {

    fun isStylus(event: MotionEvent): Boolean {
        val toolType = event.getToolType(0)
        return toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
    }

    fun isEraserTip(event: MotionEvent): Boolean {
        return event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER
    }

    fun isStylusButtonPressed(event: MotionEvent): Boolean {
        return (event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) != 0
    }

    fun extractPoint(event: MotionEvent, panOffset: Offset, zoom: Float, pointerIndex: Int = 0): Point {
        val screenX = event.getX(pointerIndex)
        val screenY = event.getY(pointerIndex)
        val worldX = (screenX - panOffset.x) / zoom
        val worldY = (screenY - panOffset.y) / zoom
        val rawPressure = event.getPressure(pointerIndex)
        val pressure = if (rawPressure > 0f) rawPressure.coerceIn(0f, 1f) else 1.0f
        val tilt = event.getAxisValue(MotionEvent.AXIS_TILT, pointerIndex)
        val orientation = event.getAxisValue(MotionEvent.AXIS_ORIENTATION, pointerIndex)

        return Point(
            x = worldX,
            y = worldY,
            pressure = pressure,
            timestampMs = event.eventTime,
            tilt = tilt,
            orientation = orientation
        )
    }

    fun extractHistoricalPoint(event: MotionEvent, histIndex: Int, panOffset: Offset, zoom: Float): Point {
        val histScreenX = event.getHistoricalX(histIndex)
        val histScreenY = event.getHistoricalY(histIndex)
        val histWorldX = (histScreenX - panOffset.x) / zoom
        val histWorldY = (histScreenY - panOffset.y) / zoom
        val rawPressure = event.getHistoricalPressure(histIndex)
        val histPressure = if (rawPressure > 0f) rawPressure.coerceIn(0f, 1f) else 1.0f
        val histTilt = event.getHistoricalAxisValue(MotionEvent.AXIS_TILT, 0, histIndex)
        val histOrientation = event.getHistoricalAxisValue(MotionEvent.AXIS_ORIENTATION, 0, histIndex)

        return Point(
            x = histWorldX,
            y = histWorldY,
            pressure = histPressure,
            timestampMs = event.getHistoricalEventTime(histIndex),
            tilt = histTilt,
            orientation = histOrientation
        )
    }
}
