package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import com.kd.creavity.domain.model.Point
import kotlin.math.pow

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

    fun calibratePressure(rawPressure: Float): Float {
        if (rawPressure <= 0f) return 1.0f
        val clamped = rawPressure.coerceIn(0f, 1f)
        // Power-law pressure curve: lifts low-force deadzone for light contact sensitivity
        return clamped.toDouble().pow(0.65).toFloat().coerceIn(0.05f, 1.0f)
    }

    fun extractPoint(event: CanvasInputEvent, panOffset: Offset, zoom: Float, pointerIndex: Int = 0): Point {
        val safeIndex = pointerIndex.coerceIn(0, (event.pointers.size - 1).coerceAtLeast(0))
        val p = event.pointers.getOrElse(safeIndex) { event.activePointer }
        val worldX = (p.x - panOffset.x) / zoom
        val worldY = (p.y - panOffset.y) / zoom
        val pressure = calibratePressure(p.pressure)

        return Point(
            x = worldX,
            y = worldY,
            pressure = pressure,
            timestampMs = event.eventTime
        )
    }

    fun extractHistoricalPoints(event: CanvasInputEvent, panOffset: Offset, zoom: Float, pointerIndex: Int = 0): List<Point> {
        val safeIndex = pointerIndex.coerceIn(0, (event.pointers.size - 1).coerceAtLeast(0))
        val p = event.pointers.getOrElse(safeIndex) { event.activePointer }
        if (p.historicalPoints.isEmpty()) return emptyList()

        return p.historicalPoints.map { pt ->
            Point(
                x = (pt.x - panOffset.x) / zoom,
                y = (pt.y - panOffset.y) / zoom,
                pressure = calibratePressure(pt.pressure),
                timestampMs = pt.timestampMs
            )
        }
    }
}
