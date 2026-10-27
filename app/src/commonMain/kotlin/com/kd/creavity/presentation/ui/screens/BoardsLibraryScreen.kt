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
import com.kd.creavity.presentation.ui.components.FolderCard
import com.kd.creavity.presentation.ui.components.FolderDetailHeader
import com.kd.creavity.presentation.ui.components.GroupFolderFilterRow
import com.kd.creavity.presentation.ui.components.LibraryNavDrawer
import com.kd.creavity.presentation.ui.components.LibrarySearchBar
import com.kd.creavity.presentation.ui.components.LibrarySelectionTopBar
import com.kd.creavity.presentation.ui.components.LibraryViewMode
import com.kd.creavity.presentation.ui.components.CreateFolderSheet
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

    var openedFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var isFolderGridView by rememberSaveable { mutableStateOf(true) }
    var showCreateFolderSheet by remember { mutableStateOf(false) }
    var folderToDeleteConfirm by remember { mutableStateOf<String?>(null) }
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

    BackHandler(enabled = !isSelectionMode && openedFolder != null) {
        openedFolder = null
    }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val folderOverviewGridState = rememberLazyGridState()
    val folderDetailGridState = rememberLazyGridState()
    val folderDetailListState = rememberLazyListState()

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

    val allFolderNames = remember(activeBoards, uiState.userFolders) {
        val fromBoards = activeBoards.map { it.group.trim() }.filter { it.isNotBlank() }
        (uiState.userFolders + fromBoards).distinct().sorted()
    }

    // Folders map: Maps folder name -> List of Boards inside it
    val foldersMap = remember(allFolderNames, activeBoards, searchQuery) {
        val q = searchQuery.trim()
        val map = LinkedHashMap<String, List<Board>>()
        allFolderNames.forEach { g ->
            val inG = activeBoards.filter { it.group.equals(g, ignoreCase = true) }
            if (q.isBlank() || g.contains(q, ignoreCase = true) || inG.any { it.title.contains(q, ignoreCase = true) }) {
                map[g] = inG
            }
        }
        val unfiled = activeBoards.filter { it.group.isBlank() }
        if (unfiled.isNotEmpty() && (q.isBlank() || unfiled.any { it.title.contains(q, ignoreCase = true) })) {
            map["Unfiled"] = unfiled
        }
        map
    }

    // Boards inside current opened folder
    val currentFolderBoards = remember(activeBoards, openedFolder, searchQuery, sortOrder, isAscending) {
        val folder = openedFolder ?: return@remember emptyList()
        val inFolder = activeBoards.filter { b ->
            if (folder == "Unfiled" || folder.isEmpty()) b.group.isBlank()
            else b.group.equals(folder, ignoreCase = true)
        }
        val searched = if (searchQuery.isBlank()) inFolder else {
            val q = searchQuery.trim()
            inFolder.filter { b ->
                b.title.contains(q, ignoreCase = true) ||
                b.venue.contains(q, ignoreCase = true) ||
                b.labels.any { l -> l.contains(q, ignoreCase = true) }
            }
        }
        when (sortOrder) {
            LibrarySortOrder.LAST_ACCESSED -> if (isAscending) searched.sortedBy { it.lastAccessedAt } else searched.sortedByDescending { it.lastAccessedAt }
            LibrarySortOrder.LAST_MODIFIED -> if (isAscending) searched.sortedBy { it.modifiedAt } else searched.sortedByDescending { it.modifiedAt }
            LibrarySortOrder.TITLE -> if (isAscending) searched.sortedBy { it.title.lowercase() } else searched.sortedByDescending { it.title.lowercase() }
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

                // 2. Sub-header Bar: Swapped Actions (New Folder / Sort) on Left, View Mode Switcher on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (LibraryViewMode.FOLDERS != LibraryViewMode.FOLDERS) {
                            // Sort Dropdown Button + Direction Toggle
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
                        } else {
                            // In Folders mode: Create New Folder Button
                            TooltipHint(text = "New folder") {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showCreateFolderSheet = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    MaterialSymbol(
                                        name = "create_new_folder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        size = 22.dp
                                    )
                                }
                            }
                        }
                    }

                    // Always folders view (folder toggler removed; inside-folder grid/list kept in FolderDetailHeader)
                }

                // Group folders filter row (in Grid or List mode when groups exist)
                if (false) {
                    GroupFolderFilterRow(
                        groups = distinctGroups,
                        selectedGroup = selectedGroup,
                        onSelectGroup = { group ->
                            selectedGroup = if (selectedGroup == group) null else group
                        }
                    )
                }

                // 3. Main Content: Grid, List, or Folders Shared Element View
                when (LibraryViewMode.FOLDERS) {
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
                    LibraryViewMode.FOLDERS -> {
                        // ==================== FOLDERS VIEW (SHARED ELEMENT) ====================
                        SharedTransitionLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            AnimatedContent(
                                targetState = openedFolder,
                                transitionSpec = {
                                    fadeIn(animationSpec = BrevityMotion.springSpatial()) togetherWith
                                    fadeOut(animationSpec = BrevityMotion.springSpatial())
                                },
                                label = "FolderTransition"
                            ) { targetFolder ->
                                if (targetFolder == null) {
                                    // Folder Overview: Folders grid with cardless cards and names strictly above
                                    if (foldersMap.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
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
                                                    MaterialSymbol(name = "folder_open", size = 48.dp, tint = MaterialTheme.colorScheme.primary)
                                                    Text(
                                                        text = if (searchQuery.isNotEmpty()) "No folders match '$searchQuery'" else "No folders yet",
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
                                            state = folderOverviewGridState,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(bottom = 96.dp),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .drawGridScrollbar(folderOverviewGridState, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                                        ) {
                                            items(foldersMap.entries.toList(), key = { it.key }) { (folderName, boards) ->
                                                FolderCard(
                                                    folderName = folderName,
                                                    boards = boards,
                                                    onOpenFolder = { openedFolder = folderName },
                                                    onDeleteFolder = if (boards.isEmpty() && folderName != "Unfiled") {
                                                        { folderToDeleteConfirm = folderName }
                                                    } else null,
                                                    folderNameModifier = Modifier.sharedBounds(
                                                        sharedContentState = rememberSharedContentState(key = "folder_title_${folderName}"),
                                                        animatedVisibilityScope = this@AnimatedContent,
                                                        boundsTransform = { _, _ -> BrevityMotion.springSpatial() }
                                                    ),
                                                    previewItemModifier = { board ->
                                                        Modifier.sharedElement(
                                                            rememberSharedContentState(key = "board_thumb_${board.id}"),
                                                            animatedVisibilityScope = this@AnimatedContent,
                                                            boundsTransform = { _, _ -> BrevityMotion.springSpatial() }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Opened Folder Detail: Grid or List of boards in this folder
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FolderDetailHeader(
                                            folderName = targetFolder,
                                            count = currentFolderBoards.size,
                                            isGridView = isFolderGridView,
                                            onToggleView = { isFolderGridView = !isFolderGridView },
                                            onBack = { openedFolder = null },
                                            onDeleteFolder = if (currentFolderBoards.isEmpty() && targetFolder != "Unfiled") {
                                                { folderToDeleteConfirm = targetFolder }
                                            } else null,
                                            folderNameModifier = Modifier.sharedBounds(
                                                sharedContentState = rememberSharedContentState(key = "folder_title_${targetFolder}"),
                                                animatedVisibilityScope = this@AnimatedContent,
                                                boundsTransform = { _, _ -> BrevityMotion.springSpatial() }
                                            )
                                        )

                                        if (currentFolderBoards.isEmpty()) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().weight(1f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No boards in $targetFolder",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        } else if (isFolderGridView) {
                                            LazyVerticalGrid(
                                                columns = GridCells.Adaptive(minSize = 170.dp),
                                                state = folderDetailGridState,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                                contentPadding = PaddingValues(bottom = 96.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .drawGridScrollbar(folderDetailGridState, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                                            ) {
                                                items(currentFolderBoards, key = { it.id }) { board ->
                                                    val isSelected = selectedBoardIds.contains(board.id)
                                                    BoardGridCard(
                                                        board = board,
                                                        isSelected = isSelected,
                                                        isSelectionMode = isSelectionMode,
                                                        thumbnailModifier = Modifier.sharedElement(
                                                            rememberSharedContentState(key = "board_thumb_${board.id}"),
                                                            animatedVisibilityScope = this@AnimatedContent,
                                                            boundsTransform = { _, _ -> BrevityMotion.springSpatial() }
                                                        ),
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
                                        } else {
                                            LazyColumn(
                                                state = folderDetailListState,
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                                contentPadding = PaddingValues(bottom = 96.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .drawListScrollbar(folderDetailListState, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                                            ) {
                                                items(currentFolderBoards, key = { it.id }) { board ->
                                                    val isSelected = selectedBoardIds.contains(board.id)
                                                    BoardListRow(
                                                        board = board,
                                                        isSelected = isSelected,
                                                        isSelectionMode = isSelectionMode,
                                                        thumbnailModifier = Modifier.sharedElement(
                                                            rememberSharedContentState(key = "board_thumb_${board.id}"),
                                                            animatedVisibilityScope = this@AnimatedContent,
                                                            boundsTransform = { _, _ -> BrevityMotion.springSpatial() }
                                                        ),
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
                            val folderGroup = if (LibraryViewMode.FOLDERS == LibraryViewMode.FOLDERS && openedFolder != null && openedFolder != "Unfiled") {
                                openedFolder ?: ""
                            } else ""
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
            if (folderToDeleteConfirm != null) {
                val targetFolder = folderToDeleteConfirm!!
                DeleteConfirmDialog(
                    title = "Delete Folder?",
                    text = "Are you sure you want to delete the empty folder '$targetFolder'?",
                    confirmText = "Delete Folder",
                    onConfirm = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAction(BoardAction.DeleteFolder(targetFolder))
                        if (openedFolder == targetFolder) {
                            openedFolder = null
                        }
                        folderToDeleteConfirm = null
                    },
                    onDismiss = { folderToDeleteConfirm = null }
                )
            }

            CreateFolderSheet(
                show = showCreateFolderSheet,
                onDismiss = { showCreateFolderSheet = false },
                onCreate = { folderName ->
                    onAction(BoardAction.CreateFolder(folderName))
                }
            )
        }
    }
}
