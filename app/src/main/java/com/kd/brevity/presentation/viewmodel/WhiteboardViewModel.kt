package com.kd.brevity.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kd.brevity.data.binary.WhiteboardPacket
import com.kd.brevity.data.nearby.NearbyConnectionsManager
import com.kd.brevity.data.storage.AppSettingsManager
import com.kd.brevity.data.storage.BoardManager
import com.kd.brevity.domain.model.AppThemePreset
import com.kd.brevity.domain.model.Board
import com.kd.brevity.domain.model.BoardPage
import com.kd.brevity.domain.model.BoardVersionSnapshot
import com.kd.brevity.domain.model.CanvasModification
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.domain.model.ImageElement
import com.kd.brevity.domain.model.ModificationType
import com.kd.brevity.domain.model.PeerDevice
import com.kd.brevity.domain.model.PeerPresence
import com.kd.brevity.domain.model.Point
import com.kd.brevity.domain.model.Stroke
import com.kd.brevity.domain.model.TextElement
import com.kd.brevity.domain.mutation.BoardMutation
import com.kd.brevity.domain.mutation.MutationHistory
import com.kd.brevity.data.mesh.MeshAccessController
import com.kd.brevity.data.mesh.MeshRole
import com.kd.brevity.presentation.ui.OotbAction
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

class WhiteboardViewModel(application: Application) : AndroidViewModel(application) {
    val nearbyManager = NearbyConnectionsManager(application)
    private val boardManager = BoardManager(application)
    private val appSettings = AppSettingsManager(application)

    internal val _uiState = MutableStateFlow(
        WhiteboardUiState(
            displayName = appSettings.displayName,
            hapticsEnabled = appSettings.hapticsEnabled,
            currentWidth = appSettings.defaultStrokeWidth,
            selectedThemePreset = appSettings.themePreset
        )
    )
    val uiState: StateFlow<WhiteboardUiState> = _uiState.asStateFlow()

    internal val remoteStrokesMap = mutableMapOf<String, Stroke>()

    init {
        viewModelScope.launch(Dispatchers.IO) { loadSavedBoards() }
        nearbyManager.isAdvertising.onEach { isAdv ->
            _uiState.update { state ->
                val code = if (isAdv) state.roomCode ?: generateRoomCode() else if (state.connectedPeers.isEmpty()) null else state.roomCode
                state.copy(isAdvertising = isAdv, roomCode = code)
            }
        }.launchIn(viewModelScope)

        nearbyManager.isDiscovering.onEach { isDisc -> _uiState.update { it.copy(isDiscovering = isDisc) } }.launchIn(viewModelScope)

        nearbyManager.connectedPeers.onEach { peers ->
            _uiState.update { state ->
                val distinct = peers.distinctBy { it.endpointId }
                val connectedIds = distinct.map { it.endpointId }.toSet()
                val code = if (distinct.isNotEmpty()) state.roomCode ?: generateRoomCode() else if (state.isAdvertising) state.roomCode else null
                state.copy(connectedPeers = distinct, discoveredPeers = state.discoveredPeers.filter { it.endpointId !in connectedIds }, roomCode = code)
            }
        }.launchIn(viewModelScope)

        nearbyManager.discoveredEndpoints.onEach { peers ->
            _uiState.update { state ->
                val connectedIds = state.connectedPeers.map { it.endpointId }.toSet()
                state.copy(discoveredPeers = peers.filter { it.endpointId !in connectedIds }.distinctBy { it.endpointId })
            }
        }.launchIn(viewModelScope)

        nearbyManager.incomingPackets.onEach { handleIncomingPacket(it) }.launchIn(viewModelScope)
    }

    private fun generateRoomCode(): String = (1..5).map { ('A'..'Z').random() }.joinToString("")

    fun persistBoard(updatedBoard: Board) {
        if (updatedBoard.id.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) { boardManager.saveBoard(updatedBoard) }
        _uiState.update { state ->
            val updatedSaved = if (state.savedBoards.any { it.id == updatedBoard.id }) {
                state.savedBoards.map { if (it.id == updatedBoard.id) updatedBoard else it }
            } else {
                listOf(updatedBoard) + state.savedBoards
            }
            state.copy(currentBoard = updatedBoard, savedBoards = updatedSaved)
        }
    }

