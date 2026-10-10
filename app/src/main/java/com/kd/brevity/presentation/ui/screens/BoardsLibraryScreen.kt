package com.kd.brevity.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.data.export.BoardExporter
import com.kd.brevity.domain.model.Board
import com.kd.brevity.presentation.ui.NavDestination
import com.kd.brevity.presentation.ui.OotbAction
import com.kd.brevity.presentation.ui.components.BoardActionsSheet
import com.kd.brevity.presentation.ui.components.BoardEditMetadataSheet
import com.kd.brevity.presentation.ui.components.BoardExportSheet
import com.kd.brevity.presentation.ui.components.BoardMetadataBadges
import com.kd.brevity.presentation.ui.components.BoardThumbnailPreview
import com.kd.brevity.presentation.ui.components.DeleteConfirmDialog
import com.kd.brevity.presentation.ui.components.GroupFolderFilterRow
import com.kd.brevity.presentation.ui.components.LibraryNavDrawer
import com.kd.brevity.presentation.ui.components.MaterialSymbol
import com.kd.brevity.presentation.ui.components.TooltipHint
import com.kd.brevity.presentation.viewmodel.WhiteboardUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LibrarySortOrder(val label: String) {
    LAST_ACCESSED("Last accessed"),
    LAST_MODIFIED("Last modified"),
    TITLE("Title")
}

