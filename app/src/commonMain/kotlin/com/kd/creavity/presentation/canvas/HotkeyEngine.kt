package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.presentation.ui.BoardAction

/**
 * Robust Hotkey Engine for Desktop Canvas Viewport.
 * Maps menu shortcuts (Ctrl+D duplicate, Ctrl+[/] reorder, Del, Esc, Ctrl+A select all,
 * Ctrl+E export, Ctrl+0 reset zoom, tools P/E/T/V/R/C/S, zoom +/-, undo/redo, pan arrows)
 * end-to-end with guards against typing in text fields or dialogs.
 */
object CanvasHotkeyEngine {

    fun handleKeyEvent(
        key: Key,
        keyEventType: KeyEventType,
        isCtrlOrMeta: Boolean,
        isShift: Boolean,
        isTyping: Boolean,
        hasSelection: Boolean,
        selectedStrokes: List<String>,
        selectedTexts: List<String>,
        currentZoom: Float,
        panOffset: Offset,
        pageStrokes: List<String>,
        pageTexts: List<String>,
        viewportCenter: Point,
        onAction: (BoardAction) -> Unit,
        onSelectAll: () -> Unit,
        onDeselect: () -> Unit,
        onDeleteSelection: () -> Unit,
        onOpenExportDialog: () -> Unit,
        onSpaceStateChanged: (Boolean) -> Unit = {}
    ): Boolean {
        // Spacebar hold-to-pan: Spacebar activates pan dragging on mouse primary down without changing tool
        if (key == Key.Spacebar) {
            if (isTyping) return false
            if (keyEventType == KeyEventType.KeyDown) {
                onSpaceStateChanged(true)
                return true
            } else if (keyEventType == KeyEventType.KeyUp) {
                onSpaceStateChanged(false)
                return true
            }
        }

        if (keyEventType != KeyEventType.KeyDown) return false

        // Guard: When actively typing in text fields or modal dialogs, do NOT intercept text editing
        if (isTyping) return false

        if (isCtrlOrMeta) {
            return when (key) {
                // Duplicate selection (Ctrl+D / Cmd+D)
                Key.D -> {
                    if (selectedStrokes.isNotEmpty() || selectedTexts.isNotEmpty()) {
                        onAction(BoardAction.DuplicateElements(selectedStrokes, selectedTexts))
                        true
                    } else {
                        false
                    }
                }
                // Bring to Front (Ctrl+] / Cmd+])
                Key.RightBracket -> {
                    if (selectedStrokes.isNotEmpty()) {
                        onAction(BoardAction.ReorderElements(selectedStrokes, bringToFront = true))
                        true
                    } else {
                        false
                    }
                }
                // Send to Back (Ctrl+[ / Cmd+[)
                Key.LeftBracket -> {
                    if (selectedStrokes.isNotEmpty()) {
                        onAction(BoardAction.ReorderElements(selectedStrokes, bringToFront = false))
                        true
                    } else {
                        false
                    }
                }
                // Select All (Ctrl+A / Cmd+A)
                Key.A -> {
                    onSelectAll()
                    true
                }
                // Export Board (Ctrl+E / Cmd+E)
                Key.E -> {
                    onOpenExportDialog()
                    true
                }
                // Reset Zoom to 100% (Ctrl+0 / Cmd+0)
                Key.Zero, Key.NumPad0 -> {
                    onAction(BoardAction.ResetCanvasTransform)
                    true
                }
                // Zoom In (Ctrl++ / Ctrl+= / Cmd++)
                Key.Equals, Key.Plus, Key.NumPadAdd -> {
                    val newZoom = (currentZoom * 1.25f).coerceAtMost(5.0f)
                    onAction(BoardAction.SetCanvasTransform(newZoom, panOffset))
                    true
                }
                // Zoom Out (Ctrl+- / Cmd+-)
                Key.Minus, Key.NumPadSubtract -> {
                    val newZoom = (currentZoom / 1.25f).coerceAtLeast(0.1f)
                    onAction(BoardAction.SetCanvasTransform(newZoom, panOffset))
                    true
                }
                // Undo / Redo (Ctrl+Z, Ctrl+Shift+Z, Cmd+Z, Cmd+Shift+Z)
                Key.Z -> {
                    if (isShift) {
                        onAction(BoardAction.Redo)
                    } else {
                        onAction(BoardAction.Undo)
                    }
                    true
                }
                // Redo (Ctrl+Y / Cmd+Y)
                Key.Y -> {
                    onAction(BoardAction.Redo)
                    true
                }
                else -> false
            }
        } else {
            return when (key) {
                // Delete selected elements (Del / Backspace)
                Key.Delete, Key.Backspace -> {
                    if (hasSelection || selectedStrokes.isNotEmpty() || selectedTexts.isNotEmpty()) {
                        onDeleteSelection()
                        true
                    } else {
                        false
                    }
                }
                // Deselect or switch back to Pen (Esc)
                Key.Escape -> {
                    if (hasSelection || selectedStrokes.isNotEmpty() || selectedTexts.isNotEmpty()) {
                        onDeselect()
                        true
                    } else {
                        onAction(BoardAction.SelectTool(DrawingTool.PEN))
                        true
                    }
                }
                // Tool switching
                Key.P -> {
                    onAction(BoardAction.SelectTool(DrawingTool.PEN))
                    true
                }
                Key.E -> {
                    onAction(BoardAction.SelectTool(DrawingTool.ERASER))
                    true
                }
                Key.T -> {
                    onAction(BoardAction.SelectTool(DrawingTool.TEXT))
                    true
                }
                Key.V -> {
                    onAction(BoardAction.SelectTool(DrawingTool.SELECT))
                    true
                }
                Key.R -> {
                    onAction(BoardAction.SelectTool(DrawingTool.RECTANGLE))
                    true
                }
                Key.C -> {
                    onAction(BoardAction.SelectTool(DrawingTool.CIRCLE))
                    true
                }
                // S = Add Sticky Note at current viewport center
                Key.S -> {
                    onAction(
                        BoardAction.AddStickyNote(
                            StickyNote(
                                x = viewportCenter.x - 70f,
                                y = viewportCenter.y - 70f,
                                text = "Note",
                                color = 0xFFFFF59D
                            )
                        )
                    )
                    true
                }
                // Keyboard Canvas Panning
                Key.DirectionUp -> {
                    onAction(BoardAction.TransformCanvas(1.0f, Offset.Zero, Offset(0f, 60f)))
                    true
                }
                Key.DirectionDown -> {
                    onAction(BoardAction.TransformCanvas(1.0f, Offset.Zero, Offset(0f, -60f)))
                    true
                }
                Key.DirectionLeft -> {
                    onAction(BoardAction.TransformCanvas(1.0f, Offset.Zero, Offset(60f, 0f)))
                    true
                }
                Key.DirectionRight -> {
                    onAction(BoardAction.TransformCanvas(1.0f, Offset.Zero, Offset(-60f, 0f)))
                    true
                }
                // Direct Zoom Keys (+ / - without Ctrl)
                Key.Equals, Key.Plus, Key.NumPadAdd -> {
                    val newZoom = (currentZoom * 1.20f).coerceAtMost(5.0f)
                    onAction(BoardAction.SetCanvasTransform(newZoom, panOffset))
                    true
                }
                Key.Minus, Key.NumPadSubtract -> {
                    val newZoom = (currentZoom / 1.20f).coerceAtLeast(0.1f)
                    onAction(BoardAction.SetCanvasTransform(newZoom, panOffset))
                    true
                }
                else -> false
            }
        }
    }
}

