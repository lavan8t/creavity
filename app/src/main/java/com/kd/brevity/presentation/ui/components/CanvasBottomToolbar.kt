package com.kd.brevity.presentation.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.domain.model.Point
import com.kd.brevity.presentation.ui.OotbAction
import com.kd.brevity.presentation.viewmodel.WhiteboardUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolbarIconButton(
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
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CanvasBottomToolbar(
    uiState: WhiteboardUiState,
    activeCalloutTool: String?,
    onAction: (OotbAction) -> Unit,
    onToggleCallout: (String) -> Unit,
    onOpenStickyNote: () -> Unit,
    onOpenTextDialog: (Point) -> Unit,
    onOpenGoogleSlides: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

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
            HorizontalFloatingToolbar(
                expanded = true,
                colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                content = {
                    // 1. Pen tool button
                    val isPen = uiState.selectedTool in listOf(
                        DrawingTool.PEN,
                        DrawingTool.MARKER,
                        DrawingTool.HIGHLIGHTER,
                        DrawingTool.BRUSH
                    )
                    val penIcon = when (uiState.selectedTool) {
                        DrawingTool.MARKER -> "edit"
                        DrawingTool.HIGHLIGHTER -> "ink_highlighter"
                        DrawingTool.BRUSH -> "brush"
                        else -> "draw"
                    }
                    ToolbarIconButton(
                        icon = penIcon,
                        tooltip = "Pen (long-press for options)",
                        isSelected = isPen,
                        tint = if (isPen) uiState.currentColor else null,
                        onClick = {
                            onToggleCallout("")
                            onAction(OotbAction.SelectTool(DrawingTool.PEN))
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
                            onAction(OotbAction.SelectTool(DrawingTool.ERASER))
                        },
                        onLongClick = {
                            onToggleCallout("ERASER")
                        }
                    )

                    // 3. Select / Move button
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
                            onAction(OotbAction.SelectTool(DrawingTool.SELECT_MOVE))
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

                    // 5. Undo / Redo button
                    ToolbarIconButton(
                        icon = "undo",
                        tooltip = "Undo (tap) / Redo (long-press)",
                        enabled = uiState.canUndo || uiState.canRedo,
                        onClick = {
                            if (uiState.canUndo) {
                                onAction(OotbAction.Undo)
                            }
                        },
                        onLongClick = {
                            onToggleCallout("UNDO_REDO")
                        }
                    )
                }
            )

            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    TooltipHint(text = if (fabMenuExpanded) "Close menu" else "Add & tools") {
                        ToggleFloatingActionButton(
                            checked = fabMenuExpanded,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                fabMenuExpanded = !fabMenuExpanded
                            }
                        ) {
                            val iconName = if (checkedProgress > 0.5f) "close" else "add"
                            MaterialSymbol(
                                name = iconName,
                                filled = true,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                size = 24.dp,
                                modifier = Modifier.animateIcon({ checkedProgress })
                            )
                        }
                    }
                },
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onOpenStickyNote()
                    },
                    text = {
                        Text(
                            text = "Sticky note",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { MaterialSymbol(name = "note_add", size = 20.dp, tint = LocalContentColor.current) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onPickImage()
                    },
                    text = {
                        Text(
                            text = "Image",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { MaterialSymbol(name = "image", size = 20.dp, tint = LocalContentColor.current) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onOpenGoogleSlides()
                    },
                    text = {
                        Text(
                            text = "Presentation",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { MaterialSymbol(name = "slideshow", size = 20.dp, tint = LocalContentColor.current) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        val point = Point(
                            x = -uiState.panOffset.x / uiState.zoom + 300f,
                            y = -uiState.panOffset.y / uiState.zoom + 300f
                        )
                        onOpenTextDialog(point)
                    },
                    text = {
                        Text(
                            text = "Text",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { MaterialSymbol(name = "title", size = 20.dp, tint = LocalContentColor.current) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onAction(OotbAction.SelectTool(DrawingTool.CIRCLE))
                    },
                    text = {
                        Text(
                            text = "Circle",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { MaterialSymbol(name = "circle", size = 20.dp, tint = LocalContentColor.current) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onAction(OotbAction.SelectTool(DrawingTool.RECTANGLE))
                    },
                    text = {
                        Text(
                            text = "Rectangle",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { MaterialSymbol(name = "rectangle", size = 20.dp, tint = LocalContentColor.current) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onAction(OotbAction.SelectTool(DrawingTool.LASER))
                    },
                    text = {
                        Text(
                            text = "Laser pointer",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = {
                        MaterialSymbol(
                            name = "flare",
                            size = 20.dp,
                            tint = LocalContentColor.current,
                            filled = uiState.selectedTool == DrawingTool.LASER
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.wrapContentWidth().heightIn(min = 40.dp).padding(vertical = 1.dp)
                )
            }
        }
    }
}