fun Modifier.drawListScrollbar(state: LazyListState, color: Color): Modifier = this.drawWithContent {
    drawContent()
    val (total, visible) = state.layoutInfo.totalItemsCount to state.layoutInfo.visibleItemsInfo.size
    if (visible in 1..<total) {
        val barH = (size.height * (visible.toFloat() / total)).coerceAtLeast(32.dp.toPx())
        val barY = (size.height - barH) * (state.firstVisibleItemIndex.toFloat() / (total - visible).coerceAtLeast(1))
        drawRoundRect(color, Offset(size.width - 4.dp.toPx(), barY), Size(3.dp.toPx(), barH), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    }
}

fun Modifier.drawGridScrollbar(state: LazyGridState, color: Color): Modifier = this.drawWithContent {
    drawContent()
    val (total, visible) = state.layoutInfo.totalItemsCount to state.layoutInfo.visibleItemsInfo.size
    if (visible in 1..<total) {
        val barH = (size.height * (visible.toFloat() / total)).coerceAtLeast(32.dp.toPx())
        val barY = (size.height - barH) * (state.firstVisibleItemIndex.toFloat() / (total - visible).coerceAtLeast(1))
        drawRoundRect(color, Offset(size.width - 4.dp.toPx(), barY), Size(3.dp.toPx(), barH), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BoardsLibraryScreen(
    modifier: Modifier = Modifier,
    uiState: WhiteboardUiState,
    onAction: (OotbAction) -> Unit,
    onNavigateToCanvas: () -> Unit,
    onNavigateToDestination: (NavDestination) -> Unit = {},
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var isGridView by remember { mutableStateOf(true) }
    var sortOrder by remember { mutableStateOf(LibrarySortOrder.LAST_ACCESSED) }
    var isAscending by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }
    var boardToEditMetadata by remember { mutableStateOf<Board?>(null) }
    var selectedBoardForActions by remember { mutableStateOf<Board?>(null) }
    var boardToExport by remember { mutableStateOf<Board?>(null) }
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedBoardIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var boardIdToDeleteWithConfirm by remember { mutableStateOf<String?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedBoardIds = emptySet()
    }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val activeBoards = remember(uiState.savedBoards) {
        uiState.savedBoards.filter { !it.isInBin }
    }
    val binnedCount = remember(uiState.savedBoards) {
        uiState.savedBoards.count { it.isInBin }
    }

    val distinctGroups = remember(activeBoards) {
        activeBoards.map { it.group.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    // Filter and sort boards with deep content search (title + venue + group + labels + content)
    val filteredBoards = remember(activeBoards, searchQuery, sortOrder, isAscending, selectedGroup) {
        var base = activeBoards
        if (selectedGroup != null) {
            base = base.filter { it.group.equals(selectedGroup, ignoreCase = true) }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            base = base.filter { b ->
                b.title.contains(q, ignoreCase = true) ||
                b.venue.contains(q, ignoreCase = true) ||
                b.group.contains(q, ignoreCase = true) ||
                b.labels.any { l -> l.contains(q, ignoreCase = true) } ||
                b.pages.any { p ->
                    p.textElements.any { t -> t.text.contains(q, ignoreCase = true) } ||
                    p.stickyNotes.any { s -> s.text.contains(q, ignoreCase = true) }
                }
            }
        }

        when (sortOrder) {
            LibrarySortOrder.LAST_ACCESSED -> if (isAscending) base.sortedBy { it.lastAccessedAt } else base.sortedByDescending { it.lastAccessedAt }
            LibrarySortOrder.LAST_MODIFIED -> if (isAscending) base.sortedBy { it.modifiedAt } else base.sortedByDescending { it.modifiedAt }
            LibrarySortOrder.TITLE -> if (isAscending) base.sortedBy { it.title.lowercase() } else base.sortedByDescending { it.title.lowercase() }
        }
    }

    LibraryNavDrawer(
        drawerState = drawerState,
        binnedCount = binnedCount,
        onNavigateToDestination = onNavigateToDestination
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Top Bar: [ Contextual Selection Bar OR Search Bar ]
                if (isSelectionMode) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TooltipHint(text = "Close selection") {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isSelectionMode = false
                                        selectedBoardIds = emptySet()
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    MaterialSymbol(name = "close", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(
                                text = "${selectedBoardIds.size} selected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                            )

                            // Select All / Deselect All
                            val allSelected = selectedBoardIds.size == filteredBoards.size && filteredBoards.isNotEmpty()
                            TooltipHint(text = if (allSelected) "Deselect all" else "Select all") {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedBoardIds = if (allSelected) emptySet() else filteredBoards.map { it.id }.toSet()
                                        if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    MaterialSymbol(
                                        name = if (allSelected) "deselect" else "select_all",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        size = 22.dp
                                    )
                                }
                            }

                            // Favourite / Star selected boards
                            TooltipHint(text = "Favourite selected") {
                                IconButton(
                                    enabled = selectedBoardIds.isNotEmpty(),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val anyUnstarred = selectedBoardIds.any { id -> activeBoards.find { it.id == id }?.isStarred != true }
                                        selectedBoardIds.forEach { id ->
                                            val isStarred = activeBoards.find { it.id == id }?.isStarred == true
                                            if (anyUnstarred && !isStarred) {
                                                onAction(OotbAction.ToggleStarBoard(id))
                                            } else if (!anyUnstarred && isStarred) {
                                                onAction(OotbAction.ToggleStarBoard(id))
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    val allStarred = selectedBoardIds.isNotEmpty() && selectedBoardIds.all { id -> activeBoards.find { it.id == id }?.isStarred == true }
                                    MaterialSymbol(
                                        name = if (allStarred) "star" else "star_outline",
                                        filled = allStarred,
                                        tint = if (selectedBoardIds.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        size = 22.dp
                                    )
                                }
                            }

                            // Edit Details (active when exactly 1 board selected)
                            TooltipHint(text = "Edit details") {
                                IconButton(
                                    enabled = selectedBoardIds.size == 1,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val singleId = selectedBoardIds.firstOrNull() ?: return@IconButton
                                        val target = activeBoards.find { it.id == singleId } ?: return@IconButton
                                        boardToEditMetadata = target
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    MaterialSymbol(
                                        name = "edit",
                                        tint = if (selectedBoardIds.size == 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                        size = 22.dp
                                    )
                                }
                            }

                            // Delete / Move to Trash
                            TooltipHint(text = "Move to trash") {
                                IconButton(
                                    enabled = selectedBoardIds.isNotEmpty(),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showBatchDeleteConfirm = true
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    MaterialSymbol(
                                        name = "delete",
                                        tint = if (selectedBoardIds.isNotEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                        size = 22.dp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Thicc Search Bar with Menu button INSIDE on the left
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TooltipHint(text = "Open menu") {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        scope.launch { drawerState.open() }
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    MaterialSymbol(
                                        name = "menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        size = 24.dp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search boards, venue, labels...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                TooltipHint(text = "Clear search") {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        MaterialSymbol(
                                            name = "close",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            size = 20.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            // 2. Sub-header Bar: Sort + Order Arrow on left, View Toggle on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sort Dropdown Button + Direction Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box {
                        TooltipHint(text = "Sort boards") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sortOrder.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MaterialSymbol(
                                    name = "arrow_drop_down",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 20.dp
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            LibrarySortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        sortOrder = order
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    TooltipHint(text = if (isAscending) "Ascending order" else "Descending order") {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAscending = !isAscending
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            MaterialSymbol(
                                name = if (isAscending) "arrow_upward" else "arrow_downward",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Selection Mode Toggle Button
                    TooltipHint(text = if (isSelectionMode) "Done selecting" else "Select boards") {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) selectedBoardIds = emptySet()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            MaterialSymbol(
                                name = if (isSelectionMode) "check_circle" else "checklist",
                                tint = if (isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                        }
                    }

                    // Grid / List Toggle Button (Screen 1 & Screen 2)
                    TooltipHint(text = if (isGridView) "Switch to list view" else "Switch to grid view") {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isGridView = !isGridView
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            MaterialSymbol(
                                name = if (isGridView) "view_list" else "grid_view",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                        }
                    }
                }
            }

            // Group folders filter row
            if (distinctGroups.isNotEmpty()) {
                GroupFolderFilterRow(
                    groups = distinctGroups,
                    selectedGroup = selectedGroup,
                    onSelectGroup = { group ->
                        selectedGroup = if (selectedGroup == group) null else group
                    }
                )
            }

            // 3. Main Content: Grid Mode (Screen 1) or List Mode (Screen 2)
            if (filteredBoards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.85f).padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MaterialSymbol(name = "draw", size = 48.dp, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No boards match '$searchQuery'" else "No whiteboards yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else if (isGridView) {
                // Adaptive Grid Mode (2 columns portrait, 4+ landscape/tablet)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .drawGridScrollbar(gridState, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                ) {
                    items(filteredBoards, key = { it.id }) { board ->
                        val isSelected = selectedBoardIds.contains(board.id)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest
                                                 else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                            if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAction(OotbAction.SwitchBoard(board.copy(lastAccessedAt = System.currentTimeMillis())))
                                            onNavigateToCanvas()
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedBoardIds = setOf(board.id)
                                        } else {
                                            selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                            if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                        }
                                    }
                                )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Thumbnail Preview (16:9)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                ) {
                                    BoardThumbnailPreview(
                                        board = board,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (isSelectionMode) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(24.dp)
                                                .background(
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                MaterialSymbol(name = "check", size = 16.dp, tint = MaterialTheme.colorScheme.onPrimary)
                                            }
                                        }
                                    }
                                }

                                // Card Footer: Title + Favourite + 3-dots
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = board.title.ifBlank { "Untitled Board" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        BoardMetadataBadges(board = board)
                                    }

                                    // Favourite Star Button
                                    TooltipHint(text = if (board.isStarred) "Remove favourite" else "Add to favourites") {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onAction(OotbAction.ToggleStarBoard(board.id))
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            MaterialSymbol(
                                                name = if (board.isStarred) "star" else "star_outline",
                                                filled = board.isStarred,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                size = 18.dp
                                            )
                                        }
                                    }

                                    // 3-dots overflow to open action sheet
                                    TooltipHint(text = "Board options") {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedBoardForActions = board
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            MaterialSymbol(name = "more_vert", size = 18.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // List Mode (Screen 2)
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .drawListScrollbar(listState, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                ) {
                    items(filteredBoards, key = { it.id }) { board ->
                        val isSelected = selectedBoardIds.contains(board.id)
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                            if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAction(OotbAction.SwitchBoard(board.copy(lastAccessedAt = System.currentTimeMillis())))
                                            onNavigateToCanvas()
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedBoardIds = setOf(board.id)
                                        } else {
                                            selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                            if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                        }
                                    }
                                )
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isSelectionMode) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = if (isSelected) 0.dp else 1.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            MaterialSymbol(name = "check", size = 16.dp, tint = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }

                                BoardThumbnailPreview(
                                    board = board,
                                    modifier = Modifier
                                        .size(width = 54.dp, height = 36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = board.title.ifBlank { "Untitled Board" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Opened by me ${dateFormat.format(Date(board.modifiedAt))}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    BoardMetadataBadges(board = board)
                                }

                                // Favourite Star Button
                                TooltipHint(text = if (board.isStarred) "Remove favourite" else "Add to favourites") {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAction(OotbAction.ToggleStarBoard(board.id))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        MaterialSymbol(
                                            name = if (board.isStarred) "star" else "star_outline",
                                            filled = board.isStarred,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            size = 20.dp
                                        )
                                    }
                                }

                                // 3-dots Menu to open action sheet
                                TooltipHint(text = "Board options") {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedBoardForActions = board
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        MaterialSymbol(name = "more_vert", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar: Connect & New Board (hidden in selection mode)
        if (!isSelectionMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToDestination(NavDestination.PEERS)
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    MaterialSymbol(name = "hub", filled = true, size = 22.dp, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Collaborate", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAction(OotbAction.CreateNewBoard("Untitled Board"))
                        onNavigateToCanvas()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    MaterialSymbol(name = "add", filled = true, size = 22.dp, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "New Board", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Board Metadata Edit Sheet (Latch-style ModalBottomSheet)
        BoardEditMetadataSheet(
            board = boardToEditMetadata,
            onDismiss = { boardToEditMetadata = null },
            onSave = { boardId, title, venue, group, labels ->
                onAction(OotbAction.UpdateBoardMetadata(boardId, title, venue, group, labels))
            }
        )

        // Board Actions Sheet (Star, Rename, Duplicate, Export, Move to Trash)
        BoardActionsSheet(
            board = selectedBoardForActions,
            onDismiss = { selectedBoardForActions = null },
            onToggleStar = { board -> onAction(OotbAction.ToggleStarBoard(board.id)) },
            onRename = { board -> boardToEditMetadata = board },
            onDuplicate = { board -> onAction(OotbAction.DuplicateBoard(board.id)) },
            onExport = { board -> boardToExport = board },
            onMoveToBin = { board ->
                boardIdToDeleteWithConfirm = board.id
                selectedBoardForActions = null
            }
        )

        // Board Export Format Picker Sheet
        BoardExportSheet(
            board = boardToExport,
            onDismiss = { boardToExport = null },
            onFormatSelected = { format -> boardToExport?.let { b -> BoardExporter.shareBoard(context, b, format) } }
        )

        if (showBatchDeleteConfirm) {
            DeleteConfirmDialog(
                title = "Move to Trash?",
                text = "Are you sure you want to move ${selectedBoardIds.size} boards to Trash?",
                confirmText = "Move to Trash",
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedBoardIds.forEach { onAction(OotbAction.MoveBoardToBin(it)) }
                    selectedBoardIds = emptySet()
                    isSelectionMode = false
                    showBatchDeleteConfirm = false
                },
                onDismiss = { showBatchDeleteConfirm = false }
            )
        }

        if (boardIdToDeleteWithConfirm != null) {
            val targetId = boardIdToDeleteWithConfirm!!
            DeleteConfirmDialog(
                title = "Move to Trash?",
                text = "Are you sure you want to move this board to Trash?",
                confirmText = "Move to Trash",
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(OotbAction.MoveBoardToBin(targetId))
                    boardIdToDeleteWithConfirm = null
                },
                onDismiss = { boardIdToDeleteWithConfirm = null }
            )
        }
    }
}
}

