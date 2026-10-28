package com.kd.creavity.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.kd.creavity.data.nearby.PeerTransport
import com.kd.creavity.domain.model.BoardPage
import java.io.File

enum class FilePickerType {
    IMAGE,
    PDF
}

@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable
expect fun PlatformSystemBarsEffect(isLandscape: Boolean)

@Composable
expect fun PlatformWebView(url: String, modifier: Modifier = Modifier)

@Composable
expect fun rememberFilePickerLauncher(
    type: FilePickerType,
    onResult: (String?) -> Unit
): () -> Unit

expect fun decodeImageFile(path: String): ImageBitmap?

expect fun getAppStorageDir(): File

expect fun getDeviceName(): String

expect fun copyImageToAppStorage(sourcePathOrUri: String): String?

expect suspend fun importPdfPages(pathOrUri: String): List<BoardPage>

expect fun createDefaultPeerTransport(): PeerTransport

expect fun shareBoard(board: com.kd.creavity.domain.model.Board, format: com.kd.creavity.data.export.ExportFormat)

expect fun isDesktopPlatform(): Boolean
