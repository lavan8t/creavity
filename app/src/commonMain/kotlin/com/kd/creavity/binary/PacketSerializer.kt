package com.kd.creavity.data.binary

import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.domain.model.ViewportBounds
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

sealed class WhiteboardPacket {
    data class StrokeStart(
        val strokeId: String,
        val color: Long,
        val strokeWidth: Float,
        val isEraser: Boolean,
        val point: Point,
        val tool: DrawingTool = DrawingTool.PEN
    ) : WhiteboardPacket()

    data class StrokePoint(
        val strokeId: String,
        val point: Point
    ) : WhiteboardPacket()

    data class StrokeEnd(val strokeId: String) : WhiteboardPacket()
    data class StrokeDelete(val strokeId: String) : WhiteboardPacket()

    data class TextAdd(val textElement: TextElement) : WhiteboardPacket()
    data class TextDelete(val textId: String) : WhiteboardPacket()

    data class StickyNoteAdd(val stickyNote: StickyNote) : WhiteboardPacket()
    data class StickyNoteDelete(val stickyNoteId: String) : WhiteboardPacket()

    data class ImageAdd(
        val imageId: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val author: String = "You",
        val imageData: ByteArray
    ) : WhiteboardPacket() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ImageAdd) return false
            return imageId == other.imageId &&
                    x == other.x &&
                    y == other.y &&
                    width == other.width &&
                    height == other.height &&
                    author == other.author &&
                    imageData.contentEquals(other.imageData)
        }

        override fun hashCode(): Int {
            var result = imageId.hashCode()
            result = 31 * result + x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + width.hashCode()
            result = 31 * result + height.hashCode()
            result = 31 * result + author.hashCode()
            result = 31 * result + imageData.contentHashCode()
            return result
        }
    }

    data class ImageDelete(val imageId: String) : WhiteboardPacket()

    data class ImageMove(val imageId: String, val x: Float, val y: Float) : WhiteboardPacket()

    data class ViewportPresence(
        val endpointId: String,
        val name: String,
        val color: Long,
        val viewport: ViewportBounds
    ) : WhiteboardPacket()

    object CanvasClear : WhiteboardPacket()
    object SyncRequest : WhiteboardPacket()
    data class SyncResponse(
        val strokes: List<Stroke>,
        val textElements: List<TextElement> = emptyList()
    ) : WhiteboardPacket()

    data class BoardSync(
        val boardId: String,
        val title: String,
        val backgroundColor: Long,
        val isReadOnly: Boolean,
        val strokes: List<Stroke>,
        val textElements: List<TextElement> = emptyList()
    ) : WhiteboardPacket()

    data class PermissionUpdate(
        val endpointId: String,
        val isReadOnly: Boolean
    ) : WhiteboardPacket()
}

object PacketSerializer {
    private const val TYPE_STROKE_START: Byte = 1
    private const val TYPE_STROKE_POINT: Byte = 2
    private const val TYPE_STROKE_END: Byte = 3
    private const val TYPE_CANVAS_CLEAR: Byte = 4
    private const val TYPE_SYNC_REQUEST: Byte = 5
    private const val TYPE_SYNC_RESPONSE: Byte = 6
    private const val TYPE_STROKE_DELETE: Byte = 7
    private const val TYPE_TEXT_ADD: Byte = 8
    private const val TYPE_TEXT_DELETE: Byte = 9
    private const val TYPE_BOARD_SYNC: Byte = 10
    private const val TYPE_PERMISSION_UPDATE: Byte = 11
    private const val TYPE_STICKY_NOTE_ADD: Byte = 12
    private const val TYPE_STICKY_NOTE_DELETE: Byte = 13
    private const val TYPE_VIEWPORT_PRESENCE: Byte = 14
    private const val TYPE_IMAGE_ADD: Byte = 15
    private const val TYPE_IMAGE_DELETE: Byte = 16
    private const val TYPE_IMAGE_MOVE: Byte = 17

    private fun serializeStrokes(strokes: List<Stroke>): List<ByteArray> {
        return strokes.map { stroke ->
            val idBytes = stroke.id.toByteArray(StandardCharsets.UTF_8)
            val ptBuffer = ByteBuffer.allocate(4 + stroke.points.size * (4 + 4 + 4 + 8))
            ptBuffer.putInt(stroke.points.size)
            stroke.points.forEach { pt ->
                ptBuffer.putFloat(pt.x)
                ptBuffer.putFloat(pt.y)
                ptBuffer.putFloat(pt.pressure)
                ptBuffer.putLong(pt.timestampMs)
            }
            val ptBytes = ptBuffer.array()
            val header = ByteBuffer.allocate(2 + idBytes.size + 8 + 4 + 1 + 1)
            header.putShort(idBytes.size.toShort())
            header.put(idBytes)
            header.putLong(stroke.color)
            header.putFloat(stroke.strokeWidth)
            header.put(stroke.tool.ordinal.toByte())
            header.put(if (stroke.isEraser) 1.toByte() else 0.toByte())
            header.array() + ptBytes
        }
    }

