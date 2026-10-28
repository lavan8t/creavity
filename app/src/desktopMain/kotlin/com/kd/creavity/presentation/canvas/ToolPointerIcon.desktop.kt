package com.kd.creavity.presentation.canvas

import androidx.compose.ui.input.pointer.PointerIcon
import com.kd.creavity.domain.model.DrawingTool
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Cursor
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage

private val cursorCache = mutableMapOf<DrawingTool, PointerIcon>()

actual fun getToolPointerIcon(tool: DrawingTool): PointerIcon {
    return cursorCache.getOrPut(tool) {
        createCustomToolCursor(tool)
    }
}

private fun createCustomToolCursor(tool: DrawingTool): PointerIcon {
    return runCatching {
        val toolkit = Toolkit.getDefaultToolkit()
        when (tool) {
            DrawingTool.PEN,
            DrawingTool.PENCIL,
            DrawingTool.MARKER,
            DrawingTool.BRUSH,
            DrawingTool.CALLIGRAPHY -> {
                val img = BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB)
                val g = img.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                
                // Angled pen nib pointing to (1, 1)
                val body = Path2D.Float()
                body.moveTo(1f, 1f)
                body.lineTo(7f, 2f)
                body.lineTo(16f, 11f)
                body.lineTo(11f, 16f)
                body.lineTo(2f, 7f)
                body.closePath()

                // High-contrast white border
                g.color = Color.WHITE
                g.stroke = BasicStroke(2.5f)
                g.draw(body)

                // Dark slate pen body
                g.color = Color(0xFF1E293B.toInt())
                g.fill(body)

                // Golden metallic nib tip
                val nib = Path2D.Float()
                nib.moveTo(1f, 1f)
                nib.lineTo(4f, 2f)
                nib.lineTo(2f, 4f)
                nib.closePath()
                g.color = Color(0xFFF59E0B.toInt())
                g.fill(nib)

                g.dispose()
                PointerIcon(toolkit.createCustomCursor(img, Point(1, 1), "PenCursor"))
            }

            DrawingTool.HIGHLIGHTER -> {
                val img = BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB)
                val g = img.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Wide chisel marker pointing to (1, 1)
                val body = Path2D.Float()
                body.moveTo(1f, 1f)
                body.lineTo(6f, 1f)
                body.lineTo(15f, 10f)
                body.lineTo(10f, 15f)
                body.lineTo(1f, 6f)
                body.closePath()

                g.color = Color.WHITE
                g.stroke = BasicStroke(2.5f)
                g.draw(body)

                g.color = Color(0xFFEAB308.toInt()) // Vibrant yellow
                g.fill(body)

                val tip = Path2D.Float()
                tip.moveTo(1f, 1f)
                tip.lineTo(4f, 1f)
                tip.lineTo(1f, 4f)
                tip.closePath()
                g.color = Color(0xFF1E293B.toInt())
                g.fill(tip)

                g.dispose()
                PointerIcon(toolkit.createCustomCursor(img, Point(1, 1), "HighlighterCursor"))
            }

            DrawingTool.ERASER,
            DrawingTool.SELECT_ERASE -> {
                val img = BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB)
                val g = img.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Clean angled eraser block
                val eraser = RoundRectangle2D.Float(2f, 2f, 14f, 14f, 4f, 4f)

                g.color = Color.WHITE
                g.stroke = BasicStroke(2.5f)
                g.draw(eraser)

                g.color = Color(0xFFFFB5BA.toInt()) // Classic eraser pink
                g.fill(eraser)

                g.color = Color(0xFFE11D48.toInt())
                g.drawLine(3, 9, 15, 9)

                g.dispose()
                PointerIcon(toolkit.createCustomCursor(img, Point(9, 9), "EraserCursor"))
            }

            DrawingTool.LASER -> {
                val img = BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)
                val g = img.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Outer red halo
                g.color = Color(0x66EF4444)
                g.fillOval(2, 2, 16, 16)

                // Sharp white ring
                g.color = Color.WHITE
                g.stroke = BasicStroke(1.5f)
                g.drawOval(4, 4, 12, 12)

                // Center laser red dot
                g.color = Color(0xFFDC2626.toInt())
                g.fillOval(7, 7, 6, 6)

                g.dispose()
                PointerIcon(toolkit.createCustomCursor(img, Point(10, 10), "LaserCursor"))
            }

            DrawingTool.STICKY_NOTE -> {
                val img = BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB)
                val g = img.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                val note = Path2D.Float()
                note.moveTo(2f, 2f)
                note.lineTo(14f, 2f)
                note.lineTo(18f, 6f)
                note.lineTo(18f, 18f)
                note.lineTo(2f, 18f)
                note.closePath()

                g.color = Color.WHITE
                g.stroke = BasicStroke(2.5f)
                g.draw(note)

                g.color = Color(0xFFFEF08A.toInt()) // Post-it yellow
                g.fill(note)

                g.color = Color(0xFFCA8A04.toInt())
                g.drawLine(14, 2, 14, 6)
                g.drawLine(14, 6, 18, 6)

                g.dispose()
                PointerIcon(toolkit.createCustomCursor(img, Point(2, 2), "StickyNoteCursor"))
            }

            DrawingTool.LINE,
            DrawingTool.ARROW,
            DrawingTool.RECTANGLE,
            DrawingTool.CIRCLE,
            DrawingTool.TRIANGLE,
            DrawingTool.DIAMOND,
            DrawingTool.STAR -> {
                PointerIcon(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
            }

            DrawingTool.TEXT -> {
                PointerIcon(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
            }

            DrawingTool.SELECT,
            DrawingTool.SELECT_MOVE,
            DrawingTool.RECT_SELECT,
            DrawingTool.LASSO_SELECT -> {
                PointerIcon(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
            }
        }
    }.getOrDefault(PointerIcon.Default)
}
