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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.Board
import com.kd.creavity.presentation.ui.screens.LibrarySortOrder
import com.kd.creavity.presentation.ui.screens.OwnershipFilter

/**
 * Reusable modifier that detects secondary pointer click (right-click on Desktop/Mouse)
 * using [PointerEventPass.Main] so child cards evaluate and consume right-clicks before
 * the background container.
 */
fun Modifier.onDesktopRightClick(
    onRightClick: (Offset) -> Unit
): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            if (event.type == PointerEventType.Press &&
                event.buttons.isSecondaryPressed &&
                !event.changes.any { it.isConsumed }
            ) {
                val position = event.changes.firstOrNull()?.position ?: Offset.Zero
                event.changes.forEach { it.consume() }
                onRightClick(position)
            }
        }
    }
}

/**
 * Material 3 Expressive Context Menu for Board Cards and Rows on Desktop.
 * Provides pro desktop actions (Open, Duplicate, Star, Edit, Export, Move to Bin).
 */
@Composable
fun DesktopBoardCardContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    screenOffset: Offset,
    board: Board,
    onOpenBoard: () -> Unit,
    onDuplicateBoard: () -> Unit,
    onToggleStar: () -> Unit,
    onEditMetadata: () -> Unit,
    onExportBoard: () -> Unit,
    onDeleteBoard: () -> Unit,
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
            // 1. Open Board
            LibraryContextMenuItem(
                icon = "draw",
                title = "Open Board",
                shortcut = "Enter",
                onClick = {
                    onDismissRequest()
                    onOpenBoard()
                }
            )

            // 2. Duplicate Board
            LibraryContextMenuItem(
                icon = "content_copy",
                title = "Duplicate",
                shortcut = "Ctrl+D",
                onClick = {
                    onDismissRequest()
                    onDuplicateBoard()
                }
            )

            // 3. Favourite / Star
            LibraryContextMenuItem(
                icon = if (board.isStarred) "star_outline" else "star",
                title = if (board.isStarred) "Remove from Favourites" else "Add to Favourites",
                shortcut = "S",
                onClick = {
                    onDismissRequest()
                    onToggleStar()
                }
            )

            // 4. Rename / Details
            LibraryContextMenuItem(
                icon = "edit",
                title = "Rename / Edit Details",
                shortcut = "F2",
                onClick = {
                    onDismissRequest()
                    onEditMetadata()
                }
            )

            // 5. Export Board
            LibraryContextMenuItem(
                icon = "share",
                title = "Export Board...",
                shortcut = "Ctrl+E",
                onClick = {
                    onDismissRequest()
                    onExportBoard()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // 6. Delete
            LibraryContextMenuItem(
                icon = "delete",
                title = "Move to Bin",
                shortcut = "Del",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDismissRequest()
                    onDeleteBoard()
                }
            )
        }
    }
}

/**
 * Material 3 Expressive Context Menu for the Dashboard Background/Grid.
 * Gives immediate access to board creation, view mode switching, and sorting/filtering.
 */
@Composable
fun DesktopDashboardBackgroundContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    screenOffset: Offset,
    currentViewMode: LibraryViewMode,
    currentSortOrder: LibrarySortOrder,
    currentOwnershipFilter: OwnershipFilter,
    onCreateBoard: () -> Unit,
    onSetViewMode: (LibraryViewMode) -> Unit,
    onSetSortOrder: (LibrarySortOrder) -> Unit,
    onSetOwnershipFilter: (OwnershipFilter) -> Unit,
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
            // New Board Action
            LibraryContextMenuItem(
                icon = "add",
                title = "New Board",
                shortcut = "Ctrl+N",
                tint = MaterialTheme.colorScheme.primary,
                onClick = {
                    onDismissRequest()
                    onCreateBoard()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // View Modes
            LibraryContextMenuItem(
                icon = "grid_view",
                title = "View as Grid",
                shortcut = if (currentViewMode == LibraryViewMode.GRID) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetViewMode(LibraryViewMode.GRID)
                }
            )
            LibraryContextMenuItem(
                icon = "view_list",
                title = "View as List",
                shortcut = if (currentViewMode == LibraryViewMode.LIST) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetViewMode(LibraryViewMode.LIST)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Sorting Options
            LibraryContextMenuItem(
                icon = "schedule",
                title = "Sort by Last Accessed",
                shortcut = if (currentSortOrder == LibrarySortOrder.LAST_ACCESSED) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetSortOrder(LibrarySortOrder.LAST_ACCESSED)
                }
            )
            LibraryContextMenuItem(
                icon = "history",
                title = "Sort by Last Modified",
                shortcut = if (currentSortOrder == LibrarySortOrder.LAST_MODIFIED) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetSortOrder(LibrarySortOrder.LAST_MODIFIED)
                }
            )
            LibraryContextMenuItem(
                icon = "text_format",
                title = "Sort by Title",
                shortcut = if (currentSortOrder == LibrarySortOrder.TITLE) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetSortOrder(LibrarySortOrder.TITLE)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Filter Options
            LibraryContextMenuItem(
                icon = "folder",
                title = "All Boards",
                shortcut = if (currentOwnershipFilter == OwnershipFilter.ALL) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetOwnershipFilter(OwnershipFilter.ALL)
                }
            )
            LibraryContextMenuItem(
                icon = "person",
                title = "Owned by me",
                shortcut = if (currentOwnershipFilter == OwnershipFilter.OWNED_BY_ME) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetOwnershipFilter(OwnershipFilter.OWNED_BY_ME)
                }
            )
            LibraryContextMenuItem(
                icon = "group",
                title = "Shared with me",
                shortcut = if (currentOwnershipFilter == OwnershipFilter.SHARED_WITH_ME) "✓" else null,
                onClick = {
                    onDismissRequest()
                    onSetOwnershipFilter(OwnershipFilter.SHARED_WITH_ME)
                }
            )
        }
    }
}

@Composable
private fun LibraryContextMenuItem(
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
                    Spacer(modifier = Modifier.width(20.dp))
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
