package com.kd.creavity.presentation.canvas

import androidx.compose.ui.input.pointer.PointerIcon
import com.kd.creavity.domain.model.DrawingTool

actual fun getToolPointerIcon(tool: DrawingTool): PointerIcon = when (tool) {
    DrawingTool.PEN,
    DrawingTool.PENCIL,
    DrawingTool.MARKER,
    DrawingTool.BRUSH,
    DrawingTool.CALLIGRAPHY,
    DrawingTool.HIGHLIGHTER,
    DrawingTool.ERASER,
    DrawingTool.SELECT_ERASE,
    DrawingTool.LINE,
    DrawingTool.ARROW,
    DrawingTool.RECTANGLE,
    DrawingTool.CIRCLE,
    DrawingTool.TRIANGLE,
    DrawingTool.DIAMOND,
    DrawingTool.CYLINDER,
    DrawingTool.STAR,
    DrawingTool.LASER -> PointerIcon.Crosshair
    DrawingTool.TEXT -> PointerIcon.Text
    DrawingTool.SELECT,
    DrawingTool.SELECT_MOVE,
    DrawingTool.RECT_SELECT,
    DrawingTool.LASSO_SELECT,
    DrawingTool.STICKY_NOTE -> PointerIcon.Default
}
