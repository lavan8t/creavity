package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.presentation.ui.BoardAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardShortcutsTest {

    @Test
    fun testCanvasDuplicateShortcutWithSelection() {
        var actionDispatched: BoardAction? = null
        val handled = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.D,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = false,
            hasSelection = true,
            selectedStrokes = listOf("s1", "s2"),
            selectedTexts = listOf("t1"),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = listOf("s1", "s2"),
            pageTexts = listOf("t1"),
            viewportCenter = Point(500f, 500f),
            onAction = { actionDispatched = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        assertTrue("Ctrl+D must be handled when elements are selected", handled)
        assertTrue("Dispatched action must be DuplicateElements", actionDispatched is BoardAction.DuplicateElements)
        val duplicateAction = actionDispatched as BoardAction.DuplicateElements
        assertEquals(listOf("s1", "s2"), duplicateAction.strokeIds)
        assertEquals(listOf("t1"), duplicateAction.textIds)
    }

    @Test
    fun testCanvasDuplicateWithoutSelectionReturnsFalse() {
        var actionDispatched: BoardAction? = null
        val handled = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.D,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = { actionDispatched = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        assertFalse("Ctrl+D without selection must return false", handled)
        assertEquals(null, actionDispatched)
    }

    @Test
    fun testCanvasReorderShortcutsWithSelection() {
        var frontAction: BoardAction? = null
        var backAction: BoardAction? = null

        val handledFront = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.RightBracket,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = false,
            hasSelection = true,
            selectedStrokes = listOf("s1"),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = listOf("s1"),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = { frontAction = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        val handledBack = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.LeftBracket,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = false,
            hasSelection = true,
            selectedStrokes = listOf("s1"),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = listOf("s1"),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = { backAction = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        assertTrue("Ctrl+] must be handled", handledFront)
        assertTrue("Ctrl+[ must be handled", handledBack)
        assertEquals(BoardAction.ReorderElements(listOf("s1"), bringToFront = true), frontAction)
        assertEquals(BoardAction.ReorderElements(listOf("s1"), bringToFront = false), backAction)
    }

    @Test
    fun testCanvasDeleteAndDeselectShortcuts() {
        var deleteInvoked = false
        var deselectInvoked = false

        val handledDelete = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.Delete,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isShift = false,
            isTyping = false,
            hasSelection = true,
            selectedStrokes = listOf("s1"),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = listOf("s1"),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = {},
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = { deleteInvoked = true },
            onOpenExportDialog = {}
        )

        val handledDeselect = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.Escape,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isShift = false,
            isTyping = false,
            hasSelection = true,
            selectedStrokes = listOf("s1"),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = listOf("s1"),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = {},
            onSelectAll = {},
            onDeselect = { deselectInvoked = true },
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        assertTrue("Del must be handled when selection exists", handledDelete)
        assertTrue("Del callback must be invoked", deleteInvoked)
        assertTrue("Esc must be handled when selection exists", handledDeselect)
        assertTrue("Deselect callback must be invoked", deselectInvoked)
    }

    @Test
    fun testCanvasEscapeWithoutSelectionResetsToPen() {
        var actionDispatched: BoardAction? = null
        val handled = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.Escape,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = { actionDispatched = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        assertTrue(handled)
        assertEquals(BoardAction.SelectTool(DrawingTool.PEN), actionDispatched)
    }

    @Test
    fun testCanvasSelectAllAndExportShortcuts() {
        var selectAllInvoked = false
        var exportInvoked = false

        val handledSelectAll = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.A,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = listOf("s1", "s2"),
            pageTexts = listOf("t1"),
            viewportCenter = Point(500f, 500f),
            onAction = {},
            onSelectAll = { selectAllInvoked = true },
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        val handledExport = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.E,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = {},
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = { exportInvoked = true }
        )

        assertTrue(handledSelectAll)
        assertTrue(selectAllInvoked)
        assertTrue(handledExport)
        assertTrue(exportInvoked)
    }

    @Test
    fun testCanvasStickyNoteShortcutAtCenter() {
        var actionDispatched: BoardAction? = null
        val center = Point(400f, 300f)

        val handled = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.S,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = center,
            onAction = { actionDispatched = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {}
        )

        assertTrue("Key.S without Ctrl must add sticky note", handled)
        assertTrue("Action must be AddStickyNote", actionDispatched is BoardAction.AddStickyNote)
        val note = (actionDispatched as BoardAction.AddStickyNote).note
        assertEquals(center.x - 70f, note.x, 0.1f)
        assertEquals(center.y - 70f, note.y, 0.1f)
    }

    @Test
    fun testCanvasTypingGuardPreventsShortcuts() {
        var actionDispatched: BoardAction? = null
        var exportInvoked = false

        val keysToTest = listOf(Key.P, Key.E, Key.T, Key.R, Key.C, Key.S, Key.Delete, Key.Backspace, Key.Escape)
        for (k in keysToTest) {
            val handled = CanvasHotkeyEngine.handleKeyEvent(
                key = k,
                keyEventType = KeyEventType.KeyDown,
                isCtrlOrMeta = false,
                isShift = false,
                isTyping = true,
                hasSelection = true,
                selectedStrokes = listOf("s1"),
                selectedTexts = listOf("t1"),
                currentZoom = 1.0f,
                panOffset = Offset.Zero,
                pageStrokes = listOf("s1"),
                pageTexts = listOf("t1"),
                viewportCenter = Point(500f, 500f),
                onAction = { actionDispatched = it },
                onSelectAll = {},
                onDeselect = {},
                onDeleteSelection = {},
                onOpenExportDialog = { exportInvoked = true }
            )
            assertFalse("Key $k must NOT be handled when user is typing in text field", handled)
        }

        val handledCtrlE = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.E,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isShift = false,
            isTyping = true,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = { actionDispatched = it },
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = { exportInvoked = true }
        )
        assertFalse("Ctrl+E must NOT trigger export when typing in modal input", handledCtrlE)
        assertEquals(null, actionDispatched)
        assertFalse(exportInvoked)
    }

    @Test
    fun testCanvasSpacebarHoldToPan() {
        var spacePressed = false

        val downHandled = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.Spacebar,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = {},
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {},
            onSpaceStateChanged = { spacePressed = it }
        )

        assertTrue(downHandled)
        assertTrue(spacePressed)

        val upHandled = CanvasHotkeyEngine.handleKeyEvent(
            key = Key.Spacebar,
            keyEventType = KeyEventType.KeyUp,
            isCtrlOrMeta = false,
            isShift = false,
            isTyping = false,
            hasSelection = false,
            selectedStrokes = emptyList(),
            selectedTexts = emptyList(),
            currentZoom = 1.0f,
            panOffset = Offset.Zero,
            pageStrokes = emptyList(),
            pageTexts = emptyList(),
            viewportCenter = Point(500f, 500f),
            onAction = {},
            onSelectAll = {},
            onDeselect = {},
            onDeleteSelection = {},
            onOpenExportDialog = {},
            onSpaceStateChanged = { spacePressed = it }
        )

        assertTrue(upHandled)
        assertFalse(spacePressed)
    }

    @Test
    fun testLibraryShortcutsEngine() {
        var actionDispatched: BoardAction? = null
        var navigatedToCanvas = false
        var editMetadataBoard: Board? = null
        var exportBoard: Board? = null
        var deletedIds: Set<String>? = null
        var selectAllInvoked = false

        val testBoard = Board(id = "board-1", title = "Algorithms")

        // 1. Ctrl+N (New Board)
        val handledNew = LibraryHotkeyEngine.handleKeyEvent(
            key = Key.N,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isTyping = false,
            isSelectionMode = false,
            selectedBoardIds = emptySet(),
            firstBoard = testBoard,
            onAction = { actionDispatched = it },
            onNavigateToCanvas = { navigatedToCanvas = true },
            onEditMetadata = { editMetadataBoard = it },
            onExportBoard = { exportBoard = it },
            onDeleteSelected = { deletedIds = it },
            onSelectAllBoards = { selectAllInvoked = true },
            onCancelSelection = {}
        )
        assertTrue(handledNew)
        assertTrue(actionDispatched is BoardAction.CreateNewBoard)
        assertTrue(navigatedToCanvas)

        // 2. Enter (Open Board)
        navigatedToCanvas = false
        actionDispatched = null
        val handledOpen = LibraryHotkeyEngine.handleKeyEvent(
            key = Key.Enter,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isTyping = false,
            isSelectionMode = false,
            selectedBoardIds = emptySet(),
            firstBoard = testBoard,
            onAction = { actionDispatched = it },
            onNavigateToCanvas = { navigatedToCanvas = true },
            onEditMetadata = { editMetadataBoard = it },
            onExportBoard = { exportBoard = it },
            onDeleteSelected = { deletedIds = it },
            onSelectAllBoards = {},
            onCancelSelection = {}
        )
        assertTrue(handledOpen)
        assertTrue(actionDispatched is BoardAction.SwitchBoard)
        assertTrue(navigatedToCanvas)

        // 3. Ctrl+D (Duplicate Board)
        actionDispatched = null
        val handledDup = LibraryHotkeyEngine.handleKeyEvent(
            key = Key.D,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = true,
            isTyping = false,
            isSelectionMode = true,
            selectedBoardIds = setOf("board-1"),
            firstBoard = testBoard,
            onAction = { actionDispatched = it },
            onNavigateToCanvas = {},
            onEditMetadata = {},
            onExportBoard = {},
            onDeleteSelected = {},
            onSelectAllBoards = {},
            onCancelSelection = {}
        )
        assertTrue(handledDup)
        assertEquals(BoardAction.DuplicateBoard("board-1"), actionDispatched)

        // 4. S (Toggle Star)
        actionDispatched = null
        val handledStar = LibraryHotkeyEngine.handleKeyEvent(
            key = Key.S,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isTyping = false,
            isSelectionMode = true,
            selectedBoardIds = setOf("board-1"),
            firstBoard = testBoard,
            onAction = { actionDispatched = it },
            onNavigateToCanvas = {},
            onEditMetadata = {},
            onExportBoard = {},
            onDeleteSelected = {},
            onSelectAllBoards = {},
            onCancelSelection = {}
        )
        assertTrue(handledStar)
        assertEquals(BoardAction.ToggleStarBoard("board-1"), actionDispatched)

        // 5. F2 (Edit Metadata)
        val handledF2 = LibraryHotkeyEngine.handleKeyEvent(
            key = Key.F2,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isTyping = false,
            isSelectionMode = false,
            selectedBoardIds = emptySet(),
            firstBoard = testBoard,
            onAction = {},
            onNavigateToCanvas = {},
            onEditMetadata = { editMetadataBoard = it },
            onExportBoard = {},
            onDeleteSelected = {},
            onSelectAllBoards = {},
            onCancelSelection = {}
        )
        assertTrue(handledF2)
        assertEquals(testBoard, editMetadataBoard)

        // 6. Delete (Move to Bin)
        val handledDel = LibraryHotkeyEngine.handleKeyEvent(
            key = Key.Delete,
            keyEventType = KeyEventType.KeyDown,
            isCtrlOrMeta = false,
            isTyping = false,
            isSelectionMode = true,
            selectedBoardIds = setOf("board-1"),
            firstBoard = testBoard,
            onAction = {},
            onNavigateToCanvas = {},
            onEditMetadata = {},
            onExportBoard = {},
            onDeleteSelected = { deletedIds = it },
            onSelectAllBoards = {},
            onCancelSelection = {}
        )
        assertTrue(handledDel)
        assertEquals(setOf("board-1"), deletedIds)
    }
}
