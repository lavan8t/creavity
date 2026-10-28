package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.Board
import com.kd.creavity.presentation.ui.motion.BrevityMotion

enum class LibraryViewMode(val label: String, val icon: String) {
    GRID("Grid", "grid_view"),
    LIST("List", "view_list")
}

/**
 * Unified 3-way view switcher for Home screen: Folders, Grid, and List.
 * Clean Material 3 button group with icons only (no text labels).
 */
@Composable
fun LibraryViewModeSwitcher(
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            LibraryViewMode.entries.forEach { mode ->
                val isSelected = viewMode == mode
                val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                val containerColor by animateColorAsState(
                    targetValue = targetContainerColor,
                    animationSpec = BrevityMotion.springEffects(),
                    label = "ViewModeContainerColor"
                )
                val targetContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                val contentColor by animateColorAsState(
                    targetValue = targetContentColor,
                    animationSpec = BrevityMotion.springEffects(),
                    label = "ViewModeContentColor"
                )

                TooltipHint(text = mode.label) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = containerColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onViewModeChange(mode)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialSymbol(
                                name = mode.icon,
                                filled = isSelected,
                                tint = contentColor,
                                size = 20.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Folder Card showing the folder name strictly above the 2x2 miniature board previews.
 * No card container background wrapper.
 * Non-empty folders cannot be deleted; empty folders stay in list and are deletable.
 */
@Composable
fun FolderCard(
    folderName: String,
    boards: List<Board>,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteFolder: (() -> Unit)? = null,
    folderNameModifier: Modifier = Modifier,
    previewItemModifier: @Composable (Board) -> Modifier = { Modifier }
) {
    val haptic = LocalHapticFeedback.current
    val previewBoards = boards.take(4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenFolder()
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Folder Name and Count Header sitting ABOVE the preview (no card wrapper)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                MaterialSymbol(
                    name = "folder",
                    filled = true,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 20.dp
                )
                Text(
                    text = folderName.ifBlank { "Unfiled" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = folderNameModifier
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${boards.size} ${if (boards.size == 1) "item" else "items"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Delete button is strictly only available when folder has 0 contents
                if (boards.isEmpty() && folderName != "Unfiled" && onDeleteFolder != null) {
                    TooltipHint(text = "Delete empty folder") {
                        IconButton(
                            onClick = onDeleteFolder,
                            modifier = Modifier.size(26.dp)
                        ) {
                            MaterialSymbol(
                                name = "delete",
                                tint = MaterialTheme.colorScheme.error,
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Miniature Preview Container directly below the name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(6.dp)
        ) {
            if (boards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MaterialSymbol(
                            name = "folder_open",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            size = 32.dp
                        )
                        Text(
                            text = "Empty folder",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Top Row: items 0 and 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val b0 = previewBoards.getOrNull(0)
                        if (b0 != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .then(previewItemModifier(b0))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                BoardThumbnailPreview(
                                    board = b0,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f))
                            )
                        }

                        val b1 = previewBoards.getOrNull(1)
                        if (b1 != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .then(previewItemModifier(b1))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                BoardThumbnailPreview(
                                    board = b1,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f))
                            )
                        }
                    }

                    // Bottom Row: items 2 and 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val b2 = previewBoards.getOrNull(2)
                        if (b2 != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .then(previewItemModifier(b2))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                BoardThumbnailPreview(
                                    board = b2,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f))
                            )
                        }

                        val b3 = previewBoards.getOrNull(3)
                        if (b3 != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .then(previewItemModifier(b3))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                BoardThumbnailPreview(
                                    board = b3,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top header shown when viewing an opened folder.
 */
@Composable
fun FolderDetailHeader(
    folderName: String,
    count: Int,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    onBack: () -> Unit,
    onDeleteFolder: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    folderNameModifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            TooltipHint(text = "Back to folders") {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    MaterialSymbol(
                        name = "arrow_back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 22.dp
                    )
                }
            }

            Column {
                Text(
                    text = folderName.ifBlank { "Unfiled" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = folderNameModifier
                )
                Text(
                    text = "$count ${if (count == 1) "item" else "items"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Delete folder button (strictly only if folder has 0 contents)
            if (count == 0 && folderName != "Unfiled" && onDeleteFolder != null) {
                TooltipHint(text = "Delete folder") {
                    IconButton(
                        onClick = onDeleteFolder,
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(
                            name = "delete",
                            tint = MaterialTheme.colorScheme.error,
                            size = 20.dp
                        )
                    }
                }
            }

            // Grid / List toggle for opened folder
            TooltipHint(text = if (isGridView) "Switch to list view" else "Switch to grid view") {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleView()
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
}

/**
 * ModalBottomSheet to create a new folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    if (!show) return
    var folderName by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    AdaptiveModalSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "New Folder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                placeholder = { Text("e.g. Work, Ideas, Sketches") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    enabled = folderName.isNotBlank() && !folderName.trim().equals("Unfiled", ignoreCase = true),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCreate(folderName.trim())
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
