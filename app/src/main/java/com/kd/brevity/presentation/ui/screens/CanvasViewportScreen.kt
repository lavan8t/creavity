package com.kd.brevity.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.blur.BlurStop
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
import com.kd.brevity.domain.model.Point
import com.kd.brevity.domain.model.StickyNote
import com.kd.brevity.presentation.canvas.WhiteboardCanvas
import com.kd.brevity.presentation.ui.NavDestination
import com.kd.brevity.presentation.ui.OotbAction
import com.kd.brevity.presentation.ui.components.BirdsEyeViewSheet
import com.kd.brevity.presentation.ui.components.CanvasBottomToolbar
import com.kd.brevity.presentation.ui.components.CanvasPresentationOverlay
import com.kd.brevity.presentation.ui.components.CanvasRenameBoardDialog
import com.kd.brevity.presentation.ui.components.CanvasSelectionBar
import com.kd.brevity.presentation.ui.components.EraserCalloutCard
import com.kd.brevity.presentation.ui.components.GoogleSlidesDialog
import com.kd.brevity.presentation.ui.components.JamboardFrameBar
import com.kd.brevity.presentation.ui.components.JamboardStickyNoteDialog
import com.kd.brevity.presentation.ui.components.JamboardTopBar
import com.kd.brevity.presentation.ui.components.MaterialSymbol
import com.kd.brevity.presentation.ui.components.PenCalloutCard
import com.kd.brevity.presentation.ui.components.SelectCalloutCard
import com.kd.brevity.presentation.ui.components.UndoRedoCalloutCard
import com.kd.brevity.presentation.ui.components.WhiteboardMinimap
import com.kd.brevity.presentation.ui.motion.BrevityMotion
import com.kd.brevity.presentation.viewmodel.WhiteboardUiState
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
    onAction: (OotbAction) -> Unit
) {
    val strokes = uiState.strokes
    val texts = uiState.textElements
    val stickyNotes = uiState.stickyNotes

    if (strokes.isEmpty() && texts.isEmpty() && stickyNotes.isEmpty()) {
        onAction(OotbAction.ResetCanvasTransform)
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

    onAction(OotbAction.SetCanvasTransform(targetZoom, Offset(targetPanX, targetPanY)))
}

@Composable
fun CanvasViewportScreen(
    modifier: Modifier = Modifier,
    uiState: WhiteboardUiState,
    onAction: (OotbAction) -> Unit,
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
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAction(OotbAction.AddImage(it)) }
    }

    // Presentation Pickers & Dialogs
    val presentationPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onAction(OotbAction.ImportPresentation(it)) }
    }
    var showGoogleSlidesDialog by remember { mutableStateOf(false) }
    var googleSlidesUrlInput by remember { mutableStateOf(uiState.presentationUrl ?: "") }

    val effectiveCanvasBg = if (uiState.backgroundColor == Color.Unspecified || uiState.backgroundColor == Color(0xFF18181C)) MaterialTheme.colorScheme.surface else uiState.backgroundColor
    val isDarkCanvas = effectiveCanvasBg.luminance() < 0.5f
    val effectivePenColor = if (uiState.currentColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        uiState.currentColor
    }

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
            onTransform = { z, focal, p -> onAction(OotbAction.TransformCanvas(z, focal, p)) },
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
            onStrokeCompleted = { onAction(OotbAction.StrokeCompleted(it)) },
            onStrokeBroadcastStart = { id, c, w, e, pt -> onAction(OotbAction.StrokeBroadcastStart(id, c, w, e, pt)) },
            onStrokeBroadcastPoint = { id, pt -> onAction(OotbAction.StrokeBroadcastPoint(id, pt)) },
            onStrokeBroadcastEnd = { onAction(OotbAction.StrokeBroadcastEnd(it)) },
            onDeleteStroke = { onAction(OotbAction.DeleteStroke(it)) },
            onDeleteText = { onAction(OotbAction.DeleteText(it)) },
            onDeleteImage = { onAction(OotbAction.DeleteImage(it)) },
            onDeleteStickyNote = { onAction(OotbAction.DeleteStickyNote(it)) },
            onElementsMoved = { s, t, im -> onAction(OotbAction.MoveElements(s, t, im)) },
            onSelectionChanged = { s, t, bounds ->
                selectedStrokes = s
                selectedTexts = t
                selectionBounds = bounds
            },
            onTapForText = { pt ->
                if (activeInlineTextPoint != null && inlineTextContent.isNotBlank()) {
                    onAction(
                        OotbAction.AddText(
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
            modifier = Modifier
                .fillMaxSize()
                .blur {
                    radius = BlurRadiusSpec.verticalGradient(
                        listOf(
                            BlurStop(fraction = 0f, radius = 28.dp),
                            BlurStop(fraction = 0.08f, radius = 0.dp),
                            BlurStop(fraction = 0.88f, radius = 0.dp),
                            BlurStop(fraction = 1f, radius = 28.dp)
                        )
                    )
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                }
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
                                        OotbAction.AddText(
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
                                    OotbAction.AddText(
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

        // 2. Top Navigation Bar (Authentic Jamboard Top Bar + Expandable Frame Bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    JamboardTopBar(
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
                                onAction(OotbAction.SwitchPage(uiState.currentBoard.activePageIndex - 1))
                            }
                        },
                        onNextPage = {
                            if (uiState.currentBoard.activePageIndex < uiState.currentBoard.pages.size - 1) {
                                onAction(OotbAction.SwitchPage(uiState.currentBoard.activePageIndex + 1))
                            }
                        },
                        onAddPage = {
                            onAction(OotbAction.AddNewPage)
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
                        zoom = uiState.zoom,
                        isHost = uiState.isHost,
                        onBirdsEyeClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showBirdsEyeSheet = true
                        }
                    )

                    // 3-dot overflow menu
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 4.dp)) {
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            // Host Bird's Eye View
                            if (uiState.isHost) {
                                DropdownMenuItem(
                                    text = { Text("Bird's Eye View (Host)") },
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
                                    onAction(OotbAction.SetInfiniteCanvas(!uiState.isInfiniteCanvas))
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
                                    onAction(OotbAction.SetPresentationMode(true))
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
                                    try {
                                        presentationPickerLauncher.launch(
                                            arrayOf(
                                                "application/pdf",
                                                "application/vnd.ms-powerpoint",
                                                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                                            )
                                        )
                                    } catch (_: Exception) {}
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
                                    onAction(OotbAction.ClearCanvas)
                                }
                            )
                        }
                    }
                }

                // Expandable Frame Bar
                JamboardFrameBar(
                    visible = showFrameBar,
                    pages = uiState.currentBoard.pages,
                    activePageIndex = uiState.currentBoard.activePageIndex,
                    onSelectPage = {
                        onAction(OotbAction.SwitchPage(it))
                        showFrameBar = false
                    },
                    onAddPage = {
                        onAction(OotbAction.AddNewPage)
                    },
                    onDeletePage = {
                        if (uiState.currentBoard.pages.size > 1) {
                            onAction(OotbAction.DeletePage(it))
                        }
                    },
                    onClose = { showFrameBar = false }
                )
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
                onPanTo = { onAction(OotbAction.SetCanvasTransform(uiState.zoom, it)) }
            )
        }

        // 4. Selection Context Bar (Duplicate, Recolor, Delete)
        CanvasSelectionBar(
            selectedCount = selectedStrokes.size + selectedTexts.size,
            onDuplicate = {
                onAction(OotbAction.DuplicateElements(selectedStrokes, selectedTexts))
            },
            onRecolor = {
                onAction(OotbAction.RecolorElements(selectedStrokes, selectedTexts, effectivePenColor))
            },
            onDelete = {
                selectedStrokes.forEach { onAction(OotbAction.DeleteStroke(it)) }
                selectedTexts.forEach { onAction(OotbAction.DeleteText(it)) }
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
                    onAction(OotbAction.SelectTool(tool))
                },
                onChangeWidth = { width ->
                    onAction(OotbAction.ChangeStrokeWidth(width))
                },
                onSelectColor = { col ->
                    onAction(OotbAction.SelectColor(col))
                },
                onDismiss = { activeCalloutTool = null }
            )

            EraserCalloutCard(
                visible = activeCalloutTool == "ERASER",
                selectedTool = uiState.selectedTool,
                onSelectTool = { tool ->
                    onAction(OotbAction.SelectTool(tool))
                    activeCalloutTool = null
                },
                onClearCanvas = {
                    onAction(OotbAction.ClearCanvas)
                    activeCalloutTool = null
                }
            )

            SelectCalloutCard(
                visible = activeCalloutTool == "SELECT",
                selectedTool = uiState.selectedTool,
                onSelectTool = { tool ->
                    onAction(OotbAction.SelectTool(tool))
                    activeCalloutTool = null
                }
            )

            UndoRedoCalloutCard(
                visible = activeCalloutTool == "UNDO_REDO",
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onUndo = { onAction(OotbAction.Undo) },
                onRedo = { onAction(OotbAction.Redo) }
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
            onPickImage = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 8. Sticky Note Creation Dialog
        JamboardStickyNoteDialog(
            visible = showStickyNoteDialog,
            onDismiss = { showStickyNoteDialog = false },
            onSave = { text, colorVal ->
                val center = Point(
                    x = -uiState.panOffset.x / uiState.zoom + 200f,
                    y = -uiState.panOffset.y / uiState.zoom + 200f
                )
                onAction(
                    OotbAction.AddStickyNote(
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
                onAction(OotbAction.SetCanvasTransform(uiState.zoom, targetPan))
                showBirdsEyeSheet = false
            },
            onFitAllContent = {
                fitAllCanvasContent(uiState, canvasSize, onAction)
                showBirdsEyeSheet = false
            },
            onSummonAll = {
                onAction(OotbAction.SetCanvasTransform(uiState.zoom, uiState.panOffset))
                showBirdsEyeSheet = false
            }
        )

        // 10. Google Slides URL Input Dialog
        GoogleSlidesDialog(
            visible = showGoogleSlidesDialog,
            url = googleSlidesUrlInput,
            onUrlChange = { googleSlidesUrlInput = it },
            onDismiss = { showGoogleSlidesDialog = false },
            onConfirm = {
                if (googleSlidesUrlInput.isNotBlank()) {
                    onAction(OotbAction.SetPresentationUrl(googleSlidesUrlInput.trim()))
                    onAction(OotbAction.SetPresentationMode(true))
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
                onAction(OotbAction.RenameBoard(uiState.currentBoard.id, newTitle))
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
