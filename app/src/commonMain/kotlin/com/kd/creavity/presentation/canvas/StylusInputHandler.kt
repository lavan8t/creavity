package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import com.kd.creavity.domain.model.Point

object StylusInputHandler {

    fun isStylus(event: CanvasInputEvent): Boolean {
        val type = event.activePointer.type
        return type == CanvasPointerType.STYLUS || type == CanvasPointerType.ERASER
    }

    fun isEraserTip(event: CanvasInputEvent): Boolean {
        return event.activePointer.type == CanvasPointerType.ERASER
    }

    fun isStylusButtonPressed(event: CanvasInputEvent): Boolean {
        return event.activePointer.isSecondaryButton
    }

    fun extractPoint(event: CanvasInputEvent, panOffset: Offset, zoom: Float, pointerIndex: Int = 0): Point {
        val safeIndex = pointerIndex.coerceIn(0, (event.pointers.size - 1).coerceAtLeast(0))
        val p = event.pointers.getOrElse(safeIndex) { event.activePointer }
        val worldX = (p.x - panOffset.x) / zoom
        val worldY = (p.y - panOffset.y) / zoom
        val rawPressure = p.pressure
        val pressure = if (rawPressure > 0f) rawPressure.coerceIn(0f, 1f) else 1.0f

        return Point(
            x = worldX,
            y = worldY,
            pressure = pressure,
            timestampMs = event.eventTime
        )
    }
}
