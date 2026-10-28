package com.kd.creavity.util

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun PlatformSystemBarsEffect(isLandscape: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current

    DisposableEffect(isLandscape) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (isLandscape) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

actual fun decodeImageFile(path: String): ImageBitmap? {
    return runCatching {
        BitmapFactory.decodeFile(path)?.asImageBitmap()
    }.getOrNull()
}

internal var appContext: android.content.Context? = null

fun initAppContext(context: android.content.Context) {
    appContext = context
}

actual fun getAppStorageDir(): File {
    val ctx = appContext
    return if (ctx != null) {
        val publicDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "CreaVITy")
        if (publicDocs.exists() || publicDocs.mkdirs()) publicDocs else File(ctx.filesDir, "saved_boards").apply { mkdirs() }
    } else {
        File(".", "saved_boards").apply { mkdirs() }
    }
}

actual fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() }
    val model = Build.MODEL.orEmpty()
    return if (model.startsWith(manufacturer, ignoreCase = true)) {
        model
    } else if (manufacturer.isNotBlank() && model.isNotBlank()) {
        "$manufacturer $model"
    } else {
        model.ifBlank { manufacturer.ifBlank { "Android Device" } }
    }
}

@Composable
actual fun PlatformWebView(url: String, modifier: androidx.compose.ui.Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                webViewClient = android.webkit.WebViewClient()
                loadUrl(url)
            }
        },
        update = { wv ->
            if (wv.url != url) wv.loadUrl(url)
        },
        modifier = modifier
    )
}

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerType,
    onResult: (String?) -> Unit
): () -> Unit {
    return when (type) {
        FilePickerType.IMAGE -> {
            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri ->
                onResult(uri?.toString())
            }
            androidx.compose.runtime.remember { { launcher.launch("image/*") } }
        }
        FilePickerType.PDF -> {
            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                onResult(uri?.toString())
            }
            androidx.compose.runtime.remember { { launcher.launch(arrayOf("application/pdf")) } }
        }
    }
}

actual fun copyImageToAppStorage(sourcePathOrUri: String): String? {
    return runCatching {
        val ctx = appContext ?: return null
        val uri = android.net.Uri.parse(sourcePathOrUri)
        val imagesDir = File(ctx.filesDir, "images").apply { mkdirs() }
        val targetFile = File(imagesDir, "img_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}.jpg")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            java.io.FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        targetFile.absolutePath
    }.getOrNull()
}

actual suspend fun importPdfPages(pathOrUri: String): List<com.kd.creavity.domain.model.BoardPage> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val ctx = appContext ?: return@withContext emptyList()
    val uri = android.net.Uri.parse(pathOrUri)
    val pfd = ctx.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext emptyList()
    val renderer = android.graphics.pdf.PdfRenderer(pfd)
    val pageCount = renderer.pageCount
    if (pageCount == 0) {
        renderer.close()
        pfd.close()
        return@withContext emptyList()
    }

    val imagesDir = File(ctx.filesDir, "slide_cache").apply { mkdirs() }
    val newPages = mutableListOf<com.kd.creavity.domain.model.BoardPage>()

    for (i in 0 until pageCount) {
        val pdfPage = renderer.openPage(i)
        val width = (pdfPage.width * 2).coerceAtMost(2560)
        val height = (pdfPage.height * 2).coerceAtMost(1440)
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        pdfPage.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfPage.close()

        val slideFile = File(imagesDir, "slide_${System.currentTimeMillis()}_$i.jpg")
        java.io.FileOutputStream(slideFile).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        }

        val imageElem = com.kd.creavity.domain.model.ImageElement(
            path = slideFile.absolutePath,
            x = 0f,
            y = 0f,
            width = 1920f,
            height = (1920f * (height.toFloat() / width.toFloat()))
        )
        newPages.add(com.kd.creavity.domain.model.BoardPage(name = "Slide ${i + 1}", imageElements = listOf(imageElem)))
    }
    renderer.close()
    pfd.close()
    newPages
}

actual fun createDefaultPeerTransport(): com.kd.creavity.data.nearby.PeerTransport {
    val ctx = appContext
    return if (ctx != null) {
        com.kd.creavity.data.nearby.NearbyConnectionsManager(ctx)
    } else {
        com.kd.creavity.data.nearby.LanDiscoveryTransport()
    }
}

actual fun shareBoard(board: com.kd.creavity.domain.model.Board, format: com.kd.creavity.data.export.ExportFormat) {
    val ctx = appContext ?: return
    com.kd.creavity.data.export.AndroidBoardExporter.shareBoard(ctx, board, format)
}

actual fun isDesktopPlatform(): Boolean = false


