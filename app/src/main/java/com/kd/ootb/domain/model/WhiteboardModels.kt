package com.kd.ootb.domain.model

import java.util.UUID

enum class DrawingTool {
    PEN,
    PENCIL,
    HIGHLIGHTER,
    LINE,
    ARROW,
    RECTANGLE,
    CIRCLE,
    TEXT,
    ERASER,
    SELECT_ERASE,
    SELECT_MOVE, // Compatibility mapping to RECT_SELECT
    RECT_SELECT,
    LASSO_SELECT
}

enum class AppThemePreset(val displayName: String, val primaryColor: Long, val isDynamic: Boolean = false) {
    DYNAMIC("Dynamic System", 0xFFDC5F00, isDynamic = true),
    STUDIO_AMBER("Studio Amber", 0xFFDC5F00),
    STUDIO_AQUA("Studio Aqua", 0xFF00B4D8),
    STUDIO_EMERALD("Studio Emerald", 0xFF00E676),
    STUDIO_VIOLET("Studio Violet", 0xFF9D4EDD),
    STUDIO_CRIMSON("Studio Crimson", 0xFFFF3366),
    STUDIO_SLATE("Studio Slate", 0xFF78909C)
}

data class Point(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestampMs: Long = System.currentTimeMillis()
)

data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Point> = emptyList(),
    val color: Long = 0xFFFFFFFF,
    val strokeWidth: Float = 5.0f,
    val tool: DrawingTool = DrawingTool.PEN,
    val isEraser: Boolean = (tool == DrawingTool.ERASER),
    val author: String = "You",
    val createdAt: Long = System.currentTimeMillis()
)

data class TextElement(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val color: Long = 0xFFFFFFFF,
    val fontSize: Float = 24.0f,
    val fontName: String = "Roboto Flex",
    val author: String = "You",
    val createdAt: Long = System.currentTimeMillis()
)

data class ImageElement(
    val id: String = UUID.randomUUID().toString(),
    val path: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 240f,
    val height: Float = 240f,
    val author: String = "You",
    val createdAt: Long = System.currentTimeMillis()
)

data class PeerPresence(
    val peerId: String,
    val name: String,
    val currentPoint: Point? = null,
    val isDrawing: Boolean = false,
    val activeTool: DrawingTool = DrawingTool.PEN,
    val color: Long = 0xFFDC5F00,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

enum class ModificationType {
    ADD_STROKE,
    DELETE_STROKE,
    ADD_TEXT,
    DELETE_TEXT,
    ADD_IMAGE,
    DELETE_IMAGE,
    MOVE_ELEMENTS,
    RECOLOR_ELEMENTS,
    CLEAR_PAGE,
    RESTORE_VERSION
}

data class CanvasModification(
    val id: String = UUID.randomUUID().toString(),
    val type: ModificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val author: String = "You",
    val pageIndex: Int = 0,
    val stroke: Stroke? = null,
    val strokeId: String? = null,
    val textElement: TextElement? = null,
    val textId: String? = null,
    val imageElement: ImageElement? = null,
    val imageId: String? = null,
    val previousStrokes: List<Stroke> = emptyList(),
    val newStrokes: List<Stroke> = emptyList(),
    val previousTexts: List<TextElement> = emptyList(),
    val newTexts: List<TextElement> = emptyList(),
    val previousImages: List<ImageElement> = emptyList(),
    val newImages: List<ImageElement> = emptyList()
)

data class BoardVersionSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Version",
    val timestamp: Long = System.currentTimeMillis(),
    val author: String = "You",
    val pageIndex: Int = 0,
    val strokes: List<Stroke> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    val imageElements: List<ImageElement> = emptyList(),
    val strokeCount: Int = strokes.size,
    val textCount: Int = textElements.size
)

data class BoardPage(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Page 1",
    val strokes: List<Stroke> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    val imageElements: List<ImageElement> = emptyList()
)

data class Board(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Board",
    val pages: List<BoardPage> = listOf(BoardPage(name = "Page 1")),
    val activePageIndex: Int = 0,
    val backgroundColor: Long = 0xFF18181C,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "You",
    val ownerName: String = "You",
    val isReadOnly: Boolean = false,
    val versionHistory: List<BoardVersionSnapshot> = emptyList(),
    val modificationLog: List<CanvasModification> = emptyList()
)

data class PeerDevice(
    val endpointId: String,
    val name: String,
    val isConnected: Boolean = false,
    val isReadOnly: Boolean = false
)

data class RoomState(
    val roomName: String = "Local Whiteboard",
    val isHost: Boolean = false,
    val connectedPeers: List<PeerDevice> = emptyList(),
    val strokes: List<Stroke> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    val imageElements: List<ImageElement> = emptyList()
)
