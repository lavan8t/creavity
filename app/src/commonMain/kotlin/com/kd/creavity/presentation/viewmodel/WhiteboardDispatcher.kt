package com.kd.creavity.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.kd.creavity.presentation.ui.BoardAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun WhiteboardViewModel.dispatch(action: BoardAction) {
    when (action) {
        is BoardAction.StrokeCompleted -> onStrokeCompleted(action.stroke)
        is BoardAction.StrokeBroadcastStart -> broadcastStrokeStart(action.id, action.color, action.width, action.isEraser, action.point, action.tool)
        is BoardAction.StrokeBroadcastPoint -> broadcastStrokePoint(action.id, action.point)
        is BoardAction.StrokeBroadcastEnd -> broadcastStrokeEnd(action.id)
        is BoardAction.TransformCanvas -> onTransform(action.zoomChange, action.focal, action.panChange)
        is BoardAction.SetCanvasTransform -> setCanvasTransform(action.zoom, action.panOffset)
        is BoardAction.ResetCanvasTransform -> resetCanvasTransform()
        is BoardAction.SelectTool -> selectTool(action.tool)
        is BoardAction.SelectColor -> setPenColor(action.color)
        is BoardAction.SelectBackgroundColor -> setCanvasBackgroundColor(action.color)
        is BoardAction.SelectBackgroundPattern -> setBackgroundPattern(action.pattern)
        is BoardAction.ChangeStrokeWidth -> setStrokeWidth(action.width)
        is BoardAction.ClearCanvas -> clearCanvas()
        is BoardAction.AddText -> addTextElement(action.text, action.x, action.y, action.color, action.fontSize, action.fontName, action.id, action.svgData)
        is BoardAction.UpdateTextElement -> updateTextElement(action.textElement)
        is BoardAction.DeleteStroke -> deleteStrokeById(action.strokeId)
        is BoardAction.DeleteText -> deleteTextElementById(action.textId)
        is BoardAction.AddImage -> addImageElement(action.pathOrUri)
        is BoardAction.DeleteImage -> deleteImageElementById(action.imageId)
        is BoardAction.MoveElements -> updateMovedElements(action.strokes, action.texts, action.images)
        is BoardAction.DuplicateElements -> duplicateElements(action.strokeIds, action.textIds)
        is BoardAction.RecolorElements -> recolorElements(action.strokeIds, action.textIds, action.color)
        is BoardAction.UpdateStrokeProperties -> updateStrokeProperties(action)
        is BoardAction.ReorderElements -> reorderElements(action.strokeIds, action.bringToFront)
        is BoardAction.Undo -> undo()
        is BoardAction.Redo -> redo()
        is BoardAction.CreateVersionSnapshot -> createVersionSnapshot(action.name)
        is BoardAction.RestoreVersionSnapshot -> restoreVersionSnapshot(action.snapshot)
        is BoardAction.AddNewPage -> addNewPage()
        is BoardAction.SwitchPage -> switchPage(action.pageIndex)
        is BoardAction.DeletePage -> deletePage(action.pageIndex)
        is BoardAction.CreateNewBoard -> createNewBoard(action.title)
        is BoardAction.SwitchBoard -> switchBoard(action.board)
        is BoardAction.RenameBoard -> renameBoard(action.boardId, action.newTitle)
        is BoardAction.DuplicateBoard -> duplicateBoard(action.boardId)
        is BoardAction.DeleteBoard -> moveBoardToBin(action.boardId)
        is BoardAction.MoveBoardToBin -> moveBoardToBin(action.boardId)
        is BoardAction.RestoreBoardFromBin -> restoreBoardFromBin(action.boardId)
        is BoardAction.DeleteBoardPermanently -> deleteBoardPermanently(action.boardId)
        is BoardAction.EmptyBin -> emptyBin()
        is BoardAction.ToggleReadOnlyBoard -> toggleReadOnlyBoard(action.boardId, action.isReadOnly)
        is BoardAction.StartAdvertising -> startAdvertising(action.deviceName)
        is BoardAction.StopAdvertising -> stopAdvertising()
        is BoardAction.StartDiscovery -> startDiscovery()
        is BoardAction.StopDiscovery -> stopDiscovery()
        is BoardAction.ConnectPeer -> connectToPeer(action.endpointId, action.localName)
        is BoardAction.DisconnectPeer -> disconnectPeer(action.endpointId)
        is BoardAction.SetPeerPermission -> setPeerPermission(action.endpointId, action.isReadOnly)
        is BoardAction.SetAllPeersPermission -> setAllPeersPermission(action.isReadOnly)
        is BoardAction.LeaveRoom -> leaveRoom()
        is BoardAction.ToggleStarBoard -> toggleStarBoard(action.boardId)
        is BoardAction.AddStickyNote -> addStickyNote(action.note)
        is BoardAction.DeleteStickyNote -> deleteStickyNoteById(action.noteId)
        is BoardAction.UpdateDisplayName -> updateDisplayName(action.name)
        is BoardAction.ToggleHaptics -> setHapticsEnabled(action.enabled)
        is BoardAction.SetInfiniteCanvas -> toggleInfiniteCanvas(action.enabled)
        is BoardAction.SetPresentationUrl -> setPresentationUrl(action.url)
        is BoardAction.SetPresentationMode -> setPresentationMode(action.enabled)
        is BoardAction.ImportPresentation -> importPresentation(action.pathOrUri)
        is BoardAction.SetThemeMode -> setThemeMode(action.mode)
        is BoardAction.UpdateBoardMetadata -> updateBoardMetadata(action.boardId, action.title, action.venue, action.group, action.labels)
        is BoardAction.CreateFolder -> createFolder(action.folderName)
        is BoardAction.DeleteFolder -> deleteFolder(action.folderName)

        // Classroom
        is BoardAction.SendChatMessage -> sendChatMessage(action.text)
        is BoardAction.ToggleChat -> toggleChat(action.isOpen)
    }
}

