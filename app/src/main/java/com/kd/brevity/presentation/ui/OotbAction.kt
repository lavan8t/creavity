package com.kd.brevity.presentation.ui

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kd.brevity.domain.model.Board
import com.kd.brevity.domain.model.BoardVersionSnapshot
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.domain.model.ImageElement
import com.kd.brevity.domain.model.Point
import com.kd.brevity.domain.model.Stroke
import com.kd.brevity.domain.model.TextElement

typealias BrevityAction = OotbAction

sealed interface OotbAction {
    // Canvas & Inking
    data class StrokeCompleted(val stroke: Stroke) : OotbAction
    data class StrokeBroadcastStart(val id: String, val color: Long, val width: Float, val isEraser: Boolean, val point: Point) : OotbAction
    data class StrokeBroadcastPoint(val id: String, val point: Point) : OotbAction
    data class StrokeBroadcastEnd(val id: String) : OotbAction
    data class TransformCanvas(val zoomChange: Float, val focal: Offset = Offset.Zero, val panChange: Offset = Offset.Zero) : OotbAction
    data class SetCanvasTransform(val zoom: Float, val panOffset: Offset) : OotbAction
    data object ResetCanvasTransform : OotbAction
    data class SelectTool(val tool: DrawingTool) : OotbAction
    data class SelectColor(val color: Color) : OotbAction
    data class SelectBackgroundColor(val color: Color) : OotbAction
    data class SelectBackgroundPattern(val pattern: com.kd.brevity.presentation.canvas.CanvasBackgroundPattern) : OotbAction
    data class ChangeStrokeWidth(val width: Float) : OotbAction
    data object ClearCanvas : OotbAction
    data class SetInfiniteCanvas(val enabled: Boolean) : OotbAction
    data class SetPresentationUrl(val url: String?) : OotbAction
    data class SetPresentationMode(val enabled: Boolean) : OotbAction
    data class ImportPresentation(val uri: Uri) : OotbAction

    // Canvas Elements
    data class AddText(val text: String, val x: Float, val y: Float, val color: Color, val fontSize: Float, val fontName: String) : OotbAction
    data class DeleteStroke(val strokeId: String) : OotbAction
    data class DeleteText(val textId: String) : OotbAction
    data class AddImage(val uri: Uri) : OotbAction
    data class DeleteImage(val imageId: String) : OotbAction
    data class MoveElements(val strokes: List<Stroke>, val texts: List<TextElement>, val images: List<ImageElement>) : OotbAction
    data class DuplicateElements(val strokeIds: List<String>, val textIds: List<String>) : OotbAction
    data class RecolorElements(val strokeIds: List<String>, val textIds: List<String>, val color: Color) : OotbAction

    // Undo / Redo / Snapshot
    data object Undo : OotbAction
    data object Redo : OotbAction
    data class CreateVersionSnapshot(val name: String) : OotbAction
    data class RestoreVersionSnapshot(val snapshot: BoardVersionSnapshot) : OotbAction

    // Board & Page Management
    data object AddNewPage : OotbAction
    data class SwitchPage(val pageIndex: Int) : OotbAction
    data class DeletePage(val pageIndex: Int) : OotbAction
    data class CreateNewBoard(val title: String) : OotbAction
    data class SwitchBoard(val board: Board) : OotbAction
    data class RenameBoard(val boardId: String, val newTitle: String) : OotbAction
    data class DuplicateBoard(val boardId: String) : OotbAction
    data class DeleteBoard(val boardId: String) : OotbAction
    data class AddStickyNote(val note: com.kd.brevity.domain.model.StickyNote) : OotbAction
    data class DeleteStickyNote(val noteId: String) : OotbAction
    data class ToggleStarBoard(val boardId: String) : OotbAction
    data class ToggleReadOnlyBoard(val boardId: String, val isReadOnly: Boolean) : OotbAction
    data class MoveBoardToBin(val boardId: String) : OotbAction
    data class RestoreBoardFromBin(val boardId: String) : OotbAction
    data class DeleteBoardPermanently(val boardId: String) : OotbAction
    data object EmptyBin : OotbAction
    data class UpdateBoardMetadata(val boardId: String, val title: String, val venue: String, val group: String, val labels: List<String>) : OotbAction

    // P2P Collaboration
    data class StartAdvertising(val deviceName: String) : OotbAction
    data object StopAdvertising : OotbAction
    data object StartDiscovery : OotbAction
    data object StopDiscovery : OotbAction
    data class ConnectPeer(val endpointId: String, val localName: String) : OotbAction
    data class DisconnectPeer(val endpointId: String) : OotbAction
    data class SetPeerPermission(val endpointId: String, val isReadOnly: Boolean) : OotbAction
    data class SetAllPeersPermission(val isReadOnly: Boolean) : OotbAction
    data object LeaveRoom : OotbAction

    // Settings
    data class UpdateDisplayName(val name: String) : OotbAction
    data class ToggleHaptics(val enabled: Boolean) : OotbAction
    data class SetThemeMode(val mode: com.kd.brevity.presentation.viewmodel.ThemeMode) : OotbAction
}
