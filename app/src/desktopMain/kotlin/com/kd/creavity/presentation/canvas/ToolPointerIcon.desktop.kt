package com.kd.creavity.presentation.canvas

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.input.pointer.PointerIcon
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.symbols.generated.*
import java.awt.Point
import java.awt.Toolkit

private val cursorCache = mutableMapOf<DrawingTool, PointerIcon>()

actual fun getToolPointerIcon(tool: DrawingTool): PointerIcon {
    return cursorCache.getOrPut(tool) {
        createMaterialToolCursor(tool)
    }
}

private fun createMaterialToolCursor(tool: DrawingTool): PointerIcon {
    return runCatching {
        val toolkit = Toolkit.getDefaultToolkit()
        val spec = when (tool) {
            DrawingTool.PEN ->
                CursorSpec(draw, Point(4, 21), Color(0xFF1E293B), Color.White)
            DrawingTool.PENCIL,
            DrawingTool.MARKER,
            DrawingTool.CALLIGRAPHY ->
                CursorSpec(edit, Point(4, 21), Color(0xFF0284C7), Color.White)
            DrawingTool.HIGHLIGHTER ->
                CursorSpec(ink_highlighter, Point(2, 21), Color(0xFFFACC15), Color(0xFF0F172A))
            DrawingTool.BRUSH ->
                CursorSpec(brush, Point(5, 20), Color(0xFF8B5CF6), Color.White)
            DrawingTool.ERASER ->
                CursorSpec(ink_eraser, Point(4, 20), Color(0xFFFB7185), Color(0xFF0F172A))
            DrawingTool.SELECT_ERASE ->
                CursorSpec(highlight_off, Point(12, 12), Color(0xFFE11D48), Color.White)
            DrawingTool.LASER ->
                CursorSpec(flare, Point(12, 12), Color(0xFFEF4444), Color.White)
            DrawingTool.STICKY_NOTE ->
                CursorSpec(sticky_note_2, Point(3, 3), Color(0xFFFEF08A), Color(0xFF0F172A))
            DrawingTool.LINE ->
                CursorSpec(arrow_right_alt, Point(12, 12), Color.White, Color(0xFF0F172A))
            DrawingTool.ARROW ->
                CursorSpec(arrow_forward, Point(21, 12), Color.White, Color(0xFF0F172A))
            DrawingTool.RECTANGLE ->
                CursorSpec(rectangle, Point(12, 12), Color.White, Color(0xFF0F172A))
            DrawingTool.CIRCLE ->
                CursorSpec(circle, Point(12, 12), Color.White, Color(0xFF0F172A))
            DrawingTool.TRIANGLE ->
                CursorSpec(change_history, Point(12, 4), Color.White, Color(0xFF0F172A))
            DrawingTool.DIAMOND ->
                CursorSpec(diamond, Point(12, 12), Color.White, Color(0xFF0F172A))
            DrawingTool.STAR ->
                CursorSpec(star, Point(12, 12), Color.White, Color(0xFF0F172A))
            DrawingTool.TEXT ->
                CursorSpec(title, Point(12, 4), Color.White, Color(0xFF0F172A))
            DrawingTool.SELECT,
            DrawingTool.SELECT_MOVE ->
                CursorSpec(pan_tool_alt, Point(8, 2), Color.White, Color(0xFF0F172A))
            DrawingTool.RECT_SELECT ->
                CursorSpec(crop_free, Point(12, 12), Color(0xFF38BDF8), Color(0xFF0F172A))
            DrawingTool.LASSO_SELECT ->
                CursorSpec(gesture, Point(12, 12), Color(0xFF38BDF8), Color(0xFF0F172A))
        }

        val path = spec.icon.toComposePath()
        val bitmap = ImageBitmap(32, 32)
        val canvas = Canvas(bitmap)

        val strokePaint = Paint().apply {
            color = spec.strokeColor
            style = PaintingStyle.Stroke
            strokeWidth = 2.5f
        }
        val fillPaint = Paint().apply {
            color = spec.fillColor
            style = PaintingStyle.Fill
        }

        // Draw dual-contrast border then fill for optimal visibility on all canvases
        canvas.drawPath(path, strokePaint)
        canvas.drawPath(path, fillPaint)

        val awtImg = bitmap.toAwtImage()
        PointerIcon(toolkit.createCustomCursor(awtImg, spec.hotspot, "${tool.name}MaterialCursor"))
    }.getOrDefault(PointerIcon.Default)
}

private data class CursorSpec(
    val icon: ImageVector,
    val hotspot: Point,
    val fillColor: Color,
    val strokeColor: Color
)

private fun ImageVector.toComposePath(): Path {
    val path = Path()
    val parser = PathParser()
    fun extract(group: VectorGroup) {
        for (node in group) {
            when (node) {
                is VectorPath -> parser.addPathNodes(node.pathData)
                is VectorGroup -> extract(node)
            }
        }
    }
    extract(root)
    parser.toPath(path)
    return path
}
