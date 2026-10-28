package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState

/**
 * Pure Compose Material 3 Expressive Board Top Menu Bar.
 *
 * Provides pro desktop "File", "Edit", "View", and "Tools" menus inside the board top bar,
 * replacing the Java AWT native menu bar with a fully Compose-native, theme-aware,
 * rounded expressive UI that is exclusively visible inside an open board.
 */
@Composable
fun DesktopBoardTopMenuBar(
    uiState: WhiteboardUiState,
    onAction: (BoardAction) -> Unit,
    onBackToBoards: () -> Unit,
    onOpenExport: () -> Unit,
    onToggleBirdsEye: () -> Unit,
    onToggleFrameBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMenu by remember { mutableStateOf<String?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        // 1. File Menu
        Box {
            BoardMenuHeaderButton(
                label = "File",
                isSelected = activeMenu == "File",
                onClick = { activeMenu = if (activeMenu == "File") null else "File" }
            )
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
            ) {
                DropdownMenu(
                    expanded = activeMenu == "File",
                    onDismissRequest = { activeMenu = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    ComposeMenuItem(
                        icon = "add",
                        title = "New Board",
                        shortcut = "Ctrl+N",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.CreateNewBoard(""))
                        }
                    )
                    ComposeMenuItem(
                        icon = "share",
                        title = "Export Board...",
                        shortcut = "Ctrl+E",
                        onClick = {
                            activeMenu = null
                            onOpenExport()
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    ComposeMenuItem(
                        icon = "arrow_back",
                        title = "Back to Boards",
                        shortcut = "Esc",
                        onClick = {
                            activeMenu = null
                            onBackToBoards()
                        }
                    )
                }
            }
        }

        // 2. Edit Menu
        Box {
            BoardMenuHeaderButton(
                label = "Edit",
                isSelected = activeMenu == "Edit",
                onClick = { activeMenu = if (activeMenu == "Edit") null else "Edit" }
            )
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
            ) {
                DropdownMenu(
                    expanded = activeMenu == "Edit",
                    onDismissRequest = { activeMenu = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    ComposeMenuItem(
                        icon = "undo",
                        title = "Undo",
                        shortcut = "Ctrl+Z",
                        enabled = uiState.canUndo,
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.Undo)
                        }
                    )
                    ComposeMenuItem(
                        icon = "redo",
                        title = "Redo",
                        shortcut = "Ctrl+Y",
                        enabled = uiState.canRedo,
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.Redo)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    ComposeMenuItem(
                        icon = "select_all",
                        title = "Select All",
                        shortcut = "Ctrl+A",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.SELECT))
                        }
                    )
                    ComposeMenuItem(
                        icon = "delete_sweep",
                        title = "Clear Frame",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.ClearCanvas)
                        }
                    )
                }
            }
        }

        // 3. View Menu
        Box {
            BoardMenuHeaderButton(
                label = "View",
                isSelected = activeMenu == "View",
                onClick = { activeMenu = if (activeMenu == "View") null else "View" }
            )
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
            ) {
                DropdownMenu(
                    expanded = activeMenu == "View",
                    onDismissRequest = { activeMenu = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    ComposeMenuItem(
                        icon = "crop_free",
                        title = "Zoom In",
                        shortcut = "Ctrl++",
                        onClick = {
                            activeMenu = null
                            val newZoom = (uiState.zoom * 1.25f).coerceAtMost(5.0f)
                            onAction(BoardAction.SetCanvasTransform(newZoom, uiState.panOffset))
                        }
                    )
                    ComposeMenuItem(
                        icon = "crop_free",
                        title = "Zoom Out",
                        shortcut = "Ctrl+-",
                        onClick = {
                            activeMenu = null
                            val newZoom = (uiState.zoom / 1.25f).coerceAtLeast(0.1f)
                            onAction(BoardAction.SetCanvasTransform(newZoom, uiState.panOffset))
                        }
                    )
                    ComposeMenuItem(
                        icon = "crop_free",
                        title = "Reset Zoom (100%)",
                        shortcut = "Ctrl+0",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SetCanvasTransform(1.0f, androidx.compose.ui.geometry.Offset.Zero))
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    ComposeMenuItem(
                        icon = "sensors",
                        title = "Birds-Eye Navigator",
                        onClick = {
                            activeMenu = null
                            onToggleBirdsEye()
                        }
                    )
                    ComposeMenuItem(
                        icon = "view_list",
                        title = "Toggle Frame Bar",
                        onClick = {
                            activeMenu = null
                            onToggleFrameBar()
                        }
                    )
                }
            }
        }

        // 4. Tools Menu
        Box {
            BoardMenuHeaderButton(
                label = "Tools",
                isSelected = activeMenu == "Tools",
                onClick = { activeMenu = if (activeMenu == "Tools") null else "Tools" }
            )
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
            ) {
                DropdownMenu(
                    expanded = activeMenu == "Tools",
                    onDismissRequest = { activeMenu = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    ComposeMenuItem(
                        icon = "draw",
                        title = "Pen",
                        shortcut = "P",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.PEN))
                        }
                    )
                    ComposeMenuItem(
                        icon = "ink_highlighter",
                        title = "Highlighter",
                        shortcut = "H",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.HIGHLIGHTER))
                        }
                    )
                    ComposeMenuItem(
                        icon = "ink_eraser",
                        title = "Eraser",
                        shortcut = "E",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.ERASER))
                        }
                    )
                    ComposeMenuItem(
                        icon = "category",
                        title = "Shapes",
                        shortcut = "R",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.RECTANGLE))
                        }
                    )
                    ComposeMenuItem(
                        icon = "pan_tool_alt",
                        title = "Select",
                        shortcut = "V",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.SELECT))
                        }
                    )
                    ComposeMenuItem(
                        icon = "sticky_note_2",
                        title = "Sticky Note",
                        shortcut = "S",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.STICKY_NOTE))
                        }
                    )
                    ComposeMenuItem(
                        icon = "text_fields",
                        title = "Text",
                        shortcut = "T",
                        onClick = {
                            activeMenu = null
                            onAction(BoardAction.SelectTool(DrawingTool.TEXT))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardMenuHeaderButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComposeMenuItem(
    icon: String,
    title: String,
    shortcut: String? = null,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val effectiveTint = if (enabled) tint else tint.copy(alpha = 0.38f)
    DropdownMenuItem(
        enabled = enabled,
        leadingIcon = {
            MaterialSymbol(
                name = icon,
                size = 18.dp,
                tint = effectiveTint
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
                    color = effectiveTint
                )
                if (shortcut != null) {
                    Spacer(modifier = Modifier.width(20.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = shortcut,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.65f else 0.25f),
                        fontSize = 11.sp
                    )
                }
            }
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = effectiveTint,
            leadingIconColor = effectiveTint
        )
    )
}
