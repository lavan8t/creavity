package com.kd.creavity.presentation.canvas

import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasExportTest {

    @Test
    fun testComputeBounds() {
        val strokes = listOf(
            Stroke(
                tool = DrawingTool.RECTANGLE,
                points = listOf(Point(100f, 100f), Point(300f, 200f))
            )
        )
        val texts = listOf(
            TextElement(x = 50f, y = 50f, text = "Header")
        )

        val bounds = CanvasExportManager.computeBounds(strokes, texts, emptyList(), padding = 20f)
        assertEquals(30f, bounds.minX, 0.01f)
        assertEquals(30f, bounds.minY, 0.01f)
        assertTrue(bounds.maxX >= 320f)
        assertTrue(bounds.maxY >= 220f)
    }

    @Test
    fun testExportSvgStructure() {
        val stroke = Stroke(
            id = "shape-1",
            tool = DrawingTool.DIAMOND,
            points = listOf(Point(100f, 100f), Point(300f, 250f)),
            fillStyle = FillStyle.SEMI_TRANSPARENT,
            roughness = RoughnessStyle.SKETCHY,
            embeddedText = "Decision Step"
        )
        val note = StickyNote(
            id = "note-1",
            x = 400f,
            y = 100f,
            width = 150f,
            height = 100f,
            text = "Follow up"
        )
        val text = TextElement(
            id = "txt-1",
            x = 100f,
            y = 400f,
            text = "System Overview"
        )

        val svg = CanvasExportManager.exportToSvg(
            strokes = listOf(stroke),
            texts = listOf(text),
            stickyNotes = listOf(note),
            isTransparentBg = false,
            backgroundColor = 0xFFFFFFFF
        )

        // 1. Root tag and namespace
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\""))
        assertTrue(svg.contains("viewBox="))
        assertTrue(svg.trimEnd().endsWith("</svg>"))

        // 2. Background rect
        assertTrue(svg.contains("<rect x="))

        // 3. Diamond path
        assertTrue(svg.contains("<path d=\"M"))

        // 4. Embedded text
        assertTrue(svg.contains("Decision Step"))

        // 5. Sticky note and free text
        assertTrue(svg.contains("Follow up"))
        assertTrue(svg.contains("System Overview"))
    }

    @Test
    fun testTransparentBackgroundOption() {
        val stroke = Stroke(
            tool = DrawingTool.LINE,
            points = listOf(Point(0f, 0f), Point(100f, 100f))
        )
        val svgTransparent = CanvasExportManager.exportToSvg(
            strokes = listOf(stroke),
            isTransparentBg = true
        )

        assertFalse("Transparent SVG must not render background rect", svgTransparent.contains("fill=\"#FFFFFF\""))
    }

    @Test
    fun testXmlCharacterEscaping() {
        assertEquals("&amp;", CanvasExportManager.escapeXml("&"))
        assertEquals("&lt;test&gt;", CanvasExportManager.escapeXml("<test>"))
        assertEquals("&quot;hello&quot;", CanvasExportManager.escapeXml("\"hello\""))
        assertEquals("&apos;quote&apos;", CanvasExportManager.escapeXml("'quote'"))
    }
}
