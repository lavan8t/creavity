package com.kd.creavity.presentation.canvas

import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import com.kd.creavity.domain.model.Point

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
        val safeIndex = pointerIndex.coerceIn(0, (event.pointerCount - 1).coerceAtLeast(0))
        val screenX = event.getX(safeIndex)
        val screenY = event.getY(safeIndex)
        val worldX = (screenX - panOffset.x) / zoom
        val worldY = (screenY - panOffset.y) / zoom
        val rawPressure = event.getPressure(safeIndex)
        val pressure = if (rawPressure > 0f) rawPressure.coerceIn(0f, 1f) else 1.0f
        val tilt = event.getAxisValue(MotionEvent.AXIS_TILT, safeIndex)
        val orientation = event.getAxisValue(MotionEvent.AXIS_ORIENTATION, safeIndex)

        return Point(
            x = worldX,
            y = worldY,
            pressure = pressure,
            timestampMs = android.os.SystemClock.uptimeMillis(),
            tilt = tilt,
            orientation = orientation
        )
    }

    fun extractHistoricalPoint(
        event: MotionEvent,
        histIndex: Int,
        panOffset: Offset,
        zoom: Float,
        pointerIndex: Int = 0
    ): Point {
        val safeIndex = pointerIndex.coerceIn(0, (event.pointerCount - 1).coerceAtLeast(0))
        val histScreenX = event.getHistoricalX(safeIndex, histIndex)
        val histScreenY = event.getHistoricalY(safeIndex, histIndex)
        val histWorldX = (histScreenX - panOffset.x) / zoom
        val histWorldY = (histScreenY - panOffset.y) / zoom
        val rawPressure = event.getHistoricalPressure(safeIndex, histIndex)
        val histPressure = if (rawPressure > 0f) rawPressure.coerceIn(0f, 1f) else 1.0f
        val histTilt = event.getHistoricalAxisValue(MotionEvent.AXIS_TILT, safeIndex, histIndex)
        val histOrientation = event.getHistoricalAxisValue(MotionEvent.AXIS_ORIENTATION, safeIndex, histIndex)

        return Point(
            x = histWorldX,
            y = histWorldY,
            pressure = histPressure,
            timestampMs = android.os.SystemClock.uptimeMillis(),
            tilt = histTilt,
            orientation = histOrientation
        )
    }
}