    private fun loadSavedBoards() {
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
            _uiState.update {
                it.copy(
                    currentBoard = Board(id = "", title = ""),
                    savedBoards = emptyList()
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

    fun onTransform(zoomChange: Float, focal: Offset = Offset.Zero, panChange: Offset = Offset.Zero) {
        _uiState.update { state ->
            val oldZoom = state.zoom
            val newZoom = (oldZoom * zoomChange).coerceIn(0.15f, 6.0f)
            val actualScale = newZoom / oldZoom
            val newPan = if (zoomChange == 1.0f || actualScale == 1.0f) {
                state.panOffset + panChange
            } else {
                Offset(
                    x = focal.x - (focal.x - state.panOffset.x) * actualScale + panChange.x,
                    y = focal.y - (focal.y - state.panOffset.y) * actualScale + panChange.y
                )
            }
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
        if (current.id.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) { boardManager.saveBoard(current) }
        }

        val newBoard = Board(
            title = if (title.isBlank()) "Untitled Board" else title,
            backgroundColor = _uiState.value.backgroundColor.toArgb().toLong(),
            createdBy = _uiState.value.displayName,
            ownerName = _uiState.value.displayName,
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(newBoard)
        _uiState.update {
            it.copy(
                backgroundColor = Color(newBoard.backgroundColor),
                zoom = 1.0f,
                panOffset = Offset.Zero
            )
        }
    }

    fun switchBoard(board: Board) {
        if (board.id.isBlank()) return
        val current = _uiState.value.currentBoard
        val updatedTarget = board.copy(lastAccessedAt = System.currentTimeMillis())

        _uiState.update { state ->
            val updatedSaved = state.savedBoards.map { if (it.id == updatedTarget.id) updatedTarget else it }
            state.copy(
                currentBoard = updatedTarget,
                savedBoards = updatedSaved,
                backgroundColor = Color(updatedTarget.backgroundColor),
                zoom = 1.0f,
                panOffset = Offset.Zero
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (current.id.isNotBlank() && current.id != board.id) {
                boardManager.saveBoard(current)
            }
            boardManager.saveBoard(updatedTarget)
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
        val role = if (isReadOnly) MeshRole.VIEWER else MeshRole.EDITOR
        val ticket = MeshAccessController.createTicket(
            secretKey = _uiState.value.currentBoard.id,
            boardId = _uiState.value.currentBoard.id,
            deviceId = endpointId,
            role = role
        )
        nearbyManager.meshRoutingEngine.registerTicket(ticket)
        _uiState.update { state ->
            val updated = state.connectedPeers.map { peer ->
                if (peer.endpointId == endpointId) peer.copy(isReadOnly = isReadOnly, role = role) else peer
            }
            state.copy(connectedPeers = updated)
        }
        nearbyManager.sendPacketToAll(WhiteboardPacket.PermissionUpdate(endpointId, isReadOnly), isMutation = true, ticket = ticket)
    }

    fun setAllPeersPermission(isReadOnly: Boolean) = _uiState.value.connectedPeers.forEach { setPeerPermission(it.endpointId, isReadOnly) }

    fun renameBoard(boardId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        val current = _uiState.value.currentBoard
        val target = if (current.id == boardId) current else _uiState.value.savedBoards.find { it.id == boardId } ?: return
        val updatedBoard = target.copy(title = newTitle, modifiedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
    }

    fun toggleReadOnlyBoard(boardId: String, isReadOnly: Boolean) {
        val current = _uiState.value.currentBoard
        val target = if (current.id == boardId) current else _uiState.value.savedBoards.find { it.id == boardId } ?: return
        val updatedBoard = target.copy(isReadOnly = isReadOnly, modifiedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
    }

    fun duplicateBoard(boardId: String) {
        val target = _uiState.value.savedBoards.find { it.id == boardId }
            ?: if (_uiState.value.currentBoard.id == boardId) _uiState.value.currentBoard else null
            ?: return
        val duplicate = target.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${target.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(duplicate)
    }

    fun moveBoardToBin(boardId: String) {
        val target = _uiState.value.savedBoards.find { it.id == boardId }
            ?: if (_uiState.value.currentBoard.id == boardId) _uiState.value.currentBoard else null
            ?: return
        val updated = target.copy(isInBin = true, binnedAt = System.currentTimeMillis())
        persistBoard(updated)
        if (_uiState.value.currentBoard.id == boardId) {
            val available = _uiState.value.savedBoards.filter { it.id != boardId && !it.isInBin }
            if (available.isNotEmpty()) {
                switchBoard(available.first())
            } else {
                _uiState.update { it.copy(currentBoard = Board(id = "", title = "")) }
            }
        }
    }

    fun restoreBoardFromBin(boardId: String) {
        val target = _uiState.value.savedBoards.find { it.id == boardId } ?: return
        val updated = target.copy(isInBin = false, binnedAt = null)
        persistBoard(updated)
    }

    fun deleteBoardPermanently(boardId: String) {
        val remaining = _uiState.value.savedBoards.filter { it.id != boardId }
        val isCurrent = _uiState.value.currentBoard.id == boardId
        if (isCurrent) {
            val available = remaining.filter { !it.isInBin }
            if (available.isNotEmpty()) {
                switchBoard(available.first())
            } else {
                _uiState.update { it.copy(currentBoard = Board(id = "", title = ""), savedBoards = remaining) }
            }
        } else {
            _uiState.update { it.copy(savedBoards = remaining) }
        }
        viewModelScope.launch(Dispatchers.IO) {
            boardManager.deleteBoard(boardId)
        }
    }

    fun emptyBin() {
        val binned = _uiState.value.savedBoards.filter { it.isInBin }
        val remaining = _uiState.value.savedBoards.filter { !it.isInBin }
        _uiState.update { it.copy(savedBoards = remaining) }
        viewModelScope.launch(Dispatchers.IO) {
            binned.forEach { boardManager.deleteBoard(it.id) }
        }
    }

    fun deleteBoard(boardId: String) = moveBoardToBin(boardId)

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
        persistBoard(updatedBoard)
    }

    fun switchPage(pageIndex: Int) {
        val board = _uiState.value.currentBoard
        if (pageIndex in board.pages.indices) {
            val updatedBoard = board.copy(activePageIndex = pageIndex)
            persistBoard(updatedBoard)
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
        persistBoard(updatedBoard)
    }

    fun saveCurrentBoard() {
        persistBoard(_uiState.value.currentBoard)
    }

    private val mutationHistory = MutationHistory()

    fun recordMutation(mutation: BoardMutation) {
        mutationHistory.record(mutation)
        _uiState.update {
            it.copy(
                canUndo = mutationHistory.canUndo,
                canRedo = mutationHistory.canRedo
            )
        }
    }


    fun undo() {
        val state = _uiState.value
        val (updatedPage, undoneMutation) = mutationHistory.undo(state.activePage)
        if (undoneMutation == null) return

        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)
        _uiState.update {
            it.copy(
                canUndo = mutationHistory.canUndo,
                canRedo = mutationHistory.canRedo
            )
        }
        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedPage.strokes, updatedPage.textElements))
    }

    fun redo() {
        val state = _uiState.value
        val (updatedPage, redoneMutation) = mutationHistory.redo(state.activePage)
        if (redoneMutation == null) return

        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)
        _uiState.update {
            it.copy(
                canUndo = mutationHistory.canUndo,
                canRedo = mutationHistory.canRedo
            )
        }
        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedPage.strokes, updatedPage.textElements))
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
        persistBoard(updatedBoard)
        _uiState.update { it.copy(versionHistory = updatedHistory) }
    }

    fun restoreVersionSnapshot(snapshot: BoardVersionSnapshot) {
        val state = _uiState.value
        val activePage = state.activePage

        recordMutation(
            BoardMutation.RestoreVersion(
                previousStrokes = activePage.strokes,
                newStrokes = snapshot.strokes,
                previousTexts = activePage.textElements,
                newTexts = snapshot.textElements
            )
        )

        val updatedPage = activePage.copy(strokes = snapshot.strokes, textElements = snapshot.textElements)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)
        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(snapshot.strokes, snapshot.textElements))
    }

    fun onStrokeCompleted(stroke: Stroke) {
        val state = _uiState.value
        val baseBoard = if (state.currentBoard.id.isBlank()) {
            Board(
                title = "Untitled Board",
                createdBy = state.displayName,
                ownerName = state.displayName
            )
        } else {
            state.currentBoard
        }
        val taggedStroke = if (stroke.author == "You" || stroke.author.isBlank()) {
            stroke.copy(author = state.displayName)
        } else stroke

        val activePage = baseBoard.pages.getOrElse(baseBoard.activePageIndex) { BoardPage() }
        val updatedStrokes = activePage.strokes + taggedStroke
        val updatedPage = activePage.copy(strokes = updatedStrokes)
        val updatedPages = baseBoard.pages.toMutableList().apply {
            if (baseBoard.activePageIndex in indices) {
                this[baseBoard.activePageIndex] = updatedPage
            } else {
                add(updatedPage)
            }
        }
        val updatedBoard = baseBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )

        persistBoard(updatedBoard)
        recordMutation(BoardMutation.AddStroke(taggedStroke))

        if (updatedStrokes.size % 15 == 0) {
            createVersionSnapshot("Milestone (${updatedStrokes.size} strokes)")
        }
    }

