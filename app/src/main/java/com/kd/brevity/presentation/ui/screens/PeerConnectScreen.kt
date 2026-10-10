package com.kd.brevity.presentation.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.domain.model.PeerDevice
import com.kd.brevity.presentation.ui.OotbAction
import com.kd.brevity.presentation.ui.components.MaterialSymbol
import com.kd.brevity.presentation.ui.components.TooltipHint
import com.kd.brevity.presentation.ui.motion.BrevityMotion
import com.kd.brevity.presentation.viewmodel.WhiteboardUiState
import kotlin.math.abs

/** Preset emojis for Quick Share avatars */
private val PRESET_EMOJIS = listOf("🎨", "🦊", "⚡", "🚀", "💎", "🌊", "🦉", "🍕", "🍀", "🐱")

/**
 * Extracts an avatar emoji and clean device name from the stored display name.
 * If no emoji prefix is present, chooses a deterministic emoji from the preset list.
 */
private fun parseEmojiAndName(raw: String): Pair<String, String> {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return Pair("🎨", "Whiteboard User")
    for (emoji in PRESET_EMOJIS) {
        if (trimmed.startsWith(emoji)) {
            return Pair(emoji, trimmed.removePrefix(emoji).trim().ifEmpty { "Whiteboard User" })
        }
    }
    val firstCodePoint = trimmed.codePointAt(0)
    if (Character.charCount(firstCodePoint) > 1 || firstCodePoint > 0x2000) {
        val emojiStr = String(Character.toChars(firstCodePoint))
        return Pair(emojiStr, trimmed.removePrefix(emojiStr).trim().ifEmpty { "Whiteboard User" })
    }
    val fallbackEmoji = PRESET_EMOJIS[abs(trimmed.hashCode()) % PRESET_EMOJIS.size]
    return Pair(fallbackEmoji, trimmed)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PeerConnectScreen(
    uiState: WhiteboardUiState,
    onAction: (OotbAction) -> Unit,
    onNavigateToCanvas: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showEditSheet by remember { mutableStateOf(false) }

    BackHandler { onNavigateToCanvas() }

    val (userEmoji, rawName) = remember(uiState.displayName) {
        parseEmojiAndName(uiState.displayName)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TooltipHint(text = "Back") {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateToCanvas()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            MaterialSymbol(name = "arrow_back", tint = MaterialTheme.colorScheme.onSurface, size = 22.dp)
                        }
                    }
                    Text(
                        text = "Collaborate",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Center Profile Hero Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Emoji Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userEmoji, fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You are",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = rawName,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showEditSheet = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                MaterialSymbol(name = "edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
            }

            // Two Pill Buttons Side by Side (Host on left, Join on right)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: Host Button toggles uiState.isAdvertising
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (uiState.isAdvertising) onAction(OotbAction.StopAdvertising)
                            else onAction(OotbAction.StartAdvertising(uiState.displayName))
                        },
                        colors = if (uiState.isAdvertising) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        MaterialSymbol(
                            name = if (uiState.isAdvertising) "sensors" else "cell_tower",
                            size = 18.dp,
                            tint = if (uiState.isAdvertising) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isAdvertising) "Broadcasting" else "Host",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Right: Join Button toggles uiState.isDiscovering
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (uiState.isDiscovering) onAction(OotbAction.StopDiscovery)
                            else onAction(OotbAction.StartDiscovery)
                        },
                        colors = if (uiState.isDiscovering) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        MaterialSymbol(
                            name = if (uiState.isDiscovering) "radar" else "hub",
                            size = 18.dp,
                            tint = if (uiState.isDiscovering) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isDiscovering) "Scanning..." else "Join",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Room Code Card (when hosted or code is active)
            uiState.roomCode?.let { code ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Room Code", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(code, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                            }
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Room Code", code))
                                    Toast.makeText(context, "Room code copied: $code", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                MaterialSymbol(name = "copy", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                            }
                        }
                    }
                }
            }

            // Expressive Radar Pulse Section
            if (uiState.isDiscovering || uiState.isAdvertising) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = BrevityMotion.springEffects()) +
                                scaleIn(initialScale = 0.85f, animationSpec = BrevityMotion.springSpatial()),
                        exit = fadeOut(animationSpec = BrevityMotion.springEffects()) +
                               scaleOut(targetScale = 0.85f, animationSpec = BrevityMotion.springSpatial())
                    ) {
                        QuickShareRadarPulse(
                            isAdvertising = uiState.isAdvertising,
                            isDiscovering = uiState.isDiscovering,
                            displayName = uiState.displayName
                        )
                    }
                }
            }

            // Idle Helper State
            if (!uiState.isDiscovering && !uiState.isAdvertising && uiState.discoveredPeers.isEmpty() && uiState.connectedPeers.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialSymbol(name = "devices", tint = MaterialTheme.colorScheme.primary, size = 28.dp)
                            }
                            Text(
                                text = "Nearby Whiteboard Sharing",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap \"Host\" to broadcast your board, or \"Join\" to find and sync with nearby peers in real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Discovered Nearby Devices (Quick Share circular avatar icons/cards)
            if (uiState.discoveredPeers.isNotEmpty()) {
                item {
                    Text(
                        text = "Available to share (${uiState.discoveredPeers.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        items(uiState.discoveredPeers, key = { it.endpointId }) { peer ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = BrevityMotion.springEffects()) +
                                        scaleIn(initialScale = 0.75f, animationSpec = BrevityMotion.springSpatial()),
                                exit = fadeOut(animationSpec = BrevityMotion.springEffects()) +
                                       scaleOut(targetScale = 0.75f, animationSpec = BrevityMotion.springSpatial())
                            ) {
                                QuickShareDeviceCard(
                                    peer = peer,
                                    onConnect = {
                                        onAction(OotbAction.ConnectPeer(peer.endpointId, uiState.displayName))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Connected Collaborators Section
            if (uiState.connectedPeers.isNotEmpty()) {
                // Mesh Network Topology Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialSymbol(name = "hub", tint = MaterialTheme.colorScheme.primary, size = 24.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mesh Relay Network Active",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Packets dynamically relay across participants for ultra-low latency whiteboard sync",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Collaborators Header & Batch Permissions Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collaborators (${uiState.connectedPeers.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        val allReadOnly = uiState.connectedPeers.all { it.isReadOnly }
                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAction(OotbAction.SetAllPeersPermission(!allReadOnly))
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            MaterialSymbol(
                                name = if (allReadOnly) "lock_open" else "lock",
                                size = 14.dp,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (allReadOnly) "Allow All to Draw" else "Mute All (View-Only)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Connected Peer Cards
                items(uiState.connectedPeers, key = { it.endpointId }) { peer ->
                    val (peerEmoji, cleanPeerName) = remember(peer.name) {
                        parseEmojiAndName(peer.name)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = peerEmoji, fontSize = 22.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (peer.isReadOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                            .border(2.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = cleanPeerName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (peer.isReadOnly) "Viewer (Read-Only)" else "Editor (Can Draw)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (peer.isReadOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TooltipHint(text = if (peer.isReadOnly) "Promote to Editor" else "Demote to Viewer") {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAction(OotbAction.SetPeerPermission(peer.endpointId, !peer.isReadOnly))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        MaterialSymbol(
                                            name = if (peer.isReadOnly) "lock" else "edit",
                                            tint = if (peer.isReadOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            size = 18.dp
                                        )
                                    }
                                }

                                TooltipHint(text = "Disconnect peer") {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAction(OotbAction.DisconnectPeer(peer.endpointId))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        MaterialSymbol(name = "close", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Disconnect Room Button
                item {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAction(OotbAction.LeaveRoom)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        MaterialSymbol(name = "close", size = 18.dp, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ModalBottomSheet (Latch Style) for Editing Profile Display Name & Avatar Emoji
    if (showEditSheet) {
        @Suppress("DEPRECATION")
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var selectedEmoji by remember { mutableStateOf(userEmoji) }
        var nameText by remember { mutableStateOf(rawName) }

        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose your avatar emoji and device name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Avatar Preview
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedEmoji, fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Preset Emoji Picker
                Text(
                    text = "Select Avatar",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_EMOJIS.forEach { emoji ->
                        val isSelected = emoji == selectedEmoji
                        val emojiScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = BrevityMotion.springSpatial(),
                            label = "EmojiScale"
                        )
                        Box(
                            modifier = Modifier
                                .scale(emojiScale)
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedEmoji = emoji
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // OutlinedTextField for Name
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEditSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val trimmedName = nameText.trim().ifEmpty { "Whiteboard User" }
                            val formattedName = "$selectedEmoji $trimmedName"
                            onAction(OotbAction.UpdateDisplayName(formattedName))
                            showEditSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Expressive radar animation with expanding concentric waves and center wavy progress indicator.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuickShareRadarPulse(
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    displayName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarWaves")
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarProgress"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxRadius = size.minDimension / 2f
                listOf(0f, 0.33f, 0.66f).forEach { offset ->
                    val progress = (waveProgress + offset) % 1f
                    val radius = maxRadius * (0.25f + 0.75f * progress)
                    val alpha = (1f - progress).coerceIn(0f, 0.6f)
                    drawCircle(
                        color = primaryColor.copy(alpha = alpha),
                        radius = radius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(46.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                MaterialSymbol(
                    name = if (isAdvertising) "sensors" else "radar",
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = when {
                isDiscovering -> "Looking for nearby whiteboards..."
                isAdvertising -> "Broadcasting as \"$displayName\"... Ready for peers to connect"
                else -> "Ready to collaborate"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Quick Share style circular device avatar card with spring press feedback.
 */
@Composable
private fun QuickShareDeviceCard(
    peer: PeerDevice,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Spring feedback when tapped or pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = BrevityMotion.springSpatial(),
        label = "DeviceSpringScale"
    )

    val (peerEmoji, cleanPeerName) = remember(peer.name) {
        parseEmojiAndName(peer.name)
    }

    Column(
        modifier = modifier
            .scale(scale)
            .width(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onConnect()
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Share Circle Avatar
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = peerEmoji, fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Peer Name
        Text(
            text = cleanPeerName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Quick Share "Tap to join" subtitle
        Text(
            text = "Tap to join",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}
