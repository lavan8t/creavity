package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.motion.BrevityMotion

/**
 * Top App Bar with 2 compact rows:
 * Row 1: Header (Back + Clickable Title | Minimap + Cast + 3-dots)
 * Row 2: Sub-bar (Centered frame navigator pill < [ 1/N ] > + New Frame '+' + Zoom Reset)
 */
@Composable
fun WhiteboardTopBar(
    title: String = "Untitled",
    onTitleClick: () -> Unit = {},
    activePageIndex: Int,
    totalPages: Int,
    onBack: () -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onAddPage: () -> Unit = {},
    onFrameCountClick: () -> Unit,
    onMoreClick: () -> Unit,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    zoom: Float = 1.0f,
    onZoomIn: (() -> Unit)? = null,
    onZoomOut: (() -> Unit)? = null,
    onResetZoom: (() -> Unit)? = null,
    onToggleChat: (() -> Unit)? = null,
    unreadChatCount: Int = 0,
    isChatOpen: Boolean = false,
    isHost: Boolean = false,
    connectedPeersCount: Int = 0,
    isBoardSwitchingLocked: Boolean = false,
    onBirdsEyeClick: (() -> Unit)? = null,
    onOmniviewClick: (() -> Unit)? = null,
    onQuizClick: (() -> Unit)? = null,
    onToggleCommandCenter: (() -> Unit)? = null,
    isCommandCenterOpen: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back button + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            TooltipHint(text = if (isBoardSwitchingLocked) "Locked to shared host session" else "Back to boards") {
                IconButton(
                    onClick = { if (!isBoardSwitchingLocked) onBack() },
                    enabled = !isBoardSwitchingLocked,
                    modifier = Modifier.size(38.dp)
                ) {
                    MaterialSymbol(
                        name = if (isBoardSwitchingLocked) "lock" else "arrow_back",
                        tint = if (isBoardSwitchingLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface,
                        size = 22.dp
                    )
                }
            }

            TooltipHint(text = if (isBoardSwitchingLocked) "Board locked by host" else "Rename board") {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isBoardSwitchingLocked, onClick = onTitleClick)
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

        // Right: Undo, Redo, and 3-dots Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Undo button (hidden if stack empty)
            AnimatedVisibility(
                visible = canUndo,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                TooltipHint(text = "Undo") {
                    IconButton(
                        onClick = onUndo,
                        modifier = Modifier.size(38.dp)
                    ) {
                        MaterialSymbol(
                            name = "undo",
                            tint = MaterialTheme.colorScheme.onSurface,
                            size = 20.dp
                        )
                    }
                }
            }

            // Redo button (hidden if stack empty)
            AnimatedVisibility(
                visible = canRedo,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                TooltipHint(text = "Redo") {
                    IconButton(
                        onClick = onRedo,
                        modifier = Modifier.size(38.dp)
                    ) {
                        MaterialSymbol(
                            name = "redo",
                            tint = MaterialTheme.colorScheme.onSurface,
                            size = 20.dp
                        )
                    }
                }
            }

            // Zoom indicator & controls pill
            if (onZoomIn != null && onZoomOut != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(32.dp).padding(horizontal = 4.dp)
                    ) {
                        TooltipHint(text = "Zoom out (Ctrl + -)") {
                            IconButton(
                                onClick = onZoomOut,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        TooltipHint(text = "Reset zoom to 100% (Ctrl + 0)") {
                            Text(
                                text = "${kotlin.math.round(zoom * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onResetZoom?.invoke() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        TooltipHint(text = "Zoom in (Ctrl + +)") {
                            IconButton(
                                onClick = onZoomIn,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Collaborative Chat Toggle Button
            if (onToggleChat != null) {
                TooltipHint(text = if (isChatOpen) "Close Chat" else "Open Classroom Chat") {
                    Box {
                        IconButton(
                            onClick = onToggleChat,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "chat",
                                tint = if (isChatOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                        if (unreadChatCount > 0 && !isChatOpen) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                            ) {
                                Text(
                                    text = if (unreadChatCount > 9) "9+" else unreadChatCount.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Host Quick Actions (only visible when a collab is active with online participants)
            if (isHost && connectedPeersCount > 0) {
                if (onToggleCommandCenter != null) {
                    TooltipHint(text = if (isCommandCenterOpen) "Hide Classroom Cockpit" else "Show Classroom Cockpit") {
                        IconButton(
                            onClick = onToggleCommandCenter,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "hub",
                                tint = if (isCommandCenterOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }
                }

                if (onOmniviewClick != null) {
                    TooltipHint(text = "Omniview Student Matrix") {
                        IconButton(
                            onClick = onOmniviewClick,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "grid_view",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }
                }

                if (onQuizClick != null) {
                    TooltipHint(text = "Interactive Quiz & Poll Deck") {
                        IconButton(
                            onClick = onQuizClick,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "checklist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }
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
}


