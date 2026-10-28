package com.kd.creavity.presentation.ui.screens

import com.kd.creavity.util.FilePickerType
import com.kd.creavity.util.rememberFilePickerLauncher
import com.kd.creavity.util.shareBoard
import com.kd.creavity.presentation.ui.components.CanvasExportDialog
import com.kd.creavity.presentation.ui.components.FloatingSelectionDock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.kd.creavity.data.mesh.MeshRole
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.presentation.canvas.CanvasColorUtils
import com.kd.creavity.presentation.canvas.WhiteboardCanvas
import com.kd.creavity.presentation.ui.NavDestination
import com.kd.creavity.presentation.ui.BoardAction
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import com.kd.creavity.presentation.ui.components.DesktopCommandCenter
import com.kd.creavity.presentation.ui.components.HostOmniviewMatrixSheet
import com.kd.creavity.presentation.ui.components.HostQuizControlDeck
import com.kd.creavity.presentation.ui.components.StudentQuizOverlay
import com.kd.creavity.presentation.ui.components.StudentWorksheetBanner
import com.kd.creavity.presentation.ui.components.BirdsEyeViewSheet
import com.kd.creavity.presentation.ui.components.CanvasBottomToolbar
import com.kd.creavity.presentation.ui.components.CanvasPresentationOverlay
import com.kd.creavity.presentation.ui.components.CanvasRenameBoardDialog
import com.kd.creavity.presentation.ui.components.CanvasSelectionBar
import com.kd.creavity.presentation.ui.components.EraserCalloutCard
import com.kd.creavity.presentation.ui.components.GoogleSlidesDialog
import com.kd.creavity.presentation.ui.components.WhiteboardStickyNoteDialog
import com.kd.creavity.presentation.ui.components.WhiteboardTopBar
import com.kd.creavity.presentation.ui.components.MaterialSymbol
import com.kd.creavity.presentation.ui.components.PenCalloutCard
import com.kd.creavity.presentation.ui.components.SelectCalloutCard
import com.kd.creavity.presentation.ui.components.UndoRedoCalloutCard
import com.kd.creavity.presentation.ui.components.WhiteboardMinimap
import com.kd.creavity.presentation.ui.motion.BrevityMotion
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

enum class MinimapCorner(val alignment: Alignment) {
    TOP_START(Alignment.TopStart),
    TOP_END(Alignment.TopEnd),
    BOTTOM_START(Alignment.BottomStart),
    BOTTOM_END(Alignment.BottomEnd)
}

private fun calculateBestMinimapCorner(touchPoints: List<Offset>, canvasSize: IntSize): MinimapCorner {
    if (touchPoints.isEmpty() || canvasSize.width <= 0 || canvasSize.height <= 0) {
        return MinimapCorner.TOP_END
    }
    val w = canvasSize.width.toFloat()
    val h = canvasSize.height.toFloat()

    val corners = listOf(
        MinimapCorner.TOP_START to Offset(0f, 0f),
        MinimapCorner.TOP_END to Offset(w, 0f),
        MinimapCorner.BOTTOM_START to Offset(0f, h),
        MinimapCorner.BOTTOM_END to Offset(w, h)
    )

    // Select corner with the largest distance to the closest touch point (furthest from all fingers)
    return corners.maxByOrNull { (_, cornerPos) ->
        touchPoints.minOfOrNull { pt ->
            val dx = pt.x - cornerPos.x
            val dy = pt.y - cornerPos.y
            dx * dx + dy * dy
        } ?: Float.MAX_VALUE
    }?.first ?: MinimapCorner.TOP_END
}

