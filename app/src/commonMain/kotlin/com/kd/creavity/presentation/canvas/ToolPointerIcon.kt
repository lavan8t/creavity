package com.kd.creavity.presentation.canvas

import androidx.compose.ui.input.pointer.PointerIcon
import com.kd.creavity.domain.model.DrawingTool

/**
 * Platform-specific provider for custom, professional tool cursors (Pen, Eraser,
 * Highlighter, Shapes, Text, Sticky Note, Laser, Select).
 */
expect fun getToolPointerIcon(tool: DrawingTool): PointerIcon

/**
 * Platform-specific mechanism to dynamically enforce the active tool cursor at the OS / window level.
 */
expect fun applyToolCursor(tool: DrawingTool)

/**
 * Resets the window / OS cursor back to default system arrow cursor when exiting canvas.
 */
expect fun resetToolCursor()
