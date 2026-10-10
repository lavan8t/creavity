package com.kd.creavity.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.ImageElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PresentationImporter {
    suspend fun importPdfPages(context: Context, uri: Uri): List<BoardPage> = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext emptyList()
        val renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount
        if (pageCount == 0) {
            renderer.close()
            pfd.close()
            return@withContext emptyList()
        }

        val imagesDir = File(context.filesDir, "slide_cache").apply { mkdirs() }
        val newPages = mutableListOf<BoardPage>()

        for (i in 0 until pageCount) {
            val pdfPage = renderer.openPage(i)
            val width = (pdfPage.width * 2).coerceAtMost(2560)
            val height = (pdfPage.height * 2).coerceAtMost(1440)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPage.close()

            val slideFile = File(imagesDir, "slide_${System.currentTimeMillis()}_$i.jpg")
            FileOutputStream(slideFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val imageElem = ImageElement(
                path = slideFile.absolutePath,
                x = 0f,
                y = 0f,
                width = 1920f,
                height = (1920f * (height.toFloat() / width.toFloat()))
            )
            newPages.add(BoardPage(name = "Slide ${i + 1}", imageElements = listOf(imageElem)))
        }
        renderer.close()
        pfd.close()
        newPages
    }
}
