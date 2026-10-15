package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed

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
    val isSecondaryButton: Boolean = false
)

data class CanvasInputEvent(
    val action: CanvasPointerAction,
    val pointers: List<CanvasPointer>,
    val targetIndex: Int = 0,
    val scrollDeltaX: Float = 0f,
    val scrollDeltaY: Float = 0f,
    val isCtrlPressed: Boolean = false,
    val eventTime: Long = System.currentTimeMillis()
) {
    val pointerCount: Int get() = pointers.size
    val activePointer: CanvasPointer
        get() = pointers.getOrElse(targetIndex) { pointers.firstOrNull() ?: CanvasPointer(0L, 0f, 0f) }
    val x: Float get() = activePointer.x
    val y: Float get() = activePointer.y
}

fun PointerEvent.toCanvasInputEvent(): CanvasInputEvent {
    val isAnyPressed = changes.any { it.pressed }
    val action = when (type) {
        PointerEventType.Press -> CanvasPointerAction.DOWN
        PointerEventType.Move -> if (isAnyPressed) CanvasPointerAction.MOVE else CanvasPointerAction.CANCEL
        PointerEventType.Release -> CanvasPointerAction.UP
        PointerEventType.Scroll -> CanvasPointerAction.SCROLL
        else -> CanvasPointerAction.CANCEL
    }
    val scrollDelta = changes.firstOrNull()?.scrollDelta ?: Offset.Zero
    val isCtrl = keyboardModifiers.isCtrlPressed
    val isSecondary = buttons.isSecondaryPressed

    val pointers = changes.map { change ->
        val pointerType = when (change.type) {
            PointerType.Stylus -> CanvasPointerType.STYLUS
            PointerType.Eraser -> CanvasPointerType.ERASER
            PointerType.Mouse -> CanvasPointerType.MOUSE
            else -> CanvasPointerType.TOUCH
        }
        CanvasPointer(
            id = change.id.value,
            x = change.position.x,
            y = change.position.y,
            pressure = change.pressure,
            type = pointerType,
            isPrimaryButton = !isSecondary,
            isSecondaryButton = isSecondary
        )
    }

    return CanvasInputEvent(
        action = action,
        pointers = if (pointers.isEmpty()) listOf(CanvasPointer(0L, 0f, 0f)) else pointers,
        targetIndex = 0,
        scrollDeltaX = scrollDelta.x,
        scrollDeltaY = scrollDelta.y,
        isCtrlPressed = isCtrl,
        eventTime = System.currentTimeMillis()
    )
}
