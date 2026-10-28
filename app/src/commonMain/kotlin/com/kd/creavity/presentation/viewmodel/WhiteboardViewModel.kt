package com.kd.creavity.presentation.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.isActive
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.data.nearby.PeerTransport
import com.kd.creavity.data.storage.AppSettingsManager
import com.kd.creavity.data.storage.BoardManager
import com.kd.creavity.domain.model.AppThemePreset
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.BoardVersionSnapshot
import com.kd.creavity.domain.model.CanvasModification
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.ModificationType
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.PeerPresence
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.domain.mutation.BoardMutation
import com.kd.creavity.domain.mutation.MutationHistory
import com.kd.creavity.data.mesh.MeshAccessController
import com.kd.creavity.data.mesh.MeshRole
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.util.copyImageToAppStorage
import com.kd.creavity.util.createDefaultPeerTransport
import com.kd.creavity.util.importPdfPages
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

class WhiteboardViewModel(
    val peerTransport: PeerTransport = createDefaultPeerTransport(),
    internal val boardManager: BoardManager = BoardManager(),
    internal val appSettings: AppSettingsManager = AppSettingsManager()
) : ViewModel() {

    internal val _uiState = MutableStateFlow(
        WhiteboardUiState(
            displayName = appSettings.displayName,
            hapticsEnabled = appSettings.hapticsEnabled,
            currentWidth = appSettings.defaultStrokeWidth,
            selectedThemePreset = appSettings.themePreset,
            themeMode = appSettings.themeMode
        )
    )
    val uiState: StateFlow<WhiteboardUiState> = _uiState.asStateFlow()

    internal val remoteStrokesMap = mutableMapOf<String, Stroke>()
    internal val userSessionColors = mutableMapOf<String, Long>()
    var hostOwnedBoardId: String? = null
    internal var quizTimerJob: kotlinx.coroutines.Job? = null

    fun getPermanentUserColor(peerKey: String): Long {
        return userSessionColors.getOrPut(peerKey) {
            val palette = listOf(
                0xFFE53935L, // Vivid Red
                0xFF1E88E5L, // Vivid Blue
                0xFF43A047L, // Vivid Green
                0xFFFB8C00L, // Vivid Orange
                0xFF8E24AAL, // Vivid Purple
                0xFF00ACC1L, // Cyan
                0xFFD81B60L, // Vivid Pink
                0xFF00897BL, // Teal
                0xFF5E35B1L, // Deep Violet
                0xFF6D4C41L  // Brown
            )
            palette[userSessionColors.size % palette.size]
        }
    }

    fun canBroadcastSessionEdits(): Boolean {
        val host = hostOwnedBoardId ?: return true
        return _uiState.value.currentBoard.id == host
    }

    init {
        viewModelScope.launch(Dispatchers.IO) { loadSavedBoards() }
        peerTransport.isAdvertising.onEach { isAdv ->
            _uiState.update { state ->
                val code = if (isAdv) state.roomCode ?: generateRoomCode() else if (state.connectedPeers.isEmpty()) null else state.roomCode
                state.copy(isAdvertising = isAdv, roomCode = code)
            }
        }.launchIn(viewModelScope)

        peerTransport.isDiscovering.onEach { isDisc -> _uiState.update { it.copy(isDiscovering = isDisc) } }.launchIn(viewModelScope)

        peerTransport.connectedPeers.onEach { peers ->
            val distinct = peers.distinctBy { it.endpointId }
            val state = _uiState.value
            if (distinct.isNotEmpty()) {
                distinct.forEach { appSettings.addKnownPeer(it.name) }
                val isHost = hostOwnedBoardId == null || hostOwnedBoardId == state.currentBoard.id
                if (isHost) {
                    val currentB = state.currentBoard
                    hostOwnedBoardId = currentB.id
                    (peerTransport as? com.kd.creavity.data.nearby.LanDiscoveryTransport)?.activeBoardId = currentB.id
                    val activeP = currentB.pages.getOrElse(currentB.activePageIndex) { BoardPage() }
                    peerTransport.sendPacketToAll(
                        WhiteboardPacket.BoardSync(
                            boardId = currentB.id,
                            title = currentB.title,
                            backgroundColor = currentB.backgroundColor,
                            isReadOnly = currentB.isReadOnly,
                            strokes = activeP.strokes,
                            textElements = activeP.textElements
                        )
                    )
                }
            } else if (!_uiState.value.isAdvertising) {
                hostOwnedBoardId = null
            }
            _uiState.update { s ->
                val connectedIds = distinct.map { it.endpointId }.toSet()
                val code = if (distinct.isNotEmpty()) s.roomCode ?: generateRoomCode() else if (s.isAdvertising) s.roomCode else null
                s.copy(connectedPeers = distinct, discoveredPeers = s.discoveredPeers.filter { it.endpointId !in connectedIds }, roomCode = code)
            }
        }.launchIn(viewModelScope)

        peerTransport.discoveredPeers.onEach { peers ->
            val state = _uiState.value
            val connectedIds = state.connectedPeers.map { it.endpointId }.toSet()
            _uiState.update { s ->
                s.copy(discoveredPeers = peers.filter { it.endpointId !in connectedIds }.distinctBy { it.endpointId })
            }
        }.launchIn(viewModelScope)

        peerTransport.incomingPackets.onEach { handleIncomingPacket(it) }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                kotlinx.coroutines.delay(3000)
                if (_uiState.value.connectedPeers.isNotEmpty()) {
                    val stateIndex = if (_uiState.value.isAppForeground) 0 else 1
                    peerTransport.sendPacketToAll(
                        WhiteboardPacket.EngagementHeartbeat(
                            nodeId = peerTransport.localNodeId,
                            stateIndex = stateIndex,
                            timestampMs = System.currentTimeMillis()
                        ),
                        isMutation = false
                    )

                    val now = System.currentTimeMillis()
                    _uiState.update { s ->
                        val updated = s.connectedPeers.map { peer ->
                            if (now - peer.lastSeenMs > 9000 && peer.engagementState != com.kd.creavity.domain.model.EngagementState.OFFLINE) {
                                peer.copy(engagementState = com.kd.creavity.domain.model.EngagementState.OFFLINE)
                            } else peer
                        }
                        s.copy(connectedPeers = updated)
                    }
                }
            }
        }
    }

    fun setAppForegroundState(isForeground: Boolean) {
        _uiState.update { it.copy(isAppForeground = isForeground) }
        val stateIndex = if (isForeground) 0 else 1
        peerTransport.sendPacketToAll(
            WhiteboardPacket.EngagementHeartbeat(
                nodeId = peerTransport.localNodeId,
                stateIndex = stateIndex,
                timestampMs = System.currentTimeMillis()
            ),
            isMutation = false
        )
    }

    private fun generateRoomCode(): String = (1..5).map { ('A'..'Z').random() }.joinToString("")

    private var persistDebounceJob: kotlinx.coroutines.Job? = null

    fun persistBoard(updatedBoard: Board, immediate: Boolean = false) {
        if (updatedBoard.id.isBlank()) return
        persistDebounceJob?.cancel()
        if (immediate) {
            viewModelScope.launch(Dispatchers.IO) { boardManager.saveBoard(updatedBoard) }
        } else {
            persistDebounceJob = viewModelScope.launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(350)
                boardManager.saveBoard(updatedBoard)
            }
        }
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
        val allFolders = (boardManager.getCustomFolders() + boards.map { it.group.trim() }.filter { it.isNotBlank() })
        val initial = boards.firstOrNull() ?: Board(id = "", title = "")
        _uiState.update {
            it.copy(
                currentBoard = initial,
                savedBoards = boards,
                userFolders = allFolders,
                backgroundColor = if (boards.isNotEmpty()) Color(initial.backgroundColor) else it.backgroundColor
            )
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

    private val boardViewportMap = mutableMapOf<String, Pair<Float, Offset>>()

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
            if (state.currentBoard.id.isNotBlank()) {
                boardViewportMap[state.currentBoard.id] = Pair(newZoom, newPan)
            }
            state.copy(zoom = newZoom, panOffset = newPan)
        }
    }

    fun setCanvasTransform(zoom: Float, panOffset: Offset) {
        val clampedZoom = zoom.coerceIn(0.2f, 5.0f)
        if (_uiState.value.currentBoard.id.isNotBlank()) {
            boardViewportMap[_uiState.value.currentBoard.id] = Pair(clampedZoom, panOffset)
        }
        _uiState.update { it.copy(zoom = clampedZoom, panOffset = panOffset) }
    }

    fun resetCanvasTransform() {
        if (_uiState.value.currentBoard.id.isNotBlank()) {
            boardViewportMap[_uiState.value.currentBoard.id] = Pair(1.0f, Offset.Zero)
        }
        _uiState.update { it.copy(zoom = 1.0f, panOffset = Offset.Zero) }
    }

    fun createNewBoard(title: String = "Untitled Board") {
        if (_uiState.value.isBoardSwitchingLocked) return
        val current = _uiState.value.currentBoard
        if (current.id.isNotBlank()) {
            boardViewportMap[current.id] = Pair(_uiState.value.zoom, _uiState.value.panOffset)
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
        boardViewportMap[newBoard.id] = Pair(1.0f, Offset.Zero)
        _uiState.update {
            it.copy(
                backgroundColor = Color(newBoard.backgroundColor),
                zoom = 1.0f,
                panOffset = Offset.Zero
            )
        }
    }

    fun switchBoard(board: Board) {
        if (_uiState.value.isBoardSwitchingLocked) return
        if (board.id.isBlank()) return
        val current = _uiState.value.currentBoard
        val updatedTarget = board.copy(lastAccessedAt = System.currentTimeMillis())

        if (current.id.isNotBlank()) {
            boardViewportMap[current.id] = Pair(_uiState.value.zoom, _uiState.value.panOffset)
        }
        val (restoredZoom, restoredPan) = boardViewportMap[updatedTarget.id] ?: Pair(1.0f, Offset.Zero)

        _uiState.update { state ->
            val updatedSaved = state.savedBoards.map { if (it.id == updatedTarget.id) updatedTarget else it }
            state.copy(
                currentBoard = updatedTarget,
                savedBoards = updatedSaved,
                backgroundColor = Color(updatedTarget.backgroundColor),
                zoom = restoredZoom,
                panOffset = restoredPan
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (current.id.isNotBlank() && current.id != board.id) {
                boardManager.saveBoard(current)
            }
            boardManager.saveBoard(updatedTarget)
        }

        val isHost = hostOwnedBoardId == null || hostOwnedBoardId == current.id
        if (_uiState.value.connectedPeers.isNotEmpty() && isHost) {
            hostOwnedBoardId = updatedTarget.id
            (peerTransport as? com.kd.creavity.data.nearby.LanDiscoveryTransport)?.activeBoardId = updatedTarget.id
            val activeP = updatedTarget.pages.getOrElse(updatedTarget.activePageIndex) { BoardPage() }
            peerTransport.sendPacketToAll(
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
        peerTransport.meshRoutingEngine.registerTicket(ticket)
        _uiState.update { state ->
            val updated = state.connectedPeers.map { peer ->
                if (peer.endpointId == endpointId) peer.copy(isReadOnly = isReadOnly, role = role) else peer
            }
            state.copy(connectedPeers = updated)
        }
        peerTransport.sendPacketToAll(WhiteboardPacket.PermissionUpdate(endpointId, isReadOnly), isMutation = true)
    }

    fun setAllPeersPermission(isReadOnly: Boolean) = _uiState.value.connectedPeers.forEach { setPeerPermission(it.endpointId, isReadOnly) }

    fun renameBoard(boardId: String, newTitle: String) {
        if (_uiState.value.isBoardSwitchingLocked) return
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
        if (_uiState.value.isBoardSwitchingLocked) return
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
        if (_uiState.value.isBoardSwitchingLocked) return
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
        persistBoard(_uiState.value.currentBoard, immediate = true)
    }

    private val mutationHistory = MutationHistory()

    fun recordMutation(mutation: BoardMutation) {
        mutationHistory.record(mutation)
        _uiState.update { it.copy(canUndo = mutationHistory.canUndo, canRedo = mutationHistory.canRedo) }
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
        _uiState.update { it.copy(canUndo = mutationHistory.canUndo, canRedo = mutationHistory.canRedo) }
        peerTransport.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedPage.strokes, updatedPage.textElements))
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
        _uiState.update { it.copy(canUndo = mutationHistory.canUndo, canRedo = mutationHistory.canRedo) }
        peerTransport.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedPage.strokes, updatedPage.textElements))
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
        peerTransport.sendPacketToAll(WhiteboardPacket.SyncResponse(snapshot.strokes, snapshot.textElements))
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
        val updatedStrokes = if (activePage.strokes.any { it.id == taggedStroke.id }) {
            activePage.strokes.map { if (it.id == taggedStroke.id) taggedStroke else it }
        } else {
            activePage.strokes + taggedStroke
        }
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
        peerTransport.sendPacketToAll(WhiteboardPacket.TextAdd(newText))
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
        peerTransport.sendPacketToAll(WhiteboardPacket.StrokeDelete(strokeId))
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
        peerTransport.sendPacketToAll(WhiteboardPacket.TextDelete(textId))
    }

    fun addImageElement(pathOrUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val localPath = copyImageToAppStorage(pathOrUri) ?: pathOrUri
                val imageFile = File(localPath)

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
                    val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis(), lastAccessedAt = System.currentTimeMillis())
                    persistBoard(updatedBoard)
                    recordMutation(BoardMutation.AddImage(newImg))

                    val bytes = imageFile.readBytes()
                    peerTransport.sendPacketToAll(
                        WhiteboardPacket.ImageAdd(
                            imageId = newImg.id,
                            x = newImg.x,
                            y = newImg.y,
                            width = newImg.width,
                            height = newImg.height,
                            author = newImg.author,
                            imageData = bytes
                        ),
                        isMutation = true
                    )
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
        val updatedBoard = state.currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis(), lastAccessedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
        if (deletedImg != null) {
            recordMutation(BoardMutation.DeleteImage(deletedImg))
        }
        peerTransport.sendPacketToAll(
            WhiteboardPacket.ImageDelete(imageId),
            isMutation = true
        )
    }

    fun disconnectPeer(endpointId: String) {
        peerTransport.disconnectPeer(endpointId)
        _uiState.update { state ->
            state.copy(connectedPeers = state.connectedPeers.filter { it.endpointId != endpointId })
        }
    }

    fun leaveRoom() {
        peerTransport.disconnectAll()
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

        peerTransport.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
        movedImages.forEach { img ->
            peerTransport.sendPacketToAll(
                WhiteboardPacket.ImageMove(img.id, img.x, img.y),
                isMutation = true
            )
        }
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

        peerTransport.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
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

        peerTransport.sendPacketToAll(WhiteboardPacket.SyncResponse(updatedStrokes, updatedTexts))
    }

    fun broadcastStrokeStart(strokeId: String, color: Long, strokeWidth: Float, isEraser: Boolean, point: Point, tool: DrawingTool = DrawingTool.PEN) {
        if (!canBroadcastSessionEdits()) return
        peerTransport.sendPacketToAll(WhiteboardPacket.StrokeStart(strokeId, color, strokeWidth, isEraser, point, tool))
    }

    fun broadcastStrokePoint(strokeId: String, point: Point) {
        if (!canBroadcastSessionEdits()) return
        peerTransport.sendPacketToAll(WhiteboardPacket.StrokePoint(strokeId, point))
    }

    fun broadcastStrokeEnd(strokeId: String) {
        if (!canBroadcastSessionEdits()) return
        peerTransport.sendPacketToAll(WhiteboardPacket.StrokeEnd(strokeId))
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

    fun setBackgroundPattern(pattern: com.kd.creavity.presentation.canvas.CanvasBackgroundPattern) = _uiState.update { it.copy(backgroundPattern = pattern) }

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

    fun importPresentation(pathOrUri: String) {
        viewModelScope.launch {
            try {
                val newPages = importPdfPages(pathOrUri)
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
        peerTransport.sendPacketToAll(WhiteboardPacket.CanvasClear)
    }

    fun startAdvertising(deviceName: String) {
        if (_uiState.value.isDiscovering) {
            stopDiscovery()
        }
        val roomCode = generateRoomCode()
        hostOwnedBoardId = _uiState.value.currentBoard.id
        (peerTransport as? com.kd.creavity.data.nearby.LanDiscoveryTransport)?.activeBoardId = hostOwnedBoardId ?: ""
        _uiState.update { it.copy(roomCode = roomCode, isAdvertising = true, isDiscovering = false) }
        val nameToUse = deviceName.ifBlank { _uiState.value.displayName }
        peerTransport.startAdvertising(nameToUse, roomCode)
    }

    fun stopAdvertising() {
        peerTransport.stopAdvertising()
        _uiState.update { state ->
            state.copy(isAdvertising = false, roomCode = if (state.connectedPeers.isNotEmpty()) state.roomCode else null)
        }
    }

    fun startDiscovery() {
        if (_uiState.value.isAdvertising) {
            stopAdvertising()
        }
        _uiState.update { it.copy(isDiscovering = true, isAdvertising = false) }
        peerTransport.startDiscovery()
    }
    fun stopDiscovery() {
        peerTransport.stopDiscovery()
        _uiState.update { it.copy(isDiscovering = false) }
    }

    fun connectToPeer(endpointId: String, localName: String) {
        val peer = _uiState.value.discoveredPeers.find { it.endpointId == endpointId }
        val codeMatch = Regex("\\[([A-Z0-9]+)\\]").find(peer?.name ?: "")?.groupValues?.getOrNull(1)
        if (codeMatch != null) {
            _uiState.update { it.copy(roomCode = codeMatch) }
        }
        val nameToUse = localName.ifBlank { _uiState.value.displayName }
        peerTransport.connectToPeer(endpointId, nameToUse)
    }

    fun toggleStarBoard(boardId: String) {
        val board = _uiState.value.savedBoards.find { it.id == boardId }
            ?: return
        val updated = board.copy(isStarred = !board.isStarred, modifiedAt = System.currentTimeMillis())
        _uiState.update { state ->
            val updatedSaved = state.savedBoards.map { if (it.id == boardId) updated else it }
            state.copy(savedBoards = updatedSaved)
        }
        viewModelScope.launch(Dispatchers.IO) {
            boardManager.saveBoard(updated)
        }
    }

    fun addStickyNote(note: com.kd.creavity.domain.model.StickyNote) {
        val currentBoard = _uiState.value.currentBoard
        val activePageIndex = currentBoard.activePageIndex
        val page = currentBoard.pages.getOrElse(activePageIndex) { BoardPage() }
        val updatedNotes = page.stickyNotes + note
        val updatedPage = page.copy(stickyNotes = updatedNotes)
        val updatedPages = currentBoard.pages.toMutableList().apply {
            if (activePageIndex in indices) this[activePageIndex] = updatedPage else add(updatedPage)
        }
        val updatedBoard = currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis(), lastAccessedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
        peerTransport.sendPacketToAll(WhiteboardPacket.StickyNoteAdd(note))
    }

    fun deleteStickyNoteById(noteId: String) {
        val currentBoard = _uiState.value.currentBoard
        val activePageIndex = currentBoard.activePageIndex
        val page = currentBoard.pages.getOrElse(activePageIndex) { BoardPage() }
        val updatedNotes = page.stickyNotes.filterNot { it.id == noteId }
        val updatedPage = page.copy(stickyNotes = updatedNotes)
        val updatedPages = currentBoard.pages.toMutableList().apply {
            if (activePageIndex in indices) this[activePageIndex] = updatedPage
        }
        val updatedBoard = currentBoard.copy(pages = updatedPages, modifiedAt = System.currentTimeMillis(), lastAccessedAt = System.currentTimeMillis())
        persistBoard(updatedBoard)
        peerTransport.sendPacketToAll(WhiteboardPacket.StickyNoteDelete(noteId))
    }

    fun setThemeMode(mode: ThemeMode) {
        appSettings.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun updateBoardMetadata(boardId: String, title: String, venue: String, group: String, labels: List<String>) {
        val current = _uiState.value.currentBoard
        val target = _uiState.value.savedBoards.find { it.id == boardId } ?: if (current.id == boardId) current else null ?: return
        val updated = target.copy(
            title = title.ifBlank { target.title },
            venue = venue,
            group = group,
            labels = labels,
            modifiedAt = System.currentTimeMillis()
        )
        _uiState.update { state ->
            val updatedSaved = state.savedBoards.map { if (it.id == boardId) updated else it }
            val updatedCurrent = if (state.currentBoard.id == boardId) updated else state.currentBoard
            val updatedFolders = if (group.isNotBlank()) state.userFolders + group.trim() else state.userFolders
            state.copy(currentBoard = updatedCurrent, savedBoards = updatedSaved, userFolders = updatedFolders)
        }
        if (group.isNotBlank()) {
            boardManager.saveCustomFolders(_uiState.value.userFolders)
        }
        viewModelScope.launch(Dispatchers.IO) { boardManager.saveBoard(updated) }
    }

    override fun onCleared() {
        super.onCleared()
        peerTransport.disconnectAll()
    }
}
