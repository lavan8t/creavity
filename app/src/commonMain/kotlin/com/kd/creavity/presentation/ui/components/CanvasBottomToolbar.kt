package com.kd.creavity.presentation.ui.components

import com.kd.creavity.util.PlatformBackHandler as BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolbarIconButton(
    icon: String,
    tooltip: String,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    tint: Color? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    TooltipHint(text = tooltip) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent,
            contentColor = tint ?: if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                           else if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    enabled = enabled,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                    onLongClick = onLongClick?.let { action ->
                        {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            action()
                        }
                    }
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                MaterialSymbol(
                    name = icon,
                    filled = isSelected,
                    tint = tint ?: if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                   else if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    size = 24.dp
                )
            }
        }
    }
}

@Composable
fun CanvasBottomToolbar(
    uiState: WhiteboardUiState,
    activeCalloutTool: String?,
    onAction: (BoardAction) -> Unit,
    onToggleCallout: (String) -> Unit,
    onOpenStickyNote: () -> Unit,
    onOpenTextDialog: (Point) -> Unit,
    onOpenGoogleSlides: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val fabFocusRequester = remember { FocusRequester() }

    val isShape = uiState.selectedTool in listOf(
        DrawingTool.RECTANGLE,
        DrawingTool.CIRCLE,
        DrawingTool.TRIANGLE,
        DrawingTool.STAR,
        DrawingTool.ARROW,
        DrawingTool.LINE
    )

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    LaunchedEffect(fabMenuExpanded) {
        if (fabMenuExpanded) {
            fabFocusRequester.requestFocus()
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Floating Toolbar Container
            Surface(
                shape = CircleShape,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. Pen tool button
                    val isPen = uiState.selectedTool in listOf(
                        DrawingTool.PEN,
                        DrawingTool.PENCIL,
                        DrawingTool.CALLIGRAPHY,
                        DrawingTool.MARKER,
                        DrawingTool.HIGHLIGHTER,
                        DrawingTool.BRUSH
                    )
                    val penIcon = when (uiState.selectedTool) {
                        DrawingTool.PENCIL -> "pencil"
                        DrawingTool.CALLIGRAPHY -> "calligraphy"
                        DrawingTool.MARKER -> "edit"
                        DrawingTool.HIGHLIGHTER -> "ink_highlighter"
                        DrawingTool.BRUSH -> "brush"
                        else -> "draw"
                    }
                    ToolbarIconButton(
                        icon = penIcon,
                        tooltip = "Pen (long-press for options)",
                        isSelected = isPen,
                        tint = if (isPen && uiState.currentColor != Color.Unspecified) uiState.currentColor else null,
                        onClick = {
                            onToggleCallout("")
                            onAction(BoardAction.SelectTool(DrawingTool.PEN))
                        },
                        onLongClick = {
                            onToggleCallout("PEN")
                        }
                    )

                    // 2. Eraser tool button
                    val isEraser = uiState.selectedTool == DrawingTool.ERASER || uiState.selectedTool == DrawingTool.SELECT_ERASE
                    val eraserIcon = if (uiState.selectedTool == DrawingTool.SELECT_ERASE) "highlight_off" else "ink_eraser"
                    ToolbarIconButton(
                        icon = eraserIcon,
                        tooltip = "Eraser (long-press for stroke/object/clear)",
                        isSelected = isEraser,
                        onClick = {
                            onToggleCallout("")
                            onAction(BoardAction.SelectTool(DrawingTool.ERASER))
                        },
                        onLongClick = {
                            onToggleCallout("ERASER")
                        }
                    )

                    // 3. Shapes tool button
                    val shapeIcon = when (uiState.selectedTool) {
                        DrawingTool.CIRCLE -> "circle"
                        DrawingTool.TRIANGLE -> "triangle"
                        DrawingTool.STAR -> "star"
                        DrawingTool.ARROW -> "arrow_forward"
                        DrawingTool.LINE -> "horizontal_rule"
                        DrawingTool.RECTANGLE -> "rectangle"
                        else -> "category"
                    }
                    ToolbarIconButton(
                        icon = shapeIcon,
                        tooltip = "Shapes (click or long-press for shapes)",
                        isSelected = isShape,
                        onClick = {
                            if (!isShape) {
                                onAction(BoardAction.SelectTool(DrawingTool.RECTANGLE))
                            }
                            onToggleCallout("SHAPES")
                        },
                        onLongClick = {
                            onToggleCallout("SHAPES")
                        }
                    )

                    // 4. Select / Move button
                    val isSelect = uiState.selectedTool in listOf(
                        DrawingTool.SELECT_MOVE,
                        DrawingTool.RECT_SELECT,
                        DrawingTool.LASSO_SELECT
                    )
                    val selectIcon = when (uiState.selectedTool) {
                        DrawingTool.RECT_SELECT -> "crop_free"
                        DrawingTool.LASSO_SELECT -> "gesture"
                        else -> "pan_tool_alt"
                    }
                    ToolbarIconButton(
                        icon = selectIcon,
                        tooltip = "Select (long-press for move, rect & lasso)",
                        isSelected = isSelect,
                        onClick = {
                            onToggleCallout("")
                            onAction(BoardAction.SelectTool(DrawingTool.SELECT_MOVE))
                        },
                        onLongClick = {
                            onToggleCallout("SELECT")
                        }
                    )

                    // 4. Sticky note quick shortcut button
                    ToolbarIconButton(
                        icon = "sticky_note_2",
                        tooltip = "Sticky note",
                        onClick = {
                            onToggleCallout("")
                            onOpenStickyNote()
                        }
                    )
                }
            }

            // Expandable Add / Extra Tools FAB
            Box(contentAlignment = Alignment.BottomEnd) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = fabMenuExpanded,
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { it / 2 } + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .padding(bottom = 68.dp)
                            .focusRequester(fabFocusRequester)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            FabMenuItem(
                                icon = "note_add",
                                label = "Sticky note",
                                onClick = {
                                    fabMenuExpanded = false
                                    onOpenStickyNote()
                                }
                            )
                            FabMenuItem(
                                icon = "image",
                                label = "Image",
                                onClick = {
                                    fabMenuExpanded = false
                                    onPickImage()
                                }
                            )
                            FabMenuItem(
                                icon = "slideshow",
                                label = "Presentation",
                                onClick = {
                                    fabMenuExpanded = false
                                    onOpenGoogleSlides()
                                }
                            )
                            FabMenuItem(
                                icon = "title",
                                label = "Text",
                                onClick = {
                                    fabMenuExpanded = false
                                    val point = Point(
                                        x = -uiState.panOffset.x / uiState.zoom + 300f,
                                        y = -uiState.panOffset.y / uiState.zoom + 300f
                                    )
                                    onOpenTextDialog(point)
                                }
                            )
                            FabMenuItem(
                                icon = "category",
                                label = "Shapes",
                                filled = isShape,
                                onClick = {
                                    fabMenuExpanded = false
                                    if (!isShape) {
                                        onAction(BoardAction.SelectTool(DrawingTool.RECTANGLE))
                                    }
                                    onToggleCallout("SHAPES")
                                }
                            )
                            FabMenuItem(
                                icon = "flare",
                                label = "Laser pointer",
                                filled = uiState.selectedTool == DrawingTool.LASER,
                                onClick = {
                                    fabMenuExpanded = false
                                    onAction(BoardAction.SelectTool(DrawingTool.LASER))
                                }
                            )
                        }
                    }
                }

                TooltipHint(text = if (fabMenuExpanded) "Close menu" else "Add & tools") {
                    FloatingActionButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            fabMenuExpanded = !fabMenuExpanded
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        MaterialSymbol(
                            name = if (fabMenuExpanded) "close" else "add",
                            filled = true,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            size = 24.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FabMenuItem(
    icon: String,
    label: String,
    onClick: () -> Unit,
    filled: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialSymbol(
                name = icon,
                size = 20.dp,
                filled = filled,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
