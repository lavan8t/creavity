package com.kd.creavity.presentation.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.ui.components.DeleteConfirmDialog
import com.kd.creavity.presentation.ui.components.MaterialSymbol
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinScreen(
    uiState: WhiteboardUiState,
    onAction: (BoardAction) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val binnedBoards = remember(uiState.savedBoards) {
        uiState.savedBoards.filter { it.isInBin }
    }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    var showEmptyBinConfirmation by remember { mutableStateOf(false) }
    var boardIdToDeletePermanently by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val isWideScreen = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Trash",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = if (isWideScreen) 24.dp else 0.dp)
                        )
                    },
                    navigationIcon = {
                        if (!isWideScreen) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBackPressed()
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .clip(CircleShape)
                            ) {
                                MaterialSymbol(
                                    name = "arrow_back",
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 20.dp
                                )
                            }
                        }
                    },
                    actions = {
                        if (binnedBoards.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showEmptyBinConfirmation = true
                                },
                                modifier = Modifier.padding(end = if (isWideScreen) 24.dp else 8.dp)
                            ) {
                                Text(
                                    text = "Empty Trash",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = if (isWideScreen) 16.dp else 0.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            if (binnedBoards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MaterialSymbol(
                                name = "delete",
                                size = 48.dp,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Trash is empty",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    contentPadding = PaddingValues(
                        horizontal = if (isWideScreen) 36.dp else 16.dp,
                        vertical = if (isWideScreen) 20.dp else 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 16.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isWideScreen) 16.dp else 12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    items(binnedBoards, key = { it.id }) { board ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = board.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val dateStr = board.binnedAt?.let {
                                            dateFormat.format(Date(it))
                                        } ?: "In Trash"
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAction(BoardAction.RestoreBoardFromBin(board.id))
                                        },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        MaterialSymbol(name = "restore", size = 18.dp, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Restore")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            boardIdToDeletePermanently = board.id
                                        },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        MaterialSymbol(name = "delete", size = 18.dp, tint = MaterialTheme.colorScheme.onErrorContainer)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyBinConfirmation) {
        DeleteConfirmDialog(
            title = "Empty Trash?",
            text = "All items will be permanently deleted.",
            confirmText = "Delete",
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAction(BoardAction.EmptyBin)
                showEmptyBinConfirmation = false
            },
            onDismiss = { showEmptyBinConfirmation = false }
        )
    }

    if (boardIdToDeletePermanently != null) {
        val targetId = boardIdToDeletePermanently!!
        DeleteConfirmDialog(
            title = "Are you sure you want to permanently delete this board?",
            text = "This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAction(BoardAction.DeleteBoardPermanently(targetId))
                boardIdToDeletePermanently = null
            },
            onDismiss = { boardIdToDeletePermanently = null }
        )
    }
}
}