    private fun serializeTextElements(textElements: List<TextElement>): List<ByteArray> {
        return textElements.map { elem ->
            val idBytes = elem.id.toByteArray(StandardCharsets.UTF_8)
            val textBytes = elem.text.toByteArray(StandardCharsets.UTF_8)
            val fontBytes = elem.fontName.toByteArray(StandardCharsets.UTF_8)
            val buf = ByteBuffer.allocate(2 + idBytes.size + 2 + textBytes.size + 4 + 4 + 8 + 4 + 2 + fontBytes.size)
            buf.putShort(idBytes.size.toShort())
            buf.put(idBytes)
            buf.putShort(textBytes.size.toShort())
            buf.put(textBytes)
            buf.putFloat(elem.x)
            buf.putFloat(elem.y)
            buf.putLong(elem.color)
            buf.putFloat(elem.fontSize)
            buf.putShort(fontBytes.size.toShort())
            buf.put(fontBytes)
            buf.array()
        }
    }

    private fun deserializeStrokes(buffer: ByteBuffer, count: Int): List<Stroke> {
        val strokes = mutableListOf<Stroke>()
        for (i in 0 until count) {
            val idLength = buffer.short.toInt()
            val idBytes = ByteArray(idLength)
            buffer.get(idBytes)
            val id = String(idBytes, StandardCharsets.UTF_8)
            val color = buffer.long
            val width = buffer.float
            val toolOrdinal = buffer.get().toInt()
            val tool = DrawingTool.entries.getOrElse(toolOrdinal) { DrawingTool.PEN }
            val isEraser = buffer.get() == 1.toByte()
            val ptCount = buffer.int
            val points = mutableListOf<Point>()
            for (j in 0 until ptCount) {
                points.add(Point(buffer.float, buffer.float, buffer.float, buffer.long))
            }
            strokes.add(Stroke(id, points, color, width, tool, isEraser))
        }
        return strokes
    }

    private fun deserializeTextElements(buffer: ByteBuffer, count: Int): List<TextElement> {
        val textElements = mutableListOf<TextElement>()
        for (t in 0 until count) {
            val idLength = buffer.short.toInt()
            val idBytes = ByteArray(idLength)
            buffer.get(idBytes)
            val id = String(idBytes, StandardCharsets.UTF_8)

            val textLength = buffer.short.toInt()
            val textBytes = ByteArray(textLength)
            buffer.get(textBytes)
            val text = String(textBytes, StandardCharsets.UTF_8)

            val x = buffer.float
            val y = buffer.float
            val color = buffer.long
            val fontSize = buffer.float

            val fontLength = buffer.short.toInt()
            val fontBytes = ByteArray(fontLength)
            buffer.get(fontBytes)
            val fontName = String(fontBytes, StandardCharsets.UTF_8)

            textElements.add(TextElement(id, text, x, y, color, fontSize, fontName))
        }
        return textElements
    }

    fun serialize(packet: WhiteboardPacket): ByteArray {
        val bytes = serializePacket(packet)
        PacketTrace.log(PacketTrace.Direction.SEND, packet, bytes.size)
        return bytes
    }

