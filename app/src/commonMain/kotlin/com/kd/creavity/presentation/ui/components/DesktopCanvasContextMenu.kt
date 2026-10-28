package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.Point

/**
 * Material 3 Expressive Right-Click Context Menu for Desktop Canvas.
 * Supports context-sensitive operations for both selection mode and open canvas mode.
 */
@Composable
fun DesktopCanvasContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    screenOffset: Offset,
    hasSelection: Boolean,
    onDuplicate: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDeleteSelection: () -> Unit,
    onDeselect: () -> Unit,
    onAddStickyNote: () -> Unit,
    onInsertText: () -> Unit,
    onInsertRectangle: () -> Unit,
    onInsertCircle: () -> Unit,
    onSelectAll: () -> Unit,
    onResetZoom: () -> Unit,
    onClearCanvas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val dpOffset = DpOffset(
        x = (screenOffset.x / density).dp,
        y = (screenOffset.y / density).dp
    )

    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = dpOffset,
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            if (hasSelection) {
                // Selection Context Actions
                ContextMenuItem(
                    icon = "content_copy",
                    title = "Duplicate",
                    shortcut = "Ctrl+D",
                    onClick = {
                        onDismissRequest()
                        onDuplicate()
                    }
                )
                ContextMenuItem(
                    icon = "arrow_upward",
                    title = "Bring to Front",
                    shortcut = "Ctrl+]",
                    onClick = {
                        onDismissRequest()
                        onBringToFront()
                    }
                )
                ContextMenuItem(
                    icon = "arrow_downward",
                    title = "Send to Back",
                    shortcut = "Ctrl+[",
                    onClick = {
                        onDismissRequest()
                        onSendToBack()
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                ContextMenuItem(
                    icon = "delete",
                    title = "Delete",
                    shortcut = "Del",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = {
                        onDismissRequest()
                        onDeleteSelection()
                    }
                )
                ContextMenuItem(
                    icon = "close",
                    title = "Deselect",
                    shortcut = "Esc",
                    onClick = {
                        onDismissRequest()
                        onDeselect()
                    }
                )
            } else {
                // Free Canvas Context Actions
                ContextMenuItem(
                    icon = "sticky_note_2",
                    title = "Add Sticky Note",
                    shortcut = "S",
                    onClick = {
                        onDismissRequest()
                        onAddStickyNote()
                    }
                )
                ContextMenuItem(
                    icon = "text_fields",
                    title = "Insert Text",
                    shortcut = "T",
                    onClick = {
                        onDismissRequest()
                        onInsertText()
                    }
                )
                ContextMenuItem(
                    icon = "rectangle",
                    title = "Insert Rectangle",
                    shortcut = "R",
                    onClick = {
                        onDismissRequest()
                        onInsertRectangle()
                    }
                )
                ContextMenuItem(
                    icon = "circle",
                    title = "Insert Circle",
                    shortcut = "C",
                    onClick = {
                        onDismissRequest()
                        onInsertCircle()
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                ContextMenuItem(
                    icon = "select_all",
                    title = "Select All",
                    shortcut = "Ctrl+A",
                    onClick = {
                        onDismissRequest()
                        onSelectAll()
                    }
                )
                ContextMenuItem(
                    icon = "crop_free",
                    title = "Reset Zoom (100%)",
                    shortcut = "Ctrl+0",
                    onClick = {
                        onDismissRequest()
                        onResetZoom()
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                ContextMenuItem(
                    icon = "delete_sweep",
                    title = "Clear Canvas",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = {
                        onDismissRequest()
                        onClearCanvas()
                    }
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: String,
    title: String,
    shortcut: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            MaterialSymbol(
                name = icon,
                size = 18.dp,
                tint = tint
            )
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
                if (shortcut != null) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = shortcut,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }
            }
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = tint,
            leadingIconColor = tint
        )
    )
}
