package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.PeerPresence
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.presentation.canvas.CanvasColorUtils
import kotlin.math.max

/**
 * Modern Material 3 Minimap for breVITy canvas.
 * - Hardware-accelerated GPU matrix transforms.
 * - Zero Path allocations during gesture frames.
 * - Stable bounds caching & uninterruptible pointer inputs.
 */
@Composable
fun WhiteboardMinimap(
    strokes: List<Stroke>,
    textElements: List<TextElement>,
    zoom: Float,
    panOffset: Offset,
    canvasSize: IntSize,
    onPanTo: (Offset) -> Unit,
    peerPresences: Map<String, PeerPresence> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val screenW = if (canvasSize.width > 0) canvasSize.width.toFloat() else with(density) { 1080.dp.toPx() }
    val screenH = if (canvasSize.height > 0) canvasSize.height.toFloat() else with(density) { 1920.dp.toPx() }

    // 1. Content bounds cached across pan/zoom (only recomputes when canvas elements change)
    val contentBounds = remember(strokes, textElements) {
        if (strokes.isEmpty() && textElements.isEmpty()) {
            Rect(0f, 0f, 1920f, 1080f)
        } else {
            var minX = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE
            strokes.forEach { stroke ->
                stroke.points.forEach { pt ->
                    if (pt.x < minX) minX = pt.x
                    if (pt.x > maxX) maxX = pt.x
                    if (pt.y < minY) minY = pt.y
                    if (pt.y > maxY) maxY = pt.y
                }
            }
            textElements.forEach { txt ->
                if (txt.x < minX) minX = txt.x
                if (txt.x + 200f > maxX) maxX = txt.x + 200f
                if (txt.y < minY) minY = txt.y
                if (txt.y + 60f > maxY) maxY = txt.y + 60f
            }
            if (minX >= maxX || minY >= maxY) {
                Rect(0f, 0f, 1920f, 1080f)
            } else {
                Rect(minX, minY, maxX, maxY)
            }
        }
    }

    // 2. Minimap Path Cache: reuses pre-built stroke paths without creating new Paths on every pan/zoom frame
    val pathCache = remember { HashMap<String, Path>() }
    if (strokes.isEmpty() && pathCache.isNotEmpty()) {
        pathCache.clear()
    }

    // Viewport in world coordinates
    val viewportLeft = -panOffset.x / zoom
    val viewportTop = -panOffset.y / zoom
    val viewportRight = (-panOffset.x + screenW) / zoom
    val viewportBottom = (-panOffset.y + screenH) / zoom

    // Expand bounds to enclose both content and current viewport plus padding
    val padding = 320f
    val bMinX = minOf(contentBounds.left, viewportLeft) - padding
    val bMaxX = maxOf(contentBounds.right, viewportRight) + padding
    val bMinY = minOf(contentBounds.top, viewportTop) - padding
    val bMaxY = maxOf(contentBounds.bottom, viewportBottom) + padding

    val rawWorldWidth = max(bMaxX - bMinX, 1000f)
    val rawWorldHeight = max(bMaxY - bMinY, 1000f)
    val centerX = (bMinX + bMaxX) / 2f
    val centerY = (bMinY + bMaxY) / 2f

    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh
    val mapSurface = MaterialTheme.colorScheme.surfaceContainerLowest
    val isDarkMap = mapSurface.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val eraserColor = MaterialTheme.colorScheme.surface

    val isPortrait = screenH > screenW
    val minimapWidth = if (isPortrait) 120.dp else 165.dp
    val minimapHeight = if (isPortrait) 165.dp else 115.dp

    // Stable state holders for gesture handling (prevents cancellation of detectDragGestures mid-pan)
    val currentZoom by rememberUpdatedState(zoom)
    val currentPanTo by rememberUpdatedState(onPanTo)
    val currentCenterX by rememberUpdatedState(centerX)
    val currentCenterY by rememberUpdatedState(centerY)
    val currentRawWorldWidth by rememberUpdatedState(rawWorldWidth)
    val currentRawWorldHeight by rememberUpdatedState(rawWorldHeight)
    val currentScreenW by rememberUpdatedState(screenW)
    val currentScreenH by rememberUpdatedState(screenH)

    Box(
        modifier = modifier
            .width(minimapWidth)
            .height(minimapHeight)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(surfaceContainer)
            .padding(5.dp)
    ) {
        // Inner Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(mapSurface)
                .pointerInput(Unit) {
                    val handleTouch = { localOffset: Offset, size: IntSize ->
                        val mapW = size.width.toFloat()
                        val mapH = size.height.toFloat()
                        if (mapW > 0f && mapH > 0f) {
                            val mapAspect = mapW / mapH
                            val worldAspect = currentRawWorldWidth / currentRawWorldHeight
                            val adjWorldW = if (worldAspect > mapAspect) currentRawWorldWidth else currentRawWorldHeight * mapAspect
                            val adjWorldH = if (worldAspect > mapAspect) currentRawWorldWidth / mapAspect else currentRawWorldHeight
                            val adjMinX = currentCenterX - adjWorldW / 2f
                            val adjMinY = currentCenterY - adjWorldH / 2f

                            val normX = (localOffset.x / mapW).coerceIn(0f, 1f)
                            val normY = (localOffset.y / mapH).coerceIn(0f, 1f)

                            val targetWorldX = adjMinX + normX * adjWorldW
                            val targetWorldY = adjMinY + normY * adjWorldH

                            val targetPanX = -targetWorldX * currentZoom + currentScreenW / 2f
                            val targetPanY = -targetWorldY * currentZoom + currentScreenH / 2f

                            currentPanTo(Offset(targetPanX, targetPanY))
                        }
                    }

                    detectTapGestures { offset ->
                        handleTouch(offset, size)
                    }
                }
                .pointerInput(Unit) {
                    val handleTouch = { localOffset: Offset, size: IntSize ->
                        val mapW = size.width.toFloat()
                        val mapH = size.height.toFloat()
                        if (mapW > 0f && mapH > 0f) {
                            val mapAspect = mapW / mapH
                            val worldAspect = currentRawWorldWidth / currentRawWorldHeight
                            val adjWorldW = if (worldAspect > mapAspect) currentRawWorldWidth else currentRawWorldHeight * mapAspect
                            val adjWorldH = if (worldAspect > mapAspect) currentRawWorldWidth / mapAspect else currentRawWorldHeight
                            val adjMinX = currentCenterX - adjWorldW / 2f
                            val adjMinY = currentCenterY - adjWorldH / 2f

                            val normX = (localOffset.x / mapW).coerceIn(0f, 1f)
                            val normY = (localOffset.y / mapH).coerceIn(0f, 1f)

                            val targetWorldX = adjMinX + normX * adjWorldW
                            val targetWorldY = adjMinY + normY * adjWorldH

                            val targetPanX = -targetWorldX * currentZoom + currentScreenW / 2f
                            val targetPanY = -targetWorldY * currentZoom + currentScreenH / 2f

                            currentPanTo(Offset(targetPanX, targetPanY))
                        }
                    }

                    detectDragGestures { change, _ ->
                        change.consume()
                        handleTouch(change.position, size)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapW = size.width
                val mapH = size.height
                if (mapW <= 0f || mapH <= 0f) return@Canvas

                val mapAspect = mapW / mapH
                val worldAspect = rawWorldWidth / rawWorldHeight
                val adjWorldW = if (worldAspect > mapAspect) rawWorldWidth else rawWorldHeight * mapAspect
                val adjWorldH = if (worldAspect > mapAspect) rawWorldWidth / mapAspect else rawWorldHeight
                val adjMinX = centerX - adjWorldW / 2f
                val adjMinY = centerY - adjWorldH / 2f

                // Aspect ratio is strictly locked 1:1 across both axes
                val scale = mapW / adjWorldW

                // Hardware-accelerated GPU matrix transform (zero CPU point loops, zero allocations)
                withTransform({
                    scale(scale, scale, Offset.Zero)
                    translate(-adjMinX, -adjMinY)
                }) {
                    // 1. Render all strokes via cached vector paths
                    strokes.forEach { stroke ->
                        if (stroke.points.size >= 2) {
                            val path = pathCache.getOrPut(stroke.id) {
                                PathUtils.createToolPath(stroke.tool, stroke.points)
                            }
                            drawPath(
                                path = path,
                                color = if (stroke.isEraser) eraserColor else CanvasColorUtils.getAdaptiveStrokeColor(Color(stroke.color), isDarkMap),
                                style = DrawStroke(
                                    width = stroke.strokeWidth.coerceAtLeast(1.4f / scale),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else if (stroke.points.size == 1) {
                            val pt = stroke.points.first()
                            drawCircle(
                                color = if (stroke.isEraser) eraserColor else CanvasColorUtils.getAdaptiveStrokeColor(Color(stroke.color), isDarkMap),
                                radius = 2f / scale,
                                center = Offset(pt.x, pt.y)
                            )
                        }
                    }

                    // 2. Render text element placeholders
                    textElements.forEach { txt ->
                        drawRoundRect(
                            color = Color(txt.color).copy(alpha = 0.55f),
                            topLeft = Offset(txt.x, txt.y),
                            size = Size(max(120f, 4f / scale), max(24f, 3f / scale)),
                            cornerRadius = CornerRadius(2f / scale, 2f / scale)
                        )
                    }

                    // 3. Render collaborator viewports & activity
                    peerPresences.values.forEach { peer ->
                        val peerColor = Color(peer.color)
                        val vp = peer.viewport
                        if (vp != null) {
                            val pVpW = vp.right - vp.left
                            val pVpH = vp.bottom - vp.top
                            drawRoundRect(
                                color = peerColor.copy(alpha = 0.15f),
                                topLeft = Offset(vp.left, vp.top),
                                size = Size(max(pVpW, 8f / scale), max(pVpH, 6f / scale)),
                                cornerRadius = CornerRadius(3f / scale, 3f / scale)
                            )
                            drawRoundRect(
                                color = peerColor.copy(alpha = 0.90f),
                                topLeft = Offset(vp.left, vp.top),
                                size = Size(max(pVpW, 8f / scale), max(pVpH, 6f / scale)),
                                cornerRadius = CornerRadius(3f / scale, 3f / scale),
                                style = DrawStroke(width = 1.2f / scale)
                            )
                        }

                        if (peer.isDrawing && peer.currentPoint != null) {
                            drawCircle(
                                color = peerColor.copy(alpha = 0.35f),
                                radius = 6.dp.toPx() / scale,
                                center = Offset(peer.currentPoint.x, peer.currentPoint.y)
                            )
                            drawCircle(
                                color = peerColor,
                                radius = 3.dp.toPx() / scale,
                                center = Offset(peer.currentPoint.x, peer.currentPoint.y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.5.dp.toPx() / scale,
                                center = Offset(peer.currentPoint.x, peer.currentPoint.y)
                            )
                        }
                    }

                    // 4. Render Local User Viewport
                    val vpW = viewportRight - viewportLeft
                    val vpH = viewportBottom - viewportTop
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.16f),
                        topLeft = Offset(viewportLeft, viewportTop),
                        size = Size(max(vpW, 8f / scale), max(vpH, 6f / scale)),
                        cornerRadius = CornerRadius(3f / scale, 3f / scale)
                    )
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(viewportLeft, viewportTop),
                        size = Size(max(vpW, 8f / scale), max(vpH, 6f / scale)),
                        cornerRadius = CornerRadius(3f / scale, 3f / scale),
                        style = DrawStroke(width = 1.6f / scale)
                    )
                }
            }

            // Sleek Floating Zoom Percentage Pill (Top-End Corner)
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = "${(zoom * 100).toInt()}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
