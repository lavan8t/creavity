package com.kd.creavity.presentation.canvas

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.PeerPresence
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.ui.theme.AppFontFamily
import com.kd.creavity.ui.theme.getDocFontFamily
import com.kd.creavity.util.TextSvgGenerator
import androidx.compose.ui.graphics.vector.PathParser
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
    onStrokeBroadcastStart: (String, Long, Float, Boolean, Point, DrawingTool) -> Unit,
    onStrokeBroadcastPoint: (String, Point) -> Unit,
    onStrokeBroadcastEnd: (String) -> Unit,
    onDeleteStroke: (String) -> Unit = {},
    onDeleteText: (String) -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
    onDeleteStickyNote: (String) -> Unit = {},
    onElementsMoved: (List<Stroke>, List<TextElement>, List<ImageElement>) -> Unit = { _, _, _ -> },
    onSelectionChanged: (List<String>, List<String>, Rect?) -> Unit = { _, _, _ -> },
    onTapForText: (Point) -> Unit = {},
    onContextMenu: ((screenOffset: Offset, worldPoint: Point, hasSelection: Boolean) -> Unit)? = null,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val imageBitmapCache = remember { HashMap<String, ImageBitmap>() }
    val geometryCache = remember { StrokeGeometryCache() }
    val textLayoutCache = remember { HashMap<String, TextLayoutResult>() }
    val svgPathCache = remember { HashMap<String, androidx.compose.ui.graphics.Path?>() }
    val stickyTextLayoutCache = remember { HashMap<String, TextLayoutResult>() }
    val embeddedTextLayoutCache = remember { HashMap<String, TextLayoutResult>() }

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
            this.onContextMenu = onContextMenu
        }
    }

    controller.onContextMenu = onContextMenu

    LaunchedEffect(controller.laserActiveToken) {
        if (controller.laserPoints.isEmpty()) return@LaunchedEffect
        while (controller.laserPoints.isNotEmpty()) {
            val now = System.currentTimeMillis()
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

    val canvasHoverIcon = remember(selectedTool) {
        getToolPointerIcon(selectedTool)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerHoverIcon(canvasHoverIcon)
            .graphicsLayer {
                // Hardware acceleration GPU backing
                clip = true
            }
            .onSizeChanged { size ->
                controller.viewWidth = size.width.toFloat()
                controller.viewHeight = size.height.toFloat()
            }
            .background(if (isInfiniteCanvas) effectiveBg else MaterialTheme.colorScheme.surfaceContainerLowest)
            .pointerInput(panOffset, zoom, selectedTool, effectiveColor, currentWidth, strokes, textElements, imageElements, stickyNotes, hapticsEnabled, isReadOnly) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val handled = controller.handleInputEvent(
                            event = event.toCanvasInputEvent(),
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
                            geometryCache = geometryCache
                        )
                        if (handled) {
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
    ) {
        // Read drawInvalidator inside DrawScope to subscribe the draw phase to gesture updates.
        // DO NOT REMOVE: CanvasGestureController mutates drawInvalidator to trigger 60/120fps redraws
        // without triggering recomposition. Removing this breaks live stroke drawing.
        @Suppress("UNUSED_VARIABLE")
        val drawTrigger = drawInvalidator.longValue

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
                        com.kd.creavity.util.decodeImageFile(activeImg.path) ?: ImageBitmap(1, 1)
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

                val bounds = geometryCache.boundsFor(activeStroke, isMoving) {
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

                val path = geometryCache.pathFor(activeStroke, isMoving) {
                    PathUtils.createToolPath(activeStroke.tool, activeStroke.points, activeStroke.strokeWidth)
                }
                val baseColor = CanvasColorUtils.getAdaptiveStrokeColor(Color(activeStroke.color.toInt()), isDark)
                val drawColor = if (activeStroke.isEraser) effectiveBg else baseColor

                // Luminous radiant glow for active in-flight peer strokes based on their assigned collaborator color
                val peerPresence = peerPresences[activeStroke.id]
                val isPeerInFlight = peerPresence?.isDrawing == true
                if (isPeerInFlight && !activeStroke.isEraser && activeStroke.points.size > 1) {
                    val glowColor = Color(peerPresence.color)
                    drawPath(
                        path = path,
                        color = glowColor.copy(alpha = 0.28f),
                        style = CanvasStrokeStyle(
                            width = (activeStroke.strokeWidth * 3.8f).coerceAtLeast(14f),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    drawPath(
                        path = path,
                        color = glowColor.copy(alpha = 0.55f),
                        style = CanvasStrokeStyle(
                            width = (activeStroke.strokeWidth * 2.0f).coerceAtLeast(7f),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                with(StrokeDrawer) {
                    drawStyledStroke(
                        points = activeStroke.points,
                        color = drawColor,
                        width = activeStroke.strokeWidth,
                        tool = activeStroke.tool,
                        cachedPath = path,
                        roughness = activeStroke.roughness,
                        fillStyle = activeStroke.fillStyle,
                        strokeId = activeStroke.id
                    )
                }
            }

            // 3b. Draw Pending Stroke (bridges finger lift and Compose state update)
            val pending = controller.pendingStroke
            if (pending != null) {
                if (strokes.any { it.id == pending.id }) {
                    controller.pendingStroke = null
                } else {
                    val path = geometryCache.pathFor(pending, isMoving = true) {
                        PathUtils.createToolPath(pending.tool, pending.points, pending.strokeWidth)
                    }
                    val baseColor = CanvasColorUtils.getAdaptiveStrokeColor(Color(pending.color.toInt()), isDark)
                    val drawColor = if (pending.isEraser) effectiveBg else baseColor
                    with(StrokeDrawer) {
                        drawStyledStroke(
                            points = pending.points,
                            color = drawColor,
                            width = pending.strokeWidth,
                            tool = pending.tool,
                            cachedPath = path,
                            roughness = pending.roughness,
                            fillStyle = pending.fillStyle,
                            strokeId = pending.id
                        )
                    }
                }
            }

            // 3b. Draw In-Shape Embedded Text (Centered within diagram nodes)
            strokes.forEach { stroke ->
                val activeStroke = movingStrokeMap[stroke.id] ?: stroke
                val text = activeStroke.embeddedText
                if (!text.isNullOrBlank() && activeStroke.points.size >= 2) {
                    val minX = activeStroke.points.minOf { it.x }
                    val maxX = activeStroke.points.maxOf { it.x }
                    val minY = activeStroke.points.minOf { it.y }
                    val maxY = activeStroke.points.maxOf { it.y }

                    if (maxX >= vLeft && minX <= vRight && maxY >= vTop && minY <= vBottom) {
                        val shapeW = maxX - minX
                        val shapeH = maxY - minY
                        val maxW = (shapeW * 0.82f).coerceAtLeast(20f)

                        val textLayout = embeddedTextLayoutCache.getOrPut("${activeStroke.id}_${text}_${maxW.toInt()}_${activeStroke.embeddedTextColor}") {
                            textMeasurer.measure(
                                text = AnnotatedString(text),
                                style = TextStyle(
                                    fontFamily = AppFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = if (activeStroke.embeddedTextColor != 0L) Color(activeStroke.embeddedTextColor) else m3OnSurface
                                ),
                                constraints = androidx.compose.ui.unit.Constraints(
                                    maxWidth = maxW.toInt()
                                )
                            )
                        }

                        val textLeft = minX + (shapeW - textLayout.size.width) / 2f
                        val textTop = minY + (shapeH - textLayout.size.height) / 2f
                        drawText(textLayout, topLeft = Offset(textLeft, textTop))
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

                val fontAvailable = TextSvgGenerator.isFontAvailable(activeElem.fontName)
                if (!fontAvailable && !activeElem.svgData.isNullOrBlank()) {
                    val pathKey = "${activeElem.id}_${activeElem.svgData.hashCode()}"
                    val path = svgPathCache.getOrPut(pathKey) {
                        try {
                            val parser = PathParser()
                            val cleanD = if (activeElem.svgData.startsWith("<")) {
                                Regex("""\bd=["']([^"']+)["']""").find(activeElem.svgData)?.groupValues?.get(1) ?: activeElem.svgData
                            } else activeElem.svgData
                            parser.parsePathString(cleanD)
                            parser.toPath()
                        } catch (e: Throwable) {
                            null
                        }
                    }

                    if (path != null) {
                        drawContext.canvas.save()
                        drawContext.canvas.translate(activeElem.x, activeElem.y)
                        drawPath(path = path, color = Color(activeElem.color))
                        drawContext.canvas.restore()
                    } else {
                        val fontFam = getDocFontFamily(activeElem.fontName)
                        val result = textLayoutCache.getOrPut("${activeElem.id}_${activeElem.text}_${activeElem.fontSize}_${activeElem.color}_${activeElem.fontName}") {
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
                } else {
                    val fontFam = getDocFontFamily(activeElem.fontName)
                    val result = textLayoutCache.getOrPut("${activeElem.id}_${activeElem.text}_${activeElem.fontSize}_${activeElem.color}_${activeElem.fontName}") {
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
            }

            // 4b. Draw Whiteboard Sticky Notes (Culled & Layout Cached)
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
                val baseColor = CanvasColorUtils.getAdaptiveStrokeColor(effectiveColor, isDark)
                val drawColor = if (controller.activeToolUsed == DrawingTool.ERASER) effectiveBg else baseColor

                with(StrokeDrawer) {
                    drawStyledStroke(
                        points = controller.activePoints,
                        color = drawColor,
                        width = currentWidth / zoom,
                        tool = controller.activeToolUsed,
                        cachedPath = if (controller.activeToolUsed == DrawingTool.CALLIGRAPHY || controller.activeToolUsed == DrawingTool.PENCIL) null else controller.activePath,
                        roughness = RoughnessStyle.CLEAN
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

            // 6b. Draw Magnetic Connector Anchor Snap Indicator
            controller.activeHoverAnchor?.let { anchor ->
                drawCircle(
                    color = selectionPrimary,
                    radius = 9.dp.toPx() / zoom,
                    center = anchor.position,
                    style = CanvasStrokeStyle(width = 2.5f / zoom)
                )
                drawCircle(
                    color = selectionPrimary.copy(alpha = 0.45f),
                    radius = 5.dp.toPx() / zoom,
                    center = anchor.position
                )
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
                        val midX = (boundRect.left + boundRect.right) / 2f
                        val midY = (boundRect.top + boundRect.bottom) / 2f
                        listOf(
                            Offset(boundRect.left, boundRect.top),
                            Offset(midX, boundRect.top),
                            Offset(boundRect.right, boundRect.top),
                            Offset(boundRect.right, midY),
                            Offset(boundRect.right, boundRect.bottom),
                            Offset(midX, boundRect.bottom),
                            Offset(boundRect.left, boundRect.bottom),
                            Offset(boundRect.left, midY)
                        ).forEach { handleCenter ->
                            drawCircle(
                                color = m3Surface,
                                radius = handleRadius,
                                center = handleCenter
                            )
                            drawCircle(
                                color = selectionPrimary,
                                radius = handleRadius,
                                center = handleCenter,
                                style = CanvasStrokeStyle(width = 2.dp.toPx() / zoom)
                            )
                        }

                        // 7b. Draw Smart Alignment Guides (Dashed snap lines)
                        if (controller.activeAlignmentGuides.isNotEmpty()) {
                            val guideDash = PathEffect.dashPathEffect(floatArrayOf(8f / zoom, 6f / zoom), 0f)
                            controller.activeAlignmentGuides.forEach { guide ->
                                if (guide.isVertical) {
                                    drawLine(
                                        color = selectionPrimary,
                                        start = Offset(guide.position, guide.start),
                                        end = Offset(guide.position, guide.end),
                                        strokeWidth = 1.5f / zoom,
                                        pathEffect = guideDash
                                    )
                                } else {
                                    drawLine(
                                        color = selectionPrimary,
                                        start = Offset(guide.start, guide.position),
                                        end = Offset(guide.end, guide.position),
                                        strokeWidth = 1.5f / zoom,
                                        pathEffect = guideDash
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 8. Render Real-Time Peer Presence Cursors with Material Shapes & Random Color Badges
            val vibrantBadgePalette = listOf(
                Color(0xFFE53935), // Vivid Red
                Color(0xFFD81B60), // Vivid Pink
                Color(0xFF8E24AA), // Vivid Purple
                Color(0xFF3949AB), // Indigo
                Color(0xFF1E88E5), // Blue
                Color(0xFF00ACC1), // Cyan
                Color(0xFF00897B), // Teal
                Color(0xFF43A047), // Green
                Color(0xFFFB8C00), // Amber
                Color(0xFF6D4C41), // Brown
                Color(0xFF5E35B1)  // Deep Violet
            )

            peerPresences.values.forEach { peer ->
                runCatching {
                    peer.currentPoint?.let { point ->
                        if (!point.x.isFinite() || !point.y.isFinite()) return@let

                        val peerSeed = abs((peer.peerId.ifBlank { peer.name }).hashCode())
                        val fallbackBadgeColor = vibrantBadgePalette[peerSeed % vibrantBadgePalette.size]
                        val cursorColor = if (peer.color != 0L) Color(peer.color) else fallbackBadgeColor
                        val badgeColor = cursorColor
                        val center = Offset(point.x, point.y)
                        val r = 11f / zoom
                        val shapeType = peerSeed % 5 // 0: Circle, 1: Rounded Square, 2: Diamond, 3: Triangle, 4: 8-Point Star

                        // Subtle outer radiant halo
                        drawCircle(
                            color = cursorColor.copy(alpha = 0.22f),
                            radius = r * 1.8f,
                            center = center
                        )

                        // Render distinct Material expressive shape cursor
                        when (shapeType) {
                            0 -> { // Circle
                                drawCircle(color = cursorColor, radius = r, center = center)
                                drawCircle(color = Color.White, radius = r * 0.4f, center = center)
                            }
                            1 -> { // Rounded Square (Squircle)
                                drawRoundRect(
                                    color = cursorColor,
                                    topLeft = Offset(center.x - r, center.y - r),
                                    size = Size(r * 2f, r * 2f),
                                    cornerRadius = CornerRadius(r * 0.45f, r * 0.45f)
                                )
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(center.x - r * 0.45f, center.y - r * 0.45f),
                                    size = Size(r * 0.9f, r * 0.9f),
                                    cornerRadius = CornerRadius(r * 0.2f, r * 0.2f)
                                )
                            }
                            2 -> { // Diamond (rotated polygon)
                                val diamond = Path().apply {
                                    moveTo(center.x, center.y - r * 1.25f)
                                    lineTo(center.x + r * 1.25f, center.y)
                                    lineTo(center.x, center.y + r * 1.25f)
                                    lineTo(center.x - r * 1.25f, center.y)
                                    close()
                                }
                                drawPath(diamond, cursorColor)
                                drawCircle(color = Color.White, radius = r * 0.38f, center = center)
                            }
                            3 -> { // Triangle
                                val tri = Path().apply {
                                    moveTo(center.x, center.y - r * 1.3f)
                                    lineTo(center.x + r * 1.2f, center.y + r * 0.9f)
                                    lineTo(center.x - r * 1.2f, center.y + r * 0.9f)
                                    close()
                                }
                                drawPath(tri, cursorColor)
                                drawCircle(color = Color.White, radius = r * 0.35f, center = Offset(center.x, center.y + r * 0.2f))
                            }
                            else -> { // 8-point expressive Star
                                val star = Path()
                                val outR = r * 1.35f
                                val inR = r * 0.65f
                                for (i in 0 until 8) {
                                    val angle = (i * Math.PI / 4.0) - (Math.PI / 2.0)
                                    val rad = if (i % 2 == 0) outR else inR
                                    val px = center.x + (rad * kotlin.math.cos(angle)).toFloat()
                                    val py = center.y + (rad * kotlin.math.sin(angle)).toFloat()
                                    if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
                                }
                                star.close()
                                drawPath(star, cursorColor)
                                drawCircle(color = Color.White, radius = r * 0.35f, center = center)
                            }
                        }

                        // Peer name & state badge with random vibrant badgeColor
                        val statusText = if (peer.isDrawing) "${peer.name.take(6)} • drawing" else peer.name.take(8)
                        val measuredText = textMeasurer.measure(
                            text = AnnotatedString(statusText),
                            style = TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = (11 / zoom).sp
                            )
                        )
                        val tagPadding = 7f / zoom
                        val tagWidth = measuredText.size.width + tagPadding * 2
                        val tagHeight = measuredText.size.height + tagPadding * 1.4f
                        val tagTopLeft = Offset(point.x + 14f / zoom, point.y - tagHeight - 6f / zoom)

                        // Badge background in peer's vibrant badgeColor
                        drawRoundRect(
                            color = badgeColor.copy(alpha = 0.95f),
                            topLeft = tagTopLeft,
                            size = Size(tagWidth, tagHeight),
                            cornerRadius = CornerRadius(6f / zoom, 6f / zoom)
                        )
                        // Thin crisp border
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.6f),
                            topLeft = tagTopLeft,
                            size = Size(tagWidth, tagHeight),
                            cornerRadius = CornerRadius(6f / zoom, 6f / zoom),
                            style = CanvasStrokeStyle(width = 1.0f / zoom)
                        )
                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(tagTopLeft.x + tagPadding, tagTopLeft.y + tagPadding * 0.7f)
                        )
                    }
                }
            }
        }
    }
}
