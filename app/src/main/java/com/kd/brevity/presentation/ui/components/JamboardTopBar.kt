package com.kd.brevity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.domain.model.BoardPage
import com.kd.brevity.presentation.ui.motion.BrevityMotion

/**
 * Top App Bar with 2 compact rows:
 * Row 1: Header (Back + Clickable Title | Minimap + Cast + 3-dots)
 * Row 2: Sub-bar (Centered frame navigator pill < [ 1/N ] > + New Frame '+' + Zoom Reset)
 */
@Composable
fun JamboardTopBar(
    title: String = "Untitled",
    onTitleClick: () -> Unit = {},
    activePageIndex: Int,
    totalPages: Int,
    onBack: () -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onAddPage: () -> Unit = {},
    onFrameCountClick: () -> Unit,
    onCastClick: () -> Unit,
    onMoreClick: () -> Unit,
    zoom: Float = 1.0f,
    onResetZoom: () -> Unit = {},
    isHost: Boolean = false,
    onBirdsEyeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: Header Bar (Back + Title on Left, Controls on Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Back button + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TooltipHint(text = "Back to boards") {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(38.dp)
                    ) {
                        MaterialSymbol(
                            name = "arrow_back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            size = 22.dp
                        )
                    }
                }

                TooltipHint(text = "Rename board") {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onTitleClick)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = title.ifBlank { "Untitled" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right: Cast and 3-dots Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                // Host Bird's Eye View Button
                if (isHost && onBirdsEyeClick != null) {
                    TooltipHint(text = "Bird's eye view (Host)") {
                        IconButton(
                            onClick = onBirdsEyeClick,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "filter_center_focus",
                                tint = MaterialTheme.colorScheme.primary,
                                size = 20.dp
                            )
                        }
                    }
                }

                // Cast to Screen / Peers
                TooltipHint(text = "Cast to screen") {
                    IconButton(
                        onClick = onCastClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        MaterialSymbol(
                            name = "devices",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 20.dp
                        )
                    }
                }

                // 3-dots Overflow Menu trigger
                TooltipHint(text = "More options") {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        MaterialSymbol(
                            name = "more_vert",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 20.dp
                        )
                    }
                }
            }
        }

        // Row 2: Sub-bar (Centered compact frame navigator pill + optional zoom reset)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp,
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    TooltipHint(text = "Previous frame") {
                        IconButton(
                            onClick = onPrevPage,
                            enabled = activePageIndex > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            MaterialSymbol(
                                name = "chevron_left",
                                tint = if (activePageIndex > 0) MaterialTheme.colorScheme.onSurface
                                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 18.dp
                            )
                        }
                    }

                    // Frame count button
                    TooltipHint(text = "Frames & pages") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onFrameCountClick)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            MaterialSymbol(
                                name = "filter_none",
                                tint = MaterialTheme.colorScheme.primary,
                                size = 15.dp
                            )
                            Text(
                                text = "${activePageIndex + 1}/${totalPages.coerceAtLeast(1)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    TooltipHint(text = "Next frame") {
                        IconButton(
                            onClick = onNextPage,
                            enabled = activePageIndex < totalPages - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            MaterialSymbol(
                                name = "chevron_right",
                                tint = if (activePageIndex < totalPages - 1) MaterialTheme.colorScheme.onSurface
                                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 18.dp
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Add Frame shortcut (+)
                    TooltipHint(text = "New frame") {
                        IconButton(
                            onClick = onAddPage,
                            modifier = Modifier.size(28.dp)
                        ) {
                            MaterialSymbol(
                                name = "add",
                                tint = MaterialTheme.colorScheme.primary,
                                size = 18.dp
                            )
                        }
                    }

                    if (kotlin.math.abs(zoom - 1.0f) > 0.05f) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        TooltipHint(text = "Reset zoom (100%)") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(onClick = onResetZoom)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${(zoom * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable Frame Bar showing all frame thumbnails at the top of the canvas.
 */
@Composable
fun JamboardFrameBar(
    visible: Boolean,
    pages: List<BoardPage>,
    activePageIndex: Int,
    onSelectPage: (Int) -> Unit,
    onAddPage: () -> Unit,
    onDeletePage: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = BrevityMotion.enterSlideDown(),
        exit = BrevityMotion.exitSlideUp(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frames (${pages.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TooltipHint(text = "Close frames") {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            MaterialSymbol(name = "close", size = 20.dp, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(pages) { index, page ->
                        val isSelected = index == activePageIndex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary) else null,
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                modifier = Modifier
                                    .size(width = 96.dp, height = 58.dp)
                                    .clickable { onSelectPage(index) }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "Frame ${index + 1}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Add Frame button
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .size(width = 96.dp, height = 58.dp)
                                    .clickable(onClick = onAddPage)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialSymbol(
                                        name = "add",
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 28.dp
                                    )
                                }
                            }
                            Text(
                                text = "New Frame",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
