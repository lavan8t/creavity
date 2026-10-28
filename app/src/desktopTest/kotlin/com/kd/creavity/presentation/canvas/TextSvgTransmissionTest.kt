package com.kd.creavity.presentation.canvas

import com.kd.creavity.data.binary.PacketSerializer
import com.kd.creavity.data.binary.WhiteboardPacket
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.util.TextSvgGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.graphics.vector.PathParser

class TextSvgTransmissionTest {

    @Test
    fun testFontAvailability() {
        assertTrue(TextSvgGenerator.isFontAvailable("Google Sans Flex"))
        assertTrue(TextSvgGenerator.isFontAvailable("Sans-Serif"))
        assertTrue(TextSvgGenerator.isFontAvailable("Serif"))
        assertTrue(TextSvgGenerator.isFontAvailable("Monospace"))
        assertTrue(TextSvgGenerator.isFontAvailable("Cursive"))

        assertFalse(TextSvgGenerator.isFontAvailable("CompletelyUnknownFont_ABC_999"))
    }

    @Test
    fun testSvgDataGenerationAndParsing() {
        val text = "Collaborative Whiteboard"
        val svgData = TextSvgGenerator.generateSvgData(text, "Sans-Serif", 28f)
        assertNotNull("Generated SVG path data should not be null", svgData)
        assertTrue("SVG path should start with M command", svgData!!.startsWith("M"))
        assertTrue("SVG path should contain Z closing command", svgData.contains("Z"))

        // Verify that Compose PathParser can parse it into a Path
        val parser = PathParser()
        parser.parsePathString(svgData)
        val path = parser.toPath()
        assertNotNull("Parsed Compose path should not be null", path)
    }

    @Test
    fun testPacketSerializationWithSvgData() {
        val originalSvg = "M 0 0 L 10 10 Q 20 20 30 10 Z"
        val originalText = TextElement(
            id = "txt-custom-font-1",
            text = "Mathematical Equation",
            x = 120f,
            y = 240f,
            color = 0xFF2196F3,
            fontSize = 32f,
            fontName = "CustomExoticFont",
            svgData = originalSvg
        )

        // Test TextAdd packet serialization & deserialization
        val packet = WhiteboardPacket.TextAdd(originalText)
        val bytes = PacketSerializer.serialize(packet)
        val deserializedPacket = PacketSerializer.deserialize(bytes) as WhiteboardPacket.TextAdd

        assertEquals(originalText.id, deserializedPacket.textElement.id)
        assertEquals(originalText.text, deserializedPacket.textElement.text)
        assertEquals(originalText.fontName, deserializedPacket.textElement.fontName)
        assertEquals(originalText.fontSize, deserializedPacket.textElement.fontSize, 0.001f)
        assertEquals(originalSvg, deserializedPacket.textElement.svgData)
    }

    @Test
    fun testSyncResponsePacketWithSvgData() {
        val originalSvg = "M 5 5 L 25 25 Z"
        val textElem = TextElement(
            id = "txt-sync-1",
            text = "Architecture Spec",
            x = 50f,
            y = 60f,
            color = 0xFF000000,
            fontSize = 20f,
            fontName = "RareCustomScript",
            svgData = originalSvg
        )

        val syncResponse = WhiteboardPacket.SyncResponse(
            strokes = emptyList(),
            textElements = listOf(textElem)
        )
        val bytes = PacketSerializer.serialize(syncResponse)
        val deserialized = PacketSerializer.deserialize(bytes) as WhiteboardPacket.SyncResponse

        assertEquals(1, deserialized.textElements.size)
        val restored = deserialized.textElements.first()
        assertEquals("txt-sync-1", restored.id)
        assertEquals("Architecture Spec", restored.text)
        assertEquals(originalSvg, restored.svgData)
    }

    @Test
    fun testFontFallbackLogicWhenFontUnavailable() {
        val peerText = TextElement(
            id = "peer-text-1",
            text = "Peer Note",
            fontName = "UnavailableFontFamily_XYZ",
            svgData = "M 0 0 Z"
        )

        // 1. Peer font is not available locally
        val isAvail = TextSvgGenerator.isFontAvailable(peerText.fontName)
        assertFalse("Peer's exotic font should not be available locally", isAvail)

        // 2. Editing will fall back to system available font
        val editingFontName = if (TextSvgGenerator.isFontAvailable(peerText.fontName)) {
            peerText.fontName
        } else {
            "Google Sans Flex"
        }
        assertEquals("Google Sans Flex", editingFontName)
        assertTrue(TextSvgGenerator.isFontAvailable(editingFontName))
    }
}
