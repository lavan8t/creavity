package com.kd.creavity.presentation.ui.screens

import com.kd.creavity.util.PlatformBackHandler as BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.util.shareBoard
import com.kd.creavity.domain.model.Board
import com.kd.creavity.presentation.ui.NavDestination
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.ui.components.BoardActionsSheet
import com.kd.creavity.presentation.ui.components.BoardEditMetadataSheet
import com.kd.creavity.presentation.ui.components.BoardExportSheet
import com.kd.creavity.presentation.ui.components.BoardGridCard
import com.kd.creavity.presentation.ui.components.BoardListRow
import com.kd.creavity.presentation.ui.components.drawGridScrollbar
import com.kd.creavity.presentation.ui.components.drawListScrollbar
import com.kd.creavity.presentation.ui.components.DeleteConfirmDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.kd.creavity.presentation.ui.components.LibraryViewModeSwitcher
import com.kd.creavity.presentation.ui.components.LibraryNavDrawer
import com.kd.creavity.presentation.ui.components.LibrarySearchBar
import com.kd.creavity.presentation.ui.components.LibrarySelectionTopBar
import com.kd.creavity.presentation.ui.components.LibraryViewMode
import com.kd.creavity.presentation.ui.components.MaterialSymbol
import com.kd.creavity.presentation.ui.components.TooltipHint
import com.kd.creavity.presentation.ui.motion.BrevityMotion
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LibrarySortOrder(val label: String) {
    LAST_ACCESSED("Last accessed"),
    LAST_MODIFIED("Last modified"),
    TITLE("Title")
}

