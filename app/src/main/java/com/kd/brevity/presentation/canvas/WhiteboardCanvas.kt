package com.kd.brevity.presentation.canvas

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.domain.math.PathUtils
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.domain.model.ImageElement
import com.kd.brevity.domain.model.PeerPresence
import com.kd.brevity.domain.model.Point
import com.kd.brevity.domain.model.StickyNote
import com.kd.brevity.domain.model.Stroke
import com.kd.brevity.domain.model.TextElement
import com.kd.brevity.ui.theme.AppFontFamily
import com.kd.brevity.ui.theme.getGoogleFontFamily
import kotlin.math.abs
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStrokeStyle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WhiteboardCanvas(
    strokes: List<Stroke>,
    textElements: List<TextElement> = emptyList(),
    imageElements: List<ImageElement> = emptyList(),
    stickyNotes: List<StickyNote> = emptyList(),
    peerPresences: Map<String, PeerPresence> = emptyMap(),
    currentColor: Color = MaterialTheme.colorScheme.onSurface,
    currentWidth: Float = 6.0f,
    selectedTool: DrawingTool = DrawingTool.PEN,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    backgroundPattern: CanvasBackgroundPattern = CanvasBackgroundPattern.ISOMETRIC_DOTS,
    hapticsEnabled: Boolean = true,
    zoom: Float = 1.0f,
    panOffset: Offset = Offset.Zero,
    isInfiniteCanvas: Boolean = true,
    canvasWidth: Float = 1920f,
    canvasHeight: Float = 1080f,
    onTransform: (zoomChange: Float, focal: Offset, panChange: Offset) -> Unit,
    onTransformStateChanged: (isTransforming: Boolean, touchPoints: List<Offset>) -> Unit = { _, _ -> },
    onStrokeCompleted: (Stroke) -> Unit,
    onStrokeBroadcastStart: (String, Long, Float, Boolean, Point) -> Unit,
    onStrokeBroadcastPoint: (String, Point) -> Unit,
    onStrokeBroadcastEnd: (String) -> Unit,
    onDeleteStroke: (String) -> Unit = {},
    onDeleteText: (String) -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
    onDeleteStickyNote: (String) -> Unit = {},
    onElementsMoved: (List<Stroke>, List<TextElement>, List<ImageElement>) -> Unit = { _, _, _ -> },
    onSelectionChanged: (List<String>, List<String>, Rect?) -> Unit = { _, _, _ -> },
    onTapForText: (Point) -> Unit = {},
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val pathCache = remember { HashMap<String, Path>() }
    val imageBitmapCache = remember { HashMap<String, ImageBitmap>() }
    val strokeBoundsCache = remember { HashMap<String, Rect>() }
    val textLayoutCache = remember { HashMap<String, TextLayoutResult>() }
    val stickyTextLayoutCache = remember { HashMap<String, TextLayoutResult>() }

    val hapticFeedback = LocalHapticFeedback.current
    val drawingHaptics = remember(hapticFeedback) { DrawingHaptics(hapticFeedback) }
    val drawInvalidator = remember { mutableLongStateOf(0L) }

    val density = LocalDensity.current.density

    // Pure gesture controller (Candidate 4 architectural seam)
    val controller = remember {
        CanvasGestureController(
            onTransform = onTransform,
            onTransformStateChanged = onTransformStateChanged,
            onStrokeCompleted = onStrokeCompleted,
            onStrokeBroadcastStart = onStrokeBroadcastStart,
            onStrokeBroadcastPoint = onStrokeBroadcastPoint,
            onStrokeBroadcastEnd = onStrokeBroadcastEnd,
            onDeleteStroke = onDeleteStroke,
            onDeleteText = onDeleteText,
            onDeleteImage = onDeleteImage,
            onDeleteStickyNote = onDeleteStickyNote,
            onElementsMoved = onElementsMoved,
            onSelectionChanged = onSelectionChanged,
            onTapForText = onTapForText,
            requestInvalidate = { drawInvalidator.longValue = System.nanoTime() }
        ).apply {
            this.density = density
        }
    }

    LaunchedEffect(controller.laserActiveToken) {
        if (controller.laserPoints.isEmpty()) return@LaunchedEffect
        while (controller.laserPoints.isNotEmpty()) {
            val now = android.os.SystemClock.uptimeMillis()
            controller.laserPoints.removeAll { now - it.timestampMs > 1200L }
            drawInvalidator.longValue = System.nanoTime()
            if (controller.laserPoints.isEmpty()) break
            delay(16L)
        }
    }

    val effectiveBg = if (backgroundColor == Color.Unspecified) MaterialTheme.colorScheme.surface else backgroundColor
    val isDark = effectiveBg.luminance() < 0.5f
    val effectiveColor = if (currentColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        currentColor
    }
    val selectionPrimary = MaterialTheme.colorScheme.primary
    val m3Surface = MaterialTheme.colorScheme.surface
    val m3OnSurface = MaterialTheme.colorScheme.onSurface
    val m3SurfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val peerColorPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Hardware acceleration GPU backing
                clip = true
            }
            .onSizeChanged { size ->
                controller.viewWidth = size.width.toFloat()
                controller.viewHeight = size.height.toFloat()
            }
            .background(if (isInfiniteCanvas) effectiveBg else MaterialTheme.colorScheme.surfaceContainerLowest)
            .pointerInteropFilter { event ->
                controller.handleTouchEvent(
                    event = event,
                    panOffset = panOffset,
                    zoom = zoom,
                    selectedTool = selectedTool,
                    currentColor = effectiveColor,
                    currentWidth = currentWidth,
                    strokes = strokes,
                    textElements = textElements,
                    imageElements = imageElements,
                    stickyNotes = stickyNotes,
                    hapticsEnabled = hapticsEnabled,
                    drawingHaptics = drawingHaptics,
                    isReadOnly = isReadOnly,
                    pathCache = pathCache
                )
            }
    ) {
        @Suppress("UNUSED_VARIABLE")
        val dummy = drawInvalidator.longValue

        withTransform({
            translate(panOffset.x, panOffset.y)
            scale(zoom, zoom, Offset.Zero)
        }) {
            // 0. Fixed Page Boundary (When infinite canvas is disabled)
            if (!isInfiniteCanvas) {
                // Draw shadow/frame around the fixed whiteboard sheet
                drawRect(
                    color = Color.Black.copy(alpha = 0.45f),
                    topLeft = Offset(-4f, -4f),
                    size = Size(canvasWidth + 8f, canvasHeight + 8f)
                )
                drawRect(
                    color = effectiveBg,
                    topLeft = Offset.Zero,
                    size = Size(canvasWidth, canvasHeight)
                )
            }

            // 1. GPU-batched Background Grid Pattern
            with(BackgroundDrawer) {
                drawCanvasPattern(backgroundPattern, effectiveBg, zoom, panOffset)
            }

            if (!isInfiniteCanvas) {
                // Draw page boundary border
                drawRect(
                    color = selectionPrimary.copy(alpha = 0.4f),
                    topLeft = Offset.Zero,
                    size = Size(canvasWidth, canvasHeight),
                    style = CanvasStrokeStyle(width = 2f / zoom)
                )
            }

            // Visible viewport in canvas space for GPU/CPU culling
            val vLeft = -panOffset.x / zoom - 64f
            val vTop = -panOffset.y / zoom - 64f
            val vRight = vLeft + (size.width / zoom) + 128f
            val vBottom = vTop + (size.height / zoom) + 128f

            val movingStrokeMap = if (controller.movingStrokes.isNotEmpty()) controller.movingStrokes.associateBy { it.id } else emptyMap()
            val movingTextMap = if (controller.movingTexts.isNotEmpty()) controller.movingTexts.associateBy { it.id } else emptyMap()
            val movingImageMap = if (controller.movingImages.isNotEmpty()) controller.movingImages.associateBy { it.id } else emptyMap()

            // 2. Draw Image Elements (Culled to visible viewport)
            imageElements.forEach { img ->
                val activeImg = movingImageMap[img.id] ?: img
                if (activeImg.x + activeImg.width < vLeft || activeImg.x > vRight ||
                    activeImg.y + activeImg.height < vTop || activeImg.y > vBottom) {
                    return@forEach
                }
                val bitmap = imageBitmapCache.getOrPut(activeImg.path) {
                    try {
                        BitmapFactory.decodeFile(activeImg.path)?.asImageBitmap() ?: ImageBitmap(1, 1)
                    } catch (_: Exception) {
                        ImageBitmap(1, 1)
                    }
                }
                if (bitmap.width > 1) {
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(activeImg.x.toInt(), activeImg.y.toInt()),
                        dstSize = IntSize(activeImg.width.toInt(), activeImg.height.toInt())
                    )
                }
            }

            // 3. Draw Completed Strokes (Culled to visible viewport)
            strokes.forEach { stroke ->
                val activeStroke = movingStrokeMap[stroke.id] ?: stroke
                if (activeStroke.points.isEmpty()) return@forEach

                val isMoving = movingStrokeMap.containsKey(stroke.id)
                val strokeKey = if (isMoving) "${stroke.id}_m" else stroke.id

                val bounds = strokeBoundsCache.getOrPut(strokeKey) {
                    var minX = Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxX = Float.MIN_VALUE
                    var maxY = Float.MIN_VALUE
                    activeStroke.points.forEach { p ->
                        if (p.x < minX) minX = p.x
                        if (p.x > maxX) maxX = p.x
                        if (p.y < minY) minY = p.y
                        if (p.y > maxY) maxY = p.y
                    }
                    val pad = activeStroke.strokeWidth * 2f
                    Rect(minX - pad, minY - pad, maxX + pad, maxY + pad)
                }
                if (bounds.right < vLeft || bounds.left > vRight || bounds.bottom < vTop || bounds.top > vBottom) {
                    return@forEach
                }

                val path = pathCache.getOrPut(strokeKey) {
                    PathUtils.createToolPath(activeStroke.tool, activeStroke.points)
                }
                val baseColor = Color(activeStroke.color)
                val drawColor = if (activeStroke.isEraser) effectiveBg else baseColor

                with(StrokeDrawer) {
                    drawStyledStroke(
                        points = activeStroke.points,
                        color = drawColor,
                        width = activeStroke.strokeWidth,
                        tool = activeStroke.tool,
                        cachedPath = path
                    )
                }
            }

            // 3b. Draw Pending Stroke (bridges finger lift and Compose state update)
            val pending = controller.pendingStroke
            if (pending != null) {
                if (strokes.any { it.id == pending.id }) {
                    controller.pendingStroke = null
                } else {
                    val path = pathCache.getOrPut("${pending.id}_${pending.points.size}") {
                        PathUtils.createToolPath(pending.tool, pending.points)
                    }
                    val baseColor = Color(pending.color)
                    val drawColor = if (pending.isEraser) effectiveBg else baseColor
                    with(StrokeDrawer) {
                        drawStyledStroke(
                            points = pending.points,
                            color = drawColor,
                            width = pending.strokeWidth,
                            tool = pending.tool,
                            cachedPath = path
                        )
                    }
                }
            }

            // 4. Draw Text Elements (Culled & Layout Cached)
            textElements.forEach { elem ->
                val activeElem = movingTextMap[elem.id] ?: elem
                if (activeElem.text.isBlank()) return@forEach
                if (activeElem.x + 800f < vLeft || activeElem.x > vRight ||
                    activeElem.y + 200f < vTop || activeElem.y > vBottom) {
                    return@forEach
                }
                val fontFam = getGoogleFontFamily(activeElem.fontName)
                val result = textLayoutCache.getOrPut("${activeElem.id}_${activeElem.text}_${activeElem.fontSize}_${activeElem.color}") {
                    textMeasurer.measure(
                        text = AnnotatedString(activeElem.text),
                        style = TextStyle(
                            fontFamily = fontFam,
                            fontSize = activeElem.fontSize.sp,
                            color = Color(activeElem.color)
                        )
                    )
                }
                drawText(result, topLeft = Offset(activeElem.x, activeElem.y))
            }

            // 4b. Draw Jamboard Sticky Notes (Culled & Layout Cached)
            stickyNotes.forEach { note ->
                if (note.x + note.width < vLeft || note.x > vRight ||
                    note.y + note.height < vTop || note.y > vBottom) {
                    return@forEach
                }
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(note.x + 4f, note.y + 6f),
                    size = Size(note.width, note.height),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                drawRoundRect(
                    color = Color(note.color),
                    topLeft = Offset(note.x, note.y),
                    size = Size(note.width, note.height),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                if (note.text.isNotBlank()) {
                    val result = stickyTextLayoutCache.getOrPut("${note.id}_${note.text}_${note.width}") {
                        textMeasurer.measure(
                            text = AnnotatedString(note.text),
                            style = TextStyle(
                                fontFamily = AppFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = m3OnSurface
                            ),
                            constraints = androidx.compose.ui.unit.Constraints(
                                maxWidth = (note.width - 24f).toInt().coerceAtLeast(10)
                            )
                        )
                    }
                    drawText(result, topLeft = Offset(note.x + 12f, note.y + 12f))
                }
            }

            // 5. Draw Active In-Progress Stroke
            if (controller.activePoints.isNotEmpty() && controller.activeToolUsed != DrawingTool.LASER) {
                val baseColor = effectiveColor
                val drawColor = if (controller.activeToolUsed == DrawingTool.ERASER) effectiveBg else baseColor

                with(StrokeDrawer) {
                    drawStyledStroke(
                        points = controller.activePoints,
                        color = drawColor,
                        width = currentWidth / zoom,
                        tool = controller.activeToolUsed,
                        cachedPath = controller.activePath
                    )
                }
            }

            // 6. Draw Ephemeral Laser Pointer
            if (controller.laserPoints.isNotEmpty()) {
                with(StrokeDrawer) {
                    drawGlowingLaserStroke(
                        points = controller.laserPoints,
                        baseWidth = (currentWidth * 1.5f) / zoom
                    )
                }
            }

            // 7. Draw Selection Overlays (Marquee Rect, Freeform Lasso, and Selection Bounding Box)
            val isSelectionMode = selectedTool == DrawingTool.SELECT_MOVE ||
                    selectedTool == DrawingTool.RECT_SELECT ||
                    selectedTool == DrawingTool.LASSO_SELECT

            if (isSelectionMode) {
                if (selectedTool == DrawingTool.LASSO_SELECT && controller.lassoPoints.size > 1) {
                    drawPath(
                        path = controller.lassoPath,
                        color = selectionPrimary.copy(alpha = 0.15f)
                    )
                    drawPath(
                        path = controller.lassoPath,
                        color = selectionPrimary,
                        style = CanvasStrokeStyle(
                            width = 2.dp.toPx() / zoom,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f / zoom, 10f / zoom), 0f)
                        )
                    )
                }

                val currentMarquee = controller.marqueeRect
                if (currentMarquee != null) {
                    drawRect(
                        color = selectionPrimary.copy(alpha = 0.15f),
                        topLeft = Offset(currentMarquee.left, currentMarquee.top),
                        size = Size(currentMarquee.width, currentMarquee.height)
                    )
                    drawRect(
                        color = selectionPrimary,
                        topLeft = Offset(currentMarquee.left, currentMarquee.top),
                        size = Size(currentMarquee.width, currentMarquee.height),
                        style = CanvasStrokeStyle(
                            width = 2.dp.toPx() / zoom,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f / zoom, 10f / zoom), 0f)
                        )
                    )
                }

                if (controller.selectedStrokeIds.isNotEmpty() || controller.selectedTextIds.isNotEmpty() || controller.selectedImageIds.isNotEmpty()) {
                    var minX = Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxX = Float.MIN_VALUE
                    var maxY = Float.MIN_VALUE

                    strokes.filter { controller.selectedStrokeIds.contains(it.id) }.forEach { s ->
                        val activeS = movingStrokeMap[s.id] ?: s
                        activeS.points.forEach { p ->
                            if (p.x < minX) minX = p.x
                            if (p.y < minY) minY = p.y
                            if (p.x > maxX) maxX = p.x
                            if (p.y > maxY) maxY = p.y
                        }
                    }

                    textElements.filter { controller.selectedTextIds.contains(it.id) }.forEach { t ->
                        val activeT = movingTextMap[t.id] ?: t
                        if (activeT.x < minX) minX = activeT.x
                        if (activeT.y < minY) minY = activeT.y
                        if (activeT.x + (activeT.fontSize * 6f) > maxX) maxX = activeT.x + (activeT.fontSize * 6f)
                        if (activeT.y + (activeT.fontSize * 1.5f) > maxY) maxY = activeT.y + (activeT.fontSize * 1.5f)
                    }

                    imageElements.filter { controller.selectedImageIds.contains(it.id) }.forEach { im ->
                        val activeIm = movingImageMap[im.id] ?: im
                        if (activeIm.x < minX) minX = activeIm.x
                        if (activeIm.y < minY) minY = activeIm.y
                        if (activeIm.x + activeIm.width > maxX) maxX = activeIm.x + activeIm.width
                        if (activeIm.y + activeIm.height > maxY) maxY = activeIm.y + activeIm.height
                    }

                    if (minX <= maxX && minY <= maxY) {
                        val pad = 12f / zoom
                        val boundRect = Rect(minX - pad, minY - pad, maxX + pad, maxY + pad)

                        drawRect(
                            color = selectionPrimary,
                            topLeft = Offset(boundRect.left, boundRect.top),
                            size = Size(boundRect.width, boundRect.height),
                            style = CanvasStrokeStyle(
                                width = 2.dp.toPx() / zoom,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f / zoom, 10f / zoom), 0f)
                            )
                        )

                        val handleRadius = 6.dp.toPx() / zoom
                        listOf(
                            Offset(boundRect.left, boundRect.top),
                            Offset(boundRect.right, boundRect.top),
                            Offset(boundRect.left, boundRect.bottom),
                            Offset(boundRect.right, boundRect.bottom)
                        ).forEach { corner ->
                            drawCircle(
                                color = m3Surface,
                                radius = handleRadius,
                                center = corner
                            )
                            drawCircle(
                                color = selectionPrimary,
                                radius = handleRadius,
                                center = corner,
                                style = CanvasStrokeStyle(width = 2.dp.toPx() / zoom)
                            )
                        }
                    }
                }
            }

            // 8. Render Real-Time Peer Presence Cursors
            peerPresences.values.forEach { peer ->
                peer.currentPoint?.let { point ->
                    val colorIndex = abs(peer.name.hashCode()) % peerColorPalette.size
                    val cursorColor = if (peer.color != 0L) Color(peer.color) else peerColorPalette[colorIndex]

                    drawCircle(
                        color = cursorColor.copy(alpha = 0.25f),
                        radius = 16f / zoom,
                        center = Offset(point.x, point.y)
                    )
                    drawCircle(
                        color = cursorColor,
                        radius = 6f / zoom,
                        center = Offset(point.x, point.y)
                    )
                    drawCircle(
                        color = m3Surface,
                        radius = 2.5f / zoom,
                        center = Offset(point.x, point.y)
                    )

                    val statusText = if (peer.isDrawing) "${peer.name} • drawing" else peer.name
                    val measuredText = textMeasurer.measure(
                        text = AnnotatedString(statusText),
                        style = TextStyle(
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = m3OnSurface,
                            fontSize = (11 / zoom).sp
                        )
                    )
                    val tagPadding = 6f / zoom
                    val tagWidth = measuredText.size.width + tagPadding * 2
                    val tagHeight = measuredText.size.height + tagPadding * 1.4f
                    val tagTopLeft = Offset(point.x + 12f / zoom, point.y - tagHeight - 6f / zoom)

                    drawRoundRect(
                        color = m3SurfaceContainerHighest.copy(alpha = 0.95f),
                        topLeft = tagTopLeft,
                        size = Size(tagWidth, tagHeight),
                        cornerRadius = CornerRadius(6f / zoom, 6f / zoom)
                    )
                    drawRoundRect(
                        color = cursorColor.copy(alpha = 0.85f),
                        topLeft = tagTopLeft,
                        size = Size(tagWidth, tagHeight),
                        cornerRadius = CornerRadius(6f / zoom, 6f / zoom),
                        style = CanvasStrokeStyle(width = 1.2f / zoom)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = statusText,
                        topLeft = Offset(tagTopLeft.x + tagPadding, tagTopLeft.y + tagPadding * 0.7f),
                        style = TextStyle(
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = m3OnSurface,
                            fontSize = (11 / zoom).sp
                        )
                    )
                }
            }
        }
    }
}
