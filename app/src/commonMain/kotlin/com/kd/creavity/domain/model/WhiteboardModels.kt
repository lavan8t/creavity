package com.kd.creavity.domain.model

import java.util.UUID

enum class DrawingTool {
    PEN,
    MARKER,
    HIGHLIGHTER,
    BRUSH,
    ERASER,
    SELECT,
    STICKY_NOTE,
    LASER,
    PENCIL,
    CALLIGRAPHY,
    LINE,
    ARROW,
    RECTANGLE,
    CIRCLE,
    TEXT,
    SELECT_ERASE,
    SELECT_MOVE, // Compatibility mapping to SELECT
    RECT_SELECT,
    LASSO_SELECT,
    TRIANGLE
}

enum class AppThemePreset(val displayName: String, val isDynamic: Boolean = true) {
    DYNAMIC("Dynamic Material You", isDynamic = true)
}

data class Point(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestampMs: Long = System.currentTimeMillis(),
    val tilt: Float = 0f,
    val orientation: Float = 0f
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
    val fontName: String = "Google Sans Flex",
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

data class StickyNote(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 220f,
    val height: Float = 220f,
    val color: Long = 0xFFFFF176, // Classic Whiteboard Yellow
    val author: String = "You",
    val createdAt: Long = System.currentTimeMillis()
)

data class ViewportBounds(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1920f,
    val bottom: Float = 1080f
)

enum class EngagementState {
    WATCHING,
    NOT_WATCHING,
    OFFLINE
}

data class PeerPresence(
    val peerId: String,
    val name: String,
    val currentPoint: Point? = null,
    val isDrawing: Boolean = false,
    val activeTool: DrawingTool = DrawingTool.PEN,
    val color: Long = 0xFFDC5F00,
    val viewport: ViewportBounds? = null,
    val lastUpdatedMs: Long = System.currentTimeMillis(),
    val engagementState: EngagementState = EngagementState.WATCHING
)

enum class ModificationType {
    ADD_STROKE,
    DELETE_STROKE,
    ADD_TEXT,
    DELETE_TEXT,
    ADD_IMAGE,
    DELETE_IMAGE,
    ADD_STICKY_NOTE,
    DELETE_STICKY_NOTE,
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
    val stickyNote: StickyNote? = null,
    val stickyNoteId: String? = null,
    val previousStrokes: List<Stroke> = emptyList(),
    val newStrokes: List<Stroke> = emptyList(),
    val previousTexts: List<TextElement> = emptyList(),
    val newTexts: List<TextElement> = emptyList(),
    val previousImages: List<ImageElement> = emptyList(),
    val newImages: List<ImageElement> = emptyList(),
    val previousStickyNotes: List<StickyNote> = emptyList(),
    val newStickyNotes: List<StickyNote> = emptyList()
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
    val stickyNotes: List<StickyNote> = emptyList(),
    val strokeCount: Int = strokes.size,
    val textCount: Int = textElements.size
)

data class BoardPage(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Page 1",
    val strokes: List<Stroke> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    val imageElements: List<ImageElement> = emptyList(),
    val stickyNotes: List<StickyNote> = emptyList()
)

data class Board(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Board",
    val pages: List<BoardPage> = listOf(BoardPage(name = "Page 1")),
    val activePageIndex: Int = 0,
    val backgroundColor: Long = 0xFF18181C,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = modifiedAt,
    val createdBy: String = "You",
    val ownerName: String = "You",
    val isStarred: Boolean = false,
    val isReadOnly: Boolean = false,
    val isInBin: Boolean = false,
    val binnedAt: Long? = null,
    val isInfiniteCanvas: Boolean = true,
    val canvasWidth: Float = 1920f,
    val canvasHeight: Float = 1080f,
    val presentationUrl: String? = null,
    val versionHistory: List<BoardVersionSnapshot> = emptyList(),
    val modificationLog: List<CanvasModification> = emptyList(),
    val labels: List<String> = emptyList(),
    val venue: String = "",
    val group: String = ""
)

data class PeerDevice(
    val endpointId: String,
    val name: String,
    val isConnected: Boolean = false,
    val isReadOnly: Boolean = false,
    val role: com.kd.creavity.data.mesh.MeshRole = com.kd.creavity.data.mesh.MeshRole.VIEWER,
    val boardId: String = "",
    val engagementState: EngagementState = EngagementState.WATCHING,
    val lastSeenMs: Long = System.currentTimeMillis()
)

data class RoomState(
    val roomName: String = "Local Whiteboard",
    val isHost: Boolean = false,
    val connectedPeers: List<PeerDevice> = emptyList(),
    val strokes: List<Stroke> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    val imageElements: List<ImageElement> = emptyList(),
    val stickyNotes: List<StickyNote> = emptyList()
)

data class StudentWorksheet(
    val sheetId: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val title: String,
    val questionText: String,
    val strokes: List<Stroke> = emptyList(),
    val isSubmitted: Boolean = false,
    val submittedAtMs: Long? = null
)

enum class QuizType {
    POLL,
    TIMED_QUIZ
}

data class ActiveQuiz(
    val quizId: String = UUID.randomUUID().toString(),
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int = -1, // -1 for open poll
    val durationSeconds: Int = 30,
    val type: QuizType = QuizType.POLL,
    val startedAtMs: Long = System.currentTimeMillis(),
    val isEnded: Boolean = false
)

data class QuizSubmission(
    val quizId: String,
    val studentId: String,
    val studentName: String,
    val selectedOptionIndex: Int,
    val responseTimeMs: Long,
    val isCorrect: Boolean = false
)

