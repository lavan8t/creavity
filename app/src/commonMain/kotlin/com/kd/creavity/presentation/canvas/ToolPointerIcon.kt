package com.kd.creavity.presentation.canvas

import androidx.compose.ui.input.pointer.PointerIcon
import com.kd.creavity.domain.model.DrawingTool

/**
 * Platform-specific provider for custom, professional tool cursors (Pen, Eraser,
 * Highlighter, Shapes, Text, Sticky Note, Laser, Select).
 */
expect fun getToolPointerIcon(tool: DrawingTool): PointerIcon