enum class OwnershipFilter(val label: String) {
    ALL("All"),
    OWNED_BY_ME("Owned by me"),
    SHARED_WITH_ME("Shared with me")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BoardsLibraryScreen(
    modifier: Modifier = Modifier,
    uiState: WhiteboardUiState,
    onAction: (BoardAction) -> Unit,
    onNavigateToCanvas: () -> Unit,
    onNavigateToDestination: (NavDestination) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var viewMode by rememberSaveable { mutableStateOf(LibraryViewMode.GRID) }
    var ownershipFilter by rememberSaveable { mutableStateOf(OwnershipFilter.ALL) }
    var selectedLabels by rememberSaveable { mutableStateOf(setOf<String>()) }
    var sortOrder by remember { mutableStateOf(LibrarySortOrder.LAST_ACCESSED) }
    var isAscending by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
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

    val distinctLabels = remember(activeBoards) {
        activeBoards.flatMap { it.labels }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    // Filter and sort boards with deep content search & ownership & labels
    val filteredBoards = remember(activeBoards, searchQuery, sortOrder, isAscending, ownershipFilter, selectedLabels) {
        var base = activeBoards

        // 1. Ownership scope filter
        base = when (ownershipFilter) {
            OwnershipFilter.ALL -> base
            OwnershipFilter.OWNED_BY_ME -> base.filter { b ->
                b.createdBy.equals("You", ignoreCase = true) ||
                b.ownerName.equals("You", ignoreCase = true) ||
                b.createdBy.equals(uiState.displayName, ignoreCase = true) ||
                b.ownerName.equals(uiState.displayName, ignoreCase = true)
            }
            OwnershipFilter.SHARED_WITH_ME -> base.filter { b ->
                !b.createdBy.equals("You", ignoreCase = true) &&
                !b.ownerName.equals("You", ignoreCase = true) &&
                !b.createdBy.equals(uiState.displayName, ignoreCase = true) &&
                !b.ownerName.equals(uiState.displayName, ignoreCase = true)
            }
        }

        // 2. Labels multi-select filter
        if (selectedLabels.isNotEmpty()) {
            base = base.filter { b ->
                selectedLabels.all { sel ->
                    b.labels.any { l -> l.equals(sel, ignoreCase = true) }
                }
            }
        }

        // 3. Search query filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            base = base.filter { b ->
                b.title.contains(q, ignoreCase = true) ||
                b.venue.contains(q, ignoreCase = true) ||
                b.labels.any { l -> l.contains(q, ignoreCase = true) } ||
                b.pages.any { p ->
                    p.textElements.any { t -> t.text.contains(q, ignoreCase = true) } ||
                    p.stickyNotes.any { s -> s.text.contains(q, ignoreCase = true) }
                }
            }
        }

        // 4. Sorting
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
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 720.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Top Bar: [ Contextual Selection Bar OR Search Bar ] with Spring Transition
                AnimatedContent(
                    targetState = isSelectionMode,
                    transitionSpec = {
                        (slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = BrevityMotion.springSpatial()
                        ) + fadeIn(animationSpec = BrevityMotion.springEffects()))
                            .togetherWith(
                                slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            )
                    },
                    label = "SelectionTopBarMotion"
                ) { selectionActive ->
                    if (selectionActive) {
                        val allStarred = selectedBoardIds.isNotEmpty() && selectedBoardIds.all { id -> activeBoards.find { it.id == id }?.isStarred == true }
                        LibrarySelectionTopBar(
                            selectedCount = selectedBoardIds.size,
                            totalCount = filteredBoards.size,
                            isAllStarred = allStarred,
                            onClose = {
                                isSelectionMode = false
                                selectedBoardIds = emptySet()
                            },
                            onToggleSelectAll = {
                                val allSelected = selectedBoardIds.size == filteredBoards.size && filteredBoards.isNotEmpty()
                                selectedBoardIds = if (allSelected) emptySet() else filteredBoards.map { it.id }.toSet()
                                if (selectedBoardIds.isEmpty()) isSelectionMode = false
                            },
                            onToggleStar = {
                                val anyUnstarred = selectedBoardIds.any { id -> activeBoards.find { it.id == id }?.isStarred != true }
                                selectedBoardIds.forEach { id ->
                                    val isStarred = activeBoards.find { it.id == id }?.isStarred == true
                                    if (anyUnstarred && !isStarred) {
                                        onAction(BoardAction.ToggleStarBoard(id))
                                    } else if (!anyUnstarred && isStarred) {
                                        onAction(BoardAction.ToggleStarBoard(id))
                                    }
                                }
                            },
                            onEditDetails = {
                                val singleId = selectedBoardIds.firstOrNull() ?: return@LibrarySelectionTopBar
                                val target = activeBoards.find { it.id == singleId } ?: return@LibrarySelectionTopBar
                                boardToEditMetadata = target
                            },
                            onMoveToTrash = {
                                showBatchDeleteConfirm = true
                            }
                        )
                    } else {
                        LibrarySearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            showMenuButton = !isWideScreen
                        )
                    }
                }

                // 2. Sub-header Bar: Sort on Left, View Mode Switcher on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sort Dropdown Button
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
                                    MaterialSymbol(name = "arrow_drop_down", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
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

                    // View Mode Switcher: Grid vs List
                    LibraryViewModeSwitcher(
                        viewMode = viewMode,
                        onViewModeChange = { mode -> viewMode = mode }
                    )
                }

                // 2.5 Dynamic Filters Row: Ownership Filter Chips + Labels Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ownership Filter Chips
                    OwnershipFilter.entries.forEach { filter ->
                        val isSelected = ownershipFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                ownershipFilter = filter
                            },
                            label = {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = if (filter == OwnershipFilter.OWNED_BY_ME) {
                                { MaterialSymbol(name = "person", size = 16.dp, tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) }
                            } else if (filter == OwnershipFilter.SHARED_WITH_ME) {
                                { MaterialSymbol(name = "group", size = 16.dp, tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) }
                            } else null,
                            border = null,
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }

                    // Divider between Ownership and Labels
                    if (distinctLabels.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        // Label Chips
                        distinctLabels.forEach { label ->
                            val isSelected = selectedLabels.contains(label)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedLabels = if (isSelected) selectedLabels - label else selectedLabels + label
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = {
                                    MaterialSymbol(
                                        name = "label",
                                        size = 15.dp,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                border = null,
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // 3. Main Content: Grid or List View
                when (viewMode) {
                    LibraryViewMode.GRID -> {
                        // ==================== ALL BOARDS GRID VIEW ====================
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
                                            text = if (searchQuery.isNotEmpty()) "No boards match '$searchQuery'" else "No boards yet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else {
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
                                    BoardGridCard(
                                        board = board,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onCardClick = {
                                            if (isSelectionMode) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                                if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onAction(BoardAction.SwitchBoard(board.copy(lastAccessedAt = System.currentTimeMillis())))
                                                onNavigateToCanvas()
                                            }
                                        },
                                        onCardLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedBoardIds = setOf(board.id)
                                            } else {
                                                selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                                if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                            }
                                        },
                                        onToggleStar = { onAction(BoardAction.ToggleStarBoard(board.id)) },
                                        onOptionsClick = { selectedBoardForActions = board }
                                    )
                                }
                            }
                        }
                    }
                    LibraryViewMode.LIST -> {
                        // ==================== ALL BOARDS LIST VIEW ====================
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
                                            text = if (searchQuery.isNotEmpty()) "No boards match '$searchQuery'" else "No boards yet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else {
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
                                    BoardListRow(
                                        board = board,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        dateFormat = dateFormat,
                                        onRowClick = {
                                            if (isSelectionMode) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                                if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onAction(BoardAction.SwitchBoard(board.copy(lastAccessedAt = System.currentTimeMillis())))
                                                onNavigateToCanvas()
                                            }
                                        },
                                        onRowLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedBoardIds = setOf(board.id)
                                            } else {
                                                selectedBoardIds = if (isSelected) selectedBoardIds - board.id else selectedBoardIds + board.id
                                                if (selectedBoardIds.isEmpty()) isSelectionMode = false
                                            }
                                        },
                                        onToggleStar = { onAction(BoardAction.ToggleStarBoard(board.id)) },
                                        onOptionsClick = { selectedBoardForActions = board }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar: Connect & New Board (slides down & fades out in selection mode)
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = BrevityMotion.springSpatial()
                ) + fadeIn(animationSpec = BrevityMotion.springEffects()),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = BrevityMotion.springSpatial()
                ) + fadeOut(animationSpec = BrevityMotion.springEffects()),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
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
                        Text(text = "Collab", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAction(BoardAction.CreateNewBoard("Untitled Board"))
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
                        Text(text = "Board", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Board Metadata Edit Sheet (Latch-style ModalBottomSheet)
            BoardEditMetadataSheet(
                board = boardToEditMetadata,
                onDismiss = { boardToEditMetadata = null },
                onSave = { boardId, title, venue, group, labels ->
                    onAction(BoardAction.UpdateBoardMetadata(boardId, title, venue, group, labels))
                }
            )

            // Board Actions Sheet (Star, Rename, Duplicate, Export, Move to Trash)
            BoardActionsSheet(
                board = selectedBoardForActions,
                onDismiss = { selectedBoardForActions = null },
                onToggleStar = { board -> onAction(BoardAction.ToggleStarBoard(board.id)) },
                onRename = { board -> boardToEditMetadata = board },
                onDuplicate = { board -> onAction(BoardAction.DuplicateBoard(board.id)) },
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
                onFormatSelected = { format -> boardToExport?.let { b -> shareBoard(b, format) } }
            )

            if (showBatchDeleteConfirm) {
                DeleteConfirmDialog(
                    title = "Move to Trash?",
                    text = "Are you sure you want to move ${selectedBoardIds.size} boards to Trash?",
                    confirmText = "Move to Trash",
                    onConfirm = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedBoardIds.forEach { onAction(BoardAction.MoveBoardToBin(it)) }
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
                        onAction(BoardAction.MoveBoardToBin(targetId))
                        boardIdToDeleteWithConfirm = null
                    },
                    onDismiss = { boardIdToDeleteWithConfirm = null }
                )
            }
        }
    }
}