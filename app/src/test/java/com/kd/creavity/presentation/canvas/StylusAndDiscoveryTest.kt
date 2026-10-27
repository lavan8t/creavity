package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.presentation.viewmodel.WhiteboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusAndDiscoveryTest {

    @Test
    fun testCalibratedPressureBoostsLowForceTouch() {
        val lowForceRaw = 0.1f
        val lowForceCalibrated = StylusInputHandler.calibratePressure(lowForceRaw)
        // With raw ^ 0.65, 0.1^0.65 is ~0.224, elevating light contact
        assertTrue("Calibrated pressure should boost low-force contact", lowForceCalibrated > lowForceRaw)
        assertEquals(0.224f, lowForceCalibrated, 0.01f)

        // Full pressure stays at 1.0
        val maxPressure = StylusInputHandler.calibratePressure(1.0f)
        assertEquals(1.0f, maxPressure, 0.001f)

        // Zero pressure defaults to 1.0f fallback
        val zeroPressure = StylusInputHandler.calibratePressure(0f)
        assertEquals(1.0f, zeroPressure, 0.001f)
    }

    @Test
    fun testExtractHistoricalPointsTransformsCorrectly() {
        val hist1 = Point(x = 100f, y = 200f, pressure = 0.3f, timestampMs = 1000L)
        val hist2 = Point(x = 120f, y = 240f, pressure = 0.4f, timestampMs = 1005L)
        val pointer = CanvasPointer(
            id = 1L,
            x = 140f,
            y = 280f,
            pressure = 0.5f,
            type = CanvasPointerType.STYLUS,
            historicalPoints = listOf(hist1, hist2)
        )
        val event = CanvasInputEvent(
            action = CanvasPointerAction.MOVE,
            pointers = listOf(pointer)
        )

        val pan = Offset(50f, 100f)
        val zoom = 2.0f
        val extracted = StylusInputHandler.extractHistoricalPoints(event, pan, zoom, 0)

        assertEquals(2, extracted.size)
        // hist1 world coord: (100 - 50) / 2 = 25, (200 - 100) / 2 = 50
        assertEquals(25f, extracted[0].x, 0.01f)
        assertEquals(50f, extracted[0].y, 0.01f)
        assertEquals(1000L, extracted[0].timestampMs)

        // hist2 world coord: (120 - 50) / 2 = 35, (240 - 100) / 2 = 70
        assertEquals(35f, extracted[1].x, 0.01f)
        assertEquals(70f, extracted[1].y, 0.01f)
        assertEquals(1005L, extracted[1].timestampMs)
    }

    @Test
    fun testStylusPalmRejectionSuppressesSingleFingerTouch() {
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
            onElementsMoved = { _, _, _ -> },
            onSelectionChanged = { _, _, _ -> },
            onTapForText = {},
            requestInvalidate = {}
        )
        val cache = StrokeGeometryCache()
        val haptics = DrawingHaptics(null)

        // 1. Stylus touch down
        val stylusPointer = CanvasPointer(
            id = 1L,
            x = 100f,
            y = 100f,
            pressure = 0.8f,
            type = CanvasPointerType.STYLUS
        )
        val stylusDown = CanvasInputEvent(
            action = CanvasPointerAction.DOWN,
            pointers = listOf(stylusPointer)
        )
        controller.handleInputEvent(
            event = stylusDown,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = haptics,
            isReadOnly = false,
            geometryCache = cache
        )
        assertEquals(1, controller.activePoints.size)

        // Lift stylus
        val stylusUp = CanvasInputEvent(
            action = CanvasPointerAction.UP,
            pointers = listOf(stylusPointer)
        )
        controller.handleInputEvent(
            event = stylusUp,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = haptics,
            isReadOnly = false,
            geometryCache = cache
        )

        // 2. Immediate accidental palm touch (single finger touch within 600ms of stylus)
        val palmPointer = CanvasPointer(
            id = 2L,
            x = 300f,
            y = 400f,
            type = CanvasPointerType.TOUCH
        )
        val palmDown = CanvasInputEvent(
            action = CanvasPointerAction.DOWN,
            pointers = listOf(palmPointer)
        )
        val handled = controller.handleInputEvent(
            event = palmDown,
            panOffset = Offset.Zero,
            zoom = 1.0f,
            selectedTool = DrawingTool.PEN,
            currentColor = Color.Black,
            currentWidth = 4f,
            strokes = emptyList(),
            textElements = emptyList(),
            imageElements = emptyList(),
            hapticsEnabled = false,
            drawingHaptics = haptics,
            isReadOnly = false,
            geometryCache = cache
        )

        assertTrue("Palm touch should be consumed", handled)
        assertEquals("Palm touch must not create active inking points", 0, controller.activePoints.size)
    }

    @Test
    fun testLastPannedViewRetainedAcrossBoardSwitches() {
        val vm = WhiteboardViewModel()
        val boardA = Board(id = "board_a", title = "Board A")
        val boardB = Board(id = "board_b", title = "Board B")

        vm.switchBoard(boardA)
        // Pan and zoom board A
        vm.setCanvasTransform(zoom = 2.5f, panOffset = Offset(350f, 620f))
        assertEquals(2.5f, vm.uiState.value.zoom, 0.001f)
        assertEquals(Offset(350f, 620f), vm.uiState.value.panOffset)

        // Switch to Board B
        vm.switchBoard(boardB)
        // Board B starts at default or its own viewport
        assertEquals(1.0f, vm.uiState.value.zoom, 0.001f)
        assertEquals(Offset.Zero, vm.uiState.value.panOffset)

        // Switch back to Board A -> Last panned view must be restored!
        vm.switchBoard(boardA)
        assertEquals("Board A zoom must be restored", 2.5f, vm.uiState.value.zoom, 0.001f)
        assertEquals("Board A panOffset must be restored", Offset(350f, 620f), vm.uiState.value.panOffset)
    }

    @Test
    fun testDiscoveryAndAdvertisingMutualExclusivity() {
        val vm = WhiteboardViewModel()

        // Start advertising -> advertising is true, discovering is false
        vm.startAdvertising("TestHost")
        assertTrue(vm.uiState.value.isAdvertising)
        assertFalse(vm.uiState.value.isDiscovering)

        // Start discovery while advertising -> advertising must immediately stop!
        vm.startDiscovery()
        assertTrue(vm.uiState.value.isDiscovering)
        assertFalse(vm.uiState.value.isAdvertising)

        // Start advertising while discovering -> discovery must immediately stop!
        vm.startAdvertising("TestHost")
        assertTrue(vm.uiState.value.isAdvertising)
        assertFalse(vm.uiState.value.isDiscovering)

        // Stop advertising
        vm.stopAdvertising()
        assertFalse(vm.uiState.value.isAdvertising)
        assertFalse(vm.uiState.value.isDiscovering)
    }
}
