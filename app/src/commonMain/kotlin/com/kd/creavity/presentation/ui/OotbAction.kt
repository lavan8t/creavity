package com.kd.creavity.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardVersionSnapshot
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement


sealed interface BoardAction {
    // Canvas & Inking
    data class StrokeCompleted(val stroke: Stroke) : BoardAction
    data class StrokeBroadcastStart(val id: String, val color: Long, val width: Float, val isEraser: Boolean, val point: Point, val tool: DrawingTool = DrawingTool.PEN) : BoardAction
    data class StrokeBroadcastPoint(val id: String, val point: Point) : BoardAction
    data class StrokeBroadcastEnd(val id: String) : BoardAction
    data class TransformCanvas(val zoomChange: Float, val focal: Offset = Offset.Zero, val panChange: Offset = Offset.Zero) : BoardAction
    data class SetCanvasTransform(val zoom: Float, val panOffset: Offset) : BoardAction
    data object ResetCanvasTransform : BoardAction
    data class SelectTool(val tool: DrawingTool) : BoardAction
    data class SelectColor(val color: Color) : BoardAction
    data class SelectBackgroundColor(val color: Color) : BoardAction
    data class SelectBackgroundPattern(val pattern: com.kd.creavity.presentation.canvas.CanvasBackgroundPattern) : BoardAction
    data class ChangeStrokeWidth(val width: Float) : BoardAction
    data object ClearCanvas : BoardAction
    data class SetInfiniteCanvas(val enabled: Boolean) : BoardAction
    data class SetPresentationUrl(val url: String?) : BoardAction
    data class SetPresentationMode(val enabled: Boolean) : BoardAction
    data class ImportPresentation(val pathOrUri: String) : BoardAction

    // Canvas Elements
    data class AddText(
        val text: String,
        val x: Float,
        val y: Float,
        val color: Color,
        val fontSize: Float,
        val fontName: String,
        val id: String? = null,
        val svgData: String? = null
    ) : BoardAction
    data class UpdateTextElement(val textElement: TextElement) : BoardAction
    data class DeleteStroke(val strokeId: String) : BoardAction
    data class DeleteText(val textId: String) : BoardAction
    data class AddImage(val pathOrUri: String) : BoardAction
    data class DeleteImage(val imageId: String) : BoardAction
    data class MoveElements(val strokes: List<Stroke>, val texts: List<TextElement>, val images: List<ImageElement>) : BoardAction
    data class DuplicateElements(val strokeIds: List<String>, val textIds: List<String>) : BoardAction
    data class RecolorElements(val strokeIds: List<String>, val textIds: List<String>, val color: Color) : BoardAction
    data class UpdateStrokeProperties(
        val strokeId: String,
        val roughness: com.kd.creavity.domain.model.RoughnessStyle? = null,
        val fillStyle: com.kd.creavity.domain.model.FillStyle? = null,
        val embeddedText: String? = null,
        val color: Color? = null,
        val width: Float? = null
    ) : BoardAction
    data class ReorderElements(val strokeIds: List<String>, val bringToFront: Boolean) : BoardAction

    // Undo / Redo / Snapshot
    data object Undo : BoardAction
    data object Redo : BoardAction
    data class CreateVersionSnapshot(val name: String) : BoardAction
    data class RestoreVersionSnapshot(val snapshot: BoardVersionSnapshot) : BoardAction

    // Board & Page Management
    data object AddNewPage : BoardAction
    data class SwitchPage(val pageIndex: Int) : BoardAction
    data class DeletePage(val pageIndex: Int) : BoardAction
    data class CreateNewBoard(val title: String) : BoardAction
    data class SwitchBoard(val board: Board) : BoardAction
    data class RenameBoard(val boardId: String, val newTitle: String) : BoardAction
    data class DuplicateBoard(val boardId: String) : BoardAction
    data class DeleteBoard(val boardId: String) : BoardAction
    data class AddStickyNote(val note: com.kd.creavity.domain.model.StickyNote) : BoardAction
    data class DeleteStickyNote(val noteId: String) : BoardAction
    data class ToggleStarBoard(val boardId: String) : BoardAction
    data class ToggleReadOnlyBoard(val boardId: String, val isReadOnly: Boolean) : BoardAction
    data class MoveBoardToBin(val boardId: String) : BoardAction
    data class RestoreBoardFromBin(val boardId: String) : BoardAction
    data class DeleteBoardPermanently(val boardId: String) : BoardAction
    data object EmptyBin : BoardAction
    data class UpdateBoardMetadata(val boardId: String, val title: String, val venue: String, val group: String, val labels: List<String>) : BoardAction
    data class CreateFolder(val folderName: String) : BoardAction
    data class DeleteFolder(val folderName: String) : BoardAction

    // P2P Collaboration
    data class StartAdvertising(val deviceName: String) : BoardAction
    data object StopAdvertising : BoardAction
    data object StartDiscovery : BoardAction
    data object StopDiscovery : BoardAction
    data class ConnectPeer(val endpointId: String, val localName: String) : BoardAction
    data class DisconnectPeer(val endpointId: String) : BoardAction
    data class SetPeerPermission(val endpointId: String, val isReadOnly: Boolean) : BoardAction
    data class SetAllPeersPermission(val isReadOnly: Boolean) : BoardAction
    data object LeaveRoom : BoardAction

    // Settings
    data class UpdateDisplayName(val name: String) : BoardAction
    data class ToggleHaptics(val enabled: Boolean) : BoardAction
    data class SetThemeMode(val mode: com.kd.creavity.presentation.viewmodel.ThemeMode) : BoardAction

    // Classroom: Collaborative Chat
    data class SendChatMessage(val text: String) : BoardAction
    data class ToggleChat(val isOpen: Boolean? = null) : BoardAction
}
