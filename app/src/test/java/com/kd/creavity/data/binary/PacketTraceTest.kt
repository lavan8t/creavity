package com.kd.creavity.data.binary

import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PacketTraceTest {

    private val lines = mutableListOf<String>()
    private val originalWrite = PacketTrace.writeLine
    private var originalEnabled = false

    @Before
    fun setUp() {
        originalEnabled = PacketTrace.isEnabled
        PacketTrace.writeLine = { lines += it }
        PacketTrace.isEnabled = true
    }

    @After
    fun tearDown() {
        PacketTrace.writeLine = originalWrite
        PacketTrace.isEnabled = originalEnabled
    }

    private val start = WhiteboardPacket.StrokeStart(
        strokeId = "stroke-7",
        color = 0xFF000000L,
        strokeWidth = 6f,
        isEraser = false,
        point = Point(1f, 2f, 1f, 3L),
        tool = DrawingTool.PEN
    )

    @Test
    fun `trace reports direction, packet type, wire size and stroke id`() {
        val bytes = PacketSerializer.serialize(start)
        PacketSerializer.deserialize(bytes)?.let { }

        assertEquals(2, lines.size)
        val send = lines[0]
        assertTrue(send, send.contains("SEND"))
        assertTrue(send, send.contains("StrokeStart"))
        assertTrue(send, send.contains(" ${bytes.size} "))
        assertTrue(send, send.contains("stroke-7"))

        assertTrue(lines[1], lines[1].contains("RECEIVE"))
    }

    @Test
    fun `trace stays silent while disabled`() {
        PacketTrace.isEnabled = false

        PacketSerializer.serialize(start)

        assertEquals(0, lines.size)
    }
}
