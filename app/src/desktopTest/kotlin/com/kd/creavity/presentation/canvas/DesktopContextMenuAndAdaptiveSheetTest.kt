package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.util.isDesktopPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopContextMenuAndAdaptiveSheetTest {

    @Test
    fun testIsDesktopPlatformIsTrueOnDesktopJvm() {
        assertTrue("Desktop JVM target must report isDesktopPlatform() == true", isDesktopPlatform())
    }

    @Test
    fun testSecondaryClickWithoutDragTriggersContextMenuOnEmptyCanvas() {
        var contextMenuInvoked = false
        var invokedScreenOffset: Offset? = null
        var invokedWorldPoint: Point? = null
        var invokedHasSelection: Boolean? = null

        val controller = CanvasGestureController(
            onTransform = { _, _, _ -> },
            onTransformStateChanged = { _, _ -> },
            onStrokeCompleted = {},
            onStrokeBroadcastStart = { _, _, _, _, _, _ -> },
            onStrokeBroadcastPoint = { _, _ -> },
            onStrokeBroadcastEnd = {},
            onDeleteStroke = {},
            onDeleteText = {},
            onDeleteImage = {},
            onDeleteStickyNote = {},
            onElementsMoved = { _, _, _ -> },
            onSelectionChanged = { _, _, _ -> },
            onTapForText = {},
            requestInvalidate = {}
        ).apply {
            onContextMenu = { screen, world, hasSel ->
                contextMenuInvoked = true
                invokedScreenOffset = screen
                invokedWorldPoint = world
                invokedHasSelection = hasSel
            }
        }

        // Mouse Down with secondary button (right click)
        val downPointer = CanvasPointer(id = 1, x = 300f, y = 200f, type = CanvasPointerType.MOUSE, isSecondaryButton = true)
        val downEvent = CanvasInputEvent(
            action = CanvasPointerAction.DOWN,
            pointers = listOf(downPointer)
        )
        controller.handleInputEvent(
            event = downEvent,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        // Mouse Up with no movement (< 8px)
        val upPointer = CanvasPointer(id = 1, x = 301f, y = 200f, type = CanvasPointerType.MOUSE, isSecondaryButton = true)
        val upEvent = CanvasInputEvent(
            action = CanvasPointerAction.UP,
            pointers = listOf(upPointer)
        )
        controller.handleInputEvent(
            event = upEvent,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        assertTrue("Context menu must be invoked on secondary button click", contextMenuInvoked)
        assertNotNull(invokedScreenOffset)
        assertEquals(301f, invokedScreenOffset!!.x, 0.1f)
        assertEquals(200f, invokedScreenOffset!!.y, 0.1f)
        assertFalse("On empty canvas, hasSelection should be false", invokedHasSelection ?: true)
    }

    @Test
    fun testSecondaryClickOnStrokeSelectsItAndTriggersContextMenuWithSelectionTrue() {
        var contextMenuInvoked = false
        var hasSelectionResult: Boolean? = null
        var selectionChangedStrokes: List<String>? = null

        val targetStroke = Stroke(
            id = "stroke-target-1",
            points = listOf(Point(100f, 100f), Point(120f, 120f)),
            color = 0xFF000000,
            strokeWidth = 4f
        )

        val controller = CanvasGestureController(
            onTransform = { _, _, _ -> },
            onTransformStateChanged = { _, _ -> },
            onStrokeCompleted = {},
            onStrokeBroadcastStart = { _, _, _, _, _, _ -> },
            onStrokeBroadcastPoint = { _, _ -> },
            onStrokeBroadcastEnd = {},
            onDeleteStroke = {},
            onDeleteText = {},
            onDeleteImage = {},
            onDeleteStickyNote = {},
            onElementsMoved = { _, _, _ -> },
            onSelectionChanged = { strokes, _, _ -> selectionChangedStrokes = strokes },
            onTapForText = {},
            requestInvalidate = {}
        ).apply {
            onContextMenu = { _, _, hasSel ->
                contextMenuInvoked = true
                hasSelectionResult = hasSel
            }
        }

        // Right click at (105, 105) which hits targetStroke
        val downPointer = CanvasPointer(id = 1, x = 105f, y = 105f, type = CanvasPointerType.MOUSE, isSecondaryButton = true)
        val downEvent = CanvasInputEvent(
            action = CanvasPointerAction.DOWN,
            pointers = listOf(downPointer)
        )
        controller.handleInputEvent(
            event = downEvent,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = listOf(targetStroke),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        val upPointer = CanvasPointer(id = 1, x = 105f, y = 105f, type = CanvasPointerType.MOUSE, isSecondaryButton = true)
        val upEvent = CanvasInputEvent(
            action = CanvasPointerAction.UP,
            pointers = listOf(upPointer)
        )
        controller.handleInputEvent(
            event = upEvent,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = listOf(targetStroke),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        assertTrue(contextMenuInvoked)
        assertTrue("hasSelection must be true when right-clicking on an element", hasSelectionResult ?: false)
        assertTrue("Controller selectedStrokeIds must contain target stroke", controller.selectedStrokeIds.contains("stroke-target-1"))
        assertNotNull(selectionChangedStrokes)
        assertTrue(selectionChangedStrokes!!.contains("stroke-target-1"))
    }

    @Test
    fun testSecondaryDragPansCanvasAndDoesNotTriggerContextMenu() {
        var contextMenuInvoked = false
        var panTotal = Offset.Zero

        val controller = CanvasGestureController(
            onTransform = { _, _, pan -> panTotal += pan },
            onTransformStateChanged = { _, _ -> },
            onStrokeCompleted = {},
            onStrokeBroadcastStart = { _, _, _, _, _, _ -> },
            onStrokeBroadcastPoint = { _, _ -> },
            onStrokeBroadcastEnd = {},
            onDeleteStroke = {},
            onDeleteText = {},
            onDeleteImage = {},
            onDeleteStickyNote = {},
            onElementsMoved = { _, _, _ -> },
            onSelectionChanged = { _, _, _ -> },
            onTapForText = {},
            requestInvalidate = {}
        ).apply {
            onContextMenu = { _, _, _ -> contextMenuInvoked = true }
        }

        // Down at (100, 100)
        val downPointer = CanvasPointer(id = 1, x = 100f, y = 100f, type = CanvasPointerType.MOUSE, isSecondaryButton = true)
        controller.handleInputEvent(
            event = CanvasInputEvent(action = CanvasPointerAction.DOWN, pointers = listOf(downPointer)),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        // Move to (150, 120) (dx = 50, dy = 20 > 8px)
        val movePointer = CanvasPointer(id = 1, x = 150f, y = 120f, type = CanvasPointerType.MOUSE, isSecondaryButton = true)
        controller.handleInputEvent(
            event = CanvasInputEvent(action = CanvasPointerAction.MOVE, pointers = listOf(movePointer)),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        // Up at (150, 120)
        controller.handleInputEvent(
            event = CanvasInputEvent(action = CanvasPointerAction.UP, pointers = listOf(movePointer)),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.SELECT,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache()
        )

        assertFalse("Right drag must not invoke context menu", contextMenuInvoked)
        assertEquals(50f, panTotal.x, 0.1f)
        assertEquals(20f, panTotal.y, 0.1f)
    }
}