/**
 * Robust Hotkey Engine for Desktop Boards Library.
 * Maps menu shortcuts (Ctrl+N new board, Enter open, Ctrl+D duplicate,
 * S star, F2 rename, Ctrl+E export, Del move to bin, Ctrl+A select all, Esc cancel selection).
 */
object LibraryHotkeyEngine {

    fun handleKeyEvent(
        key: Key,
        keyEventType: KeyEventType,
        isCtrlOrMeta: Boolean,
        isTyping: Boolean,
        isSelectionMode: Boolean,
        selectedBoardIds: Set<String>,
        firstBoard: Board?,
        onAction: (BoardAction) -> Unit,
        onNavigateToCanvas: () -> Unit,
        onEditMetadata: (Board) -> Unit,
        onExportBoard: (Board) -> Unit,
        onDeleteSelected: (Set<String>) -> Unit,
        onSelectAllBoards: () -> Unit,
        onCancelSelection: () -> Unit
    ): Boolean {
        if (keyEventType != KeyEventType.KeyDown) return false
        if (isTyping) return false

        if (isCtrlOrMeta) {
            return when (key) {
                // New Board (Ctrl+N / Cmd+N)
                Key.N -> {
                    onAction(BoardAction.CreateNewBoard(""))
                    onNavigateToCanvas()
                    true
                }
                // Select All Boards (Ctrl+A / Cmd+A)
                Key.A -> {
                    onSelectAllBoards()
                    true
                }
                // Duplicate Board (Ctrl+D / Cmd+D)
                Key.D -> {
                    val targetIds = if (selectedBoardIds.isNotEmpty()) {
                        selectedBoardIds
                    } else if (firstBoard != null) {
                        setOf(firstBoard.id)
                    } else {
                        emptySet()
                    }
                    if (targetIds.isNotEmpty()) {
                        targetIds.forEach { onAction(BoardAction.DuplicateBoard(it)) }
                        true
                    } else {
                        false
                    }
                }
                // Export Board (Ctrl+E / Cmd+E)
                Key.E -> {
                    val target = if (selectedBoardIds.isNotEmpty()) {
                        firstBoard?.takeIf { it.id in selectedBoardIds } ?: firstBoard
                    } else {
                        firstBoard
                    }
                    if (target != null) {
                        onExportBoard(target)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        } else {
            return when (key) {
                // Open Board (Enter)
                Key.Enter, Key.NumPadEnter -> {
                    if (firstBoard != null) {
                        onAction(BoardAction.SwitchBoard(firstBoard.copy(lastAccessedAt = System.currentTimeMillis())))
                        onNavigateToCanvas()
                        true
                    } else {
                        false
                    }
                }
                // Add to Favourites / Toggle Star (S)
                Key.S -> {
                    val targetIds = if (selectedBoardIds.isNotEmpty()) {
                        selectedBoardIds
                    } else if (firstBoard != null) {
                        setOf(firstBoard.id)
                    } else {
                        emptySet()
                    }
                    if (targetIds.isNotEmpty()) {
                        targetIds.forEach { onAction(BoardAction.ToggleStarBoard(it)) }
                        true
                    } else {
                        false
                    }
                }
                // Rename / Edit Details (F2)
                Key.F2 -> {
                    val target = if (selectedBoardIds.isNotEmpty()) {
                        firstBoard?.takeIf { it.id in selectedBoardIds } ?: firstBoard
                    } else {
                        firstBoard
                    }
                    if (target != null) {
                        onEditMetadata(target)
                        true
                    } else {
                        false
                    }
                }
                // Move to Bin (Del / Backspace)
                Key.Delete, Key.Backspace -> {
                    val targetIds = if (selectedBoardIds.isNotEmpty()) {
                        selectedBoardIds
                    } else if (firstBoard != null) {
                        setOf(firstBoard.id)
                    } else {
                        emptySet()
                    }
                    if (targetIds.isNotEmpty()) {
                        onDeleteSelected(targetIds)
                        true
                    } else {
                        false
                    }
                }
                // Dismiss selection mode (Esc)
                Key.Escape -> {
                    if (isSelectionMode) {
                        onCancelSelection()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }
}
