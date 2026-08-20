package com.kd.ootb.presentation.canvas

import android.graphics.BitmapFactory
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStrokeStyle
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.ootb.domain.math.PathUtils
import com.kd.ootb.domain.model.DrawingTool
import com.kd.ootb.domain.model.ImageElement
import com.kd.ootb.domain.model.PeerPresence
import com.kd.ootb.domain.model.Point
import com.kd.ootb.domain.model.Stroke
import com.kd.ootb.domain.model.TextElement
import com.kd.ootb.ui.theme.getGoogleFontFamily
import com.kd.ootb.ui.theme.robotoFlexAppFontFamily
import java.io.File
import java.util.UUID
import kotlin.math.abs
import kotlin.math.hypot

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WhiteboardCanvas(
    strokes: List<Stroke>,
    textElements: List<TextElement> = emptyList(),
    imageElements: List<ImageElement> = emptyList(),
    peerPresences: Map<String, PeerPresence> = emptyMap(),
    currentColor: Color = Color.White,
    currentWidth: Float = 6.0f,
    selectedTool: DrawingTool = DrawingTool.PEN,
    backgroundColor: Color = Color(0xFF18181C),
    zoom: Float = 1.0f,
    panOffset: Offset = Offset.Zero,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onTransformStateChanged: (isTransforming: Boolean) -> Unit = {},
    onStrokeCompleted: (Stroke) -> Unit,
    onStrokeBroadcastStart: (String, Long, Float, Boolean, Point) -> Unit,
    onStrokeBroadcastPoint: (String, Point) -> Unit,
    onStrokeBroadcastEnd: (String) -> Unit,
    onDeleteStroke: (String) -> Unit = {},
    onDeleteText: (String) -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
    onElementsMoved: (List<Stroke>, List<TextElement>, List<ImageElement>) -> Unit = { _, _, _ -> },
    onSelectionChanged: (List<String>, List<String>, Rect?) -> Unit = { _, _, _ -> },
    onTapForText: (Point) -> Unit = {},
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val pathCache = remember { HashMap<String, Path>() }
    val imageBitmapCache = remember { HashMap<String, ImageBitmap>() }

    // Active in-progress stroke path and points
    val activePath = remember { Path() }
    val activePoints = remember { mutableListOf<Point>() }
    var activeStrokeId by remember { mutableStateOf("") }
    val drawInvalidator = remember { mutableLongStateOf(0L) }

    // Selection & Move State
    val selectedStrokeIds = remember { mutableStateListOf<String>() }
    val selectedTextIds = remember { mutableStateListOf<String>() }
    val selectedImageIds = remember { mutableStateListOf<String>() }
    var isMovingSelection by remember { mutableStateOf(false) }
    var marqueeStart by remember { mutableStateOf<Point?>(null) }
    var marqueeRect by remember { mutableStateOf<Rect?>(null) }
    val lassoPoints = remember { mutableStateListOf<Point>() }
    val lassoPath = remember { Path() }
    var lastMovePoint by remember { mutableStateOf(Point(0f, 0f)) }

    // Working copies for moving elements in real-time
    val movingStrokes = remember { mutableStateListOf<Stroke>() }
    val movingTexts = remember { mutableStateListOf<TextElement>() }
    val movingImages = remember { mutableStateListOf<ImageElement>() }

    var lastTouchX = 0f
    var lastTouchY = 0f
    var lastTouchDist = 0f
    var lastEventTime = 0L
    var isMultiTouch = false

    fun notifySelectionChanged() {
        if (selectedStrokeIds.isEmpty() && selectedTextIds.isEmpty() && selectedImageIds.isEmpty()) {
            onSelectionChanged(emptyList(), emptyList(), null)
            return
        }
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        strokes.filter { selectedStrokeIds.contains(it.id) }.forEach { s ->
            s.points.forEach { p ->
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
        }
        textElements.filter { selectedTextIds.contains(it.id) }.forEach { t ->
            if (t.x < minX) minX = t.x
            if (t.y < minY) minY = t.y
            if (t.x + (t.fontSize * 6f) > maxX) maxX = t.x + (t.fontSize * 6f)
            if (t.y + (t.fontSize * 1.5f) > maxY) maxY = t.y + (t.fontSize * 1.5f)
        }
        imageElements.filter { selectedImageIds.contains(it.id) }.forEach { im ->
            if (im.x < minX) minX = im.x
            if (im.y < minY) minY = im.y
            if (im.x + im.width > maxX) maxX = im.x + im.width
            if (im.y + im.height > maxY) maxY = im.y + im.height
        }

        if (minX <= maxX && minY <= maxY) {
            val pad = 12f
            onSelectionChanged(selectedStrokeIds.toList(), selectedTextIds.toList(), Rect(minX - pad, minY - pad, maxX + pad, maxY + pad))
        } else {
            onSelectionChanged(selectedStrokeIds.toList(), selectedTextIds.toList(), null)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInteropFilter { event ->
                val pointerCount = event.pointerCount

                // Handle touch release events unconditionally to ensure HUD always returns
                if (event.actionMasked == MotionEvent.ACTION_UP ||
                    event.actionMasked == MotionEvent.ACTION_CANCEL ||
                    event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                    if (isMultiTouch) {
                        isMultiTouch = false
                    }
                    onTransformStateChanged(false)
                }

                if (pointerCount >= 2) {
                    if (activePoints.isNotEmpty()) {
                        activePoints.clear()
                        activePath.reset()
                        drawInvalidator.longValue = System.nanoTime()
                    }

                    val x0 = event.getX(0)
                    val y0 = event.getY(0)
                    val x1 = event.getX(1)
                    val y1 = event.getY(1)

                    val midX = (x0 + x1) / 2f
                    val midY = (y0 + y1) / 2f
                    val dist = hypot(x1 - x0, y1 - y0)

                    when (event.actionMasked) {
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            lastTouchX = midX
                            lastTouchY = midY
                            lastTouchDist = dist
                            lastEventTime = event.eventTime
                            isMultiTouch = true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (lastTouchDist > 0f) {
                                val scaleFactor = (dist / lastTouchDist).coerceIn(0.6f, 1.8f)
                                val dx = midX - lastTouchX
                                val dy = midY - lastTouchY
                                val dt = (event.eventTime - lastEventTime).coerceAtLeast(1)
                                val moveDist = hypot(dx, dy)
                                val velocity = (moveDist / dt) * 1000f
                                val zoomDelta = abs(scaleFactor - 1.0f)

                                // Velocity sensing: Only tuck HUD outward if user is panning/zooming fast
                                if (velocity > 1200f || zoomDelta > 0.08f || moveDist > 26f) {
                                    onTransformStateChanged(true)
                                } else if (velocity < 350f && zoomDelta < 0.02f) {
                                    onTransformStateChanged(false)
                                }

                                onTransform(scaleFactor, Offset(dx, dy))
                            }
                            lastTouchX = midX
                            lastTouchY = midY
                            lastTouchDist = dist
                            lastEventTime = event.eventTime
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            isMultiTouch = false
                            onTransformStateChanged(false)
                        }
                    }
                    true
                } else {
                    if (isMultiTouch) {
                        if (event.actionMasked == MotionEvent.ACTION_UP ||
                            event.actionMasked == MotionEvent.ACTION_CANCEL ||
                            event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                            isMultiTouch = false
                            onTransformStateChanged(false)
                        }
                        return@pointerInteropFilter true
                    }

                    val screenX = event.x
                    val screenY = event.y
                    val worldX = (screenX - panOffset.x) / zoom
                    val worldY = (screenY - panOffset.y) / zoom
                    val pressure = if (event.pressure > 0f) event.pressure else 1.0f

                    val worldPoint = Point(
                        x = worldX,
                        y = worldY,
                        pressure = pressure,
                        timestampMs = event.eventTime
                    )

                    if (isReadOnly) {
                        return@pointerInteropFilter true
                    }

                    // Text Tool Placement
                    if (selectedTool == DrawingTool.TEXT) {
                        if (event.actionMasked == MotionEvent.ACTION_UP) {
                            onTapForText(worldPoint)
                        }
                        return@pointerInteropFilter true
                    }

                    // Select Erase Hit-Testing
                    if (selectedTool == DrawingTool.SELECT_ERASE) {
                        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
                            val hitRadius = (32f / zoom)
                            val hitRadiusSq = hitRadius * hitRadius

                            val hitStroke = strokes.findLast { s ->
                                s.points.any { p ->
                                    val dx = p.x - worldPoint.x
                                    val dy = p.y - worldPoint.y
                                    (dx * dx + dy * dy) <= hitRadiusSq
                                }
                            }
                            if (hitStroke != null) {
                                pathCache.remove(hitStroke.id)
                                onDeleteStroke(hitStroke.id)
                                return@pointerInteropFilter true
                            }

                            val hitText = textElements.findLast { t ->
                                val dx = t.x - worldPoint.x
                                val dy = t.y - worldPoint.y
                                (dx * dx + dy * dy) <= (hitRadiusSq * 3f)
                            }
                            if (hitText != null) {
                                onDeleteText(hitText.id)
                                return@pointerInteropFilter true
                            }

                            val hitImage = imageElements.findLast { im ->
                                worldPoint.x in im.x..(im.x + im.width) && worldPoint.y in im.y..(im.y + im.height)
                            }
                            if (hitImage != null) {
                                onDeleteImage(hitImage.id)
                                return@pointerInteropFilter true
                            }
                        }
                        return@pointerInteropFilter true
                    }

                    // SELECT & MOVE / RECT_SELECT / LASSO_SELECT
                    val isSelectionTool = selectedTool == DrawingTool.SELECT_MOVE ||
                            selectedTool == DrawingTool.RECT_SELECT ||
                            selectedTool == DrawingTool.LASSO_SELECT

                    if (isSelectionTool) {
                        val hitRadius = (32f / zoom)
                        val hitRadiusSq = hitRadius * hitRadius

                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                lastMovePoint = worldPoint

                                val hitStroke = strokes.findLast { s ->
                                    s.points.any { p ->
                                        val dx = p.x - worldPoint.x
                                        val dy = p.y - worldPoint.y
                                        (dx * dx + dy * dy) <= hitRadiusSq
                                    }
                                }
                                val hitText = textElements.findLast { t ->
                                    val dx = t.x - worldPoint.x
                                    val dy = t.y - worldPoint.y
                                    (dx * dx + dy * dy) <= (hitRadiusSq * 3f)
                                }
                                val hitImage = imageElements.findLast { im ->
                                    worldPoint.x in im.x..(im.x + im.width) && worldPoint.y in im.y..(im.y + im.height)
                                }

                                val isTappingExistingSelection = (hitStroke != null && selectedStrokeIds.contains(hitStroke.id)) ||
                                        (hitText != null && selectedTextIds.contains(hitText.id)) ||
                                        (hitImage != null && selectedImageIds.contains(hitImage.id))

                                if (isTappingExistingSelection || ((selectedStrokeIds.isNotEmpty() || selectedTextIds.isNotEmpty() || selectedImageIds.isNotEmpty()) && (hitStroke != null || hitText != null || hitImage != null))) {
                                    if (!isTappingExistingSelection) {
                                        if (hitStroke != null && !selectedStrokeIds.contains(hitStroke.id)) {
                                            selectedStrokeIds.clear()
                                            selectedTextIds.clear()
                                            selectedImageIds.clear()
                                            selectedStrokeIds.add(hitStroke.id)
                                        }
                                        if (hitText != null && !selectedTextIds.contains(hitText.id)) {
                                            selectedStrokeIds.clear()
                                            selectedTextIds.clear()
                                            selectedImageIds.clear()
                                            selectedTextIds.add(hitText.id)
                                        }
                                        if (hitImage != null && !selectedImageIds.contains(hitImage.id)) {
                                            selectedStrokeIds.clear()
                                            selectedTextIds.clear()
                                            selectedImageIds.clear()
                                            selectedImageIds.add(hitImage.id)
                                        }
                                    }

                                    isMovingSelection = true
                                    movingStrokes.clear()
                                    movingStrokes.addAll(strokes.filter { selectedStrokeIds.contains(it.id) })
                                    movingTexts.clear()
                                    movingTexts.addAll(textElements.filter { selectedTextIds.contains(it.id) })
                                    movingImages.clear()
                                    movingImages.addAll(imageElements.filter { selectedImageIds.contains(it.id) })
                                } else if (hitStroke != null || hitText != null || hitImage != null) {
                                    selectedStrokeIds.clear()
                                    selectedTextIds.clear()
                                    selectedImageIds.clear()
                                    if (hitStroke != null) selectedStrokeIds.add(hitStroke.id)
                                    if (hitText != null) selectedTextIds.add(hitText.id)
                                    if (hitImage != null) selectedImageIds.add(hitImage.id)

                                    isMovingSelection = true
                                    movingStrokes.clear()
                                    movingStrokes.addAll(strokes.filter { selectedStrokeIds.contains(it.id) })
                                    movingTexts.clear()
                                    movingTexts.addAll(textElements.filter { selectedTextIds.contains(it.id) })
                                    movingImages.clear()
                                    movingImages.addAll(imageElements.filter { selectedImageIds.contains(it.id) })
                                } else {
                                    selectedStrokeIds.clear()
                                    selectedTextIds.clear()
                                    selectedImageIds.clear()
                                    isMovingSelection = false

                                    if (selectedTool == DrawingTool.LASSO_SELECT) {
                                        lassoPoints.clear()
                                        lassoPoints.add(worldPoint)
                                        lassoPath.reset()
                                        lassoPath.moveTo(worldPoint.x, worldPoint.y)
                                    } else {
                                        marqueeStart = worldPoint
                                        marqueeRect = Rect(worldPoint.x, worldPoint.y, worldPoint.x, worldPoint.y)
                                    }
                                }
                                notifySelectionChanged()
                                drawInvalidator.longValue = System.nanoTime()
                                return@pointerInteropFilter true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                if (isMovingSelection) {
                                    val dx = worldPoint.x - lastMovePoint.x
                                    val dy = worldPoint.y - lastMovePoint.y

                                    for (i in movingStrokes.indices) {
                                        val strk = movingStrokes[i]
                                        val updatedPoints = strk.points.map { p -> p.copy(x = p.x + dx, y = p.y + dy) }
                                        val updatedStroke = strk.copy(points = updatedPoints)
                                        movingStrokes[i] = updatedStroke
                                        pathCache["${strk.id}_${updatedPoints.size}"] = PathUtils.createToolPath(strk.tool, updatedPoints)
                                    }

                                    for (i in movingTexts.indices) {
                                        val txt = movingTexts[i]
                                        movingTexts[i] = txt.copy(x = txt.x + dx, y = txt.y + dy)
                                    }

                                    for (i in movingImages.indices) {
                                        val im = movingImages[i]
                                        movingImages[i] = im.copy(x = im.x + dx, y = im.y + dy)
                                    }

                                    lastMovePoint = worldPoint
                                } else if (selectedTool == DrawingTool.LASSO_SELECT) {
                                    lassoPoints.add(worldPoint)
                                    lassoPath.lineTo(worldPoint.x, worldPoint.y)

                                    if (lassoPoints.size > 2) {
                                        selectedStrokeIds.clear()
                                        strokes.forEach { s ->
                                            if (PathUtils.isStrokeInPolygon(s.points, lassoPoints.toList())) {
                                                selectedStrokeIds.add(s.id)
                                            }
                                        }

                                        selectedTextIds.clear()
                                        textElements.forEach { t ->
                                            if (PathUtils.isPointInPolygon(Point(t.x, t.y), lassoPoints.toList())) {
                                                selectedTextIds.add(t.id)
                                            }
                                        }

                                        selectedImageIds.clear()
                                        imageElements.forEach { im ->
                                            if (PathUtils.isPointInPolygon(Point(im.x, im.y), lassoPoints.toList())) {
                                                selectedImageIds.add(im.id)
                                            }
                                        }
                                    }
                                } else {
                                    val start = marqueeStart
                                    if (start != null) {
                                        val left = minOf(start.x, worldPoint.x)
                                        val top = minOf(start.y, worldPoint.y)
                                        val right = maxOf(start.x, worldPoint.x)
                                        val bottom = maxOf(start.y, worldPoint.y)
                                        val rect = Rect(left, top, right, bottom)
                                        marqueeRect = rect

                                        selectedStrokeIds.clear()
                                        strokes.forEach { s ->
                                            if (s.points.any { p -> rect.contains(Offset(p.x, p.y)) }) {
                                                selectedStrokeIds.add(s.id)
                                            }
                                        }

                                        selectedTextIds.clear()
                                        textElements.forEach { t ->
                                            if (rect.contains(Offset(t.x, t.y))) {
                                                selectedTextIds.add(t.id)
                                            }
                                        }

                                        selectedImageIds.clear()
                                        imageElements.forEach { im ->
                                            if (rect.overlaps(Rect(im.x, im.y, im.x + im.width, im.y + im.height))) {
                                                selectedImageIds.add(im.id)
                                            }
                                        }
                                    }
                                }
                                notifySelectionChanged()
                                drawInvalidator.longValue = System.nanoTime()
                                return@pointerInteropFilter true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                if (isMovingSelection) {
                                    onElementsMoved(movingStrokes.toList(), movingTexts.toList(), movingImages.toList())
                                    isMovingSelection = false
                                    movingStrokes.clear()
                                    movingTexts.clear()
                                    movingImages.clear()
                                }
                                marqueeStart = null
                                marqueeRect = null
                                lassoPoints.clear()
                                lassoPath.reset()
                                notifySelectionChanged()
                                drawInvalidator.longValue = System.nanoTime()
                                return@pointerInteropFilter true
                            }
                        }
                        return@pointerInteropFilter true
                    }

                    val isCurvedTool = selectedTool == DrawingTool.PEN || selectedTool == DrawingTool.PENCIL ||
                            selectedTool == DrawingTool.HIGHLIGHTER || selectedTool == DrawingTool.ERASER

                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            activeStrokeId = UUID.randomUUID().toString()
                            activePoints.clear()
                            activePoints.add(worldPoint)
                            activePath.reset()

                            if (isCurvedTool) {
                                activePath.moveTo(worldPoint.x, worldPoint.y)
                            }

                            onStrokeBroadcastStart(
                                activeStrokeId,
                                currentColor.toArgb().toLong(),
                                currentWidth / zoom,
                                selectedTool == DrawingTool.ERASER,
                                worldPoint
                            )
                            drawInvalidator.longValue = System.nanoTime()
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (activePoints.isEmpty()) {
                                activePoints.add(worldPoint)
                                if (isCurvedTool) {
                                    activePath.moveTo(worldPoint.x, worldPoint.y)
                                }
                            } else {
                                if (isCurvedTool) {
                                    val historySize = event.historySize
                                    for (h in 0 until historySize) {
                                        val histScreenX = event.getHistoricalX(h)
                                        val histScreenY = event.getHistoricalY(h)
                                        val histWorldX = (histScreenX - panOffset.x) / zoom
                                        val histWorldY = (histScreenY - panOffset.y) / zoom
                                        val histPressure = if (event.getHistoricalPressure(h) > 0f) event.getHistoricalPressure(h) else 1.0f

                                        val histPt = Point(
                                            x = histWorldX,
                                            y = histWorldY,
                                            pressure = histPressure,
                                            timestampMs = event.getHistoricalEventTime(h)
                                        )
                                        val lastPt = activePoints.last()
                                        val midX = (lastPt.x + histPt.x) / 2f
                                        val midY = (lastPt.y + histPt.y) / 2f
                                        activePath.quadraticTo(lastPt.x, lastPt.y, midX, midY)
                                        activePoints.add(histPt)
                                    }

                                    val lastPt = activePoints.last()
                                    val midX = (lastPt.x + worldPoint.x) / 2f
                                    val midY = (lastPt.y + worldPoint.y) / 2f
                                    activePath.quadraticTo(lastPt.x, lastPt.y, midX, midY)
                                    activePoints.add(worldPoint)
                                } else {
                                    activePoints.add(worldPoint)
                                    activePath.reset()
                                    activePath.addPath(PathUtils.createToolPath(selectedTool, listOf(activePoints.first(), worldPoint)))
                                }

                                onStrokeBroadcastPoint(activeStrokeId, worldPoint)
                            }
                            drawInvalidator.longValue = System.nanoTime()
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (activePoints.isNotEmpty()) {
                                val finalPoints = if (isCurvedTool) {
                                    val lastPt = activePoints.last()
                                    activePath.lineTo(lastPt.x, lastPt.y)
                                    activePoints.toList()
                                } else {
                                    listOf(activePoints.first(), activePoints.last())
                                }

                                val finalStroke = Stroke(
                                    id = activeStrokeId,
                                    points = finalPoints,
                                    color = currentColor.toArgb().toLong(),
                                    strokeWidth = currentWidth / zoom,
                                    tool = selectedTool,
                                    isEraser = selectedTool == DrawingTool.ERASER
                                )
                                pathCache["${finalStroke.id}_${finalPoints.size}"] = PathUtils.createToolPath(selectedTool, finalPoints)
                                onStrokeCompleted(finalStroke)
                                onStrokeBroadcastEnd(activeStrokeId)

                                activePoints.clear()
                                activePath.reset()
                                drawInvalidator.longValue = System.nanoTime()
                            }
                            true
                        }
                        else -> false
                    }
                }
            }
    ) {
        @Suppress("UNUSED_VARIABLE")
        val dummy = drawInvalidator.longValue

        withTransform({
            translate(panOffset.x, panOffset.y)
            scale(zoom, zoom, Offset.Zero)
        }) {
            // Subtle, GPU-batched Isometric Dot Grid
            val dotSpacing = 36.dp.toPx()
            val rowHeight = dotSpacing * 0.8660254f
            val dotColor = if (backgroundColor.luminance() < 0.5f) Color(0x18FFFFFF) else Color(0x10000000)

            val leftBound = (-panOffset.x / zoom) - dotSpacing
            val topBound = (-panOffset.y / zoom) - dotSpacing
            val rightBound = (size.width - panOffset.x) / zoom + dotSpacing
            val bottomBound = (size.height - panOffset.y) / zoom + dotSpacing

            val startRow = (topBound / rowHeight).toInt() - 1
            val endRow = (bottomBound / rowHeight).toInt() + 1
            val startCol = (leftBound / dotSpacing).toInt() - 1
            val endCol = (rightBound / dotSpacing).toInt() + 1

            val dotPoints = mutableListOf<Offset>()
            for (rowIndex in startRow..endRow) {
                val y = rowIndex * rowHeight
                val xOffset = if (rowIndex % 2 != 0) dotSpacing / 2f else 0f
                for (colIndex in startCol..endCol) {
                    dotPoints.add(Offset(colIndex * dotSpacing + xOffset, y))
                }
            }

            if (dotPoints.isNotEmpty()) {
                drawPoints(
                    points = dotPoints,
                    pointMode = PointMode.Points,
                    color = dotColor,
                    strokeWidth = (1.5.dp.toPx() / zoom).coerceAtLeast(1.0f),
                    cap = StrokeCap.Round
                )
            }

            val movingStrokeMap = movingStrokes.associateBy { it.id }
            val movingTextMap = movingTexts.associateBy { it.id }
            val movingImageMap = movingImages.associateBy { it.id }

            // 1. Draw Image Elements
            imageElements.forEach { img ->
                val activeImg = movingImageMap[img.id] ?: img
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

            // 2. Draw Completed & Live Real-Time Strokes (Dynamic cache key on stroke points count)
            strokes.forEach { stroke ->
                val activeStroke = movingStrokeMap[stroke.id] ?: stroke
                val path = pathCache.getOrPut("${activeStroke.id}_${activeStroke.points.size}") {
                    PathUtils.createToolPath(activeStroke.tool, activeStroke.points)
                }
                val baseColor = Color(activeStroke.color)
                val drawColor = when {
                    activeStroke.isEraser -> backgroundColor
                    activeStroke.tool == DrawingTool.PENCIL -> baseColor.copy(alpha = 0.75f)
                    activeStroke.tool == DrawingTool.HIGHLIGHTER -> baseColor.copy(alpha = 0.38f)
                    else -> baseColor
                }
                val drawWidth = when (activeStroke.tool) {
                    DrawingTool.PENCIL -> (activeStroke.strokeWidth * 0.6f).coerceAtLeast(1.5f)
                    DrawingTool.HIGHLIGHTER -> activeStroke.strokeWidth * 3.2f
                    else -> activeStroke.strokeWidth
                }

                drawPath(
                    path = path,
                    color = drawColor,
                    style = CanvasStrokeStyle(
                        width = drawWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 3. Draw Text Elements
            textElements.forEach { elem ->
                val activeElem = movingTextMap[elem.id] ?: elem
                if (activeElem.text.isNotBlank()) {
                    val fontFam = getGoogleFontFamily(activeElem.fontName)
                    val result = textMeasurer.measure(
                        text = AnnotatedString(activeElem.text),
                        style = TextStyle(
                            fontFamily = fontFam,
                            fontSize = activeElem.fontSize.sp,
                            color = Color(activeElem.color)
                        )
                    )
                    drawText(result, topLeft = Offset(activeElem.x, activeElem.y))
                }
            }

            // 4. Draw Active In-Progress Stroke
            if (!activePath.isEmpty) {
                val baseColor = currentColor
                val drawColor = when {
                    selectedTool == DrawingTool.ERASER -> backgroundColor
                    selectedTool == DrawingTool.PENCIL -> baseColor.copy(alpha = 0.75f)
                    selectedTool == DrawingTool.HIGHLIGHTER -> baseColor.copy(alpha = 0.38f)
                    else -> baseColor
                }
                val drawWidth = when (selectedTool) {
                    DrawingTool.PENCIL -> ((currentWidth / zoom) * 0.6f).coerceAtLeast(1.5f)
                    DrawingTool.HIGHLIGHTER -> (currentWidth / zoom) * 3.2f
                    else -> currentWidth / zoom
                }

                drawPath(
                    path = activePath,
                    color = drawColor,
                    style = CanvasStrokeStyle(
                        width = drawWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 5. Draw Selection Overlays (Marquee Rect, Freeform Lasso, and Selection Bounding Box)
            val isSelectionMode = selectedTool == DrawingTool.SELECT_MOVE ||
                    selectedTool == DrawingTool.RECT_SELECT ||
                    selectedTool == DrawingTool.LASSO_SELECT

            if (isSelectionMode) {
                if (selectedTool == DrawingTool.LASSO_SELECT && lassoPoints.size > 1) {
                    drawPath(
                        path = lassoPath,
                        color = Color(0xFFDC5F00).copy(alpha = 0.15f)
                    )
                    drawPath(
                        path = lassoPath,
                        color = Color(0xFFDC5F00),
                        style = CanvasStrokeStyle(
                            width = 2.dp.toPx() / zoom,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f / zoom, 10f / zoom), 0f)
                        )
                    )
                }

                val currentMarquee = marqueeRect
                if (currentMarquee != null) {
                    drawRect(
                        color = Color(0xFFDC5F00).copy(alpha = 0.15f),
                        topLeft = Offset(currentMarquee.left, currentMarquee.top),
                        size = androidx.compose.ui.geometry.Size(currentMarquee.width, currentMarquee.height)
                    )
                    drawRect(
                        color = Color(0xFFDC5F00),
                        topLeft = Offset(currentMarquee.left, currentMarquee.top),
                        size = androidx.compose.ui.geometry.Size(currentMarquee.width, currentMarquee.height),
                        style = CanvasStrokeStyle(
                            width = 2.dp.toPx() / zoom,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f / zoom, 10f / zoom), 0f)
                        )
                    )
                }

                if (selectedStrokeIds.isNotEmpty() || selectedTextIds.isNotEmpty() || selectedImageIds.isNotEmpty()) {
                    var minX = Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxX = Float.MIN_VALUE
                    var maxY = Float.MIN_VALUE

                    strokes.filter { selectedStrokeIds.contains(it.id) }.forEach { s ->
                        val activeS = movingStrokeMap[s.id] ?: s
                        activeS.points.forEach { p ->
                            if (p.x < minX) minX = p.x
                            if (p.y < minY) minY = p.y
                            if (p.x > maxX) maxX = p.x
                            if (p.y > maxY) maxY = p.y
                        }
                    }

                    textElements.filter { selectedTextIds.contains(it.id) }.forEach { t ->
                        val activeT = movingTextMap[t.id] ?: t
                        if (activeT.x < minX) minX = activeT.x
                        if (activeT.y < minY) minY = activeT.y
                        if (activeT.x + (activeT.fontSize * 6f) > maxX) maxX = activeT.x + (activeT.fontSize * 6f)
                        if (activeT.y + (activeT.fontSize * 1.5f) > maxY) maxY = activeT.y + (activeT.fontSize * 1.5f)
                    }

                    imageElements.filter { selectedImageIds.contains(it.id) }.forEach { im ->
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
                            color = Color(0xFFDC5F00),
                            topLeft = Offset(boundRect.left, boundRect.top),
                            size = androidx.compose.ui.geometry.Size(boundRect.width, boundRect.height),
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
                                color = Color.White,
                                radius = handleRadius,
                                center = corner
                            )
                            drawCircle(
                                color = Color(0xFFDC5F00),
                                radius = handleRadius,
                                center = corner,
                                style = CanvasStrokeStyle(width = 2.dp.toPx() / zoom)
                            )
                        }
                    }
                }
            }

            // 6. Render Real-Time Peer Presence Cursors with Dynamic User Colors & App Font
            val peerColorPalette = listOf(
                Color(0xFF00E676), // Bright Emerald Green
                Color(0xFF3897F0), // Vibrant Blue
                Color(0xFFFF9D42), // Warm Amber
                Color(0xFFFF3366), // Crimson
                Color(0xFFAF52DE), // Violet
                Color(0xFF00E5FF), // Cyan
                Color(0xFFFFD60A)  // Gold
            )

            peerPresences.values.forEach { peer ->
                peer.currentPoint?.let { point ->
                    val colorIndex = abs(peer.name.hashCode()) % peerColorPalette.size
                    val cursorColor = if (peer.color != 0L && peer.color != 0xFFDC5F00) Color(peer.color) else peerColorPalette[colorIndex]

                    // 1. Glowing animated peer cursor dot
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
                        color = Color.White,
                        radius = 2.5f / zoom,
                        center = Offset(point.x, point.y)
                    )

                    // 2. Translucent Peer Nametag Pill using exact typography and dynamic color
                    val statusText = if (peer.isDrawing) "${peer.name} • drawing" else peer.name
                    val measuredText = textMeasurer.measure(
                        text = AnnotatedString(statusText),
                        style = TextStyle(
                            fontFamily = robotoFlexAppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = (11 / zoom).sp
                        )
                    )
                    val tagPadding = 6f / zoom
                    val tagWidth = measuredText.size.width + tagPadding * 2
                    val tagHeight = measuredText.size.height + tagPadding * 1.4f
                    val tagTopLeft = Offset(point.x + 12f / zoom, point.y - tagHeight - 6f / zoom)

                    drawRoundRect(
                        color = Color(0xFF121418).copy(alpha = 0.92f),
                        topLeft = tagTopLeft,
                        size = androidx.compose.ui.geometry.Size(tagWidth, tagHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f / zoom, 6f / zoom)
                    )
                    drawRoundRect(
                        color = cursorColor.copy(alpha = 0.85f),
                        topLeft = tagTopLeft,
                        size = androidx.compose.ui.geometry.Size(tagWidth, tagHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f / zoom, 6f / zoom),
                        style = CanvasStrokeStyle(width = 1.2f / zoom)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = statusText,
                        topLeft = Offset(tagTopLeft.x + tagPadding, tagTopLeft.y + tagPadding * 0.7f),
                        style = TextStyle(
                            fontFamily = robotoFlexAppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = (11 / zoom).sp
                        )
                    )
                }
            }
        }
    }
}
