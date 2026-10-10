package com.kd.brevity.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke as DrawScopeStroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.kd.brevity.domain.model.Board
import com.kd.brevity.presentation.canvas.CanvasColorUtils

@Composable
fun BoardThumbnailPreview(
    board: Board,
    modifier: Modifier = Modifier
) {
    val page = board.pages.getOrNull(board.activePageIndex) ?: board.pages.firstOrNull()
    val strokes = page?.strokes ?: emptyList()
    val stickyNotes = page?.stickyNotes ?: emptyList()

    Box(
        modifier = modifier
            .background(Color(board.backgroundColor)),
        contentAlignment = Alignment.Center
    ) {
        if (strokes.isEmpty() && stickyNotes.isEmpty()) {
            MaterialSymbol(
                name = "brush",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                size = 28.dp
            )
        } else {
            // Precompute bounds and paths once per board version outside draw pass
            val thumbnailData = remember(board.id, board.modifiedAt, strokes.size, stickyNotes.size) {
                var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
                var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
                val previewStrokes = strokes.take(40)
                for (s in previewStrokes) {
                    for (pt in s.points) {
                        minX = minOf(minX, pt.x); minY = minOf(minY, pt.y)
                        maxX = maxOf(maxX, pt.x); maxY = maxOf(maxY, pt.y)
                    }
                }
                for (sn in stickyNotes) {
                    minX = minOf(minX, sn.x); minY = minOf(minY, sn.y)
                    maxX = maxOf(maxX, sn.x + sn.width); maxY = maxOf(maxY, sn.y + sn.height)
                }
                if (minX == Float.MAX_VALUE) { minX = 0f; minY = 0f; maxX = 1920f; maxY = 1080f }
                val spanX = (maxX - minX).coerceAtLeast(1f)
                val spanY = (maxY - minY).coerceAtLeast(1f)
                val isDarkBg = Color(board.backgroundColor).luminance() < 0.5f
                val paths = previewStrokes.mapNotNull { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                        Triple(path, CanvasColorUtils.getAdaptiveStrokeColor(Color(stroke.color), isDarkBg), stroke.strokeWidth)
                    } else null
                }
                val notes = stickyNotes.map { floatArrayOf(it.x, it.y, it.width, it.height, it.color.toFloat()) }
                Triple(floatArrayOf(minX, minY, spanX, spanY), paths, notes)
            }

            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val (bounds, paths, notes) = thumbnailData
                val minX = bounds[0]
                val minY = bounds[1]
                val spanX = bounds[2]
                val spanY = bounds[3]
                val scale = minOf(size.width / spanX, size.height / spanY).coerceIn(0.01f, 1.0f)
                val offsetX = (size.width - spanX * scale) / 2f
                val offsetY = (size.height - spanY * scale) / 2f

                // Draw sticky notes preview
                notes.forEach { noteData ->
                    val nx = (noteData[0] - minX) * scale + offsetX
                    val ny = (noteData[1] - minY) * scale + offsetY
                    val nw = (noteData[2] * scale).coerceAtLeast(4f)
                    val nh = (noteData[3] * scale).coerceAtLeast(4f)
                    drawRoundRect(
                        color = Color(noteData[4].toLong()),
                        topLeft = Offset(nx, ny),
                        size = androidx.compose.ui.geometry.Size(nw, nh),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }

                // Draw pre-cached stroke paths with GPU translate/scale
                withTransform({
                    translate(offsetX - minX * scale, offsetY - minY * scale)
                    scale(scale, scale, pivot = Offset.Zero)
                }) {
                    paths.forEach { (path, color, strokeWidth) ->
                        drawPath(
                            path = path,
                            color = color,
                            style = DrawScopeStroke(
                                width = strokeWidth.coerceAtLeast(1.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
    }
}
