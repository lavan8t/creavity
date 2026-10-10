package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.presentation.ui.motion.BrevityMotion
import kotlinx.coroutines.delay

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
            .height(62.dp)
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
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
    var showBrandIntro by rememberSaveable { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }

    val isLeftAligned = isFocused || searchQuery.isNotEmpty()
    val textAlign = if (isLeftAligned) TextAlign.Start else TextAlign.Center

    // When focused or search query changes, dismiss brand intro immediately
    LaunchedEffect(isFocused, searchQuery) {
        if (isFocused || searchQuery.isNotEmpty()) {
            showBrandIntro = false
        }
    }

    // On app open, show CreaVITy briefly, then fade out into "Search Boards"
    LaunchedEffect(Unit) {
        if (showBrandIntro) {
            delay(1200L)
            showBrandIntro = false
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Menu icon outside search bar with Material theme color
        TooltipHint(text = "Open menu") {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenDrawer()
                },
                modifier = Modifier.size(48.dp)
            ) {
                MaterialSymbol(
                    name = "menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 24.dp
                )
            }
        }

        // Search Bar Pill
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 2.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = if (searchQuery.isNotEmpty()) 8.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (isLeftAligned) Alignment.CenterStart else Alignment.Center
                ) {
                    if (searchQuery.isEmpty()) {
                        AnimatedContent(
                            targetState = showBrandIntro,
                            transitionSpec = {
                                fadeIn(animationSpec = BrevityMotion.springEffects()) togetherWith
                                    fadeOut(animationSpec = BrevityMotion.springEffects())
                            },
                            label = "SearchBarPlaceholder"
                        ) { isBrand ->
                            if (isBrand) {
                                Text(
                                    text = "CreaVITy",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = textAlign,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "Search Boards",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp,
                                    textAlign = textAlign,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        interactionSource = interactionSource,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            textAlign = textAlign
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    TooltipHint(text = "Clear search") {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
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
}
