package com.kd.creavity.presentation.ui.components

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
import com.kd.creavity.domain.model.BoardPage
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
    onCastClick: () -> Unit,
    onMoreClick: () -> Unit,
    zoom: Float = 1.0f,
    isHost: Boolean = false,
    onBirdsEyeClick: (() -> Unit)? = null,
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
}


