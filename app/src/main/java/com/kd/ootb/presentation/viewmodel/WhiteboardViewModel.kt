package com.kd.ootb.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kd.ootb.data.binary.WhiteboardPacket
import com.kd.ootb.data.nearby.NearbyConnectionsManager
import com.kd.ootb.data.storage.AppSettingsManager
import com.kd.ootb.data.storage.BoardManager
import com.kd.ootb.domain.model.AppThemePreset
import com.kd.ootb.domain.model.Board
import com.kd.ootb.domain.model.BoardPage
import com.kd.ootb.domain.model.BoardVersionSnapshot
import com.kd.ootb.domain.model.CanvasModification
import com.kd.ootb.domain.model.DrawingTool
import com.kd.ootb.domain.model.ImageElement
import com.kd.ootb.domain.model.ModificationType
import com.kd.ootb.domain.model.PeerDevice
import com.kd.ootb.domain.model.PeerPresence
import com.kd.ootb.domain.model.Point
import com.kd.ootb.domain.model.Stroke
import com.kd.ootb.domain.model.TextElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class WhiteboardUiState(
    val currentBoard: Board = Board(),
    val savedBoards: List<Board> = emptyList(),
    val currentColor: Color = Color.White,
    val backgroundColor: Color = Color(0xFF18181C),
    val currentWidth: Float = 6.0f,
    val selectedTool: DrawingTool = DrawingTool.PEN,
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val roomCode: String? = null,
    val displayName: String = "Whiteboard User",
    val hapticsEnabled: Boolean = true,
    val isAdvertising: Boolean = false,
    val isDiscovering: Boolean = false,
    val connectedPeers: List<PeerDevice> = emptyList(),
    val discoveredPeers: List<PeerDevice> = emptyList(),
    val selectedThemePreset: AppThemePreset = AppThemePreset.STUDIO_AMBER,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val livePeerPresences: Map<String, PeerPresence> = emptyMap(),
    val versionHistory: List<BoardVersionSnapshot> = emptyList(),
    val myAccessReadOnly: Boolean = false
) {
    val isCanvasReadOnly: Boolean
        get() = currentBoard.isReadOnly || myAccessReadOnly

    val activePage: BoardPage
        get() = currentBoard.pages.getOrElse(currentBoard.activePageIndex) { BoardPage() }

    val strokes: List<Stroke>
        get() = activePage.strokes

    val textElements: List<TextElement>
        get() = activePage.textElements

    val imageElements: List<ImageElement>
        get() = activePage.imageElements
}

class WhiteboardViewModel(application: Application) : AndroidViewModel(application) {
    val nearbyManager = NearbyConnectionsManager(application)
    private val boardManager = BoardManager(application)
    private val appSettings = AppSettingsManager(application)

    private val _uiState = MutableStateFlow(
        WhiteboardUiState(
            displayName = appSettings.displayName,
            hapticsEnabled = appSettings.hapticsEnabled,
            currentWidth = appSettings.defaultStrokeWidth,
            selectedThemePreset = appSettings.themePreset
        )
    )
    val uiState: StateFlow<WhiteboardUiState> = _uiState.asStateFlow()

    private val remoteStrokesMap = mutableMapOf<String, Stroke>()

    init {
        loadSavedBoards()

        nearbyManager.isAdvertising
            .onEach { isAdv ->
                _uiState.update { state ->
                    val code = if (isAdv) state.roomCode ?: generateRoomCode() else if (state.connectedPeers.isEmpty()) null else state.roomCode
                    state.copy(isAdvertising = isAdv, roomCode = code)
                }
            }
            .launchIn(viewModelScope)

        nearbyManager.isDiscovering
            .onEach { isDisc -> _uiState.update { it.copy(isDiscovering = isDisc) } }
            .launchIn(viewModelScope)

        nearbyManager.connectedPeers
            .onEach { peers ->
                _uiState.update { state ->
                    val distinctConnected = peers.distinctBy { it.endpointId }
                    val connectedIds = distinctConnected.map { it.endpointId }.toSet()
                    val code = if (distinctConnected.isNotEmpty()) state.roomCode ?: generateRoomCode() else if (state.isAdvertising) state.roomCode else null
                    state.copy(
                        connectedPeers = distinctConnected,
                        discoveredPeers = state.discoveredPeers.filter { it.endpointId !in connectedIds },
                        roomCode = code
                    )
                }
            }
            .launchIn(viewModelScope)

        nearbyManager.discoveredEndpoints
            .onEach { peers ->
                _uiState.update { state ->
                    val connectedIds = state.connectedPeers.map { it.endpointId }.toSet()
                    val filteredDiscovered = peers.filter { it.endpointId !in connectedIds }.distinctBy { it.endpointId }
                    state.copy(discoveredPeers = filteredDiscovered)
                }
            }
            .launchIn(viewModelScope)

        nearbyManager.incomingPackets
            .onEach { packet -> handleIncomingPacket(packet) }
            .launchIn(viewModelScope)
    }

