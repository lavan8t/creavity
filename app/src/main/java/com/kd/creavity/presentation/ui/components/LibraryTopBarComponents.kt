package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top bar displayed when multiple boards are selected in library selection mode.
 */
@Composable
fun LibrarySelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    isAllStarred: Boolean,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onToggleStar: () -> Unit,
    onEditDetails: () -> Unit,
    onMoveToTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val allSelected = selectedCount == totalCount && totalCount > 0

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
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
                        onClose()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    MaterialSymbol(name = "close", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = "$selectedCount selected",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )

            // Select All / Deselect All
            TooltipHint(text = if (allSelected) "Deselect all" else "Select all") {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSelectAll()
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
                    enabled = selectedCount > 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleStar()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    MaterialSymbol(
                        name = if (isAllStarred) "star" else "star_outline",
                        filled = isAllStarred,
                        tint = if (selectedCount == 0) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 22.dp
                    )
                }
            }

            // Edit Details (active when exactly 1 board selected)
            TooltipHint(text = "Edit details") {
                IconButton(
                    enabled = selectedCount == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEditDetails()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    MaterialSymbol(
                        name = "edit",
                        tint = if (selectedCount == 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        size = 22.dp
                    )
                }
            }

            // Delete / Move to Trash
            TooltipHint(text = "Move to trash") {
                IconButton(
                    enabled = selectedCount > 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveToTrash()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    MaterialSymbol(
                        name = "delete",
                        tint = if (selectedCount > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        size = 22.dp
                    )
                }
            }
        }
    }
}

/**
 * Top search bar for library screen with navigation drawer hamburger button and clear button.
 */
@Composable
fun LibrarySearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
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
                        onOpenDrawer()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    MaterialSymbol(name = "menu", tint = MaterialTheme.colorScheme.onSurface, size = 24.dp)
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
                    onValueChange = onSearchQueryChange,
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
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(name = "close", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                    }
                }
            }
        }
    }
}
