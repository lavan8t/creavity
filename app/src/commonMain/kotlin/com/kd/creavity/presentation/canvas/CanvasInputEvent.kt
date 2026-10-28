package com.kd.creavity.presentation.canvas

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import com.kd.creavity.domain.model.Point

enum class CanvasPointerAction {
    DOWN,
    MOVE,
    UP,
    CANCEL,
    SCROLL
}

enum class CanvasPointerType {
    TOUCH,
    STYLUS,
    ERASER,
    MOUSE
}

data class CanvasPointer(
    val id: Long,
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val type: CanvasPointerType = CanvasPointerType.TOUCH,
    val isPrimaryButton: Boolean = true,
    val isSecondaryButton: Boolean = false,
    val isTertiaryButton: Boolean = false,
    val historicalPoints: List<Point> = emptyList()
)

data class CanvasInputEvent(
    val action: CanvasPointerAction,
    val pointers: List<CanvasPointer>,
    val targetIndex: Int = 0,
    val scrollDeltaX: Float = 0f,
    val scrollDeltaY: Float = 0f,
    val isCtrlPressed: Boolean = false,
    val isShiftPressed: Boolean = false,
    val isAltPressed: Boolean = false,
    val isMetaPressed: Boolean = false,
    val eventTime: Long = System.currentTimeMillis()
) {
    val pointerCount: Int get() = pointers.size
    val activePointer: CanvasPointer
        get() = pointers.getOrElse(targetIndex) { pointers.firstOrNull() ?: CanvasPointer(0L, 0f, 0f) }
    val x: Float get() = activePointer.x
    val y: Float get() = activePointer.y
}

@OptIn(ExperimentalComposeUiApi::class)
fun PointerEvent.toCanvasInputEvent(): CanvasInputEvent {
    val hasMouse = changes.any { it.type == PointerType.Mouse }
    val isAnyMouseDown = buttons.isPrimaryPressed || buttons.isSecondaryPressed || buttons.isTertiaryPressed
    val isDown = if (hasMouse) isAnyMouseDown else changes.any { it.pressed }

    val action = when (type) {
        PointerEventType.Press -> if (isDown) CanvasPointerAction.DOWN else CanvasPointerAction.CANCEL
        PointerEventType.Move -> if (isDown) CanvasPointerAction.MOVE else CanvasPointerAction.CANCEL
        PointerEventType.Release -> CanvasPointerAction.UP
        PointerEventType.Scroll -> CanvasPointerAction.SCROLL
        else -> CanvasPointerAction.CANCEL
    }
    val scrollDelta = changes.firstOrNull()?.scrollDelta ?: Offset.Zero
    val isCtrl = keyboardModifiers.isCtrlPressed
    val isShift = keyboardModifiers.isShiftPressed
    val isAlt = keyboardModifiers.isAltPressed
    val isMeta = keyboardModifiers.isMetaPressed
    val isSecondary = buttons.isSecondaryPressed
    val isTertiary = buttons.isTertiaryPressed

    val pointers = changes.map { change ->
        val pointerType = when (change.type) {
            PointerType.Stylus -> CanvasPointerType.STYLUS
            PointerType.Eraser -> CanvasPointerType.ERASER
            PointerType.Mouse -> CanvasPointerType.MOUSE
            else -> CanvasPointerType.TOUCH
        }
        val isPrimary = if (change.type == PointerType.Mouse) buttons.isPrimaryPressed else (!isSecondary && !isTertiary)
        val historicalPoints = change.historical.map { h ->
            Point(
                x = h.position.x,
                y = h.position.y,
                pressure = change.pressure,
                timestampMs = h.uptimeMillis
            )
        }
        CanvasPointer(
            id = change.id.value,
            x = change.position.x,
            y = change.position.y,
            pressure = change.pressure,
            type = pointerType,
            isPrimaryButton = isPrimary,
            isSecondaryButton = isSecondary,
            isTertiaryButton = isTertiary,
            historicalPoints = historicalPoints
        )
    }

    return CanvasInputEvent(
        action = action,
        pointers = if (pointers.isEmpty()) listOf(CanvasPointer(0L, 0f, 0f)) else pointers,
        targetIndex = 0,
        scrollDeltaX = scrollDelta.x,
        scrollDeltaY = scrollDelta.y,
        isCtrlPressed = isCtrl,
        isShiftPressed = isShift,
        isAltPressed = isAlt,
        isMetaPressed = isMeta,
        eventTime = System.currentTimeMillis()
    )
}