    private fun generateRoomCode(): String {
        return (1..5).map { ('A'..'Z').random() }.joinToString("")
    }

    private fun isBoardEmptyUntitled(board: Board): Boolean {
        val isUntitled = board.title.isBlank() || board.title == "Untitled Board" || board.title.startsWith("Untitled Board") || board.title == "My Board"
        val hasNoContent = board.pages.all { it.strokes.isEmpty() && it.textElements.isEmpty() }
        return isUntitled && hasNoContent
    }

    private fun loadSavedBoards() {
        boardManager.cleanEmptyUntitledBoards()
        val boards = boardManager.loadAllBoards()
        if (boards.isNotEmpty()) {
            val initial = boards.first()
            _uiState.update {
                it.copy(
                    currentBoard = initial,
                    savedBoards = boards,
                    backgroundColor = Color(initial.backgroundColor)
                )
            }
        } else {
            val defaultBoard = Board(
                title = "My Infinite Board",
                createdBy = _uiState.value.displayName,
                ownerName = _uiState.value.displayName
            )
            boardManager.saveBoard(defaultBoard)
            _uiState.update {
                it.copy(
                    currentBoard = defaultBoard,
                    savedBoards = listOf(defaultBoard),
                    backgroundColor = Color(defaultBoard.backgroundColor)
                )
            }
        }
    }