private fun fitAllCanvasContent(
    uiState: WhiteboardUiState,
    canvasSize: IntSize,
    onAction: (BoardAction) -> Unit
) {
    val strokes = uiState.strokes
    val texts = uiState.textElements
    val stickyNotes = uiState.stickyNotes

    if (strokes.isEmpty() && texts.isEmpty() && stickyNotes.isEmpty()) {
        onAction(BoardAction.ResetCanvasTransform)
        return
    }

    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE

    strokes.forEach { s ->
        s.points.forEach { p ->
            minX = min(minX, p.x)
            minY = min(minY, p.y)
            maxX = max(maxX, p.x)
            maxY = max(maxY, p.y)
        }
    }
    texts.forEach { t ->
        minX = min(minX, t.x)
        minY = min(minY, t.y)
        maxX = max(maxX, t.x + 200f)
        maxY = max(maxY, t.y + 40f)
    }
    stickyNotes.forEach { sn ->
        minX = min(minX, sn.x)
        minY = min(minY, sn.y)
        maxX = max(maxX, sn.x + sn.width)
        maxY = max(maxY, sn.y + sn.height)
    }

    val contentW = max(maxX - minX, 100f)
    val contentH = max(maxY - minY, 100f)
    val screenW = if (canvasSize.width > 0) canvasSize.width.toFloat() else 1080f
    val screenH = if (canvasSize.height > 0) canvasSize.height.toFloat() else 1920f

    val padding = 80f
    val scaleX = (screenW - padding * 2) / contentW
    val scaleY = (screenH - padding * 2) / contentH
    val targetZoom = min(scaleX, scaleY).coerceIn(0.2f, 3.0f)

    val contentCenterX = (minX + maxX) / 2f
    val contentCenterY = (minY + maxY) / 2f

    val targetPanX = screenW / 2f - contentCenterX * targetZoom
    val targetPanY = screenH / 2f - contentCenterY * targetZoom

    onAction(BoardAction.SetCanvasTransform(targetZoom, Offset(targetPanX, targetPanY)))
}

