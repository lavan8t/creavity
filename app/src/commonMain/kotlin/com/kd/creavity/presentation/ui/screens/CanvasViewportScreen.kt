package com.kd.creavity.presentation.ui.screens

import com.kd.creavity.util.FilePickerType
import com.kd.creavity.util.rememberFilePickerLauncher
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

    // Host Bird's Eye View
    var showBirdsEyeSheet by remember { mutableStateOf(false) }

    // Selection state
    var selectedStrokes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedTexts by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectionBounds by remember { mutableStateOf<Rect?>(null) }

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

    val effectiveCanvasBg = if (uiState.backgroundColor == Color.Unspecified || uiState.backgroundColor == Color(0xFF18181C)) MaterialTheme.colorScheme.surface else uiState.backgroundColor
    val isDarkCanvas = effectiveCanvasBg.luminance() < 0.5f
    val effectivePenColor = CanvasColorUtils.getAdaptiveStrokeColor(
        if (uiState.currentColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else uiState.currentColor,
        isDarkCanvas
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        // 1. Drawing Canvas
        WhiteboardCanvas(
            strokes = uiState.strokes,
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
            onStrokeCompleted = { onAction(BoardAction.StrokeCompleted(it)) },
            onStrokeBroadcastStart = { id, c, w, e, pt, tool -> onAction(BoardAction.StrokeBroadcastStart(id, c, w, e, pt, tool)) },
            onStrokeBroadcastPoint = { id, pt -> onAction(BoardAction.StrokeBroadcastPoint(id, pt)) },
            onStrokeBroadcastEnd = { onAction(BoardAction.StrokeBroadcastEnd(it)) },
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

            Box(
                modifier = Modifier
                    .offset { IntOffset(screenX, screenY) }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        onCastClick = {
                            onNavigateToDestination(NavDestination.PEERS)
                        },
                        onMoreClick = {
                            showOptionsMenu = true
                        },
                        zoom = uiState.zoom
                    )

                    // 3-dot overflow menu
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 4.dp)) {
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
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
        AnimatedVisibility(
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

        // 4. Selection Context Bar (Duplicate, Recolor, Delete)
        CanvasSelectionBar(
            selectedCount = selectedStrokes.size + selectedTexts.size,
            onDuplicate = {
                onAction(BoardAction.DuplicateElements(selectedStrokes, selectedTexts))
            },
            onRecolor = {
                onAction(BoardAction.RecolorElements(selectedStrokes, selectedTexts, effectivePenColor))
            },
            onDelete = {
                selectedStrokes.forEach { onAction(BoardAction.DeleteStroke(it)) }
                selectedTexts.forEach { onAction(BoardAction.DeleteText(it)) }
                selectedStrokes = emptyList()
                selectedTexts = emptyList()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 54.dp)
        )

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

            UndoRedoCalloutCard(
                visible = activeCalloutTool == "UNDO_REDO",
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onUndo = { onAction(BoardAction.Undo) },
                onRedo = { onAction(BoardAction.Redo) }
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
    }
}
