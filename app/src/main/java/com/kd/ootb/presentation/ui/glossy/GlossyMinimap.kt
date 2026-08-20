package com.kd.ootb.presentation.ui.glossy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.ootb.domain.model.Stroke
import com.kd.ootb.domain.model.TextElement
import com.kd.ootb.presentation.ui.components.MaterialSymbol
import kotlin.math.max
import kotlin.math.min

@Composable
fun GlossyMinimap(
    strokes: List<Stroke>,
    textElements: List<TextElement>,
    zoom: Float,
    panOffset: Offset,
    canvasSize: IntSize,
    onPanTo: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val screenW = if (canvasSize.width > 0) canvasSize.width.toFloat() else with(density) { 1080.dp.toPx() }
    val screenH = if (canvasSize.height > 0) canvasSize.height.toFloat() else with(density) { 1920.dp.toPx() }

    // Compute bounding box encompassing all strokes, texts, and current viewport
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

    // Add padding to world bounds
    val padding = 300f
    minX -= padding
    minY -= padding
    maxX += padding
    maxY += padding

    val worldWidth = max(maxX - minX, 1000f)
    val worldHeight = max(maxY - minY, 1000f)

    Box(modifier = modifier) {
        if (!isExpanded) {
            // Collapsed 3D Orb Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2E323B), Color(0xFF1C1E24), Color(0xFF141518))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.50f)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.50f
                            ),
                            size = Size(size.width, size.height * 0.50f)
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isExpanded = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "map", tint = Color.White, size = 18.dp)
            }
        } else {
            // Expanded Skeuomorphic Minimap Window (155dp x 115dp)
            Box(
                modifier = Modifier
                    .width(155.dp)
                    .height(115.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF262932), Color(0xFF17191E), Color(0xFF0F1014))
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.60f),
                                Color.White.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.60f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .drawBehind {
                        // Glossy top specular reflection
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.40f
                            ),
                            size = Size(size.width, size.height * 0.40f)
                        )
                    }
                    .padding(6.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header: Title + Close Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        MaterialSymbol(name = "map", tint = Color(0xFF00E676), size = 12.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MINIMAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0B4C0),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .pointerInput(Unit) {
                                    detectTapGestures { isExpanded = false }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialSymbol(name = "close", tint = Color.White, size = 12.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Minimap Canvas Viewport
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0D10))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .pointerInput(strokes, textElements, zoom, panOffset, minX, minY, worldWidth, worldHeight, screenW, screenH) {
                                val handleTouch = { localOffset: Offset, size: IntSize ->
                                    val mapW = size.width.toFloat()
                                    val mapH = size.height.toFloat()
                                    if (mapW > 0 && mapH > 0) {
                                        val normX = (localOffset.x / mapW).coerceIn(0f, 1f)
                                        val normY = (localOffset.y / mapH).coerceIn(0f, 1f)

                                        val targetWorldX = minX + normX * worldWidth
                                        val targetWorldY = minY + normY * worldHeight

                                        val targetPanX = -targetWorldX * zoom + screenW / 2f
                                        val targetPanY = -targetWorldY * zoom + screenH / 2f

                                        onPanTo(Offset(targetPanX, targetPanY))
                                    }
                                }

                                detectTapGestures { offset ->
                                    handleTouch(offset, size)
                                }
                            }
                            .pointerInput(strokes, textElements, zoom, panOffset, minX, minY, worldWidth, worldHeight, screenW, screenH) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val mapW = size.width.toFloat()
                                    val mapH = size.height.toFloat()
                                    if (mapW > 0 && mapH > 0) {
                                        val normX = (change.position.x / mapW).coerceIn(0f, 1f)
                                        val normY = (change.position.y / mapH).coerceIn(0f, 1f)

                                        val targetWorldX = minX + normX * worldWidth
                                        val targetWorldY = minY + normY * worldHeight

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

                            val scaleX = mapW / worldWidth
                            val scaleY = mapH / worldHeight

                            // Render all strokes in scaled mini form
                            strokes.forEach { stroke ->
                                if (stroke.points.size >= 2) {
                                    val path = Path()
                                    val first = stroke.points.first()
                                    val startX = (first.x - minX) * scaleX
                                    val startY = (first.y - minY) * scaleY
                                    path.moveTo(startX, startY)

                                    for (i in 1 until stroke.points.size) {
                                        val pt = stroke.points[i]
                                        val px = (pt.x - minX) * scaleX
                                        val py = (pt.y - minY) * scaleY
                                        path.lineTo(px, py)
                                    }

                                    drawPath(
                                        path = path,
                                        color = if (stroke.isEraser) Color.Black else Color(stroke.color),
                                        style = DrawStroke(
                                            width = max(1.2f, stroke.strokeWidth * scaleX),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                } else if (stroke.points.size == 1) {
                                    val pt = stroke.points.first()
                                    val px = (pt.x - minX) * scaleX
                                    val py = (pt.y - minY) * scaleY
                                    drawCircle(
                                        color = if (stroke.isEraser) Color.Black else Color(stroke.color),
                                        radius = 1.5f,
                                        center = Offset(px, py)
                                    )
                                }
                            }

                            // Render text bounding blocks
                            textElements.forEach { txt ->
                                val tx = (txt.x - minX) * scaleX
                                val ty = (txt.y - minY) * scaleY
                                val tw = 120f * scaleX
                                val th = 24f * scaleY
                                drawRoundRect(
                                    color = Color(txt.color).copy(alpha = 0.55f),
                                    topLeft = Offset(tx, ty),
                                    size = Size(max(tw, 4f), max(th, 3f)),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }

                            // Render Camera Viewport Rectangle
                            val vpX = (viewportLeft - minX) * scaleX
                            val vpY = (viewportTop - minY) * scaleY
                            val vpW = (viewportRight - viewportLeft) * scaleX
                            val vpH = (viewportBottom - viewportTop) * scaleY

                            // Fill
                            drawRoundRect(
                                color = Color(0xFF00E676).copy(alpha = 0.12f),
                                topLeft = Offset(vpX, vpY),
                                size = Size(max(vpW, 8f), max(vpH, 6f)),
                                cornerRadius = CornerRadius(3f, 3f)
                            )
                            // Glowing border
                            drawRoundRect(
                                color = Color(0xFF00E676).copy(alpha = 0.85f),
                                topLeft = Offset(vpX, vpY),
                                size = Size(max(vpW, 8f), max(vpH, 6f)),
                                cornerRadius = CornerRadius(3f, 3f),
                                style = DrawStroke(width = 1.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