fun WhiteboardViewModel.sendChatMessage(text: String) {
    if (text.isBlank()) return
    val messageId = java.util.UUID.randomUUID().toString()
    val myNodeId = uiState.value.roomCode ?: "local-host"
    val myName = uiState.value.displayName
    val timestamp = System.currentTimeMillis()
    val chatMessage = com.kd.creavity.domain.model.ChatMessage(
        id = messageId,
        senderId = myNodeId,
        senderName = myName,
        text = text.trim(),
        timestampMs = timestamp
    )
    _uiState.update { current ->
        current.copy(
            chatMessages = current.chatMessages + chatMessage
        )
    }
    val packet = com.kd.creavity.data.binary.WhiteboardPacket.ChatMessage(
        messageId = messageId,
        senderId = myNodeId,
        senderName = myName,
        text = text.trim(),
        timestampMs = timestamp
    )
    peerTransport.sendPacketToAll(packet, isMutation = true)
}

fun WhiteboardViewModel.toggleChat(isOpen: Boolean? = null) {
    _uiState.update { current ->
        val nextOpen = isOpen ?: !current.isChatOpen
        current.copy(
            isChatOpen = nextOpen,
            unreadChatCount = if (nextOpen) 0 else current.unreadChatCount
        )
    }
}

fun WhiteboardViewModel.createFolder(name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank() || trimmed.equals("Unfiled", ignoreCase = true)) return
    val updated = _uiState.value.userFolders + trimmed
    boardManager.saveCustomFolders(updated)
    _uiState.update { it.copy(userFolders = updated) }
}

fun WhiteboardViewModel.deleteFolder(name: String) {
    val trimmed = name.trim()
    val hasActiveBoards = _uiState.value.savedBoards.any { !it.isInBin && it.group.equals(trimmed, ignoreCase = true) }
    if (hasActiveBoards) return // Folder not deletable when contents are in it
    val updated = _uiState.value.userFolders - trimmed
    boardManager.saveCustomFolders(updated)
    _uiState.update { it.copy(userFolders = updated) }
}


