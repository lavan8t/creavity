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
        is BoardAction.AddText -> addTextElement(action.text, action.x, action.y, action.color, action.fontSize, action.fontName)
        is BoardAction.DeleteStroke -> deleteStrokeById(action.strokeId)
        is BoardAction.DeleteText -> deleteTextElementById(action.textId)
        is BoardAction.AddImage -> addImageElement(action.pathOrUri)
        is BoardAction.DeleteImage -> deleteImageElementById(action.imageId)
        is BoardAction.MoveElements -> updateMovedElements(action.strokes, action.texts, action.images)
        is BoardAction.DuplicateElements -> duplicateElements(action.strokeIds, action.textIds)
        is BoardAction.RecolorElements -> recolorElements(action.strokeIds, action.textIds, action.color)
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
        is BoardAction.DelegateWorksheet -> delegateWorksheet(action.title, action.questionText, action.targetStudentId)
        is BoardAction.SubmitWorksheet -> submitWorksheet(action.sheetId)
        is BoardAction.SetWorksheetMode -> setWorksheetMode(action.enabled)
        is BoardAction.InspectStudentWorksheet -> inspectStudentWorksheet(action.studentId)
        is BoardAction.AddStudentWorksheetStroke -> addStudentWorksheetStroke(action.stroke)
        is BoardAction.StartQuiz -> startQuiz(action.quiz)
        is BoardAction.AnswerQuiz -> answerQuiz(action.quizId, action.selectedOptionIndex)
        is BoardAction.EndQuiz -> endQuiz(action.quizId, action.correctOptionIndex)
    }
}

fun WhiteboardViewModel.delegateWorksheet(title: String, questionText: String, targetStudentId: String = "") {
    val sheetId = java.util.UUID.randomUUID().toString()
    val packet = com.kd.creavity.data.binary.WhiteboardPacket.DelegateSheet(
        sheetId = sheetId,
        title = title,
        questionText = questionText,
        assignedToAll = targetStudentId.isBlank(),
        targetStudentId = targetStudentId
    )
    peerTransport.sendPacketToAll(packet, isMutation = true)

    val students = if (targetStudentId.isNotBlank()) {
        _uiState.value.connectedPeers.filter { it.endpointId == targetStudentId }
    } else {
        _uiState.value.connectedPeers
    }
    val newSheets = students.associate { peer ->
        peer.endpointId to com.kd.creavity.domain.model.StudentWorksheet(
            sheetId = sheetId,
            studentId = peer.endpointId,
            studentName = peer.name,
            title = title,
            questionText = questionText,
            strokes = emptyList(),
            isSubmitted = false
        )
    }
    _uiState.update { it.copy(studentWorksheets = it.studentWorksheets + newSheets) }
}

fun WhiteboardViewModel.submitWorksheet(sheetId: String) {
    val active = _uiState.value.activeWorksheet ?: return
    if (active.sheetId != sheetId || active.isSubmitted) return
    val now = System.currentTimeMillis()
    val submittedSheet = active.copy(isSubmitted = true, submittedAtMs = now)
    _uiState.update { it.copy(activeWorksheet = submittedSheet) }

    val packet = com.kd.creavity.data.binary.WhiteboardPacket.SheetSubmission(
        sheetId = sheetId,
        studentId = peerTransport.localNodeId,
        timestampMs = now
    )
    peerTransport.sendPacketToAll(packet, isMutation = true)
}

fun WhiteboardViewModel.setWorksheetMode(enabled: Boolean) {
    _uiState.update { it.copy(isWorksheetMode = enabled) }
}

fun WhiteboardViewModel.inspectStudentWorksheet(studentId: String?) {
    _uiState.update { it.copy(inspectedStudentId = studentId) }
}

fun WhiteboardViewModel.addStudentWorksheetStroke(stroke: com.kd.creavity.domain.model.Stroke) {
    val active = _uiState.value.activeWorksheet ?: return
    if (active.isSubmitted) return
    val updatedStrokes = active.strokes + stroke
    val updatedSheet = active.copy(strokes = updatedStrokes)
    _uiState.update { it.copy(activeWorksheet = updatedSheet) }

    val strokeBytes = com.kd.creavity.data.binary.PacketSerializer.serializeSingleStroke(stroke)
    val packet = com.kd.creavity.data.binary.WhiteboardPacket.StudentSheetStroke(
        sheetId = active.sheetId,
        studentId = peerTransport.localNodeId,
        strokePacketBytes = strokeBytes
    )
    peerTransport.sendPacketToAll(packet, isMutation = false)
}

fun WhiteboardViewModel.startQuiz(quiz: com.kd.creavity.domain.model.ActiveQuiz) {
    quizTimerJob?.cancel()
    val packet = com.kd.creavity.data.binary.WhiteboardPacket.QuizStart(
        quizId = quiz.quizId,
        quizType = if (quiz.type == com.kd.creavity.domain.model.QuizType.TIMED_QUIZ) 1 else 0,
        question = quiz.questionText,
        options = quiz.options,
        durationSeconds = quiz.durationSeconds,
        startTimestampMs = quiz.startedAtMs
    )
    peerTransport.sendPacketToAll(packet, isMutation = true)
    _uiState.update {
        it.copy(
            activeQuiz = quiz,
            quizSubmissions = emptyList(),
            userQuizAnswer = null,
            quizTimeRemainingSeconds = quiz.durationSeconds
        )
    }

    if (quiz.durationSeconds > 0) {
        quizTimerJob = viewModelScope.launch {
            var remaining = quiz.durationSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(quizTimeRemainingSeconds = remaining) }
            }
            if (uiState.value.isHost && uiState.value.activeQuiz?.quizId == quiz.quizId && !uiState.value.activeQuiz!!.isEnded) {
                endQuiz(quiz.quizId, quiz.correctOptionIndex)
            }
        }
    }
}

fun WhiteboardViewModel.answerQuiz(quizId: String, selectedOptionIndex: Int) {
    val active = _uiState.value.activeQuiz ?: return
    if (active.quizId != quizId || active.isEnded) return
    val now = System.currentTimeMillis()
    _uiState.update { it.copy(userQuizAnswer = selectedOptionIndex) }
    val packet = com.kd.creavity.data.binary.WhiteboardPacket.QuizAnswer(
        quizId = quizId,
        studentId = peerTransport.localNodeId,
        selectedOptionIndex = selectedOptionIndex,
        answerTimestampMs = now
    )
    peerTransport.sendPacketToAll(packet, isMutation = false)
}

fun WhiteboardViewModel.endQuiz(quizId: String, correctOptionIndex: Int = -1) {
    quizTimerJob?.cancel()
    quizTimerJob = null
    val active = _uiState.value.activeQuiz ?: return
    if (active.quizId != quizId) return
    val updatedQuiz = active.copy(isEnded = true, correctOptionIndex = correctOptionIndex)
    _uiState.update { it.copy(activeQuiz = updatedQuiz) }
    val packet = com.kd.creavity.data.binary.WhiteboardPacket.QuizEnd(
        quizId = quizId,
        correctOptionIndex = correctOptionIndex
    )
    peerTransport.sendPacketToAll(packet, isMutation = true)
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