    fun addTextElement(text: String, x: Float, y: Float, color: Color, fontSize: Float, fontName: String) {
        if (text.isBlank()) return
        val state = _uiState.value
        val baseBoard = if (state.currentBoard.id.isBlank()) {
            Board(
                title = "Untitled Board",
                createdBy = state.displayName,
                ownerName = state.displayName
            )
        } else state.currentBoard

        val newText = TextElement(
            text = text,
            x = x,
            y = y,
            color = color.toArgb().toLong(),
            fontSize = fontSize,
            fontName = fontName,
            author = state.displayName
        )
        val activePage = baseBoard.pages.getOrElse(baseBoard.activePageIndex) { BoardPage() }
        val updatedTextList = activePage.textElements + newText
        val updatedPage = activePage.copy(textElements = updatedTextList)
        val updatedPages = baseBoard.pages.toMutableList().apply {
            if (baseBoard.activePageIndex in indices) {
                this[baseBoard.activePageIndex] = updatedPage
            } else {
                add(updatedPage)
            }
        }
        val updatedBoard = baseBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )

        persistBoard(updatedBoard)
        recordMutation(BoardMutation.AddText(newText))
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
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )

        persistBoard(updatedBoard)
        if (deletedStroke != null) {
            recordMutation(BoardMutation.DeleteStroke(deletedStroke))
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
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )

        persistBoard(updatedBoard)
        if (deletedText != null) {
            recordMutation(BoardMutation.DeleteText(deletedText))
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
                    val updatedBoard = state.currentBoard.copy(
                        pages = updatedPages,
                        modifiedAt = System.currentTimeMillis(),
                        lastAccessedAt = System.currentTimeMillis()
                    )
                    persistBoard(updatedBoard)
                    recordMutation(BoardMutation.AddImage(newImg))
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
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )

        persistBoard(updatedBoard)
        if (deletedImg != null) {
            recordMutation(BoardMutation.DeleteImage(deletedImg))
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
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)

        recordMutation(
            BoardMutation.MoveElements(
                previousStrokes = previousStrokes,
                newStrokes = movedStrokes,
                previousTexts = previousTexts,
                newTexts = movedTexts,
                previousImages = previousImages,
                newImages = movedImages
            )
        )

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
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)

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
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)

        recordMutation(
            BoardMutation.RecolorElements(
                previousStrokes = previousStrokes,
                newStrokes = updatedStrokes.filter { strokeIds.contains(it.id) },
                previousTexts = previousTexts,
                newTexts = updatedTexts.filter { textIds.contains(it.id) }
            )
        )

        nearbyManager.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun broadcastStrokeStart(strokeId: String, color: Long, strokeWidth: Float, isEraser: Boolean, point: Point) {
        nearbyManager.sendPacketToAll(WhiteboardPacket.StrokeStart(strokeId, color, strokeWidth, isEraser, point))
    }

    fun broadcastStrokePoint(strokeId: String, point: Point) {
        nearbyManager.sendPacketToAll(WhiteboardPacket.StrokePoint(strokeId, point))
    }

    fun broadcastStrokeEnd(strokeId: String) {
        nearbyManager.sendPacketToAll(WhiteboardPacket.StrokeEnd(strokeId))
    }

    fun selectTool(tool: DrawingTool) = _uiState.update { it.copy(selectedTool = tool) }

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
        val updatedBoard = _uiState.value.currentBoard.copy(backgroundColor = colorLong, modifiedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
        _uiState.update { it.copy(backgroundColor = color) }
    }

    fun setBackgroundPattern(pattern: com.kd.brevity.presentation.canvas.CanvasBackgroundPattern) = _uiState.update { it.copy(backgroundPattern = pattern) }

    fun setStrokeWidth(width: Float) {
        appSettings.defaultStrokeWidth = width
        _uiState.update { it.copy(currentWidth = width) }
    }

    fun toggleInfiniteCanvas(enabled: Boolean) {
        val updatedBoard = _uiState.value.currentBoard.copy(isInfiniteCanvas = enabled, modifiedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
        _uiState.update { it.copy(isInfiniteCanvas = enabled) }
    }

    fun setPresentationUrl(url: String?) {
        val normalized = url?.let { normalizeGoogleSlidesUrl(it) }
        val updatedBoard = _uiState.value.currentBoard.copy(presentationUrl = normalized, modifiedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
        _uiState.update { it.copy(presentationUrl = normalized) }
    }

    private fun normalizeGoogleSlidesUrl(input: String): String {
        val trimmed = input.trim()
        val regex = Regex("docs\\.google\\.com/presentation/d/([a-zA-Z0-9_-]+)")
        val match = regex.find(trimmed)
        return if (match != null) {
            val id = match.groupValues[1]
            "https://docs.google.com/presentation/d/$id/embed?start=false&loop=false&delayms=3000"
        } else {
            trimmed
        }
    }

    fun setPresentationMode(enabled: Boolean) {
        _uiState.update { it.copy(isPresentationMode = enabled) }
    }

    fun importPresentation(uri: Uri) {
        viewModelScope.launch {
            try {
                val newPages = PresentationImporter.importPdfPages(getApplication(), uri)
                if (newPages.isEmpty()) return@launch

                val newBoard = Board(
                    title = "Presentation (${newPages.size} Slides)",
                    pages = newPages,
                    activePageIndex = 0,
                    isInfiniteCanvas = false,
                    canvasWidth = 1920f,
                    canvasHeight = 1080f,
                    createdBy = _uiState.value.displayName,
                    ownerName = _uiState.value.displayName,
                    lastAccessedAt = System.currentTimeMillis()
                )
                persistBoard(newBoard)

                _uiState.update {
                    it.copy(
                        isInfiniteCanvas = false,
                        zoom = 1.0f,
                        panOffset = Offset.Zero,
                        isPresentationMode = true
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun clearCanvas() {
        val state = _uiState.value
        val activePage = state.activePage
        if (activePage.strokes.isNotEmpty() || activePage.textElements.isNotEmpty() || activePage.imageElements.isNotEmpty()) {
            recordMutation(
                BoardMutation.ClearPage(
                    previousStrokes = activePage.strokes,
                    previousTexts = activePage.textElements,
                    previousImages = activePage.imageElements
                )
            )
        }

        val updatedActivePage = activePage.copy(strokes = emptyList(), textElements = emptyList())
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedActivePage
        }
        val updatedBoard = state.currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)
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

    fun startDiscovery() = nearbyManager.startDiscovery()
    fun stopDiscovery() = nearbyManager.stopDiscovery()

    fun connectToPeer(endpointId: String, localName: String) {
        val peer = _uiState.value.discoveredPeers.find { it.endpointId == endpointId }
        val codeMatch = Regex("\\[([A-Z0-9]+)\\]").find(peer?.name ?: "")?.groupValues?.getOrNull(1)
        if (codeMatch != null) {
            _uiState.update { it.copy(roomCode = codeMatch) }
        }
        val nameToUse = localName.ifBlank { _uiState.value.displayName }
        nearbyManager.connectToPeer(endpointId, nameToUse)
    }

    fun toggleStarBoard(boardId: String) {
        val board = _uiState.value.savedBoards.find { it.id == boardId }
            ?: if (_uiState.value.currentBoard.id == boardId) _uiState.value.currentBoard else null
            ?: return
        val updated = board.copy(isStarred = !board.isStarred, modifiedAt = System.currentTimeMillis())
        persistBoard(updated)
    }

    fun addStickyNote(note: com.kd.brevity.domain.model.StickyNote) {
        val currentBoard = _uiState.value.currentBoard
        val activePageIndex = currentBoard.activePageIndex
        val page = currentBoard.pages.getOrElse(activePageIndex) { BoardPage() }
        val updatedNotes = page.stickyNotes + note
        val updatedPage = page.copy(stickyNotes = updatedNotes)
        val updatedPages = currentBoard.pages.toMutableList()
        if (activePageIndex in updatedPages.indices) {
            updatedPages[activePageIndex] = updatedPage
        } else {
            updatedPages.add(updatedPage)
        }
        val updatedBoard = currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)
        nearbyManager.sendPacketToAll(WhiteboardPacket.StickyNoteAdd(note))
    }

    fun deleteStickyNoteById(noteId: String) {
        val currentBoard = _uiState.value.currentBoard
        val activePageIndex = currentBoard.activePageIndex
        val page = currentBoard.pages.getOrElse(activePageIndex) { BoardPage() }
        val updatedNotes = page.stickyNotes.filterNot { it.id == noteId }
        val updatedPage = page.copy(stickyNotes = updatedNotes)
        val updatedPages = currentBoard.pages.toMutableList()
        if (activePageIndex in updatedPages.indices) {
            updatedPages[activePageIndex] = updatedPage
        }
        val updatedBoard = currentBoard.copy(
            pages = updatedPages,
            modifiedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        persistBoard(updatedBoard)
        nearbyManager.sendPacketToAll(WhiteboardPacket.StickyNoteDelete(noteId))
    }

    override fun onCleared() {
        super.onCleared()
        nearbyManager.stopAllEndpoints()
    }
}