@Composable
fun CanvasViewportScreen(
    modifier: Modifier = Modifier,
    uiState: WhiteboardUiState,
    onAction: (BoardAction) -> Unit,
    onNavigateToDestination: (NavDestination) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeCalloutTool by remember { mutableStateOf<String?>(null) }

    // Auto-minimap state: shows on pan/zoom in a free corner and stays locked in place until gesture ends
    var isTransforming by remember { mutableStateOf(false) }
    var minimapCorner by remember { mutableStateOf(MinimapCorner.TOP_END) }
    var hideMinimapJob by remember { mutableStateOf<Job?>(null) }

    var showOptionsMenu by remember { mutableStateOf(false) }
    var showFrameBar by remember { mutableStateOf(false) }
    var showStickyNoteDialog by remember { mutableStateOf(false) }

    // Board Rename Dialog
    var showRenameBoardDialog by remember { mutableStateOf(false) }
    var renameBoardInput by remember { mutableStateOf("") }

    // Inline Canvas Text Editing (No Dialogs or Bottom Sheets)
    var activeInlineTextPoint by remember { mutableStateOf<Point?>(null) }
    var inlineTextContent by remember { mutableStateOf("") }
    var textFontSize by remember { mutableFloatStateOf(24f) }

    // Host Classroom Sheets
    var showBirdsEyeSheet by remember { mutableStateOf(false) }
    var showOmniviewSheet by remember { mutableStateOf(false) }
    var showQuizDeckSheet by remember { mutableStateOf(false) }
    var showDesktopCommandCenter by remember { mutableStateOf(true) }
    var inspectedStudentId by remember { mutableStateOf<String?>(null) }

    // Selection state
    var selectedStrokes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedTexts by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectionBounds by remember { mutableStateOf<Rect?>(null) }
    var activeEditingStrokeId by remember { mutableStateOf<String?>(null) }

    // Image Picker
    val launchImagePicker = rememberFilePickerLauncher(FilePickerType.IMAGE) { path ->
        path?.let { onAction(BoardAction.AddImage(it)) }
    }

    // Presentation Pickers & Dialogs
    val launchPresentationPicker = rememberFilePickerLauncher(FilePickerType.PDF) { path ->
        path?.let { onAction(BoardAction.ImportPresentation(it)) }
    }
    var showGoogleSlidesDialog by remember { mutableStateOf(false) }
    var GoogleSlidesUrlInput by remember { mutableStateOf(uiState.presentationUrl ?: "") }
    var showExportDialog by remember { mutableStateOf(false) }

    val effectiveCanvasBg = if (uiState.backgroundColor == Color.Unspecified || uiState.backgroundColor == Color(0xFF18181C)) MaterialTheme.colorScheme.surface else uiState.backgroundColor
    val isDarkCanvas = effectiveCanvasBg.luminance() < 0.5f
    val effectivePenColor = CanvasColorUtils.getAdaptiveStrokeColor(
        if (uiState.currentColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else uiState.currentColor,
        isDarkCanvas
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(effectiveCanvasBg)
                    .onSizeChanged { canvasSize = it }
            ) {
                // 1. Drawing Canvas
                WhiteboardCanvas(
                    strokes = if (uiState.isWorksheetMode && uiState.activeWorksheet != null) {
                        uiState.activeWorksheet.strokes
                    } else {
                        uiState.strokes
                    },
                    textElements = uiState.textElements,
                    imageElements = uiState.imageElements,
                    stickyNotes = uiState.stickyNotes,
                    peerPresences = uiState.livePeerPresences,
                    currentColor = effectivePenColor,
                    currentWidth = uiState.currentWidth,
                    selectedTool = uiState.selectedTool,
                    backgroundColor = effectiveCanvasBg,
                    backgroundPattern = uiState.backgroundPattern,
                    hapticsEnabled = uiState.hapticsEnabled,
                    zoom = uiState.zoom,
                    panOffset = uiState.panOffset,
                    isInfiniteCanvas = uiState.isInfiniteCanvas,
                    canvasWidth = uiState.currentBoard.canvasWidth,
                    canvasHeight = uiState.currentBoard.canvasHeight,
                    onTransform = { z, focal, p -> onAction(BoardAction.TransformCanvas(z, focal, p)) },
                    onTransformStateChanged = { transforming, touchPoints ->
                        if (transforming) {
                            hideMinimapJob?.cancel()
                            if (!isTransforming) {
                                minimapCorner = calculateBestMinimapCorner(touchPoints, canvasSize)
                                isTransforming = true
                            }
                        } else {
                            hideMinimapJob?.cancel()
                            hideMinimapJob = coroutineScope.launch {
                                delay(900L.milliseconds)
                                isTransforming = false
                            }
                        }
                    },
                    onStrokeCompleted = { stroke ->
                        if (uiState.isWorksheetMode && uiState.activeWorksheet != null) {
                            onAction(BoardAction.AddStudentWorksheetStroke(stroke))
                        } else {
                            onAction(BoardAction.StrokeCompleted(stroke))
                        }
                    },
                    onStrokeBroadcastStart = { id, c, w, e, pt, tool ->
                        if (!uiState.isWorksheetMode) {
                            onAction(BoardAction.StrokeBroadcastStart(id, c, w, e, pt, tool))
                        }
                    },
                    onStrokeBroadcastPoint = { id, pt ->
                        if (!uiState.isWorksheetMode) {
                            onAction(BoardAction.StrokeBroadcastPoint(id, pt))
                        }
                    },
                    onStrokeBroadcastEnd = { id ->
                        if (!uiState.isWorksheetMode) {
                            onAction(BoardAction.StrokeBroadcastEnd(id))
                        }
                    },
            onDeleteStroke = { onAction(BoardAction.DeleteStroke(it)) },
            onDeleteText = { onAction(BoardAction.DeleteText(it)) },
            onDeleteImage = { onAction(BoardAction.DeleteImage(it)) },
            onDeleteStickyNote = { onAction(BoardAction.DeleteStickyNote(it)) },
            onElementsMoved = { s, t, im -> onAction(BoardAction.MoveElements(s, t, im)) },
            onSelectionChanged = { s, t, bounds ->
                selectedStrokes = s
                selectedTexts = t
                selectionBounds = bounds
            },
            onTapForText = { pt ->
                if (activeInlineTextPoint != null && inlineTextContent.isNotBlank()) {
                    onAction(
                        BoardAction.AddText(
                            text = inlineTextContent,
                            x = activeInlineTextPoint!!.x,
                            y = activeInlineTextPoint!!.y,
                            color = effectivePenColor,
                            fontSize = textFontSize,
                            fontName = "Google Sans Flex"
                        )
                    )
                }
                activeInlineTextPoint = pt
                inlineTextContent = ""
            },
            isReadOnly = uiState.isCanvasReadOnly,
            modifier = Modifier.fillMaxSize()
        )

        // 1.5 Inline Canvas Text Editor (Directly on canvas, no dialogs or bottom sheets)
        if (activeInlineTextPoint != null) {
            val screenX = (activeInlineTextPoint!!.x * uiState.zoom + uiState.panOffset.x).roundToInt()
            val screenY = (activeInlineTextPoint!!.y * uiState.zoom + uiState.panOffset.y).roundToInt()
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(activeInlineTextPoint) {
                focusRequester.requestFocus()
            }

            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .offset { IntOffset(screenX, screenY) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    BasicTextField(
                        value = inlineTextContent,
                        onValueChange = { inlineTextContent = it },
                        textStyle = TextStyle(
                            color = effectivePenColor,
                            fontSize = (textFontSize * uiState.zoom).coerceIn(14f, 48f).sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(effectivePenColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (inlineTextContent.isNotBlank()) {
                                    if (activeEditingStrokeId != null) {
                                        onAction(
                                            BoardAction.UpdateStrokeProperties(
                                                strokeId = activeEditingStrokeId!!,
                                                embeddedText = inlineTextContent
                                            )
                                        )
                                    } else {
                                        onAction(
                                            BoardAction.AddText(
                                                text = inlineTextContent,
                                                x = activeInlineTextPoint!!.x,
                                                y = activeInlineTextPoint!!.y,
                                                color = effectivePenColor,
                                                fontSize = textFontSize,
                                                fontName = "Google Sans Flex"
                                            )
                                        )
                                    }
                                }
                                activeEditingStrokeId = null
                                activeInlineTextPoint = null
                                inlineTextContent = ""
                            }
                        ),
                        modifier = Modifier
                            .widthIn(min = 120.dp, max = 320.dp)
                            .focusRequester(focusRequester)
                    )

                    IconButton(
                        onClick = {
                            if (inlineTextContent.isNotBlank()) {
                                if (activeEditingStrokeId != null) {
                                    onAction(
                                        BoardAction.UpdateStrokeProperties(
                                            strokeId = activeEditingStrokeId!!,
                                            embeddedText = inlineTextContent
                                        )
                                    )
                                } else {
                                    onAction(
                                        BoardAction.AddText(
                                            text = inlineTextContent,
                                            x = activeInlineTextPoint!!.x,
                                            y = activeInlineTextPoint!!.y,
                                            color = effectivePenColor,
                                            fontSize = textFontSize,
                                            fontName = "Google Sans Flex"
                                        )
                                    )
                                }
                            }
                            activeEditingStrokeId = null
                            activeInlineTextPoint = null
                            inlineTextContent = ""
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        MaterialSymbol(name = "check", tint = MaterialTheme.colorScheme.onPrimary, size = 18.dp)
                    }

                    IconButton(
                        onClick = {
                            activeEditingStrokeId = null
                            activeInlineTextPoint = null
                            inlineTextContent = ""
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        MaterialSymbol(name = "close", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 16.dp)
                    }
                }
            }
        }

        // 2. Top Navigation Bar (Authentic Whiteboard Top Bar + Expandable Frame Bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    WhiteboardTopBar(
                        title = uiState.currentBoard.title,
                        onTitleClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            renameBoardInput = uiState.currentBoard.title
                            showRenameBoardDialog = true
                        },
                        activePageIndex = uiState.currentBoard.activePageIndex,
                        totalPages = uiState.currentBoard.pages.size,
                        onBack = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(NavDestination.BOARDS)
                        },
                        onPrevPage = {
                            if (uiState.currentBoard.activePageIndex > 0) {
                                onAction(BoardAction.SwitchPage(uiState.currentBoard.activePageIndex - 1))
                            }
                        },
                        onNextPage = {
                            if (uiState.currentBoard.activePageIndex < uiState.currentBoard.pages.size - 1) {
                                onAction(BoardAction.SwitchPage(uiState.currentBoard.activePageIndex + 1))
                            }
                        },
                        onAddPage = {
                            onAction(BoardAction.AddNewPage)
                        },
                        onFrameCountClick = {
                            showFrameBar = !showFrameBar
                        },
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        onUndo = { onAction(BoardAction.Undo) },
                        onRedo = { onAction(BoardAction.Redo) },
                        onMoreClick = {
                            showOptionsMenu = true
                        },
                        zoom = uiState.zoom,
                        isHost = uiState.isHost,
                        isBoardSwitchingLocked = uiState.isBoardSwitchingLocked,
                        onBirdsEyeClick = { showBirdsEyeSheet = true },
                        onOmniviewClick = { showOmniviewSheet = true },
                        onQuizClick = { showQuizDeckSheet = true },
                        onToggleCommandCenter = { showDesktopCommandCenter = !showDesktopCommandCenter },
                        isCommandCenterOpen = showDesktopCommandCenter
                    )

                    // 3-dot overflow menu
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 4.dp)) {
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.widthIn(min = 280.dp, max = 340.dp)
                        ) {
                            // 1. Connect & Share Feature Button
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = if (uiState.connectedPeers.isNotEmpty()) "Collaborating" else "Connect & Share",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (uiState.connectedPeers.isNotEmpty()) {
                                                "${uiState.connectedPeers.size} peer(s) connected"
                                            } else if (uiState.isAdvertising) {
                                                "Broadcasting (Room ${uiState.roomCode ?: ""})"
                                            } else if (uiState.isDiscovering) {
                                                "Scanning nearby..."
                                            } else {
                                                "Nearby & same network whiteboard"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (uiState.connectedPeers.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MaterialSymbol(
                                            name = if (uiState.connectedPeers.isNotEmpty()) "hub" else "devices",
                                            tint = if (uiState.connectedPeers.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            size = 20.dp
                                        )
                                    }
                                },
                                trailingIcon = {
                                    MaterialSymbol(name = "chevron_right", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onNavigateToDestination(NavDestination.PEERS)
                                }
                            )

                            // 2. Connected Devices Scrollable Row & Details
                            if (uiState.connectedPeers.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Connected devices (${uiState.connectedPeers.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        uiState.connectedPeers.forEach { peer ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(62.dp)
                                            ) {
                                                Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        val initial = peer.name.trim().take(1).uppercase().ifEmpty { "?" }
                                                        Text(
                                                            text = initial,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                    // Active status dot
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .align(Alignment.BottomEnd)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .padding(1.5.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF4CAF50))
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = peer.name.ifBlank { "Peer" },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (peer.role == MeshRole.HOST) "Host" else if (peer.isReadOnly) "Viewer" else "Editor",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    uiState.roomCode?.let { code ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Room Code: $code",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = if (uiState.isHost) "Hosting" else "Joined",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Export Diagram & Canvas (SVG/PNG)
                            DropdownMenuItem(
                                text = { Text("Export Diagram (SVG/PNG)") },
                                leadingIcon = {
                                    MaterialSymbol(name = "share", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showExportDialog = true
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            // Bird's Eye View (Shown only to host when collab is connected)
                            if (uiState.isHost && uiState.connectedPeers.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Bird's eye view") },
                                    leadingIcon = {
                                        MaterialSymbol(name = "filter_center_focus", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showBirdsEyeSheet = true
                                    }
                                )
                            }

                            // Classroom Omniview Matrix & Quiz Deck (Host only)
                            if (uiState.isHost) {
                                DropdownMenuItem(
                                    text = { Text("Omniview Matrix (Student Sheets)") },
                                    leadingIcon = {
                                        MaterialSymbol(name = "grid_view", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showOmniviewSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Interactive Quiz & Poll Deck") },
                                    leadingIcon = {
                                        MaterialSymbol(name = "checklist", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showQuizDeckSheet = true
                                    }
                                )
                            }

                            // Infinite Canvas Toggle
                            DropdownMenuItem(
                                text = { Text(if (uiState.isInfiniteCanvas) "Infinite Canvas: On" else "Fixed Sheet (16:9)") },
                                leadingIcon = {
                                    MaterialSymbol(
                                        name = if (uiState.isInfiniteCanvas) "all_inclusive" else "crop_free",
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 20.dp
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onAction(BoardAction.SetInfiniteCanvas(!uiState.isInfiniteCanvas))
                                }
                            )

                            // Presentation Mode
                            DropdownMenuItem(
                                text = { Text("Presentation Mode") },
                                leadingIcon = {
                                    MaterialSymbol(name = "slideshow", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onAction(BoardAction.SetPresentationMode(true))
                                }
                            )

                            // Google Slides Link
                            DropdownMenuItem(
                                text = { Text("Google Slides Link") },
                                leadingIcon = {
                                    MaterialSymbol(name = "link", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showGoogleSlidesDialog = true
                                }
                            )

                            // Import Presentation Slides
                            DropdownMenuItem(
                                text = { Text("Import Presentation (PDF/PPT)") },
                                leadingIcon = {
                                    MaterialSymbol(name = "file_present", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    launchPresentationPicker()
                                }
                            )

                            // Export Diagram & Canvas (SVG/PNG)
                            DropdownMenuItem(
                                text = { Text("Export Diagram (SVG/PNG)") },
                                leadingIcon = {
                                    MaterialSymbol(name = "share", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showExportDialog = true
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Clear Board
                            DropdownMenuItem(
                                text = { Text("Clear Board", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    MaterialSymbol(name = "delete_sweep", tint = MaterialTheme.colorScheme.error, size = 20.dp)
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onAction(BoardAction.ClearCanvas)
                                }
                            )
                        }
                    }
                }

                // Frame bar removed
            }
        }

        // 3. Minimap Overlay (Auto-shows when panning and zooming in free corner, stays locked until gesture ends)
        androidx.compose.animation.AnimatedVisibility(
            visible = isTransforming,
            enter = BrevityMotion.enterFadeScale(),
            exit = BrevityMotion.exitFadeScale(),
            modifier = when (minimapCorner) {
                MinimapCorner.TOP_START -> Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 58.dp, start = 14.dp)
                MinimapCorner.TOP_END -> Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 58.dp, end = 14.dp)
                MinimapCorner.BOTTOM_START -> Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp, start = 14.dp)
                MinimapCorner.BOTTOM_END -> Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp, end = 14.dp)
            }
        ) {
            WhiteboardMinimap(
                strokes = uiState.strokes,
                textElements = uiState.textElements,
                zoom = uiState.zoom,
                panOffset = uiState.panOffset,
                canvasSize = canvasSize,
                peerPresences = uiState.livePeerPresences,
                onPanTo = { onAction(BoardAction.SetCanvasTransform(uiState.zoom, it)) }
            )
        }

        // 4. Floating Contextual Selection Dock (Fill, Roughness, Layers, Text, Colors)
        if (selectedStrokes.isNotEmpty() || selectedTexts.isNotEmpty()) {
            FloatingSelectionDock(
                selectedStrokeIds = selectedStrokes,
                selectedTextIds = selectedTexts,
                strokes = uiState.strokes,
                onAction = onAction,
                onOpenEmbeddedTextEditor = { strokeId, currentText ->
                    val stroke = uiState.strokes.firstOrNull { it.id == strokeId }
                    if (stroke != null && stroke.points.isNotEmpty()) {
                        val midX = (stroke.points.minOf { it.x } + stroke.points.maxOf { it.x }) / 2f
                        val midY = (stroke.points.minOf { it.y } + stroke.points.maxOf { it.y }) / 2f
                        activeInlineTextPoint = Point(midX, midY)
                        inlineTextContent = currentText ?: ""
                        activeEditingStrokeId = strokeId
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 54.dp)
            )
        }

        // 5. Callout backdrop overlay (dismisses callouts when tapping canvas)
        if (activeCalloutTool != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        activeCalloutTool = null
                    }
            )
        }

        // 6. Tools Callout Cards anchored directly above the floating toolbar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 84.dp)
        ) {
            PenCalloutCard(
                visible = activeCalloutTool == "PEN",
                selectedTool = uiState.selectedTool,
                currentWidth = uiState.currentWidth,
                currentColor = effectivePenColor,
                onSelectTool = { tool ->
                    onAction(BoardAction.SelectTool(tool))
                },
                onChangeWidth = { width ->
                    onAction(BoardAction.ChangeStrokeWidth(width))
                },
                onSelectColor = { col ->
                    onAction(BoardAction.SelectColor(col))
                },
                onDismiss = { activeCalloutTool = null }
            )

            EraserCalloutCard(
                visible = activeCalloutTool == "ERASER",
                selectedTool = uiState.selectedTool,
                onSelectTool = { tool ->
                    onAction(BoardAction.SelectTool(tool))
                    activeCalloutTool = null
                },
                onClearCanvas = {
                    onAction(BoardAction.ClearCanvas)
                    activeCalloutTool = null
                }
            )

            SelectCalloutCard(
                visible = activeCalloutTool == "SELECT",
                selectedTool = uiState.selectedTool,
                onSelectTool = { tool ->
                    onAction(BoardAction.SelectTool(tool))
                    activeCalloutTool = null
                }
            )
        }

        // 7. Material 3 Expressive Horizontal Floating Toolbar with FAB
        CanvasBottomToolbar(
            uiState = uiState,
            activeCalloutTool = activeCalloutTool,
            onAction = onAction,
            onToggleCallout = { tool ->
                activeCalloutTool = if (activeCalloutTool == tool || tool.isEmpty()) null else tool
            },
            onOpenStickyNote = { showStickyNoteDialog = true },
            onOpenTextDialog = { point ->
                activeInlineTextPoint = point
                inlineTextContent = ""
            },
            onOpenGoogleSlides = { showGoogleSlidesDialog = true },
            onPickImage = { launchImagePicker() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 8. Sticky Note Creation Dialog
        WhiteboardStickyNoteDialog(
            visible = showStickyNoteDialog,
            onDismiss = { showStickyNoteDialog = false },
            onSave = { text, colorVal ->
                val center = Point(
                    x = -uiState.panOffset.x / uiState.zoom + 200f,
                    y = -uiState.panOffset.y / uiState.zoom + 200f
                )
                onAction(
                    BoardAction.AddStickyNote(
                        StickyNote(
                            text = text,
                            x = center.x,
                            y = center.y,
                            color = colorVal
                        )
                    )
                )
            }
        )

        // 9. Host-Only Bird's Eye View Radar Sheet
        BirdsEyeViewSheet(
            visible = showBirdsEyeSheet,
            isHost = uiState.isHost,
            strokes = uiState.strokes,
            textElements = uiState.textElements,
            stickyNotes = uiState.stickyNotes,
            peerPresences = uiState.livePeerPresences,
            connectedPeers = uiState.connectedPeers,
            zoom = uiState.zoom,
            panOffset = uiState.panOffset,
            canvasSize = canvasSize,
            onDismiss = { showBirdsEyeSheet = false },
            onJumpToPoint = { targetPan ->
                onAction(BoardAction.SetCanvasTransform(uiState.zoom, targetPan))
                showBirdsEyeSheet = false
            },
            onFitAllContent = {
                fitAllCanvasContent(uiState, canvasSize, onAction)
                showBirdsEyeSheet = false
            },
            onSummonAll = {
                onAction(BoardAction.SetCanvasTransform(uiState.zoom, uiState.panOffset))
                showBirdsEyeSheet = false
            }
        )

        // 10. Google Slides URL Input Dialog
        GoogleSlidesDialog(
            visible = showGoogleSlidesDialog,
            url = GoogleSlidesUrlInput,
            onUrlChange = { GoogleSlidesUrlInput = it },
            onDismiss = { showGoogleSlidesDialog = false },
            onConfirm = {
                if (GoogleSlidesUrlInput.isNotBlank()) {
                    onAction(BoardAction.SetPresentationUrl(GoogleSlidesUrlInput.trim()))
                    onAction(BoardAction.SetPresentationMode(true))
                }
                showGoogleSlidesDialog = false
            }
        )

        // 11. Board Rename Dialog
        CanvasRenameBoardDialog(
            visible = showRenameBoardDialog,
            title = renameBoardInput,
            onTitleChange = { renameBoardInput = it },
            onDismiss = { showRenameBoardDialog = false },
            onConfirm = {
                val newTitle = renameBoardInput.ifBlank { "Untitled" }
                onAction(BoardAction.RenameBoard(uiState.currentBoard.id, newTitle))
                showRenameBoardDialog = false
            }
        )

        // 12. Presentation Mode Fullscreen Overlay
        CanvasPresentationOverlay(
            uiState = uiState,
            onAction = onAction
        )

        // 13. Student Worksheet Pinned Top Banner
        StudentWorksheetBanner(
            worksheet = uiState.activeWorksheet,
            visible = uiState.isWorksheetMode && uiState.activeWorksheet != null,
            onSubmit = { onAction(BoardAction.SubmitWorksheet(it)) },
            onCloseOrToggle = { onAction(BoardAction.SetWorksheetMode(!uiState.isWorksheetMode)) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 54.dp)
        )

        // 14. Student Interactive Quiz & Poll Overlay (Mentimeter style)
        if (uiState.activeQuiz != null && !uiState.isHost) {
            StudentQuizOverlay(
                activeQuiz = uiState.activeQuiz,
                userAnswer = uiState.userQuizAnswer,
                submissions = uiState.quizSubmissions,
                timeRemainingSeconds = uiState.quizTimeRemainingSeconds,
                onAnswer = { qId, opt -> onAction(BoardAction.AnswerQuiz(qId, opt)) },
                onDismiss = { onAction(BoardAction.EndQuiz(uiState.activeQuiz?.quizId ?: "", -1)) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 15. Host Omniview Matrix Sheet (Mobile / Compact View)
        HostOmniviewMatrixSheet(
            visible = showOmniviewSheet,
            isHost = uiState.isHost,
            studentWorksheets = uiState.studentWorksheets,
            connectedPeers = uiState.connectedPeers,
            inspectedStudentId = inspectedStudentId,
            onDismiss = { showOmniviewSheet = false; inspectedStudentId = null },
            onDelegateWorksheet = { title, q, target ->
                onAction(BoardAction.DelegateWorksheet(title, q, target))
            },
            onInspectStudent = { inspectedStudentId = it }
        )

        // 16. Host Quiz & Poll Control Deck (Mobile / Compact View)
        HostQuizControlDeck(
            visible = showQuizDeckSheet,
            isHost = uiState.isHost,
            activeQuiz = uiState.activeQuiz,
            submissions = uiState.quizSubmissions,
            timeRemainingSeconds = uiState.quizTimeRemainingSeconds,
            connectedPeers = uiState.connectedPeers,
            onLaunchQuiz = { onAction(BoardAction.StartQuiz(it)) },
            onEndQuiz = { qId, key -> onAction(BoardAction.EndQuiz(qId, key)) },
            onDismiss = { showQuizDeckSheet = false }
        )

        // 17. High-Fidelity Canvas Export Dialog (SVG/PNG/Clipboard)
        if (showExportDialog) {
            CanvasExportDialog(
                boardTitle = uiState.currentBoard.title,
                strokes = uiState.strokes,
                texts = uiState.textElements,
                stickyNotes = uiState.stickyNotes,
                selectedStrokeIds = selectedStrokes,
                backgroundColor = effectiveCanvasBg.toArgb().toLong(),
                onDismiss = { showExportDialog = false },
                onExportFile = { format, isTransparent, isSelectionOnly ->
                    shareBoard(uiState.currentBoard, format)
                }
            )
        }
    }

    // Desktop Command Center Side-Deck (Expanded screens >= 840dp)
    if (isExpandedScreen && showDesktopCommandCenter && uiState.isHost) {
        DesktopCommandCenter(
            uiState = uiState,
            onAction = onAction,
            onClose = { showDesktopCommandCenter = false },
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
        )
    }
}
}
}
