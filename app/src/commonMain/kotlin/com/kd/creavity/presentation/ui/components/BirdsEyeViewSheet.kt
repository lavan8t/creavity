package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import com.kd.creavity.presentation.canvas.CanvasColorUtils
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.PeerPresence
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import kotlin.math.max
import kotlin.math.min

/**
 * Host-Only Panoramic Bird's Eye View Modal Bottom Sheet.
 * - Live radar showing all participants, what they are viewing, and what they are drawing.
 * - Interactive canvas radar with tap-to-jump.
 * - Quick participant navigation: "Jump to Participant", "Fit All Content", "Summon All".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdsEyeViewSheet(
    visible: Boolean,
    isHost: Boolean,
    strokes: List<Stroke>,
    textElements: List<TextElement>,
    stickyNotes: List<StickyNote>,
    peerPresences: Map<String, PeerPresence>,
    connectedPeers: List<PeerDevice>,
    zoom: Float,
    panOffset: Offset,
    canvasSize: IntSize,
    onDismiss: () -> Unit,
    onJumpToPoint: (Offset) -> Unit,
    onFitAllContent: () -> Unit,
    onSummonAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || !isHost) return

    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = surfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title + Host Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bird's Eye View",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "HOST ONLY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Panoramic radar & live collaborator oversight",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    MaterialSymbol(
                        name = "close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 18.dp
                    )
                }
            }

            // Panoramic Live Radar Canvas
            val screenW = if (canvasSize.width > 0) canvasSize.width.toFloat() else with(density) { 1080.dp.toPx() }
            val screenH = if (canvasSize.height > 0) canvasSize.height.toFloat() else with(density) { 1920.dp.toPx() }
            val safeZoom = zoom.coerceAtLeast(0.1f)

            // Local host viewport bounds in world coordinates
            val hostVpLeft = -panOffset.x / safeZoom
            val hostVpTop = -panOffset.y / safeZoom
            val hostVpRight = (-panOffset.x + screenW) / safeZoom
            val hostVpBottom = (-panOffset.y + screenH) / safeZoom

            // World bounds calculation
            var minX = min(hostVpLeft, 0f)
            var minY = min(hostVpTop, 0f)
            var maxX = max(hostVpRight, 1920f)
            var maxY = max(hostVpBottom, 1080f)

            strokes.forEach { s ->
                s.points.forEach { p ->
                    minX = min(minX, p.x)
                    minY = min(minY, p.y)
                    maxX = max(maxX, p.x)
                    maxY = max(maxY, p.y)
                }
            }
            textElements.forEach { t ->
                minX = min(minX, t.x)
                minY = min(minY, t.y)
                maxX = max(maxX, t.x + 200f)
                maxY = max(maxY, t.y + 40f)
            }
            stickyNotes.forEach { sn ->
                minX = min(minX, sn.x)
                minY = min(minY, sn.y)
                maxX = max(maxX, sn.x + sn.width)
                maxY = max(maxY, sn.y + sn.height)
            }
            peerPresences.values.forEach { p ->
                p.viewport?.let { vp ->
                    minX = min(minX, vp.left)
                    minY = min(minY, vp.top)
                    maxX = max(maxX, vp.right)
                    maxY = max(maxY, vp.bottom)
                }
            }

            val margin = 200f
            val radarMinX = minX - margin
            val radarMinY = minY - margin
            val radarMaxX = maxX + margin
            val radarMaxY = maxY + margin
            val rawWorldW = max(radarMaxX - radarMinX, 100f)
            val rawWorldH = max(radarMaxY - radarMinY, 100f)
            val radarCenterX = (radarMinX + radarMaxX) / 2f
            val radarCenterY = (radarMinY + radarMaxY) / 2f

            val radarBg = MaterialTheme.colorScheme.surfaceContainerHighest
            val isDarkSheet = radarBg.luminance() < 0.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(radarBg.copy(alpha = 0.55f))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(radarCenterX, radarCenterY, rawWorldW, rawWorldH) {
                            detectTapGestures { tapOffset ->
                                val canvasW = size.width.toFloat()
                                val canvasH = size.height.toFloat()
                                if (canvasW > 0f && canvasH > 0f) {
                                    val radarAspect = canvasW / canvasH
                                    val worldAspect = rawWorldW / rawWorldH
                                    val adjW = if (worldAspect > radarAspect) rawWorldW else rawWorldH * radarAspect
                                    val adjH = if (worldAspect > radarAspect) rawWorldW / radarAspect else rawWorldH
                                    val adjMinX = radarCenterX - adjW / 2f
                                    val adjMinY = radarCenterY - adjH / 2f

                                    val normX = (tapOffset.x / canvasW).coerceIn(0f, 1f)
                                    val normY = (tapOffset.y / canvasH).coerceIn(0f, 1f)
                                    val targetWorldX = adjMinX + normX * adjW
                                    val targetWorldY = adjMinY + normY * adjH
                                    val newPanX = -targetWorldX * safeZoom + (screenW / 2f)
                                    val newPanY = -targetWorldY * safeZoom + (screenH / 2f)
                                    onJumpToPoint(Offset(newPanX, newPanY))
                                }
                            }
                        }
                ) {
                    val canvasW = size.width
                    val canvasH = size.height
                    if (canvasW <= 0f || canvasH <= 0f) return@Canvas

                    val radarAspect = canvasW / canvasH
                    val worldAspect = rawWorldW / rawWorldH
                    val adjW = if (worldAspect > radarAspect) rawWorldW else rawWorldH * radarAspect
                    val adjH = if (worldAspect > radarAspect) rawWorldW / radarAspect else rawWorldH
                    val adjMinX = radarCenterX - adjW / 2f
                    val adjMinY = radarCenterY - adjH / 2f

                    // 1:1 Aspect ratio locked across both axes
                    val scale = canvasW / adjW

                    // 1. Draw strokes in miniature
                    strokes.forEach { stroke ->
                        if (stroke.points.size >= 2) {
                            val path = Path()
                            val first = stroke.points.first()
                            path.moveTo((first.x - adjMinX) * scale, (first.y - adjMinY) * scale)
                            for (i in 1 until stroke.points.size) {
                                val pt = stroke.points[i]
                                path.lineTo((pt.x - adjMinX) * scale, (pt.y - adjMinY) * scale)
                            }
                            drawPath(
                                path = path,
                                color = if (stroke.isEraser) Color.DarkGray else CanvasColorUtils.getAdaptiveStrokeColor(Color(stroke.color), isDarkSheet),
                                style = DrawStroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // 2. Draw Sticky notes
                    stickyNotes.forEach { sn ->
                        val sx = (sn.x - adjMinX) * scale
                        val sy = (sn.y - adjMinY) * scale
                        val sw = sn.width * scale
                        val sh = sn.height * scale
                        drawRoundRect(
                            color = Color(sn.color).copy(alpha = 0.75f),
                            topLeft = Offset(sx, sy),
                            size = Size(max(sw, 6f), max(sh, 6f)),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }

                    // 3. Draw Remote Collaborators Viewports
                    peerPresences.values.forEach { peer ->
                        val peerColor = Color(peer.color)
                        val vp = peer.viewport
                        if (vp != null) {
                            val px = (vp.left - adjMinX) * scale
                            val py = (vp.top - adjMinY) * scale
                            val pw = (vp.right - vp.left) * scale
                            val ph = (vp.bottom - vp.top) * scale

                            drawRoundRect(
                                color = peerColor.copy(alpha = 0.18f),
                                topLeft = Offset(px, py),
                                size = Size(max(pw, 10f), max(ph, 8f)),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = peerColor,
                                topLeft = Offset(px, py),
                                size = Size(max(pw, 10f), max(ph, 8f)),
                                cornerRadius = CornerRadius(4f, 4f),
                                style = DrawStroke(width = 1.5f)
                            )

                            // Peer tag
                            drawText(
                                textMeasurer = textMeasurer,
                                text = peer.name.take(8),
                                topLeft = Offset(px + 4.dp.toPx(), (py + 4.dp.toPx()).coerceAtLeast(4.dp.toPx())),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Drawing pulse cursor
                        if (peer.isDrawing && peer.currentPoint != null) {
                            val dotX = (peer.currentPoint.x - adjMinX) * scale
                            val dotY = (peer.currentPoint.y - adjMinY) * scale
                            drawCircle(color = peerColor.copy(alpha = 0.4f), radius = 8.dp.toPx(), center = Offset(dotX, dotY))
                            drawCircle(color = peerColor, radius = 4.dp.toPx(), center = Offset(dotX, dotY))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(dotX, dotY))
                        }
                    }

                    // 4. Draw Local Host Viewport (Highlighted)
                    val hx = (hostVpLeft - adjMinX) * scale
                    val hy = (hostVpTop - adjMinY) * scale
                    val hw = (hostVpRight - hostVpLeft) * scale
                    val hh = (hostVpBottom - hostVpTop) * scale

                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.20f),
                        topLeft = Offset(hx, hy),
                        size = Size(max(hw, 10f), max(hh, 8f)),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(hx, hy),
                        size = Size(max(hw, 10f), max(hh, 8f)),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = DrawStroke(width = 2.2f)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "You (Host)",
                        topLeft = Offset(hx + 4.dp.toPx(), (hy + 4.dp.toPx()).coerceAtLeast(4.dp.toPx())),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Tap-to-jump hint badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Tap radar to jump",
                        fontSize = 10.sp,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Quick Actions: Fit All & Summon All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onFitAllContent,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    MaterialSymbol(name = "fit_screen", tint = MaterialTheme.colorScheme.primary, size = 18.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Fit All", maxLines = 1)
                }

                Button(
                    onClick = onSummonAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    MaterialSymbol(name = "group", tint = MaterialTheme.colorScheme.onPrimary, size = 18.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Summon All", maxLines = 1)
                }
            }

            // Collaborators Roster
            Text(
                text = "Connected Collaborators (${connectedPeers.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (connectedPeers.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No other collaborators connected. Share your room code to collaborate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(connectedPeers, key = { it.endpointId }) { peer ->
                        val presence = peerPresences[peer.endpointId]
                        val peerColor = presence?.let { Color(it.color) } ?: MaterialTheme.colorScheme.secondary

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(peerColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = peer.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = peer.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (peer.isReadOnly) "Viewer" else if (presence?.isDrawing == true) "Drawing now" else "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (presence?.isDrawing == true) MaterialTheme.colorScheme.primary else onSurfaceVariant
                                        )
                                    }
                                }

                                if (presence?.viewport != null) {
                                    OutlinedButton(
                                        onClick = {
                                            val vp = presence.viewport
                                            val targetCenterX = (vp.left + vp.right) / 2f
                                            val targetCenterY = (vp.top + vp.bottom) / 2f
                                            val newPanX = -targetCenterX * safeZoom + (screenW / 2f)
                                            val newPanY = -targetCenterY * safeZoom + (screenH / 2f)
                                            onJumpToPoint(Offset(newPanX, newPanY))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        MaterialSymbol(name = "near_me", tint = MaterialTheme.colorScheme.primary, size = 14.dp)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Jump to", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
