package com.kd.creavity.presentation.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kd.creavity.domain.model.AppThemePreset
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.BoardVersionSnapshot
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.PeerPresence
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.StudentWorksheet
import com.kd.creavity.domain.model.ActiveQuiz
import com.kd.creavity.domain.model.QuizSubmission
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.presentation.canvas.CanvasBackgroundPattern

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class WhiteboardUiState(
    val currentBoard: Board = Board(),
    val savedBoards: List<Board> = emptyList(),
    val userFolders: Set<String> = emptySet(),
    val currentColor: Color = Color.Unspecified,
    val backgroundColor: Color = Color.Unspecified,
    val backgroundPattern: CanvasBackgroundPattern = CanvasBackgroundPattern.ISOMETRIC_DOTS,
    val currentWidth: Float = 6.0f,
    val selectedTool: DrawingTool = DrawingTool.PEN,
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val roomCode: String? = null,
    val displayName: String = "Whiteboard User",
    val hapticsEnabled: Boolean = true,
    val isAdvertising: Boolean = false,
    val isDiscovering: Boolean = false,
    val connectedPeers: List<PeerDevice> = emptyList(),
    val discoveredPeers: List<PeerDevice> = emptyList(),
    val selectedThemePreset: AppThemePreset = AppThemePreset.DYNAMIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val livePeerPresences: Map<String, PeerPresence> = emptyMap(),
    val versionHistory: List<BoardVersionSnapshot> = emptyList(),
    val myAccessReadOnly: Boolean = false,
    val isInfiniteCanvas: Boolean = true,
    val presentationUrl: String? = null,
    val isPresentationMode: Boolean = false,
    val presentationCurrentSlide: Int = 0,
    val presentationTotalSlides: Int = 1,
    val isAppForeground: Boolean = true,
    val activeWorksheet: StudentWorksheet? = null,
    val studentWorksheets: Map<String, StudentWorksheet> = emptyMap(),
    val isWorksheetMode: Boolean = false,
    val inspectedStudentId: String? = null,
    val activeQuiz: ActiveQuiz? = null,
    val quizSubmissions: List<QuizSubmission> = emptyList(),
    val userQuizAnswer: Int? = null,
    val quizTimeRemainingSeconds: Int = 0
) {
    val isHost: Boolean
        get() = isAdvertising || (!isDiscovering && connectedPeers.none { it.role == com.kd.creavity.data.mesh.MeshRole.HOST })

    val isBoardSwitchingLocked: Boolean
        get() = !isHost && connectedPeers.isNotEmpty()

    val isCanvasReadOnly: Boolean
        get() = currentBoard.isReadOnly || myAccessReadOnly

    val activePage: BoardPage
        get() = currentBoard.pages.getOrElse(currentBoard.activePageIndex) { BoardPage() }

    val strokes: List<Stroke>
        get() = activePage.strokes

    val textElements: List<TextElement>
        get() = activePage.textElements

    val imageElements: List<ImageElement>
        get() = activePage.imageElements

    val stickyNotes: List<StickyNote>
        get() = activePage.stickyNotes
}
