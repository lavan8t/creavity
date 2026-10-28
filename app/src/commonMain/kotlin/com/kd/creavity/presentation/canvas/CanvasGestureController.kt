package com.kd.creavity.presentation.canvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import com.kd.creavity.domain.math.PathUtils
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import java.util.UUID
import kotlin.math.hypot

class CanvasGestureController(
    private val onTransform: (zoomChange: Float, focal: Offset, panChange: Offset) -> Unit,
    private val onTransformStateChanged: (isTransforming: Boolean, touchPoints: List<Offset>) -> Unit,
    private val onStrokeCompleted: (Stroke) -> Unit,
    private val onStrokeBroadcastStart: (String, Long, Float, Boolean, Point, DrawingTool) -> Unit,
    private val onStrokeBroadcastPoint: (String, Point) -> Unit,
    private val onStrokeBroadcastEnd: (String) -> Unit,
    private val onDeleteStroke: (String) -> Unit,
    private val onDeleteText: (String) -> Unit,
    private val onDeleteImage: (String) -> Unit,
    private val onDeleteStickyNote: (String) -> Unit = {},
    private val onElementsMoved: (List<Stroke>, List<TextElement>, List<ImageElement>) -> Unit,
    private val onSelectionChanged: (List<String>, List<String>, Rect?) -> Unit,
    private val onTapForText: (Point) -> Unit,
    private val requestInvalidate: () -> Unit
) {
    // Multi-touch pinch & pan state
    var isPinching = false
        private set
    private var lastMidX = 0f
    private var lastMidY = 0f
    private var lastDist = 0f

    // Drawing in-progress state
    val userSessionId = UUID.randomUUID().toString().take(8)
    val activePath = Path()
    val activePoints = mutableListOf<Point>()
    var activeStrokeId = ""
        private set
    var activeToolUsed = DrawingTool.PEN
        private set
    var pendingStroke: Stroke? = null

    // Laser pointer points (ephemeral)
    val laserPoints = mutableListOf<Point>()
    var laserActiveToken by mutableLongStateOf(0L)
        private set

    // Selection & move state
    val selectedStrokeIds = mutableListOf<String>()
    val selectedTextIds = mutableListOf<String>()
    val selectedImageIds = mutableListOf<String>()
    var isMovingSelection = false
        private set
    var marqueeStart: Point? = null
        private set
    var marqueeRect: Rect? = null
        private set
    val lassoPoints = mutableListOf<Point>()
    val lassoPath = Path()
    private var lastMovePoint = Point(0f, 0f)

    // Elements currently being moved in real time
    val movingStrokes = mutableListOf<Stroke>()
    val movingTexts = mutableListOf<TextElement>()
    val movingImages = mutableListOf<ImageElement>()

    // Stylus button state
    var isStylusButtonActive = false
        private set

    // Palm rejection & active drawing pointer tracking
    private var lastStylusTimestampMs: Long = 0L
    private var activeStrokeIsStylus: Boolean = false
    private var activeDrawingPointerId: Long = -1L

    // Viewport dimensions & display density
    var viewWidth = 0f
    var viewHeight = 0f
    var density = 2.5f

    private fun discardActiveStroke() {
        if (activePoints.isNotEmpty()) {
            activePoints.clear()
            activePath.reset()
            if (activeStrokeId.isNotEmpty()) {
                onStrokeBroadcastEnd(activeStrokeId)
                activeStrokeId = ""
            }
            laserPoints.clear()
            requestInvalidate()
        }
        activeDrawingPointerId = -1L
        activeStrokeIsStylus = false
    }

    private fun distanceSqToSegment(p: Point, a: Point, b: Point): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return (p.x - a.x) * (p.x - a.x) + (p.y - a.y) * (p.y - a.y)
        val t = (((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2).coerceIn(0f, 1f)
        val projX = a.x + t * (b.x - a.x)
        val projY = a.y + t * (b.y - a.y)
        val dx = p.x - projX
        val dy = p.y - projY
        return dx * dx + dy * dy
    }

    fun handleInputEvent(
        event: CanvasInputEvent,
        panOffset: Offset,
        zoom: Float,
        selectedTool: DrawingTool,
        currentColor: Color,
        currentWidth: Float,
        strokes: List<Stroke>,
        textElements: List<TextElement>,
        imageElements: List<ImageElement>,
        stickyNotes: List<StickyNote> = emptyList(),
        hapticsEnabled: Boolean,
        drawingHaptics: DrawingHaptics,
        isReadOnly: Boolean,
        geometryCache: StrokeGeometryCache
    ): Boolean {
        val pointerCount = event.pointerCount

        // 1. Mouse / Stylus scroll wheel handling
        if (event.action == CanvasPointerAction.SCROLL) {
            val vScroll = event.scrollDeltaY
            val hScroll = event.scrollDeltaX
            val isCtrl = event.isCtrlPressed
            val focal = Offset(event.x, event.y)
            if (isCtrl) {
                val zoomFactor = if (vScroll > 0) 0.92f else if (vScroll < 0) 1.08f else 1.0f
                onTransform(zoomFactor, focal, Offset.Zero)
            } else {
                onTransform(1.0f, Offset.Zero, Offset(-hScroll * 48f, -vScroll * 48f))
            }
            onTransformStateChanged(true, listOf(focal))
            return true
        }

        // 1.5. Mouse Right-Click handling (Canvas Pan on drag, Deselect on click)
        if (event.activePointer.isSecondaryButton) {
            discardActiveStroke()
            when (event.action) {
                CanvasPointerAction.DOWN -> {
                    lastMidX = event.x
                    lastMidY = event.y
                }
                CanvasPointerAction.MOVE -> {
                    val dx = event.x - lastMidX
                    val dy = event.y - lastMidY
                    onTransform(1.0f, Offset.Zero, Offset(dx, dy))
                    lastMidX = event.x
                    lastMidY = event.y
                }
                CanvasPointerAction.UP -> {
                    if (selectedStrokeIds.isNotEmpty() || selectedTextIds.isNotEmpty() || selectedImageIds.isNotEmpty()) {
                        selectedStrokeIds.clear()
                        selectedTextIds.clear()
                        selectedImageIds.clear()
                        notifySelectionChanged(strokes, textElements, imageElements)
                    }
                }
                else -> {}
            }
            return true
        }

        // 2. Stylus button inspection
        val stylusPressed = StylusInputHandler.isStylusButtonPressed(event)
        val eraserTip = StylusInputHandler.isEraserTip(event)
        if (stylusPressed != isStylusButtonActive) {
            isStylusButtonActive = stylusPressed
            if (hapticsEnabled) drawingHaptics.performStylusToggle()
        }
        val effectiveTool = if (eraserTip || stylusPressed) DrawingTool.ERASER else selectedTool

        var activeStylusIndex = -1
        for (i in 0 until pointerCount) {
            val t = event.pointers[i].type
            if (t == CanvasPointerType.STYLUS || t == CanvasPointerType.ERASER) {
                activeStylusIndex = i
                break
            }
        }
        val hasStylusPointer = (activeStylusIndex != -1)
        val now = System.currentTimeMillis()
        if (hasStylusPointer) {
            lastStylusTimestampMs = now
            if (activePoints.isNotEmpty() && !activeStrokeIsStylus) {
                discardActiveStroke()
            }
        }

        // Palm rejection: if a stylus was active within the last 600ms, ignore accidental single-finger palm touches
        if (!hasStylusPointer && (now - lastStylusTimestampMs < 600L) && pointerCount == 1) {
            if (event.action == CanvasPointerAction.DOWN) {
                return true
            }
            if (event.action == CanvasPointerAction.MOVE && !activeStrokeIsStylus) {
                discardActiveStroke()
                return true
            }
        }

        // 3. Multi-touch Pinch to Zoom & Pan (Finger mode only, when no stylus is touching)
        if (!hasStylusPointer && pointerCount >= 2) {
            discardActiveStroke()
            isPinching = true

            val x0 = event.pointers[0].x
            val y0 = event.pointers[0].y
            val x1 = event.pointers[1].x
            val y1 = event.pointers[1].y
            val midX = (x0 + x1) / 2f
            val midY = (y0 + y1) / 2f
            val dist = hypot(x1 - x0, y1 - y0)

            when (event.action) {
                CanvasPointerAction.DOWN -> {
                    lastMidX = midX
                    lastMidY = midY
                    lastDist = dist
                    val touchPoints = event.pointers.map { Offset(it.x, it.y) }
                    onTransformStateChanged(true, touchPoints)
                }
                CanvasPointerAction.MOVE -> {
                    if (lastDist > 10f && dist > 10f) {
                        val scaleFactor = (dist / lastDist).coerceIn(0.7f, 1.4f)
                        val dx = midX - lastMidX
                        val dy = midY - lastMidY

                        onTransform(scaleFactor, Offset(lastMidX, lastMidY), Offset(dx, dy))
                    }
                    lastMidX = midX
                    lastMidY = midY
                    lastDist = dist
                }
                CanvasPointerAction.UP -> {
                    lastDist = 0f
                    onTransformStateChanged(false, emptyList())
                }
                else -> {}
            }
            return true
        }

        // 4. Handle cancellation
        if (event.action == CanvasPointerAction.CANCEL) {
            onTransformStateChanged(false, emptyList())
            isPinching = false
            lastDist = 0f
            discardActiveStroke()

            isMovingSelection = false
            movingStrokes.clear()
            movingTexts.clear()
            movingImages.clear()
            marqueeStart = null
            marqueeRect = null
            lassoPoints.clear()
            lassoPath.reset()

            requestInvalidate()
            return true
        }

        // 4b. Handle pointer lift (Stroke / gesture completion)
        if (event.action == CanvasPointerAction.UP) {
            val liftingPointerId = event.activePointer.id
            val isDrawingPointerLifting = (activeDrawingPointerId == -1L || liftingPointerId == activeDrawingPointerId)

            if (!isDrawingPointerLifting && pointerCount > 1) {
                return true
            }

            onTransformStateChanged(false, emptyList())
            val wasPinching = isPinching
            isPinching = false
            lastDist = 0f
            activeDrawingPointerId = -1L
            activeStrokeIsStylus = false

            if (wasPinching) {
                activePoints.clear()
                activePath.reset()
                if (activeStrokeId.isNotEmpty()) {
                    onStrokeBroadcastEnd(activeStrokeId)
                    activeStrokeId = ""
                }
                laserPoints.clear()
                requestInvalidate()
                return true
            }

            // Finish moving elements if active
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

            // Complete active stroke
            if (activePoints.isNotEmpty()) {
                if (activeToolUsed != DrawingTool.LASER) {
                    val isCurved = activeToolUsed == DrawingTool.PEN || activeToolUsed == DrawingTool.MARKER ||
                            activeToolUsed == DrawingTool.BRUSH || activeToolUsed == DrawingTool.PENCIL ||
                            activeToolUsed == DrawingTool.CALLIGRAPHY ||
                            activeToolUsed == DrawingTool.HIGHLIGHTER || activeToolUsed == DrawingTool.ERASER

                    val finalPoints = if (isCurved) {
                        PathUtils.smoothAndSimplifyPoints(activePoints)
                    } else {
                        listOf(activePoints.first(), activePoints.last())
                    }

                    if (finalPoints.isNotEmpty()) {
                        val finalStroke = Stroke(
                            id = activeStrokeId,
                            points = finalPoints,
                            color = currentColor.toArgb().toLong(),
                            strokeWidth = currentWidth / zoom,
                            tool = activeToolUsed,
                            isEraser = activeToolUsed == DrawingTool.ERASER
                        )
                        pendingStroke = finalStroke
                        onStrokeCompleted(finalStroke)
                    }
                }
                if (activeStrokeId.isNotEmpty()) {
                    onStrokeBroadcastEnd(activeStrokeId)
                    activeStrokeId = ""
                }
                activePoints.clear()
                activePath.reset()
            }
            requestInvalidate()
            return true
        }

        if (isPinching) {
            return true
        }

        // Determine target pointer index for drawing/interaction
        val targetIndex = if (hasStylusPointer) {
            activeStylusIndex
        } else if (activeDrawingPointerId != -1L) {
            val idx = event.pointers.indexOfFirst { it.id == activeDrawingPointerId }
            if (idx != -1) idx else 0
        } else {
            0
        }

        val worldPoint = StylusInputHandler.extractPoint(event, panOffset, zoom, targetIndex)
        if (isReadOnly) return true

        // 6. Text Tool
        if (effectiveTool == DrawingTool.TEXT) {
            if (event.action == CanvasPointerAction.UP) {
                val effectiveWidth = if (viewWidth > 0f) viewWidth else 1080f
                val edgeGutter = 28f * density
                if (event.x > edgeGutter && event.x < (effectiveWidth - edgeGutter)) {
                    onTapForText(worldPoint)
                }
            }
            return true
        }

        // 7. Erase Tool (Stroke / Text / Image / Sticky Note Eraser)
        if (effectiveTool == DrawingTool.ERASER || effectiveTool == DrawingTool.SELECT_ERASE) {
            if (event.action == CanvasPointerAction.DOWN || event.action == CanvasPointerAction.MOVE) {
                val hitRadius = (32f / zoom)
                val hitRadiusSq = hitRadius * hitRadius

                val hitStroke = strokes.findLast { s ->
                    if (s.points.isEmpty()) false
                    else if (s.points.size == 1) {
                        val dx = s.points[0].x - worldPoint.x
                        val dy = s.points[0].y - worldPoint.y
                        (dx * dx + dy * dy) <= hitRadiusSq
                    } else {
                        var hit = false
                        for (i in 0 until s.points.size - 1) {
                            if (distanceSqToSegment(worldPoint, s.points[i], s.points[i + 1]) <= hitRadiusSq) {
                                hit = true
                                break
                            }
                        }
                        hit
                    }
                }
                if (hitStroke != null) {
                    geometryCache.removeStroke(hitStroke.id)
                    if (hapticsEnabled) drawingHaptics.performEraserHit()
                    onDeleteStroke(hitStroke.id)
                    requestInvalidate()
                    return true
                }

                val hitText = textElements.findLast { t ->
                    val dx = t.x - worldPoint.x
                    val dy = t.y - worldPoint.y
                    (dx * dx + dy * dy) <= (hitRadiusSq * 3f)
                }
                if (hitText != null) {
                    if (hapticsEnabled) drawingHaptics.performEraserHit()
                    onDeleteText(hitText.id)
                    requestInvalidate()
                    return true
                }

                val hitImage = imageElements.findLast { im ->
                    worldPoint.x in im.x..(im.x + im.width) && worldPoint.y in im.y..(im.y + im.height)
                }
                if (hitImage != null) {
                    if (hapticsEnabled) drawingHaptics.performEraserHit()
                    onDeleteImage(hitImage.id)
                    requestInvalidate()
                    return true
                }

                val hitSticky = stickyNotes.findLast { n ->
                    worldPoint.x in n.x..(n.x + n.width) && worldPoint.y in n.y..(n.y + n.height)
                }
                if (hitSticky != null) {
                    if (hapticsEnabled) drawingHaptics.performEraserHit()
                    onDeleteStickyNote(hitSticky.id)
                    requestInvalidate()
                    return true
                }
            }
            return true
        }

        // 8. Selection Tools (Select & Move, Rect Select, Lasso Select)
        val isSelectionTool = effectiveTool == DrawingTool.SELECT ||
                effectiveTool == DrawingTool.SELECT_MOVE ||
                effectiveTool == DrawingTool.RECT_SELECT ||
                effectiveTool == DrawingTool.LASSO_SELECT

        if (isSelectionTool) {
            val hitRadius = (32f / zoom)
            val hitRadiusSq = hitRadius * hitRadius

            when (event.action) {
                CanvasPointerAction.DOWN -> {
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

                    val isTappingSelected = (hitStroke != null && selectedStrokeIds.contains(hitStroke.id)) ||
                            (hitText != null && selectedTextIds.contains(hitText.id)) ||
                            (hitImage != null && selectedImageIds.contains(hitImage.id))

                    if (isTappingSelected || ((selectedStrokeIds.isNotEmpty() || selectedTextIds.isNotEmpty() || selectedImageIds.isNotEmpty()) && (hitStroke != null || hitText != null || hitImage != null))) {
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

                        if (effectiveTool == DrawingTool.LASSO_SELECT) {
                            lassoPoints.clear()
                            lassoPoints.add(worldPoint)
                            lassoPath.reset()
                            lassoPath.moveTo(worldPoint.x, worldPoint.y)
                        } else {
                            marqueeStart = worldPoint
                            marqueeRect = Rect(worldPoint.x, worldPoint.y, worldPoint.x, worldPoint.y)
                        }
                    }
                    notifySelectionChanged(strokes, textElements, imageElements)
                    requestInvalidate()
                    return true
                }
                CanvasPointerAction.MOVE -> {
                    if (isMovingSelection) {
                        val dx = worldPoint.x - lastMovePoint.x
                        val dy = worldPoint.y - lastMovePoint.y

                        for (i in movingStrokes.indices) {
                            val strk = movingStrokes[i]
                            val updatedPoints = strk.points.map { p -> p.copy(x = p.x + dx, y = p.y + dy) }
                            movingStrokes[i] = strk.copy(points = updatedPoints)
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
                    } else if (effectiveTool == DrawingTool.LASSO_SELECT) {
                        lassoPoints.add(worldPoint)
                        lassoPath.lineTo(worldPoint.x, worldPoint.y)
                        if (lassoPoints.size > 2) {
                            selectedStrokeIds.clear()
                            strokes.forEach { s ->
                                if (PathUtils.isStrokeInPolygon(s.points, lassoPoints)) selectedStrokeIds.add(s.id)
                            }
                            selectedTextIds.clear()
                            textElements.forEach { t ->
                                if (PathUtils.isPointInPolygon(Point(t.x, t.y), lassoPoints)) selectedTextIds.add(t.id)
                            }
                            selectedImageIds.clear()
                            imageElements.forEach { im ->
                                if (PathUtils.isPointInPolygon(Point(im.x, im.y), lassoPoints)) selectedImageIds.add(im.id)
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
                                if (s.points.any { p -> rect.contains(Offset(p.x, p.y)) }) selectedStrokeIds.add(s.id)
                            }
                            selectedTextIds.clear()
                            textElements.forEach { t ->
                                if (rect.contains(Offset(t.x, t.y))) selectedTextIds.add(t.id)
                            }
                            selectedImageIds.clear()
                            imageElements.forEach { im ->
                                if (rect.overlaps(Rect(im.x, im.y, im.x + im.width, im.y + im.height))) selectedImageIds.add(im.id)
                            }
                        }
                    }
                    notifySelectionChanged(strokes, textElements, imageElements)
                    requestInvalidate()
                    return true
                }
                CanvasPointerAction.UP, CanvasPointerAction.CANCEL -> {
                    if (isMovingSelection) {
                        isMovingSelection = false
                        onElementsMoved(
                            movingStrokes.toList(),
                            movingTexts.toList(),
                            movingImages.toList()
                        )
                        movingStrokes.clear()
                        movingTexts.clear()
                        movingImages.clear()
                    }
                    marqueeStart = null
                    marqueeRect = null
                    lassoPoints.clear()
                    lassoPath.reset()
                    requestInvalidate()
                    return true
                }
                else -> {}
            }
            return true
        }

        // 9. Standard Inking / Drawing Tools & Laser Pointer
        when (event.action) {
            CanvasPointerAction.DOWN -> {
                activeDrawingPointerId = event.pointers.getOrNull(targetIndex)?.id ?: 0L
                activeStrokeIsStylus = hasStylusPointer

                activeToolUsed = effectiveTool
                activeStrokeId = "${userSessionId}_${UUID.randomUUID()}"
                activePoints.clear()
                activePoints.add(worldPoint)
                activePath.reset()

                val isCurved = activeToolUsed == DrawingTool.PEN || activeToolUsed == DrawingTool.PENCIL ||
                        activeToolUsed == DrawingTool.CALLIGRAPHY || activeToolUsed == DrawingTool.MARKER ||
                        activeToolUsed == DrawingTool.BRUSH || activeToolUsed == DrawingTool.HIGHLIGHTER ||
                        activeToolUsed == DrawingTool.ERASER || activeToolUsed == DrawingTool.LASER

                if (isCurved) {
                    activePath.moveTo(worldPoint.x, worldPoint.y)
                }

                if (activeToolUsed == DrawingTool.LASER) {
                    val now = System.currentTimeMillis()
                    laserPoints.removeAll { now - it.timestampMs > 1200L }
                    laserPoints.add(worldPoint)
                    laserActiveToken++
                } else {
                    drawingHaptics.onStrokeStart(event.pointers[targetIndex].x, event.pointers[targetIndex].y, event.eventTime)
                    onStrokeBroadcastStart(
                        activeStrokeId,
                        currentColor.toArgb().toLong(),
                        currentWidth / zoom,
                        activeToolUsed == DrawingTool.ERASER,
                        worldPoint,
                        activeToolUsed
                    )
                }
                requestInvalidate()
                return true
            }
            CanvasPointerAction.MOVE -> {
                val isCurved = activeToolUsed == DrawingTool.PEN || activeToolUsed == DrawingTool.PENCIL ||
                        activeToolUsed == DrawingTool.CALLIGRAPHY || activeToolUsed == DrawingTool.MARKER ||
                        activeToolUsed == DrawingTool.BRUSH || activeToolUsed == DrawingTool.HIGHLIGHTER ||
                        activeToolUsed == DrawingTool.ERASER || activeToolUsed == DrawingTool.LASER

                if (activeToolUsed == DrawingTool.LASER) {
                    val now = System.currentTimeMillis()
                    laserPoints.add(worldPoint)
                    laserPoints.removeAll { now - it.timestampMs > 1200L }
                    laserActiveToken++
                } else {
                    drawingHaptics.onStrokeMove(event.pointers[targetIndex].x, event.pointers[targetIndex].y, event.eventTime, hapticsEnabled)
                }

                if (activePoints.isEmpty()) {
                    return false
                }

                // Ingest sub-frame micro-points from high-refresh hardware digitizer (120Hz-480Hz)
                val historicalPoints = StylusInputHandler.extractHistoricalPoints(event, panOffset, zoom, targetIndex)
                for (histPt in historicalPoints) {
                    if (isCurved) {
                        val lastPt = activePoints.last()
                        val midX = (lastPt.x + histPt.x) / 2f
                        val midY = (lastPt.y + histPt.y) / 2f
                        activePath.quadraticTo(lastPt.x, lastPt.y, midX, midY)
                    }
                    activePoints.add(histPt)
                    if (activeToolUsed != DrawingTool.LASER) {
                        onStrokeBroadcastPoint(activeStrokeId, histPt)
                    }
                }

                if (isCurved) {
                    val lastPt = activePoints.last()
                    val midX = (lastPt.x + worldPoint.x) / 2f
                    val midY = (lastPt.y + worldPoint.y) / 2f
                    activePath.quadraticTo(lastPt.x, lastPt.y, midX, midY)
                    activePoints.add(worldPoint)
                } else {
                    activePoints.add(worldPoint)
                    activePath.reset()
                    activePath.addPath(PathUtils.createToolPath(activeToolUsed, listOf(activePoints.first(), worldPoint)))
                }

                if (activeToolUsed != DrawingTool.LASER) {
                    onStrokeBroadcastPoint(activeStrokeId, worldPoint)
                }
                requestInvalidate()
                return true
            }
            else -> {}
        }

        return false
    }

    private fun notifySelectionChanged(
        strokes: List<Stroke>,
        textElements: List<TextElement>,
        imageElements: List<ImageElement>
    ) {
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

        if (minX < Float.MAX_VALUE && maxX > Float.MIN_VALUE) {
            val bounds = Rect(minX - 16f, minY - 16f, maxX + 16f, maxY + 16f)
            onSelectionChanged(selectedStrokeIds.toList(), selectedTextIds.toList(), bounds)
        } else {
            onSelectionChanged(selectedStrokeIds.toList(), selectedTextIds.toList(), null)
        }
    }
}