    private fun serializePacket(packet: WhiteboardPacket): ByteArray {
        return when (packet) {
            is WhiteboardPacket.StrokeStart -> {
                val idBytes = packet.strokeId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size + 8 + 4 + 1 + 1 + 4 + 4 + 4 + 8)
                buffer.put(TYPE_STROKE_START)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putLong(packet.color)
                buffer.putFloat(packet.strokeWidth)
                buffer.put(if (packet.isEraser) 1.toByte() else 0.toByte())
                buffer.put(packet.tool.ordinal.toByte())
                buffer.putFloat(packet.point.x)
                buffer.putFloat(packet.point.y)
                buffer.putFloat(packet.point.pressure)
                buffer.putLong(packet.point.timestampMs)
                buffer.array()
            }
            is WhiteboardPacket.StrokePoint -> {
                val idBytes = packet.strokeId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size + 4 + 4 + 4 + 8)
                buffer.put(TYPE_STROKE_POINT)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putFloat(packet.point.x)
                buffer.putFloat(packet.point.y)
                buffer.putFloat(packet.point.pressure)
                buffer.putLong(packet.point.timestampMs)
                buffer.array()
            }
            is WhiteboardPacket.StrokeEnd -> {
                val idBytes = packet.strokeId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size)
                buffer.put(TYPE_STROKE_END)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.array()
            }
            is WhiteboardPacket.StrokeDelete -> {
                val idBytes = packet.strokeId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size)
                buffer.put(TYPE_STROKE_DELETE)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.array()
            }
            is WhiteboardPacket.TextAdd -> {
                val elem = packet.textElement
                val idBytes = elem.id.toByteArray(StandardCharsets.UTF_8)
                val textBytes = elem.text.toByteArray(StandardCharsets.UTF_8)
                val fontBytes = elem.fontName.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size + 2 + textBytes.size + 4 + 4 + 8 + 4 + 2 + fontBytes.size)
                buffer.put(TYPE_TEXT_ADD)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putShort(textBytes.size.toShort())
                buffer.put(textBytes)
                buffer.putFloat(elem.x)
                buffer.putFloat(elem.y)
                buffer.putLong(elem.color)
                buffer.putFloat(elem.fontSize)
                buffer.putShort(fontBytes.size.toShort())
                buffer.put(fontBytes)
                buffer.array()
            }
            is WhiteboardPacket.TextDelete -> {
                val idBytes = packet.textId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size)
                buffer.put(TYPE_TEXT_DELETE)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.array()
            }
            is WhiteboardPacket.StickyNoteAdd -> {
                val note = packet.stickyNote
                val idBytes = note.id.toByteArray(StandardCharsets.UTF_8)
                val textBytes = note.text.toByteArray(StandardCharsets.UTF_8)
                val authorBytes = note.author.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size + 2 + textBytes.size + 4 + 4 + 4 + 4 + 8 + 2 + authorBytes.size)
                buffer.put(TYPE_STICKY_NOTE_ADD)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putShort(textBytes.size.toShort())
                buffer.put(textBytes)
                buffer.putFloat(note.x)
                buffer.putFloat(note.y)
                buffer.putFloat(note.width)
                buffer.putFloat(note.height)
                buffer.putLong(note.color)
                buffer.putShort(authorBytes.size.toShort())
                buffer.put(authorBytes)
                buffer.array()
            }
            is WhiteboardPacket.StickyNoteDelete -> {
                val idBytes = packet.stickyNoteId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size)
                buffer.put(TYPE_STICKY_NOTE_DELETE)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.array()
            }
            is WhiteboardPacket.ViewportPresence -> {
                val epBytes = packet.endpointId.toByteArray(StandardCharsets.UTF_8)
                val nameBytes = packet.name.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + epBytes.size + 2 + nameBytes.size + 8 + 4 + 4 + 4 + 4)
                buffer.put(TYPE_VIEWPORT_PRESENCE)
                buffer.putShort(epBytes.size.toShort())
                buffer.put(epBytes)
                buffer.putShort(nameBytes.size.toShort())
                buffer.put(nameBytes)
                buffer.putLong(packet.color)
                buffer.putFloat(packet.viewport.left)
                buffer.putFloat(packet.viewport.top)
                buffer.putFloat(packet.viewport.right)
                buffer.putFloat(packet.viewport.bottom)
                buffer.array()
            }
            is WhiteboardPacket.CanvasClear -> byteArrayOf(TYPE_CANVAS_CLEAR)
            is WhiteboardPacket.SyncRequest -> byteArrayOf(TYPE_SYNC_REQUEST)
            is WhiteboardPacket.SyncResponse -> {
                val bytesList = serializeStrokes(packet.strokes)
                val textBytesList = serializeTextElements(packet.textElements)
                val totalSize = 1 + 4 + bytesList.sumOf { it.size } + 4 + textBytesList.sumOf { it.size }
                val buffer = ByteBuffer.allocate(totalSize)
                buffer.put(TYPE_SYNC_RESPONSE)
                buffer.putInt(packet.strokes.size)
                bytesList.forEach { buffer.put(it) }
                buffer.putInt(packet.textElements.size)
                textBytesList.forEach { buffer.put(it) }
                buffer.array()
            }
            is WhiteboardPacket.BoardSync -> {
                val idBytes = packet.boardId.toByteArray(StandardCharsets.UTF_8)
                val titleBytes = packet.title.toByteArray(StandardCharsets.UTF_8)
                val strokesBytes = serializeStrokes(packet.strokes)
                val textsBytes = serializeTextElements(packet.textElements)
                val totalSize = 1 + 2 + idBytes.size + 2 + titleBytes.size + 8 + 1 + 4 + strokesBytes.sumOf { it.size } + 4 + textsBytes.sumOf { it.size }
                val buffer = ByteBuffer.allocate(totalSize)
                buffer.put(TYPE_BOARD_SYNC)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putShort(titleBytes.size.toShort())
                buffer.put(titleBytes)
                buffer.putLong(packet.backgroundColor)
                buffer.put(if (packet.isReadOnly) 1.toByte() else 0.toByte())
                buffer.putInt(packet.strokes.size)
                strokesBytes.forEach { buffer.put(it) }
                buffer.putInt(packet.textElements.size)
                textsBytes.forEach { buffer.put(it) }
                buffer.array()
            }
            is WhiteboardPacket.PermissionUpdate -> {
                val idBytes = packet.endpointId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size + 1)
                buffer.put(TYPE_PERMISSION_UPDATE)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.put(if (packet.isReadOnly) 1.toByte() else 0.toByte())
                buffer.array()
            }
            is WhiteboardPacket.ImageAdd -> {
                val idBytes = packet.imageId.toByteArray(StandardCharsets.UTF_8)
                val authorBytes = packet.author.toByteArray(StandardCharsets.UTF_8)
                val totalSize = 1 + 2 + idBytes.size + 4 + 4 + 4 + 4 + 2 + authorBytes.size + 4 + packet.imageData.size
                val buffer = ByteBuffer.allocate(totalSize)
                buffer.put(TYPE_IMAGE_ADD)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putFloat(packet.x)
                buffer.putFloat(packet.y)
                buffer.putFloat(packet.width)
                buffer.putFloat(packet.height)
                buffer.putShort(authorBytes.size.toShort())
                buffer.put(authorBytes)
                buffer.putInt(packet.imageData.size)
                buffer.put(packet.imageData)
                buffer.array()
            }
            is WhiteboardPacket.ImageDelete -> {
                val idBytes = packet.imageId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size)
                buffer.put(TYPE_IMAGE_DELETE)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.array()
            }
            is WhiteboardPacket.ImageMove -> {
                val idBytes = packet.imageId.toByteArray(StandardCharsets.UTF_8)
                val buffer = ByteBuffer.allocate(1 + 2 + idBytes.size + 4 + 4)
                buffer.put(TYPE_IMAGE_MOVE)
                buffer.putShort(idBytes.size.toShort())
                buffer.put(idBytes)
                buffer.putFloat(packet.x)
                buffer.putFloat(packet.y)
                buffer.array()
            }
        }
    }

    fun deserialize(bytes: ByteArray): WhiteboardPacket? {
        val packet = deserializePacket(bytes) ?: return null
        PacketTrace.log(PacketTrace.Direction.RECEIVE, packet, bytes.size)
        return packet
    }

    private fun deserializePacket(bytes: ByteArray): WhiteboardPacket? {
        if (bytes.isEmpty()) return null
        val buffer = ByteBuffer.wrap(bytes)
        return when (buffer.get()) {
            TYPE_STROKE_START -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                val color = buffer.long
                val width = buffer.float
                val isEraser = buffer.get() == 1.toByte()
                val toolOrdinal = buffer.get().toInt()
                val tool = DrawingTool.entries.getOrElse(toolOrdinal) { DrawingTool.PEN }
                val pt = Point(buffer.float, buffer.float, buffer.float, buffer.long)
                WhiteboardPacket.StrokeStart(id, color, width, isEraser, pt, tool)
            }
            TYPE_STROKE_POINT -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                val pt = Point(buffer.float, buffer.float, buffer.float, buffer.long)
                WhiteboardPacket.StrokePoint(id, pt)
            }
            TYPE_STROKE_END -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                WhiteboardPacket.StrokeEnd(id)
            }
            TYPE_STROKE_DELETE -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                WhiteboardPacket.StrokeDelete(id)
            }
            TYPE_TEXT_ADD -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)

                val textLength = buffer.short.toInt()
                val textBytes = ByteArray(textLength)
                buffer.get(textBytes)
                val text = String(textBytes, StandardCharsets.UTF_8)

                val x = buffer.float
                val y = buffer.float
                val color = buffer.long
                val fontSize = buffer.float

                val fontLength = buffer.short.toInt()
                val fontBytes = ByteArray(fontLength)
                buffer.get(fontBytes)
                val fontName = String(fontBytes, StandardCharsets.UTF_8)

                WhiteboardPacket.TextAdd(TextElement(id, text, x, y, color, fontSize, fontName))
            }
            TYPE_TEXT_DELETE -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                WhiteboardPacket.TextDelete(id)
            }
            TYPE_CANVAS_CLEAR -> WhiteboardPacket.CanvasClear
            TYPE_SYNC_REQUEST -> WhiteboardPacket.SyncRequest
            TYPE_SYNC_RESPONSE -> {
                val strokeCount = buffer.int
                val strokes = deserializeStrokes(buffer, strokeCount)
                val textElements = if (buffer.hasRemaining()) {
                    val textCount = buffer.int
                    deserializeTextElements(buffer, textCount)
                } else emptyList()
                WhiteboardPacket.SyncResponse(strokes, textElements)
            }
            TYPE_BOARD_SYNC -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val boardId = String(idBytes, StandardCharsets.UTF_8)

                val titleLength = buffer.short.toInt()
                val titleBytes = ByteArray(titleLength)
                buffer.get(titleBytes)
                val title = String(titleBytes, StandardCharsets.UTF_8)

                val backgroundColor = buffer.long
                val isReadOnly = buffer.get() == 1.toByte()

                val strokeCount = buffer.int
                val strokes = deserializeStrokes(buffer, strokeCount)
                val textElements = if (buffer.hasRemaining()) {
                    val textCount = buffer.int
                    deserializeTextElements(buffer, textCount)
                } else emptyList()

                WhiteboardPacket.BoardSync(boardId, title, backgroundColor, isReadOnly, strokes, textElements)
            }
            TYPE_PERMISSION_UPDATE -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val endpointId = String(idBytes, StandardCharsets.UTF_8)
                val isReadOnly = buffer.get() == 1.toByte()
                WhiteboardPacket.PermissionUpdate(endpointId, isReadOnly)
            }
            TYPE_STICKY_NOTE_ADD -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)

                val textLength = buffer.short.toInt()
                val textBytes = ByteArray(textLength)
                buffer.get(textBytes)
                val text = String(textBytes, StandardCharsets.UTF_8)

                val x = buffer.float
                val y = buffer.float
                val width = buffer.float
                val height = buffer.float
                val color = buffer.long

                val authorLength = buffer.short.toInt()
                val authorBytes = ByteArray(authorLength)
                buffer.get(authorBytes)
                val author = String(authorBytes, StandardCharsets.UTF_8)

                WhiteboardPacket.StickyNoteAdd(
                    StickyNote(id = id, text = text, x = x, y = y, width = width, height = height, color = color, author = author)
                )
            }
            TYPE_STICKY_NOTE_DELETE -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                WhiteboardPacket.StickyNoteDelete(id)
            }
            TYPE_VIEWPORT_PRESENCE -> {
                val epLength = buffer.short.toInt()
                val epBytes = ByteArray(epLength)
                buffer.get(epBytes)
                val epId = String(epBytes, StandardCharsets.UTF_8)

                val nameLength = buffer.short.toInt()
                val nameBytes = ByteArray(nameLength)
                buffer.get(nameBytes)
                val name = String(nameBytes, StandardCharsets.UTF_8)

                val color = buffer.long
                val left = buffer.float
                val top = buffer.float
                val right = buffer.float
                val bottom = buffer.float

                WhiteboardPacket.ViewportPresence(
                    endpointId = epId,
                    name = name,
                    color = color,
                    viewport = ViewportBounds(left = left, top = top, right = right, bottom = bottom)
                )
            }
            TYPE_IMAGE_ADD -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)

                val x = buffer.float
                val y = buffer.float
                val width = buffer.float
                val height = buffer.float

                val authorLength = buffer.short.toInt()
                val authorBytes = ByteArray(authorLength)
                buffer.get(authorBytes)
                val author = String(authorBytes, StandardCharsets.UTF_8)

                val dataLength = buffer.int
                val dataBytes = ByteArray(dataLength)
                buffer.get(dataBytes)

                WhiteboardPacket.ImageAdd(
                    imageId = id,
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    author = author,
                    imageData = dataBytes
                )
            }
            TYPE_IMAGE_DELETE -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                WhiteboardPacket.ImageDelete(id)
            }
            TYPE_IMAGE_MOVE -> {
                val idLength = buffer.short.toInt()
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val id = String(idBytes, StandardCharsets.UTF_8)
                val x = buffer.float
                val y = buffer.float
                WhiteboardPacket.ImageMove(id, x, y)
            }
            else -> null
        }
    }
}
