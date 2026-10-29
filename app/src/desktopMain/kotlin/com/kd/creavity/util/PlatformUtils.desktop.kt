package com.kd.creavity.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.kd.creavity.data.nearby.LanDiscoveryTransport
import com.kd.creavity.data.nearby.PeerTransport
import com.kd.creavity.domain.model.BoardPage
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.InetAddress
import java.net.URI
import java.util.UUID

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop navigation is driven by on-screen back buttons and window controls.
}

@Composable
actual fun PlatformSystemBarsEffect(isLandscape: Boolean) {
    // Desktop window has standard OS title bar/borders; no Android system bar insets.
}

@Composable
actual fun PlatformWebView(url: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Google Slides Presentation",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                runCatching {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(URI(url))
                    }
                }
            }) {
                Text("Open Slides in Browser")
            }
        }
    }
}

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerType,
    onResult: (String?) -> Unit
): () -> Unit {
    return remember(type, onResult) {
        {
            try {
                val mode = FileDialog.LOAD
                val title = if (type == FilePickerType.IMAGE) "Select Image" else "Select PDF Presentation"
                val dialog = FileDialog(null as Frame?, title, mode)
                if (type == FilePickerType.IMAGE) {
                    dialog.setFilenameFilter { _, name ->
                        val lower = name.lowercase()
                        lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp")
                    }
                } else {
                    dialog.setFilenameFilter { _, name ->
                        name.lowercase().endsWith(".pdf")
                    }
                }
                dialog.isVisible = true
                val selectedFile = dialog.file
                val selectedDir = dialog.directory
                if (selectedFile != null && selectedDir != null) {
                    onResult(File(selectedDir, selectedFile).absolutePath)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}

actual fun decodeImageFile(path: String): ImageBitmap? {
    return runCatching {
        val file = File(path)
        if (!file.exists()) return null
        val bytes = file.readBytes()
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }.getOrNull()
}

actual fun getAppStorageDir(): File {
    val userHome = System.getProperty("user.home") ?: "."
    val dir = File(userHome, ".creavity")
    if (!dir.exists()) dir.mkdirs()
    return dir
}

actual fun getDeviceName(): String {
    val hostEnv = runCatching { System.getenv("HOSTNAME") ?: System.getenv("HOST") }.getOrNull()
    if (!hostEnv.isNullOrBlank()) return hostEnv
    return runCatching {
        InetAddress.getLocalHost().hostName
    }.getOrNull()?.ifBlank { "Desktop PC" } ?: "Desktop PC"
}

actual fun copyImageToAppStorage(sourcePathOrUri: String): String? {
    return runCatching {
        val srcFile = File(sourcePathOrUri)
        if (!srcFile.exists()) return null
        val destDir = File(getAppStorageDir(), "images").apply { mkdirs() }
        val destFile = File(destDir, "img_${UUID.randomUUID()}_${srcFile.name}")
        srcFile.copyTo(destFile, overwrite = true)
        destFile.absolutePath
    }.getOrNull()
}

actual suspend fun importPdfPages(pathOrUri: String): List<BoardPage> {
    return emptyList()
}

actual fun createDefaultPeerTransport(): PeerTransport {
    return LanDiscoveryTransport()
}

actual fun shareBoard(board: com.kd.creavity.domain.model.Board, format: com.kd.creavity.data.export.ExportFormat) {
    runCatching {
        val userHome = System.getProperty("user.home") ?: "."
        val exportDir = File(userHome, "Downloads").takeIf { it.exists() } ?: File(getAppStorageDir(), "exports")
        exportDir.mkdirs()
        val sanitized = board.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").trim('_').ifBlank { "Whiteboard" }
        val target = File(exportDir, "$sanitized.${format.extension}")

        when (format) {
            com.kd.creavity.data.export.ExportFormat.SVG -> {
                val page = board.pages.getOrElse(board.activePageIndex) { board.pages.firstOrNull() ?: com.kd.creavity.domain.model.BoardPage() }
                val svg = com.kd.creavity.presentation.canvas.CanvasExportManager.exportToSvg(
                    strokes = page.strokes,
                    texts = page.textElements,
                    stickyNotes = page.stickyNotes,
                    backgroundColor = board.backgroundColor
                )
                target.writeText(svg)
            }
            else -> {
                val bm = com.kd.creavity.data.storage.BoardManager()
                bm.saveBoard(board)
                val src = File(File(getAppStorageDir(), "boards"), "${board.id}.json")
                if (src.exists()) {
                    src.copyTo(target, overwrite = true)
                }
            }
        }
    }
}

actual fun isDesktopPlatform(): Boolean = true


