package com.kd.creavity.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.PeerPresence
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.domain.model.ViewportBounds
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.util.getAppStorageDir
import java.io.File
import kotlinx.coroutines.flow.update

internal fun WhiteboardViewModel.handleIncomingPacket(packet: WhiteboardPacket) {
    if (packet is WhiteboardPacket.BoardSync) {
        hostOwnedBoardId = packet.boardId
    } else if (packet !is WhiteboardPacket.SyncRequest && packet !is WhiteboardPacket.SyncResponse && packet !is WhiteboardPacket.PermissionUpdate) {
        val hostBoard = hostOwnedBoardId
        if (hostBoard != null && uiState.value.currentBoard.id != hostBoard) {
            return
        }
    }
    when (packet) {
        is WhiteboardPacket.StrokeStart -> {
            val peerKey = if (packet.strokeId.contains('_')) {
                packet.strokeId.substringBefore('_')
            } else {
                uiState.value.connectedPeers.firstOrNull()?.endpointId ?: packet.strokeId
            }
            val peerName = uiState.value.connectedPeers.firstOrNull { it.endpointId == peerKey }?.name
                ?: uiState.value.connectedPeers.firstOrNull()?.name ?: "Peer"
            val validColor = if (packet.color == 0L || (packet.color and 0xFF000000L) == 0L) 0xFF1B1B1FL else packet.color
            val assignedColor = getPermanentUserColor(peerKey.ifBlank { peerName })
            val stroke = Stroke(
                id = packet.strokeId,
                points = listOf(packet.point),
                color = validColor,
                strokeWidth = packet.strokeWidth,
                tool = if (packet.isEraser) DrawingTool.ERASER else packet.tool,
                isEraser = packet.isEraser,
                author = peerName
            )
            remoteStrokesMap[packet.strokeId] = stroke
            updateRemoteStrokes()

            _uiState.update { state ->
                val presences = state.livePeerPresences.toMutableMap()
                presences[packet.strokeId] = PeerPresence(
                    peerId = peerKey,
                    name = peerName,
                    currentPoint = packet.point,
                    isDrawing = true,
                    activeTool = if (packet.isEraser) DrawingTool.ERASER else packet.tool,
                    color = assignedColor,
                    viewport = ViewportBounds(
                        left = packet.point.x - 960f,
                        top = packet.point.y - 540f,
                        right = packet.point.x + 960f,
                        bottom = packet.point.y + 540f
                    )
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
                    val peerKey = existing?.peerId ?: if (packet.strokeId.contains('_')) packet.strokeId.substringBefore('_') else packet.strokeId
                    val assignedColor = existing?.color ?: getPermanentUserColor(peerKey)
                    presences[packet.strokeId] = PeerPresence(
                        peerId = peerKey,
                        name = existing?.name ?: "Peer",
                        currentPoint = packet.point,
                        isDrawing = true,
                        activeTool = existing?.activeTool ?: DrawingTool.PEN,
                        color = assignedColor,
                        viewport = ViewportBounds(
                            left = packet.point.x - 960f,
                            top = packet.point.y - 540f,
                            right = packet.point.x + 960f,
                            bottom = packet.point.y + 540f
                        )
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
        is WhiteboardPacket.StickyNoteAdd -> {
            addStickyNoteLocally(packet.stickyNote)
        }
        is WhiteboardPacket.StickyNoteDelete -> {
            deleteStickyNoteLocally(packet.stickyNoteId)
        }
        is WhiteboardPacket.ImageAdd -> {
            addImageLocally(
                id = packet.imageId,
                x = packet.x,
                y = packet.y,
                width = packet.width,
                height = packet.height,
                author = packet.author,
                imageData = packet.imageData
            )
        }
        is WhiteboardPacket.ImageDelete -> {
            deleteImageLocally(packet.imageId)
        }
        is WhiteboardPacket.ImageMove -> {
            moveImageLocally(packet.imageId, packet.x, packet.y)
        }
        is WhiteboardPacket.ViewportPresence -> {
            _uiState.update { state ->
                val presences = state.livePeerPresences.toMutableMap()
                val existing = presences[packet.endpointId]
                presences[packet.endpointId] = PeerPresence(
                    peerId = packet.endpointId,
                    name = packet.name,
                    currentPoint = existing?.currentPoint,
                    isDrawing = existing?.isDrawing ?: false,
                    activeTool = existing?.activeTool ?: DrawingTool.PEN,
                    color = packet.color,
                    viewport = packet.viewport,
                    lastUpdatedMs = System.currentTimeMillis()
                )
                state.copy(livePeerPresences = presences)
            }
        }
        is WhiteboardPacket.CanvasClear -> {
            remoteStrokesMap.clear()
            clearCanvas()
        }
        is WhiteboardPacket.SyncRequest -> {
            val state = uiState.value
            val activeP = state.activePage
            peerTransport.sendPacketToAll(
                WhiteboardPacket.BoardSync(
                    boardId = state.currentBoard.id,
                    title = state.currentBoard.title,
                    backgroundColor = state.currentBoard.backgroundColor,
                    isReadOnly = state.currentBoard.isReadOnly,
                    strokes = activeP.strokes,
                    textElements = activeP.textElements
                )
            )
            activeP.stickyNotes.forEach { note ->
                peerTransport.sendPacketToAll(WhiteboardPacket.StickyNoteAdd(note))
            }
            activeP.imageElements.forEach { img ->
                runCatching {
                    val file = File(img.path)
                    if (file.exists()) {
                        peerTransport.sendPacketToAll(
                            WhiteboardPacket.ImageAdd(
                                imageId = img.id,
                                x = img.x,
                                y = img.y,
                                width = img.width,
                                height = img.height,
                                author = img.author,
                                imageData = file.readBytes()
                            )
                        )
                    }
                }
            }
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

internal fun WhiteboardViewModel.deleteStrokeLocally(strokeId: String) {
    _uiState.update { state ->
        val activePage = state.activePage.copy(strokes = state.activePage.strokes.filterNot { it.id == strokeId })
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = activePage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.addTextLocally(elem: TextElement) {
    _uiState.update { state ->
        val activePage = state.activePage.copy(textElements = state.activePage.textElements + elem)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = activePage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.deleteTextLocally(textId: String) {
    _uiState.update { state ->
        val activePage = state.activePage.copy(textElements = state.activePage.textElements.filterNot { it.id == textId })
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = activePage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.updateRemoteStrokes() {
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

internal fun WhiteboardViewModel.addStickyNoteLocally(note: StickyNote) {
    _uiState.update { state ->
        val activePage = state.activePage.copy(stickyNotes = state.activePage.stickyNotes + note)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = activePage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.deleteStickyNoteLocally(noteId: String) {
    _uiState.update { state ->
        val activePage = state.activePage.copy(stickyNotes = state.activePage.stickyNotes.filterNot { it.id == noteId })
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = activePage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.addImageLocally(
    id: String,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    author: String,
    imageData: ByteArray
) {
    val imagesDir = File(getAppStorageDir(), "images").apply { mkdirs() }
    val localFile = File(imagesDir, "img_${id}.jpg")
    runCatching {
        if (!localFile.exists() || localFile.length() != imageData.size.toLong()) {
            localFile.writeBytes(imageData)
        }
    }

    val newImg = ImageElement(
        id = id,
        path = localFile.absolutePath,
        x = x,
        y = y,
        width = width,
        height = height,
        author = author
    )

    _uiState.update { state ->
        val activePage = state.activePage
        val existingIndex = activePage.imageElements.indexOfFirst { it.id == id }
        val updatedImages = if (existingIndex >= 0) {
            activePage.imageElements.toMutableList().apply { this[existingIndex] = newImg }
        } else {
            activePage.imageElements + newImg
        }
        val updatedPage = activePage.copy(imageElements = updatedImages)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.deleteImageLocally(imageId: String) {
    _uiState.update { state ->
        val activePage = state.activePage
        val updatedImages = activePage.imageElements.filterNot { it.id == imageId }
        val updatedPage = activePage.copy(imageElements = updatedImages)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

internal fun WhiteboardViewModel.moveImageLocally(imageId: String, x: Float, y: Float) {
    _uiState.update { state ->
        val activePage = state.activePage
        val updatedImages = activePage.imageElements.map {
            if (it.id == imageId) it.copy(x = x, y = y) else it
        }
        val updatedPage = activePage.copy(imageElements = updatedImages)
        val updatedPages = state.currentBoard.pages.toMutableList().apply {
            this[state.currentBoard.activePageIndex] = updatedPage
        }
        state.copy(currentBoard = state.currentBoard.copy(pages = updatedPages))
    }
}

