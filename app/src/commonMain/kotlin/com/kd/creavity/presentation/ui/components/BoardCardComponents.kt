package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.Board
import com.kd.creavity.util.isDesktopPlatform
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Reusable Board Card for Grid views (Recent view and Opened Folder view).
 * Supports custom [thumbnailModifier] for shared element transitions.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardGridCard(
    board: Board,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onCardClick: () -> Unit,
    onCardLongClick: () -> Unit,
    onToggleStar: () -> Unit,
    onOptionsClick: () -> Unit,
    onCardRightClick: ((Offset) -> Unit)? = null,
    onOpenBoard: () -> Unit = onCardClick,
    onDuplicateBoard: () -> Unit = {},
    onEditMetadata: () -> Unit = {},
    onExportBoard: () -> Unit = {},
    onDeleteBoard: () -> Unit = {},
    modifier: Modifier = Modifier,
    thumbnailModifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(14.dp),
            border = null,
            modifier = Modifier
                .fillMaxWidth()
                .onDesktopRightClick { offset ->
                    contextMenuOffset = offset
                    showContextMenu = true
                    onCardRightClick?.invoke(offset)
                }
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onCardClick,
                    onLongClick = onCardLongClick
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Thumbnail Preview (16:9)
                Box(
                    modifier = thumbnailModifier
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

                // Card Footer: Title + Favourite + 3-dots on top row, metadata badges below
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = board.title.ifBlank { "Untitled Board" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Favourite Star Button
                        TooltipHint(text = if (board.isStarred) "Remove favourite" else "Add to favourites") {
                            IconButton(
                                onClick = onToggleStar,
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

                        // 3-dots overflow to open action sheet or desktop menu
                        TooltipHint(text = "Board options") {
                            IconButton(
                                onClick = {
                                    if (isDesktopPlatform()) {
                                        contextMenuOffset = Offset.Zero
                                        showContextMenu = true
                                    } else {
                                        onOptionsClick()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                MaterialSymbol(name = "more_vert", size = 18.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    BoardMetadataBadges(
                        board = board,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Cursor anchor point for exact right-click dropdown positioning
        Box(
            modifier = Modifier
                .offset { IntOffset(contextMenuOffset.x.toInt(), contextMenuOffset.y.toInt()) }
                .size(0.dp)
        ) {
            DesktopBoardCardContextMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                screenOffset = Offset.Zero,
                board = board,
                onOpenBoard = {
                    showContextMenu = false
                    onOpenBoard()
                },
                onDuplicateBoard = {
                    showContextMenu = false
                    onDuplicateBoard()
                },
                onToggleStar = {
                    showContextMenu = false
                    onToggleStar()
                },
                onEditMetadata = {
                    showContextMenu = false
                    onEditMetadata()
                },
                onExportBoard = {
                    showContextMenu = false
                    onExportBoard()
                },
                onDeleteBoard = {
                    showContextMenu = false
                    onDeleteBoard()
                }
            )
        }
    }
}

/**
 * Reusable Board List Row for List views (Recent view and Opened Folder view).
 * Supports custom [thumbnailModifier] for shared element transitions.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardListRow(
    board: Board,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    dateFormat: SimpleDateFormat,
    onRowClick: () -> Unit,
    onRowLongClick: () -> Unit,
    onToggleStar: () -> Unit,
    onOptionsClick: () -> Unit,
    onRowRightClick: ((Offset) -> Unit)? = null,
    onOpenBoard: () -> Unit = onRowClick,
    onDuplicateBoard: () -> Unit = {},
    onEditMetadata: () -> Unit = {},
    onExportBoard: () -> Unit = {},
    onDeleteBoard: () -> Unit = {},
    modifier: Modifier = Modifier,
    thumbnailModifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        Surface(
            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f) else Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            border = null,
            modifier = Modifier
                .fillMaxWidth()
                .onDesktopRightClick { offset ->
                    contextMenuOffset = offset
                    showContextMenu = true
                    onRowRightClick?.invoke(offset)
                }
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onRowClick,
                    onLongClick = onRowLongClick
                )
                .padding(vertical = 6.dp, horizontal = if (isDesktopPlatform()) 12.dp else 4.dp)
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

                Box(
                    modifier = thumbnailModifier
                        .size(width = 54.dp, height = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                ) {
                    BoardThumbnailPreview(
                        board = board,
                        modifier = Modifier.fillMaxSize()
                    )
                }

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
                        onClick = onToggleStar,
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

                // 3-dots Menu to open action sheet or desktop menu
                TooltipHint(text = "Board options") {
                    IconButton(
                        onClick = {
                            if (isDesktopPlatform()) {
                                contextMenuOffset = Offset.Zero
                                showContextMenu = true
                            } else {
                                onOptionsClick()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(name = "more_vert", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                    }
                }
            }
        }

        // Cursor anchor point for exact right-click dropdown positioning
        Box(
            modifier = Modifier
                .offset { IntOffset(contextMenuOffset.x.toInt(), contextMenuOffset.y.toInt()) }
                .size(0.dp)
        ) {
            DesktopBoardCardContextMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                screenOffset = Offset.Zero,
                board = board,
                onOpenBoard = {
                    showContextMenu = false
                    onOpenBoard()
                },
                onDuplicateBoard = {
                    showContextMenu = false
                    onDuplicateBoard()
                },
                onToggleStar = {
                    showContextMenu = false
                    onToggleStar()
                },
                onEditMetadata = {
                    showContextMenu = false
                    onEditMetadata()
                },
                onExportBoard = {
                    showContextMenu = false
                    onExportBoard()
                },
                onDeleteBoard = {
                    showContextMenu = false
                    onDeleteBoard()
                }
            )
        }
    }
}

fun Modifier.drawListScrollbar(state: androidx.compose.foundation.lazy.LazyListState, color: Color): Modifier = this.drawWithContent {
    drawContent()
    val (total, visible) = state.layoutInfo.totalItemsCount to state.layoutInfo.visibleItemsInfo.size
    if (visible in 1..<total) {
        val barH = (size.height * (visible.toFloat() / total)).coerceAtLeast(32.dp.toPx())
        val barY = (size.height - barH) * (state.firstVisibleItemIndex.toFloat() / (total - visible).coerceAtLeast(1))
        drawRoundRect(color, Offset(size.width - 4.dp.toPx(), barY), Size(3.dp.toPx(), barH), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    }
}

fun Modifier.drawGridScrollbar(state: androidx.compose.foundation.lazy.grid.LazyGridState, color: Color): Modifier = this.drawWithContent {
    drawContent()
    val (total, visible) = state.layoutInfo.totalItemsCount to state.layoutInfo.visibleItemsInfo.size
    if (visible in 1..<total) {
        val barH = (size.height * (visible.toFloat() / total)).coerceAtLeast(32.dp.toPx())
        val barY = (size.height - barH) * (state.firstVisibleItemIndex.toFloat() / (total - visible).coerceAtLeast(1))
        drawRoundRect(color, Offset(size.width - 4.dp.toPx(), barY), Size(3.dp.toPx(), barH), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    }
}

