package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import kotlin.math.roundToInt

/**
 * Draggable Desktop Material 3 Expressive Toolbar.
 *
 * Positions the drawing toolset as a sleek, vertical dock on the left side of the canvas by default,
 * and allows users to freely drag and position it anywhere on their screen.
 */
@Composable
fun DesktopDraggableToolbar(
    uiState: WhiteboardUiState,
    activeCalloutTool: String?,
    canvasSize: IntSize,
    onAction: (BoardAction) -> Unit,
    onToggleCallout: (String) -> Unit,
    onOpenStickyNote: () -> Unit,
    onOpenTextDialog: (Point) -> Unit,
    onToolbarPositionChanged: (Offset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val toolbarWidthPx = 64f * density
    val toolbarHeightPx = 520f * density

    var rawOffset by remember { mutableStateOf(Offset(24f * density, 90f * density)) }

    val maxX = (canvasSize.width.toFloat() - toolbarWidthPx - 16f).coerceAtLeast(16f)
    val maxY = (canvasSize.height.toFloat() - toolbarHeightPx - 16f).coerceAtLeast(70f)
    val clampedX = rawOffset.x.coerceIn(16f, maxX)
    val clampedY = rawOffset.y.coerceIn(70f, maxY)
    val currentClampedOffset = Offset(clampedX, clampedY)

    onToolbarPositionChanged(currentClampedOffset)

    val isPen = uiState.selectedTool in listOf(
        DrawingTool.PEN,
        DrawingTool.PENCIL,
        DrawingTool.CALLIGRAPHY,
        DrawingTool.MARKER
    )
    val penIcon = when (uiState.selectedTool) {
        DrawingTool.PENCIL -> "pencil"
        DrawingTool.CALLIGRAPHY -> "calligraphy"
        DrawingTool.MARKER -> "edit"
        else -> "pen"
    }

    val isHighlighter = uiState.selectedTool in listOf(
        DrawingTool.HIGHLIGHTER,
        DrawingTool.BRUSH
    )
    val highlighterIcon = if (uiState.selectedTool == DrawingTool.BRUSH) "brush" else "ink_highlighter"

    val isEraser = uiState.selectedTool == DrawingTool.ERASER || uiState.selectedTool == DrawingTool.SELECT_ERASE
    val eraserIcon = if (uiState.selectedTool == DrawingTool.SELECT_ERASE) "highlight_off" else "eraser"

    val isShape = uiState.selectedTool in listOf(
        DrawingTool.RECTANGLE,
        DrawingTool.CIRCLE,
        DrawingTool.TRIANGLE,
        DrawingTool.STAR,
        DrawingTool.ARROW,
        DrawingTool.LINE
    )
    val shapeIcon = when (uiState.selectedTool) {
        DrawingTool.CIRCLE -> "circle"
        DrawingTool.TRIANGLE -> "triangle"
        DrawingTool.STAR -> "star"
        DrawingTool.ARROW -> "arrow_forward"
        DrawingTool.LINE -> "arrow_right_alt"
        DrawingTool.RECTANGLE -> "rectangle"
        else -> "shapes"
    }

    val isSelect = uiState.selectedTool in listOf(
        DrawingTool.SELECT,
        DrawingTool.SELECT_MOVE,
        DrawingTool.RECT_SELECT,
        DrawingTool.LASSO_SELECT
    )
    val selectIcon = when (uiState.selectedTool) {
        DrawingTool.RECT_SELECT -> "crop_free"
        DrawingTool.LASSO_SELECT -> "gesture"
        else -> "cursor"
    }

    Surface(
        modifier = modifier
            .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
            .width(56.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            // Drag Handle Indicator Header
            Box(
                modifier = Modifier
                    .fillParentWidth()
                    .padding(vertical = 6.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rawOffset += dragAmount
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )
            }

            // 1. Pen Tool
            DesktopToolButton(
                icon = penIcon,
                label = "Pen",
                isSelected = isPen,
                onClick = {
                    if (isPen) {
                        onToggleCallout("PEN")
                    } else {
                        onAction(BoardAction.SelectTool(DrawingTool.PEN))
                    }
                }
            )

            // 2. Highlighter Tool
            DesktopToolButton(
                icon = highlighterIcon,
                label = "Highlighter",
                isSelected = isHighlighter,
                onClick = { onAction(BoardAction.SelectTool(DrawingTool.HIGHLIGHTER)) }
            )

            // 3. Eraser Tool
            DesktopToolButton(
                icon = eraserIcon,
                label = "Eraser",
                isSelected = isEraser,
                onClick = {
                    if (isEraser) {
                        onToggleCallout("ERASER")
                    } else {
                        onAction(BoardAction.SelectTool(DrawingTool.ERASER))
                    }
                }
            )

            // 4. Shapes Tool (Word / PowerPoint office shapes)
            DesktopToolButton(
                icon = shapeIcon,
                label = "Shapes",
                isSelected = isShape,
                onClick = { onToggleCallout("SHAPES") }
            )

            // 5. Select / Transform Tool
            DesktopToolButton(
                icon = selectIcon,
                label = "Select",
                isSelected = isSelect,
                onClick = {
                    if (isSelect) {
                        onToggleCallout("SELECT")
                    } else {
                        onAction(BoardAction.SelectTool(DrawingTool.SELECT))
                    }
                }
            )

            // 6. Sticky Note
            DesktopToolButton(
                icon = "sticky_note_2",
                label = "Sticky Note",
                isSelected = uiState.selectedTool == DrawingTool.STICKY_NOTE,
                onClick = onOpenStickyNote
            )

            // 7. Text Tool
            DesktopToolButton(
                icon = "text_fields",
                label = "Text",
                isSelected = uiState.selectedTool == DrawingTool.TEXT,
                onClick = {
                    onAction(BoardAction.SelectTool(DrawingTool.TEXT))
                    val viewCenterWorld = Point(
                        (-uiState.panOffset.x + canvasSize.width / 2f) / uiState.zoom,
                        (-uiState.panOffset.y + canvasSize.height / 2f) / uiState.zoom
                    )
                    onOpenTextDialog(viewCenterWorld)
                }
            )

            HorizontalDivider(
                modifier = Modifier
                    .width(32.dp)
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // 8. Undo
            IconButton(
                onClick = { onAction(BoardAction.Undo) },
                enabled = uiState.canUndo,
                modifier = Modifier.size(40.dp)
            ) {
                MaterialSymbol(
                    name = "undo",
                    size = 20.dp,
                    tint = if (uiState.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            // 9. Redo
            IconButton(
                onClick = { onAction(BoardAction.Redo) },
                enabled = uiState.canRedo,
                modifier = Modifier.size(40.dp)
            ) {
                MaterialSymbol(
                    name = "redo",
                    size = 20.dp,
                    tint = if (uiState.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Composable
private fun DesktopToolButton(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(40.dp)
    ) {
        MaterialSymbol(
            name = icon,
            size = 22.dp,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Modifier.fillParentWidth(): Modifier = this.width(48.dp)
