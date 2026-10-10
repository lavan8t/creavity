package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

enum class LibraryTabMode(val label: String, val icon: String) {
    RECENT("Recent", "schedule"),
    FOLDERS("Folders", "folder")
}

/**
 * Modern M3 segmented pill tab switcher for Home screen views:
 * "Recent" (recently accessed boards) vs "Folders" (folder preview cards).
 */
@Composable
fun LibraryTabSwitcher(
    selectedTab: LibraryTabMode,
    onTabSelected: (LibraryTabMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LibraryTabMode.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    shape = RoundedCornerShape(50),
                    color = containerColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(tab)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MaterialSymbol(
                            name = tab.icon,
                            filled = isSelected,
                            tint = contentColor,
                            size = 18.dp
                        )
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Folder Card showing a 2x2 miniature preview container holding up to 4 items inside that folder.
 * [previewItemModifier] provides the shared element modifier per board to enable seamless translation.
 */
@Composable
fun FolderCard(
    folderName: String,
    boards: List<Board>,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier,
    previewItemModifier: @Composable (Board) -> Modifier = { Modifier }
) {
    val haptic = LocalHapticFeedback.current
    val previewBoards = boards.take(4)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenFolder()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2x2 Miniature Preview Container (holding up to 4 items inside)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(6.dp)
            ) {
                if (boards.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialSymbol(
                            name = "folder_open",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            size = 36.dp
                        )
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

            // Folder Title & Item Count Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialSymbol(
                    name = "folder",
                    filled = true,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 22.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folderName.ifBlank { "Unfiled" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${boards.size} ${if (boards.size == 1) "item" else "items"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                MaterialSymbol(
                    name = "chevron_right",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 18.dp
                )
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
    modifier: Modifier = Modifier
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
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count ${if (count == 1) "item" else "items"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
