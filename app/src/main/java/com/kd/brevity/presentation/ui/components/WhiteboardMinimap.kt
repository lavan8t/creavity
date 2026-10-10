package com.kd.brevity.presentation.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.domain.model.PeerPresence
import com.kd.brevity.domain.model.Stroke
import com.kd.brevity.domain.model.TextElement
import kotlin.math.max

/**
 * Modern Material 3 Minimap for breVITy canvas.
 * - Borderless rounded rectangle showing only canvas, zoom %, local viewport, and other people's viewports & activity.
 * - No labels or icons ("not say minimal and not have icon").
 * - Shows where other collaborators are seeing and actively drawing in real time.
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

    // Compute bounding box encompassing all strokes, texts, local viewport, and peer viewports
    val viewportLeft = -panOffset.x / zoom
    val viewportTop = -panOffset.y / zoom
    val viewportRight = (-panOffset.x + screenW) / zoom
    val viewportBottom = (-panOffset.y + screenH) / zoom

    var minX = viewportLeft
    var maxX = viewportRight
    var minY = viewportTop
    var maxY = viewportBottom

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

    peerPresences.values.forEach { peer ->
        peer.viewport?.let { vp ->
            if (vp.left < minX) minX = vp.left
            if (vp.right > maxX) maxX = vp.right
            if (vp.top < minY) minY = vp.top
            if (vp.bottom > maxY) maxY = vp.bottom
        }
        peer.currentPoint?.let { pt ->
            if (pt.x < minX) minX = pt.x
            if (pt.x > maxX) maxX = pt.x
            if (pt.y < minY) minY = pt.y
            if (pt.y > maxY) maxY = pt.y
        }
    }

    // Add margin to world bounds
    val padding = 320f
    minX -= padding
    minY -= padding
    maxX += padding
    maxY += padding

    val rawWorldWidth = max(maxX - minX, 1000f)
    val rawWorldHeight = max(maxY - minY, 1000f)
    val centerX = (minX + maxX) / 2f
    val centerY = (minY + maxY) / 2f

    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh
    val mapSurface = MaterialTheme.colorScheme.surfaceContainerLowest
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val eraserColor = MaterialTheme.colorScheme.background

    val isPortrait = screenH > screenW
    val minimapWidth = if (isPortrait) 120.dp else 165.dp
    val minimapHeight = if (isPortrait) 165.dp else 115.dp

    Box(
        modifier = modifier
            .width(minimapWidth)
            .height(minimapHeight)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(surfaceContainer)
            .padding(5.dp)
    ) {
        // Inner Canvas Viewport (Rounded, No Border, 1:1 Aspect Ratio Locked)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(mapSurface)
                .pointerInput(strokes, textElements, zoom, panOffset, centerX, centerY, rawWorldWidth, rawWorldHeight, screenW, screenH) {
                    val handleTouch = { localOffset: Offset, size: IntSize ->
                        val mapW = size.width.toFloat()
                        val mapH = size.height.toFloat()
                        if (mapW > 0f && mapH > 0f) {
                            val mapAspect = mapW / mapH
                            val worldAspect = rawWorldWidth / rawWorldHeight
                            val adjWorldW = if (worldAspect > mapAspect) rawWorldWidth else rawWorldHeight * mapAspect
                            val adjWorldH = if (worldAspect > mapAspect) rawWorldWidth / mapAspect else rawWorldHeight
                            val adjMinX = centerX - adjWorldW / 2f
                            val adjMinY = centerY - adjWorldH / 2f

                            val normX = (localOffset.x / mapW).coerceIn(0f, 1f)
                            val normY = (localOffset.y / mapH).coerceIn(0f, 1f)

                            val targetWorldX = adjMinX + normX * adjWorldW
                            val targetWorldY = adjMinY + normY * adjWorldH

                            val targetPanX = -targetWorldX * zoom + screenW / 2f
                            val targetPanY = -targetWorldY * zoom + screenH / 2f

                            onPanTo(Offset(targetPanX, targetPanY))
                        }
                    }

                    detectTapGestures { offset ->
                        handleTouch(offset, size)
                    }
                }
                .pointerInput(strokes, textElements, zoom, panOffset, centerX, centerY, rawWorldWidth, rawWorldHeight, screenW, screenH) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val mapW = size.width.toFloat()
                        val mapH = size.height.toFloat()
                        if (mapW > 0f && mapH > 0f) {
                            val mapAspect = mapW / mapH
                            val worldAspect = rawWorldWidth / rawWorldHeight
                            val adjWorldW = if (worldAspect > mapAspect) rawWorldWidth else rawWorldHeight * mapAspect
                            val adjWorldH = if (worldAspect > mapAspect) rawWorldWidth / mapAspect else rawWorldHeight
                            val adjMinX = centerX - adjWorldW / 2f
                            val adjMinY = centerY - adjWorldH / 2f

                            val normX = (change.position.x / mapW).coerceIn(0f, 1f)
                            val normY = (change.position.y / mapH).coerceIn(0f, 1f)

                            val targetWorldX = adjMinX + normX * adjWorldW
                            val targetWorldY = adjMinY + normY * adjWorldH

                            val targetPanX = -targetWorldX * zoom + screenW / 2f
                            val targetPanY = -targetWorldY * zoom + screenH / 2f

                            onPanTo(Offset(targetPanX, targetPanY))
                        }
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

                // 1. Render all strokes in scaled form
                strokes.forEach { stroke ->
                    if (stroke.points.size >= 2) {
                        val path = Path()
                        val first = stroke.points.first()
                        val startX = (first.x - adjMinX) * scale
                        val startY = (first.y - adjMinY) * scale
                        path.moveTo(startX, startY)

                        for (i in 1 until stroke.points.size) {
                            val pt = stroke.points[i]
                            val px = (pt.x - adjMinX) * scale
                            val py = (pt.y - adjMinY) * scale
                            path.lineTo(px, py)
                        }

                        drawPath(
                            path = path,
                            color = if (stroke.isEraser) eraserColor else Color(stroke.color),
                            style = DrawStroke(
                                width = max(1.2f, stroke.strokeWidth * scale),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else if (stroke.points.size == 1) {
                        val pt = stroke.points.first()
                        val px = (pt.x - adjMinX) * scale
                        val py = (pt.y - adjMinY) * scale
                        drawCircle(
                            color = if (stroke.isEraser) eraserColor else Color(stroke.color),
                            radius = 1.5f,
                            center = Offset(px, py)
                        )
                    }
                }

                // 2. Render text elements
                textElements.forEach { txt ->
                    val tx = (txt.x - adjMinX) * scale
                    val ty = (txt.y - adjMinY) * scale
                    val tw = 120f * scale
                    val th = 24f * scale
                    drawRoundRect(
                        color = Color(txt.color).copy(alpha = 0.55f),
                        topLeft = Offset(tx, ty),
                        size = Size(max(tw, 4f), max(th, 3f)),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }

                // 3. Render OTHER PEOPLE'S Viewports & Activity (What they are seeing & doing)
                peerPresences.values.forEach { peer ->
                    val peerColor = Color(peer.color)
                    val vp = peer.viewport

                    if (vp != null) {
                        val pVpX = (vp.left - adjMinX) * scale
                        val pVpY = (vp.top - adjMinY) * scale
                        val pVpW = (vp.right - vp.left) * scale
                        val pVpH = (vp.bottom - vp.top) * scale

                        // Peer viewport fill
                        drawRoundRect(
                            color = peerColor.copy(alpha = 0.15f),
                            topLeft = Offset(pVpX, pVpY),
                            size = Size(max(pVpW, 8f), max(pVpH, 6f)),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                        // Peer viewport stroke
                        drawRoundRect(
                            color = peerColor.copy(alpha = 0.90f),
                            topLeft = Offset(pVpX, pVpY),
                            size = Size(max(pVpW, 8f), max(pVpH, 6f)),
                            cornerRadius = CornerRadius(3f, 3f),
                            style = DrawStroke(width = 1.2f)
                        )

                        // Peer name tag on their viewport
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 8.dp.toPx()
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val peerLabel = peer.name.take(6)
                        drawContext.canvas.nativeCanvas.drawText(
                            peerLabel,
                            pVpX + 3.dp.toPx(),
                            (pVpY + 10.dp.toPx()).coerceAtLeast(10.dp.toPx()),
                            textPaint
                        )
                    }

                    // What they are DOING: Active live drawing cursor
                    if (peer.isDrawing && peer.currentPoint != null) {
                        val pX = (peer.currentPoint.x - adjMinX) * scale
                        val pY = (peer.currentPoint.y - adjMinY) * scale

                        // Outer glowing pulse
                        drawCircle(
                            color = peerColor.copy(alpha = 0.35f),
                            radius = 6.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                        // Active drawing dot
                        drawCircle(
                            color = peerColor,
                            radius = 3.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.5.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                    }
                }

                // 4. Render LOCAL USER'S Viewport Rectangle (Where I am seeing)
                val vpX = (viewportLeft - adjMinX) * scale
                val vpY = (viewportTop - adjMinY) * scale
                val vpW = (viewportRight - viewportLeft) * scale
                val vpH = (viewportBottom - viewportTop) * scale

                // Viewport Fill
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.16f),
                    topLeft = Offset(vpX, vpY),
                    size = Size(max(vpW, 8f), max(vpH, 6f)),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Viewport Stroke
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(vpX, vpY),
                    size = Size(max(vpW, 8f), max(vpH, 6f)),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = DrawStroke(width = 1.6f)
                )
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
