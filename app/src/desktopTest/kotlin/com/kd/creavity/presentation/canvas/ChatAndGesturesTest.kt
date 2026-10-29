package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatAndGesturesTest {

    private fun createTestController(onTransform: (Float, Offset, Offset) -> Unit): CanvasGestureController {
        return CanvasGestureController(
            onTransform = onTransform,
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
        )
    }

    @Test
    fun testChatMessagePacketSerializationRoundtrip() {
        val originalPacket = WhiteboardPacket.ChatMessage(
            messageId = "chat-msg-12345",
            senderId = "peer-node-abc",
            senderName = "Prof. Turing",
            text = "Welcome to the collaborative session! Let's solve equation 4.",
            timestampMs = 1700000000000L
        )

        val bytes = PacketSerializer.serialize(originalPacket)
        assertNotNull("Serialized bytes should not be null", bytes)
        assertTrue("Serialized packet must not be empty", bytes.isNotEmpty())

        val deserialized = PacketSerializer.deserialize(bytes)
        assertNotNull("Deserialized packet should not be null", deserialized)
        assertTrue("Deserialized packet must be ChatMessage", deserialized is WhiteboardPacket.ChatMessage)

        val chat = deserialized as WhiteboardPacket.ChatMessage
        assertEquals(originalPacket.messageId, chat.messageId)
        assertEquals(originalPacket.senderId, chat.senderId)
        assertEquals(originalPacket.senderName, chat.senderName)
        assertEquals(originalPacket.text, chat.text)
        assertEquals(originalPacket.timestampMs, chat.timestampMs)
    }

    @Test
    fun testCanvasGestureScrollHorizontalPanWithShift() {
        var transformedZoom = 1.0f
        var transformedPan = Offset.Zero
        val controller = createTestController { zoom, _, pan ->
            transformedZoom *= zoom
            transformedPan += pan
        }

        val scrollWithShift = CanvasInputEvent(
            action = CanvasPointerAction.SCROLL,
            pointers = listOf(CanvasPointer(id = 1L, x = 200f, y = 300f, type = CanvasPointerType.MOUSE)),
            scrollDeltaX = 0f,
            scrollDeltaY = 2f,
            isShiftPressed = true
        )

        val handled = controller.handleInputEvent(
            event = scrollWithShift,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        assertTrue("Scroll event must be handled", handled)
        assertEquals("Zoom must not change when scrolling with Shift", 1.0f, transformedZoom, 0.001f)
        assertEquals("Horizontal pan must be -2 * 48 = -96", -96f, transformedPan.x, 0.001f)
        assertEquals("Vertical pan must be 0", 0f, transformedPan.y, 0.001f)
    }

    @Test
    fun testCanvasGestureScrollZoomWithCtrl() {
        var transformedZoom = 1.0f
        var focalPoint = Offset.Zero
        val controller = createTestController { zoom, focal, _ ->
            transformedZoom *= zoom
            focalPoint = focal
        }

        val zoomInEvent = CanvasInputEvent(
            action = CanvasPointerAction.SCROLL,
            pointers = listOf(CanvasPointer(id = 1L, x = 400f, y = 300f, type = CanvasPointerType.MOUSE)),
            scrollDeltaX = 0f,
            scrollDeltaY = -1f,
            isCtrlPressed = true
        )

        val handled = controller.handleInputEvent(
            event = zoomInEvent,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        assertTrue(handled)
        assertTrue("Zoom should increase when scrolling up (-1f) with Ctrl", transformedZoom > 1.0f)
        assertEquals(400f, focalPoint.x, 0.001f)
        assertEquals(300f, focalPoint.y, 0.001f)
    }

    @Test
    fun testMiddleMouseButtonPan() {
        var totalPan = Offset.Zero
        val controller = createTestController { _, _, pan ->
            totalPan += pan
        }

        // Middle mouse press at (100, 100)
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 100f, y = 100f, isPrimaryButton = false, isTertiaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        // Middle mouse drag to (140, 170) -> dx = +40, dy = +70
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.MOVE,
                pointers = listOf(CanvasPointer(id = 1L, x = 140f, y = 170f, isPrimaryButton = false, isTertiaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        assertEquals("Middle-click pan dx must be 40", 40f, totalPan.x, 0.001f)
        assertEquals("Middle-click pan dy must be 70", 70f, totalPan.y, 0.001f)
    }

    @Test
    fun testSelectToolUsesCursorPointerIcon() {
        val icon = getToolPointerIcon(DrawingTool.SELECT)
        assertNotNull("Select tool pointer icon must not be null", icon)
    }

    @Test
    fun testCanvasGestureScrollHorizontalPanWithShiftOnDesktopHScroll() {
        var transformedZoom = 1.0f
        var transformedPan = Offset.Zero
        val controller = createTestController { zoom, _, pan ->
            transformedZoom *= zoom
            transformedPan += pan
        }

        // Compose Desktop maps Shift + Mouse Wheel to scrollDeltaX with scrollDeltaY = 0
        val desktopShiftScroll = CanvasInputEvent(
            action = CanvasPointerAction.SCROLL,
            pointers = listOf(CanvasPointer(id = 1L, x = 200f, y = 300f, type = CanvasPointerType.MOUSE)),
            scrollDeltaX = 3f,
            scrollDeltaY = 0f,
            isShiftPressed = true
        )

        val handled = controller.handleInputEvent(
            event = desktopShiftScroll,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        assertTrue("Scroll event must be handled", handled)
        assertEquals("Zoom must not change", 1.0f, transformedZoom, 0.001f)
        assertEquals("Horizontal pan must be -3 * 48 = -144", -144f, transformedPan.x, 0.001f)
        assertEquals("Vertical pan must be 0", 0f, transformedPan.y, 0.001f)
    }

    @Test
    fun testAltKeyLeftClickDragPan() {
        var totalPan = Offset.Zero
        val controller = createTestController { _, _, pan ->
            totalPan += pan
        }

        // Alt + Left click down at (200, 200)
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 200f, y = 200f, isPrimaryButton = true, type = CanvasPointerType.MOUSE)),
                isAltPressed = true
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        // Drag to (250, 180) -> dx = +50, dy = -20
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.MOVE,
                pointers = listOf(CanvasPointer(id = 1L, x = 250f, y = 180f, isPrimaryButton = true, type = CanvasPointerType.MOUSE)),
                isAltPressed = true
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        assertEquals("Alt+Left click pan dx must be 50", 50f, totalPan.x, 0.001f)
        assertEquals("Alt+Left click pan dy must be -20", -20f, totalPan.y, 0.001f)
    }

    @Test
    fun testSpacebarPanWithPenTool() {
        var totalPan = Offset.Zero
        val controller = createTestController { _, _, pan ->
            totalPan += pan
        }

        // Primary button down at (100, 100) with PEN tool and isSpacePressed = true
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 100f, y = 100f, isPrimaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache(),
            isSpacePressed = true
        )

        // Drag to (180, 140) -> dx = +80, dy = +40
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.MOVE,
                pointers = listOf(CanvasPointer(id = 1L, x = 180f, y = 140f, isPrimaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = DrawingHaptics(null),
            isReadOnly = false,
            geometryCache = StrokeGeometryCache(),
            isSpacePressed = true
        )

        assertEquals("Spacebar drag dx must be 80", 80f, totalPan.x, 0.001f)
        assertEquals("Spacebar drag dy must be 40", 40f, totalPan.y, 0.001f)
    }

    @Test
    fun testPanToolDragTriggersCanvasTransform() {
        var totalPan = Offset.Zero
        val controller = createTestController { _, _, pan ->
            totalPan += pan
        }

        // Primary button down at (100, 100) with DrawingTool.PAN
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 100f, y = 100f, isPrimaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PAN,
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

        // Drag to (180, 140) -> dx = +80, dy = +40
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.MOVE,
                pointers = listOf(CanvasPointer(id = 1L, x = 180f, y = 140f, isPrimaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PAN,
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

        assertEquals("Pan tool drag dx must be 80", 80f, totalPan.x, 0.001f)
        assertEquals("Pan tool drag dy must be 40", 40f, totalPan.y, 0.001f)
    }

    @Test
    fun testMiddleMouseButtonDragAndReleaseCleanEnd() {
        var totalPan = Offset.Zero
        var isTransforming = false
        val controller = CanvasGestureController(
            onTransform = { _, _, pan -> totalPan += pan },
            onTransformStateChanged = { transforming, _ -> isTransforming = transforming },
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
        )

        // 1. Middle mouse down
        val downHandled = controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 100f, y = 100f, isPrimaryButton = false, isTertiaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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
        assertTrue(downHandled)
        assertTrue("Transforming state should be true during drag", isTransforming)

        // 2. Middle mouse move
        val moveHandled = controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.MOVE,
                pointers = listOf(CanvasPointer(id = 1L, x = 150f, y = 130f, isPrimaryButton = false, isTertiaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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
        assertTrue(moveHandled)
        assertEquals(50f, totalPan.x, 0.001f)
        assertEquals(30f, totalPan.y, 0.001f)

        // 3. Middle mouse release (UP): In Compose Desktop, buttons.isTertiaryPressed is FALSE on release
        val upHandled = controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.UP,
                pointers = listOf(CanvasPointer(id = 1L, x = 150f, y = 130f, isPrimaryButton = false, isTertiaryButton = false, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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
        assertTrue("UP event for middle mouse drag should be handled by gesture controller", upHandled)
        assertTrue("Transforming state should be reset to false on release", !isTransforming)
    }

    @Test
    fun testRightMouseButtonClickOpensContextMenuOnRelease() {
        var contextMenuOpened = false
        var menuScreenOffset = Offset.Zero
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
            onContextMenu = { screenOffset, _, _ ->
                contextMenuOpened = true
                menuScreenOffset = screenOffset
            }
        }

        // 1. Right click down at (200, 300)
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 200f, y = 300f, isPrimaryButton = false, isSecondaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        // 2. Right click release at (200, 300) without movement (isSecondaryButton is FALSE on release)
        val handled = controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.UP,
                pointers = listOf(CanvasPointer(id = 1L, x = 200f, y = 300f, isPrimaryButton = false, isSecondaryButton = false, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        assertTrue("Right click UP should be handled", handled)
        assertTrue("Context menu should open on right click release", contextMenuOpened)
        assertEquals(200f, menuScreenOffset.x, 0.001f)
        assertEquals(300f, menuScreenOffset.y, 0.001f)
    }

    @Test
    fun testRightMouseButtonDragPanAndRelease() {
        var totalPan = Offset.Zero
        var isTransforming = false
        var contextMenuOpened = false
        val controller = CanvasGestureController(
            onTransform = { _, _, pan -> totalPan += pan },
            onTransformStateChanged = { transforming, _ -> isTransforming = transforming },
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
            onContextMenu = { _, _, _ -> contextMenuOpened = true }
        }

        // 1. Right click down at (100, 100)
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.DOWN,
                pointers = listOf(CanvasPointer(id = 1L, x = 100f, y = 100f, isPrimaryButton = false, isSecondaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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

        // 2. Right click drag to (170, 140) -> dist > 8f -> Pan by dx=70, dy=40
        controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.MOVE,
                pointers = listOf(CanvasPointer(id = 1L, x = 170f, y = 140f, isPrimaryButton = false, isSecondaryButton = true, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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
        assertEquals(70f, totalPan.x, 0.001f)
        assertEquals(40f, totalPan.y, 0.001f)
        assertTrue("Transforming state should be true during drag", isTransforming)

        // 3. Right click release: isSecondaryButton is false on release in Compose Desktop
        val handled = controller.handleInputEvent(
            event = CanvasInputEvent(
                action = CanvasPointerAction.UP,
                pointers = listOf(CanvasPointer(id = 1L, x = 170f, y = 140f, isPrimaryButton = false, isSecondaryButton = false, type = CanvasPointerType.MOUSE))
            ),
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
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
        assertTrue("UP event for right mouse drag should be handled", handled)
        assertTrue("Context menu must NOT open when dragging right click", !contextMenuOpened)
        assertTrue("Transforming state must reset to false on release", !isTransforming)
    }
}