    fun updateDisplayName(name: String) {
        val safeName = name.ifBlank { "Whiteboard User" }
        appSettings.displayName = safeName
        _uiState.update { it.copy(displayName = safeName) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        appSettings.hapticsEnabled = enabled
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun setAppThemePreset(preset: AppThemePreset) {
        appSettings.themePreset = preset
        _uiState.update { it.copy(selectedThemePreset = preset) }
    }

    fun onTransform(zoomChange: Float, panChange: Offset) {
        _uiState.update { state ->
            val newZoom = (state.zoom * zoomChange).coerceIn(0.2f, 5.0f)
            val newPan = state.panOffset + panChange
            state.copy(zoom = newZoom, panOffset = newPan)
        }
    }

    fun setCanvasTransform(zoom: Float, panOffset: Offset) {
        _uiState.update { it.copy(zoom = zoom.coerceIn(0.2f, 5.0f), panOffset = panOffset) }
    }

    fun resetCanvasTransform() {
        _uiState.update { it.copy(zoom = 1.0f, panOffset = Offset.Zero) }
    }

    fun createNewBoard(title: String = "Untitled Board") {
        val current = _uiState.value.currentBoard
        if (isBoardEmptyUntitled(current)) {
            boardManager.deleteBoard(current.id)
        } else {
            saveCurrentBoard()
        }
        boardManager.cleanEmptyUntitledBoards()

        val newBoard = Board(
            title = if (title.isBlank()) "Untitled Board" else title,
            backgroundColor = _uiState.value.backgroundColor.toArgb().toLong(),
            createdBy = _uiState.value.displayName,
            ownerName = _uiState.value.displayName
        )
        boardManager.saveBoard(newBoard)
        val all = boardManager.loadAllBoards()
        _uiState.update {
            it.copy(
                currentBoard = newBoard,
                savedBoards = all,
                zoom = 1.0f,
                panOffset = Offset.Zero
            )
        }
    }

    fun switchBoard(board: Board) {
        val current = _uiState.value.currentBoard
        if (current.id != board.id) {
            if (isBoardEmptyUntitled(current)) {
                boardManager.deleteBoard(current.id)
            } else {
                saveCurrentBoard()
            }
            boardManager.cleanEmptyUntitledBoards(excludeBoardId = board.id)
        }

        val updatedTarget = board.copy(modifiedAt = System.currentTimeMillis())
        boardManager.saveBoard(updatedTarget)
        val all = boardManager.loadAllBoards()

        _uiState.update {
            it.copy(
                currentBoard = updatedTarget,
                savedBoards = all,
                backgroundColor = Color(updatedTarget.backgroundColor),
                zoom = 1.0f,
                panOffset = Offset.Zero
            )
        }

        if (_uiState.value.connectedPeers.isNotEmpty()) {
            val activeP = updatedTarget.pages.getOrElse(updatedTarget.activePageIndex) { BoardPage() }
            nearbyManager.sendPacketToAll(
                WhiteboardPacket.BoardSync(
                    boardId = updatedTarget.id,
                    title = updatedTarget.title,
                    backgroundColor = updatedTarget.backgroundColor,
                    isReadOnly = updatedTarget.isReadOnly,
                    strokes = activeP.strokes,
                    textElements = activeP.textElements
                )
            )
        }
    }

    fun setPeerPermission(endpointId: String, isReadOnly: Boolean) {
        _uiState.update { state ->
            val updated = state.connectedPeers.map { peer ->
                if (peer.endpointId == endpointId) peer.copy(isReadOnly = isReadOnly) else peer
            }
            state.copy(connectedPeers = updated)
        }
        nearbyManager.sendPacketToAll(WhiteboardPacket.PermissionUpdate(endpointId, isReadOnly))
    }

    fun renameBoard(boardId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        val current = _uiState.value.currentBoard
        val updatedBoard = if (current.id == boardId) current.copy(title = newTitle) else {
            _uiState.value.savedBoards.find { it.id == boardId }?.copy(title = newTitle) ?: return
        }
        boardManager.saveBoard(updatedBoard)
        val all = boardManager.loadAllBoards()
        _uiState.update {
            it.copy(
                currentBoard = if (it.currentBoard.id == boardId) updatedBoard else it.currentBoard,
                savedBoards = all
            )
        }
    }

    fun toggleReadOnlyBoard(boardId: String, isReadOnly: Boolean) {
        val current = _uiState.value.currentBoard
        val updatedBoard = if (current.id == boardId) current.copy(isReadOnly = isReadOnly, modifiedAt = System.currentTimeMillis()) else {
            _uiState.value.savedBoards.find { it.id == boardId }?.copy(isReadOnly = isReadOnly, modifiedAt = System.currentTimeMillis()) ?: return
        }
        boardManager.saveBoard(updatedBoard)
        val all = boardManager.loadAllBoards()
        _uiState.update {
            it.copy(
                currentBoard = if (it.currentBoard.id == boardId) updatedBoard else it.currentBoard,
                savedBoards = all
            )
        }
    }

    fun duplicateBoard(boardId: String) {
        val target = _uiState.value.savedBoards.find { it.id == boardId }
            ?: if (_uiState.value.currentBoard.id == boardId) _uiState.value.currentBoard else null
            ?: return
        val duplicate = target.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${target.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        boardManager.saveBoard(duplicate)
        val all = boardManager.loadAllBoards()
        _uiState.update { it.copy(savedBoards = all) }
    }

    fun deleteBoard(boardId: String) {
        boardManager.deleteBoard(boardId)
        val remaining = boardManager.loadAllBoards()
        if (remaining.isNotEmpty()) {
            switchBoard(remaining.first())
        } else {
            createNewBoard("My Board")
        }
    }

    fun addNewPage() {
        val board = _uiState.value.currentBoard
        val newPageNumber = board.pages.size + 1
        val newPage = BoardPage(name = "Page $newPageNumber")
        val updatedPages = board.pages + newPage
        val updatedBoard = board.copy(
            pages = updatedPages,
            activePageIndex = updatedPages.size - 1,
            modifiedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)
    }

    fun switchPage(pageIndex: Int) {
        val board = _uiState.value.currentBoard
        if (pageIndex in board.pages.indices) {
            val updatedBoard = board.copy(activePageIndex = pageIndex)
            _uiState.update { it.copy(currentBoard = updatedBoard) }
            boardManager.saveBoard(updatedBoard)
        }
    }

    fun deletePage(pageIndex: Int) {
        val board = _uiState.value.currentBoard
        if (board.pages.size <= 1) {
            clearCanvas()
            return
        }
        val updatedPages = board.pages.filterIndexed { index, _ -> index != pageIndex }
        val newIndex = (if (pageIndex >= updatedPages.size) updatedPages.size - 1 else pageIndex).coerceAtLeast(0)
        val updatedBoard = board.copy(pages = updatedPages, activePageIndex = newIndex)
        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)
    }

    fun saveCurrentBoard() {
        boardManager.saveBoard(_uiState.value.currentBoard)
        _uiState.update { it.copy(savedBoards = boardManager.loadAllBoards()) }
    }

    private val undoStack = mutableListOf<CanvasModification>()
    private val redoStack = mutableListOf<CanvasModification>()

    private fun recordModification(mod: CanvasModification) {
        val board = _uiState.value.currentBoard
        val updatedLog = board.modificationLog + mod
        val updatedBoard = board.copy(modificationLog = updatedLog)
        undoStack.add(mod)
        redoStack.clear()
        _uiState.update {
            it.copy(
                currentBoard = updatedBoard,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val mod = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(mod)

        val state = _uiState.value
        val activePage = state.activePage

        val (updatedStrokes, updatedTexts) = when (mod.type) {
            ModificationType.ADD_STROKE -> {
                Pair(activePage.strokes.filterNot { it.id == mod.stroke?.id }, activePage.textElements)
            }
            ModificationType.DELETE_STROKE -> {
                val restored = if (mod.stroke != null && !activePage.strokes.any { it.id == mod.stroke.id }) activePage.strokes + mod.stroke else activePage.strokes
                Pair(restored, activePage.textElements)
            }
            ModificationType.ADD_TEXT -> {
                Pair(activePage.strokes, activePage.textElements.filterNot { it.id == mod.textElement?.id })
            }
            ModificationType.DELETE_TEXT -> {
                val restored = if (mod.textElement != null && !activePage.textElements.any { it.id == mod.textElement.id }) activePage.textElements + mod.textElement else activePage.textElements
                Pair(activePage.strokes, restored)
            }
            ModificationType.MOVE_ELEMENTS, ModificationType.RECOLOR_ELEMENTS -> {
                val strokeMap = mod.previousStrokes.associateBy { it.id }
                val textMap = mod.previousTexts.associateBy { it.id }
                val restoredStrokes = activePage.strokes.map { strokeMap[it.id] ?: it }
                val restoredTexts = activePage.textElements.map { textMap[it.id] ?: it }
                Pair(restoredStrokes, restoredTexts)
            }
            ModificationType.CLEAR_PAGE, ModificationType.RESTORE_VERSION -> {
                Pair(mod.previousStrokes, mod.previousTexts)
            }
            ModificationType.ADD_IMAGE, ModificationType.DELETE_IMAGE -> {
                Pair(activePage.strokes, activePage.textElements)
            }
        }

        val updatedPage = activePage.copy(strokes = updatedStrokes, textElements = updatedTexts)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())
        _uiState.update {
            it.copy(
                currentBoard = updatedBoard,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        boardManager.saveBoard(updatedBoard)
        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val mod = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(mod)

        val state = _uiState.value
        val activePage = state.activePage

        val (updatedStrokes, updatedTexts) = when (mod.type) {
            ModificationType.ADD_STROKE -> {
                val restored = if (mod.stroke != null && !activePage.strokes.any { it.id == mod.stroke.id }) activePage.strokes + mod.stroke else activePage.strokes
                Pair(restored, activePage.textElements)
            }
            ModificationType.DELETE_STROKE -> {
                val targetId = mod.strokeId ?: mod.stroke?.id
                Pair(activePage.strokes.filterNot { it.id == targetId }, activePage.textElements)
            }
            ModificationType.ADD_TEXT -> {
                val restored = if (mod.textElement != null && !activePage.textElements.any { it.id == mod.textElement.id }) activePage.textElements + mod.textElement else activePage.textElements
                Pair(activePage.strokes, restored)
            }
            ModificationType.DELETE_TEXT -> {
                val targetId = mod.textId ?: mod.textElement?.id
                Pair(activePage.strokes, activePage.textElements.filterNot { it.id == targetId })
            }
            ModificationType.MOVE_ELEMENTS, ModificationType.RECOLOR_ELEMENTS -> {
                val strokeMap = mod.newStrokes.associateBy { it.id }
                val textMap = mod.newTexts.associateBy { it.id }
                val restoredStrokes = activePage.strokes.map { strokeMap[it.id] ?: it }
                val restoredTexts = activePage.textElements.map { textMap[it.id] ?: it }
                Pair(restoredStrokes, restoredTexts)
            }
            ModificationType.CLEAR_PAGE -> {
                Pair(emptyList<Stroke>(), emptyList<TextElement>())
            }
            ModificationType.RESTORE_VERSION -> {
                Pair(mod.newStrokes, mod.newTexts)
            }
            ModificationType.ADD_IMAGE, ModificationType.DELETE_IMAGE -> {
                Pair(activePage.strokes, activePage.textElements)
            }
        }

        val updatedPage = activePage.copy(strokes = updatedStrokes, textElements = updatedTexts)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())
        _uiState.update {
            it.copy(
                currentBoard = updatedBoard,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        boardManager.saveBoard(updatedBoard)
        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun createVersionSnapshot(name: String = "Manual Snapshot") {
        val state = _uiState.value
        val activePage = state.activePage
        val snapshot = BoardVersionSnapshot(
            name = if (name.isBlank()) "Snapshot #${state.currentBoard.versionHistory.size + 1}" else name,
            author = state.displayName,
            pageIndex = state.currentBoard.activePageIndex,
            strokes = activePage.strokes,
            textElements = activePage.textElements
        )
        val updatedHistory = listOf(snapshot) + state.currentBoard.versionHistory
        val updatedBoard = state.currentBoard.copy(versionHistory = updatedHistory)
        _uiState.update { it.copy(currentBoard = updatedBoard, versionHistory = updatedHistory) }
        boardManager.saveBoard(updatedBoard)
    }

    fun restoreVersionSnapshot(snapshot: BoardVersionSnapshot) {
        val state = _uiState.value
        val activePage = state.activePage

        val mod = CanvasModification(
            type = ModificationType.RESTORE_VERSION,
            author = state.displayName,
            pageIndex = state.currentBoard.activePageIndex,
            previousStrokes = activePage.strokes,
            newStrokes = snapshot.strokes,
            previousTexts = activePage.textElements,
            newTexts = snapshot.textElements
        )
        recordModification(mod)

        val updatedPage = activePage.copy(strokes = snapshot.strokes, textElements = snapshot.textElements)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())
        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)
        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(snapshot.strokes, snapshot.textElements))
    }

    fun onStrokeCompleted(stroke: Stroke) {
        val taggedStroke = if (stroke.author == "You" || stroke.author.isBlank()) {
            stroke.copy(author = _uiState.value.displayName)
        } else stroke

        val state = _uiState.value
        val activePage = state.activePage
        val updatedStrokes = activePage.strokes + taggedStroke
        val updatedPage = activePage.copy(strokes = updatedStrokes)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        val mod = CanvasModification(
            type = ModificationType.ADD_STROKE,
            author = taggedStroke.author,
            pageIndex = state.currentBoard.activePageIndex,
            stroke = taggedStroke
        )
        recordModification(mod)

        if (updatedStrokes.size % 15 == 0) {
            createVersionSnapshot("Milestone (${updatedStrokes.size} strokes)")
        }
    }

    fun addTextElement(text: String, x: Float, y: Float, color: Color, fontSize: Float, fontName: String) {
        if (text.isBlank()) return
        val newText = TextElement(
            text = text,
            x = x,
            y = y,
            color = color.toArgb().toLong(),
            fontSize = fontSize,
            fontName = fontName,
            author = _uiState.value.displayName
        )
        val state = _uiState.value
        val activePage = state.activePage
        val updatedTextList = activePage.textElements + newText
        val updatedPage = activePage.copy(textElements = updatedTextList)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        val mod = CanvasModification(
            type = ModificationType.ADD_TEXT,
            author = newText.author,
            pageIndex = state.currentBoard.activePageIndex,
            textElement = newText
        )
        recordModification(mod)

        nearbyManager.sendPacketToAll(WhiteboardPacket.TextAdd(newText))
    }

    fun deleteStrokeById(strokeId: String) {
        val state = _uiState.value
        val activePage = state.activePage
        val deletedStroke = activePage.strokes.find { it.id == strokeId }
        val updatedStrokes = activePage.strokes.filterNot { it.id == strokeId }
        val updatedPage = activePage.copy(strokes = updatedStrokes)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        if (deletedStroke != null) {
            val mod = CanvasModification(
                type = ModificationType.DELETE_STROKE,
                author = _uiState.value.displayName,
                pageIndex = state.currentBoard.activePageIndex,
                stroke = deletedStroke,
                strokeId = strokeId
            )
            recordModification(mod)
        }

        nearbyManager.sendPacketToAll(WhiteboardPacket.StrokeDelete(strokeId))
    }

    fun deleteTextElementById(textId: String) {
        val state = _uiState.value
        val activePage = state.activePage
        val deletedText = activePage.textElements.find { it.id == textId }
        val updatedTextList = activePage.textElements.filterNot { it.id == textId }
        val updatedPage = activePage.copy(textElements = updatedTextList)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        if (deletedText != null) {
            val mod = CanvasModification(
                type = ModificationType.DELETE_TEXT,
                author = _uiState.value.displayName,
                pageIndex = state.currentBoard.activePageIndex,
                textElement = deletedText,
                textId = textId
            )
            recordModification(mod)
        }

        nearbyManager.sendPacketToAll(WhiteboardPacket.TextDelete(textId))
    }

    fun addImageElement(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val imagesDir = File(context.filesDir, "board_images").apply { mkdirs() }
                val imageFile = File(imagesDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (imageFile.exists()) {
                    val state = _uiState.value
                    val initialX = ((-state.panOffset.x) + 200f) / state.zoom
                    val initialY = ((-state.panOffset.y) + 300f) / state.zoom

                    val newImg = ImageElement(
                        path = imageFile.absolutePath,
                        x = initialX,
                        y = initialY,
                        width = 240f,
                        height = 240f,
                        author = state.displayName
                    )

                    val activePage = state.activePage
                    val updatedImages = activePage.imageElements + newImg
                    val updatedPage = activePage.copy(imageElements = updatedImages)
                    val updatedPages = state.currentBoard.pages.toMutableList().apply {
                        this[state.currentBoard.activePageIndex] = updatedPage
                    }
                    val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

                    _uiState.update { it.copy(currentBoard = updatedBoard) }
                    boardManager.saveBoard(updatedBoard)

                    val mod = CanvasModification(
                        type = ModificationType.ADD_IMAGE,
                        author = state.displayName,
                        pageIndex = state.currentBoard.activePageIndex,
                        imageElement = newImg,
                        imageId = newImg.id
                    )
                    recordModification(mod)
                }
            } catch (_: Exception) {}
        }
    }

    fun deleteImageElementById(imageId: String) {
        val state = _uiState.value
        val activePage = state.activePage
        val deletedImg = activePage.imageElements.find { it.id == imageId }
        val updatedImageList = activePage.imageElements.filterNot { it.id == imageId }
        val updatedPage = activePage.copy(imageElements = updatedImageList)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        if (deletedImg != null) {
            val mod = CanvasModification(
                type = ModificationType.DELETE_IMAGE,
                author = state.displayName,
                pageIndex = state.currentBoard.activePageIndex,
                imageElement = deletedImg,
                imageId = imageId
            )
            recordModification(mod)
        }
    }

    fun disconnectPeer(endpointId: String) {
        nearbyManager.disconnectFromPeer(endpointId)
        _uiState.update { state ->
            state.copy(connectedPeers = state.connectedPeers.filter { it.endpointId != endpointId })
        }
    }

    fun leaveRoom() {
        nearbyManager.stopAllEndpoints()
        _uiState.update { it.copy(connectedPeers = emptyList(), discoveredPeers = emptyList(), roomCode = null) }
    }

    fun updateMovedElements(
        movedStrokes: List<Stroke>,
        movedTexts: List<TextElement>,
        movedImages: List<ImageElement> = emptyList()
    ) {
        val state = _uiState.value
        val activePage = state.activePage

        val strokeMap = movedStrokes.associateBy { it.id }
        val textMap = movedTexts.associateBy { it.id }
        val imageMap = movedImages.associateBy { it.id }

        val previousStrokes = activePage.strokes.filter { strokeMap.containsKey(it.id) }
        val previousTexts = activePage.textElements.filter { textMap.containsKey(it.id) }
        val previousImages = activePage.imageElements.filter { imageMap.containsKey(it.id) }

        val updatedStrokes = activePage.strokes.map { strokeMap[it.id] ?: it }
        val updatedTexts = activePage.textElements.map { textMap[it.id] ?: it }
        val updatedImages = activePage.imageElements.map { imageMap[it.id] ?: it }

        val updatedPage = activePage.copy(
            strokes = updatedStrokes,
            textElements = updatedTexts,
            imageElements = updatedImages
        )
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        val mod = CanvasModification(
            type = ModificationType.MOVE_ELEMENTS,
            author = _uiState.value.displayName,
            pageIndex = state.currentBoard.activePageIndex,
            previousStrokes = previousStrokes,
            newStrokes = movedStrokes,
            previousTexts = previousTexts,
            newTexts = movedTexts,
            previousImages = previousImages,
            newImages = movedImages
        )
        recordModification(mod)

        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun duplicateElements(strokeIds: List<String>, textIds: List<String>) {
        val state = _uiState.value
        val activePage = state.activePage

        val offset = 24f
        val newStrokes = activePage.strokes.filter { strokeIds.contains(it.id) }.map { s ->
            s.copy(
                id = java.util.UUID.randomUUID().toString(),
                points = s.points.map { p -> p.copy(x = p.x + offset, y = p.y + offset) },
                author = _uiState.value.displayName
            )
        }
        val newTexts = activePage.textElements.filter { textIds.contains(it.id) }.map { t ->
            t.copy(
                id = java.util.UUID.randomUUID().toString(),
                x = t.x + offset,
                y = t.y + offset,
                author = _uiState.value.displayName
            )
        }

        val updatedStrokes = activePage.strokes + newStrokes
        val updatedTexts = activePage.textElements + newTexts

        val updatedPage = activePage.copy(strokes = updatedStrokes, textElements = updatedTexts)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun recolorElements(strokeIds: List<String>, textIds: List<String>, color: Color) {
        val state = _uiState.value
        val activePage = state.activePage
        val argb = color.toArgb().toLong()

        val previousStrokes = activePage.strokes.filter { strokeIds.contains(it.id) }
        val previousTexts = activePage.textElements.filter { textIds.contains(it.id) }

        val updatedStrokes = activePage.strokes.map { s ->
            if (strokeIds.contains(s.id)) s.copy(color = argb) else s
        }
        val updatedTexts = activePage.textElements.map { t ->
            if (textIds.contains(t.id)) t.copy(color = argb) else t
        }

        val updatedPage = activePage.copy(strokes = updatedStrokes, textElements = updatedTexts)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis())

        _uiState.update { it.copy(currentBoard = updatedBoard) }
        boardManager.saveBoard(updatedBoard)

        val mod = CanvasModification(
            type = ModificationType.RECOLOR_ELEMENTS,
            author = _uiState.value.displayName,
            pageIndex = state.currentBoard.activePageIndex,
            previousStrokes = previousStrokes,
            newStrokes = updatedStrokes.filter { strokeIds.contains(it.id) },
            previousTexts = previousTexts,
            newTexts = updatedTexts.filter { textIds.contains(it.id) }
        )
        recordModification(mod)

        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun broadcastStrokeStart(strokeId: String, color: Long, strokeWidth: Float, isEraser: Boolean, point: Point) {
        nearbyManager.sendPacketToAll(
            WhiteboardPacket.StrokeStart(strokeId, color, strokeWidth, isEraser, point)
        )
    }

    fun broadcastStrokePoint(strokeId: String, point: Point) {
        nearbyManager.sendPacketToAll(
            WhiteboardPacket.StrokePoint(strokeId, point)
        )
    }

    fun broadcastStrokeEnd(strokeId: String) {
        nearbyManager.sendPacketToAll(WhiteboardPacket.StrokeEnd(strokeId))
    }

    fun selectTool(tool: DrawingTool) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun setPenColor(color: Color) {
        _uiState.update {
            it.copy(
                currentColor = color,
                selectedTool = if (it.selectedTool == DrawingTool.ERASER || it.selectedTool == DrawingTool.SELECT_ERASE) DrawingTool.PEN else it.selectedTool
            )
        }
    }

    fun setCanvasBackgroundColor(color: Color) {
        val colorLong = color.toArgb().toLong()
        _uiState.update { state ->
            val updatedBoard = state.currentBoard.copy(backgroundColor = colorLong)
            boardManager.saveBoard(updatedBoard)
            state.copy(backgroundColor = color, currentBoard = updatedBoard)
        }
    }

    fun setStrokeWidth(width: Float) {
        appSettings.defaultStrokeWidth = width
        _uiState.update { it.copy(currentWidth = width) }
    }

    fun clearCanvas() {
        val state = _uiState.value
        val activePage = state.activePage
        if (activePage.strokes.isNotEmpty() || activePage.textElements.isNotEmpty()) {
            val mod = CanvasModification(
                type = ModificationType.CLEAR_PAGE,
                author = state.displayName,
                pageIndex = state.currentBoard.activePageIndex,
                previousStrokes = activePage.strokes,
                previousTexts = activePage.textElements
            )
            recordModification(mod)
        }

        _uiState.update { currentState ->
            val updatedActivePage = currentState.activePage.copy(strokes = emptyList(), textElements = emptyList())
            val updatedPages = currentState.currentBoard.pages.toMutableList().apply {
                this[currentState.currentBoard.activePageIndex] = updatedActivePage
            }
            val updatedBoard = currentState.currentBoard.copy(pages = updatedPages)
            boardManager.saveBoard(updatedBoard)
            currentState.copy(currentBoard = updatedBoard)
        }
        nearbyManager.sendPacketToAll(WhiteboardPacket.CanvasClear)
    }

    fun startAdvertising(deviceName: String) {
        val roomCode = generateRoomCode()
        _uiState.update { it.copy(roomCode = roomCode) }
        val nameToUse = deviceName.ifBlank { _uiState.value.displayName }
        nearbyManager.startAdvertising("$nameToUse [$roomCode]")
    }

    fun stopAdvertising() {
        nearbyManager.stopAdvertising()
        _uiState.update { state ->
            state.copy(roomCode = if (state.connectedPeers.isNotEmpty()) state.roomCode else null)
        }
    }

    fun startDiscovery() {
        nearbyManager.startDiscovery()
    }

    fun stopDiscovery() {
        nearbyManager.stopDiscovery()
    }

    fun connectToPeer(endpointId: String, localName: String) {
        val peer = _uiState.value.discoveredPeers.find { it.endpointId == endpointId }
        val codeMatch = Regex("\\[([A-Z0-9]+)\\]").find(peer?.name ?: "")?.groupValues?.getOrNull(1)
        if (codeMatch != null) {
            _uiState.update { it.copy(roomCode = codeMatch) }
        }
        val nameToUse = localName.ifBlank { _uiState.value.displayName }
        nearbyManager.connectToPeer(endpointId, nameToUse)
    }

    private fun handleIncomingPacket(packet: WhiteboardPacket) {
        when (packet) {
            is WhiteboardPacket.StrokeStart -> {
                val peerName = _uiState.value.connectedPeers.firstOrNull()?.name ?: "Peer"
                val stroke = Stroke(
                    id = packet.strokeId,
                    points = listOf(packet.point),
                    color = packet.color,
                    strokeWidth = packet.strokeWidth,
                    tool = if (packet.isEraser) DrawingTool.ERASER else DrawingTool.PEN,
                    isEraser = packet.isEraser,
                    author = peerName
                )
                remoteStrokesMap[packet.strokeId] = stroke
                updateRemoteStrokes()

                _uiState.update { state ->
                    val presences = state.livePeerPresences.toMutableMap()
                    presences[packet.strokeId] = PeerPresence(
                        peerId = packet.strokeId,
                        name = peerName,
                        currentPoint = packet.point,
                        isDrawing = true,
                        activeTool = if (packet.isEraser) DrawingTool.ERASER else DrawingTool.PEN,
                        color = packet.color
                    )
                    state.copy(livePeerPresences = presences)
                }
            }
            is WhiteboardPacket.StrokePoint -> {
                remoteStrokesMap[packet.strokeId]?.let { stroke ->
                    val updated = stroke.copy(points = stroke.points + packet.point)
                    remoteStrokesMap[packet.strokeId] = updated
                    updateRemoteStrokes()

                    _uiState.update { state ->
                        val presences = state.livePeerPresences.toMutableMap()
                        val existing = presences[packet.strokeId]
                        presences[packet.strokeId] = PeerPresence(
                            peerId = packet.strokeId,
                            name = existing?.name ?: "Peer",
                            currentPoint = packet.point,
                            isDrawing = true,
                            activeTool = existing?.activeTool ?: DrawingTool.PEN,
                            color = existing?.color ?: packet.point.timestampMs
                        )
                        state.copy(livePeerPresences = presences)
                    }
                }
            }
            is WhiteboardPacket.StrokeEnd -> {
                val finishedStroke = remoteStrokesMap.remove(packet.strokeId)
                if (finishedStroke != null) {
                    onStrokeCompleted(finishedStroke)
                }
                _uiState.update { state ->
                    val presences = state.livePeerPresences.toMutableMap()
                    presences.remove(packet.strokeId)
                    state.copy(livePeerPresences = presences)
                }
            }
            is WhiteboardPacket.StrokeDelete -> {
                deleteStrokeLocally(packet.strokeId)
            }
            is WhiteboardPacket.TextAdd -> {
                addTextLocally(packet.textElement)
            }
            is WhiteboardPacket.TextDelete -> {
                deleteTextLocally(packet.textId)
            }
            is WhiteboardPacket.CanvasClear -> {
                remoteStrokesMap.clear()
                clearCanvas()
            }
            is WhiteboardPacket.SyncRequest -> {
                val state = _uiState.value
                val activeP = state.activePage
                nearbyManager.sendPacketToAll(
                    WhiteboardPacket.BoardSync(
                        boardId = state.currentBoard.id,
                        title = state.currentBoard.title,
                        backgroundColor = state.currentBoard.backgroundColor,
                        isReadOnly = state.currentBoard.isReadOnly,
                        strokes = activeP.strokes,
                        textElements = activeP.textElements
                    )
                )
            }
            is WhiteboardPacket.SyncResponse -> {
                _uiState.update { state ->
                    val activePage = state.activePage.copy(
                        strokes = packet.strokes,
                        textElements = packet.textElements
                    )
                    val updatedPages = state.currentBoard.pages.toMutableList().apply {
                        this[state.currentBoard.activePageIndex] = activePage
                    }
                    val updatedBoard = state.currentBoard.copy(pages = updatedPages)
                    state.copy(currentBoard = updatedBoard)
                }
            }
            is WhiteboardPacket.BoardSync -> {
                _uiState.update { state ->
                    val isReadOnlyEffective = packet.isReadOnly || state.myAccessReadOnly
                    val newBoard = Board(
                        id = packet.boardId,
                        title = packet.title,
                        backgroundColor = packet.backgroundColor,
                        isReadOnly = isReadOnlyEffective,
                        pages = listOf(
                            BoardPage(
                                strokes = packet.strokes,
                                textElements = packet.textElements
                            )
                        ),
                        activePageIndex = 0
                    )
                    state.copy(
                        currentBoard = newBoard,
                        backgroundColor = Color(packet.backgroundColor)
                    )
                }
            }
            is WhiteboardPacket.PermissionUpdate -> {
                _uiState.update { state ->
                    val updatedPeers = state.connectedPeers.map {
                        if (it.endpointId == packet.endpointId) it.copy(isReadOnly = packet.isReadOnly) else it
                    }
                    val isForMe = state.connectedPeers.none { it.endpointId == packet.endpointId }
                    val myReadOnly = if (isForMe) packet.isReadOnly else state.myAccessReadOnly
                    state.copy(
                        connectedPeers = updatedPeers,
                        myAccessReadOnly = myReadOnly,
                        currentBoard = state.currentBoard.copy(isReadOnly = myReadOnly || state.currentBoard.isReadOnly)
                    )
                }
            }
        }
    }

    private fun deleteStrokeLocally(strokeId: String) {
        _uiState.update { state ->
            val activePage = state.activePage.copy(strokes = state.activePage.strokes.filterNot { it.id == strokeId })
            val updatedPages = state.currentBoard.pages.toMutableList().apply {
                this[state.currentBoard.activePageIndex] = activePage
            }
            state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
        }
    }

    private fun addTextLocally(elem: TextElement) {
        _uiState.update { state ->
            val activePage = state.activePage.copy(textElements = state.activePage.textElements + elem)
            val updatedPages = state.currentBoard.pages.toMutableList().apply {
                this[state.currentBoard.activePageIndex] = activePage
            }
            state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
        }
    }

    private fun deleteTextLocally(textId: String) {
        _uiState.update { state ->
            val activePage = state.activePage.copy(textElements = state.activePage.textElements.filterNot { it.id == textId })
            val updatedPages = state.currentBoard.pages.toMutableList().apply {
                this[state.currentBoard.activePageIndex] = activePage
            }
            state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
        }
    }

    private fun updateRemoteStrokes() {
        val remoteList = remoteStrokesMap.values.toList()
        _uiState.update { state ->
            val activePage = state.activePage
            val localStrokes = activePage.strokes.filterNot { remoteStrokesMap.containsKey(it.id) }
            val mergedPage = activePage.copy(strokes = localStrokes + remoteList)
            val updatedPages = state.currentBoard.pages.toMutableList().apply {
                this[state.currentBoard.activePageIndex] = mergedPage
            }
            state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
        }
    }

    override fun onCleared() {
        super.onCleared()
        nearbyManager.stopAllEndpoints()
    }
}
