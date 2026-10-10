package com.kd.creavity.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.kd.creavity.data.storage.BoardManager
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import java.io.File
import java.io.FileOutputStream

enum class ExportFormat(
    val extension: String,
    val mimeType: String,
    val displayName: String,
    val description: String,
    val iconName: String
) {
    PDF("pdf", "application/pdf", "PDF Document", "Multi-page vector document for sharing or printing", "picture_as_pdf"),
    PNG("png", "image/png", "PNG Image", "High-resolution raster image of the active board", "image"),
    SVG("svg", "image/svg+xml", "SVG Vector", "Scalable vector graphic compatible with Figma & Illustrator", "auto_awesome"),
    CREAVITY("creavity", "application/json", "CreaVITy Backup", "Native lossless whiteboard file for backup and transfer", "folder"),
    BREVITY("breVITy", "application/json", "CreaVITy Backup", "Native lossless whiteboard file for backup and transfer", "folder")
}

object BoardExporter {

    fun exportBoard(context: Context, board: Board, format: ExportFormat, pageIndex: Int = board.activePageIndex): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val sanitizedTitle = board.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").trim('_').ifBlank { "Whiteboard" }
        val targetFile = File(exportDir, "$sanitizedTitle.${format.extension}")

        when (format) {
            ExportFormat.PDF -> exportToPdf(board, targetFile)
            ExportFormat.PNG -> exportToPng(board, pageIndex, targetFile)
            ExportFormat.SVG -> exportToSvg(board, pageIndex, targetFile)
            ExportFormat.CREAVITY,
            ExportFormat.BREVITY -> exportToCreavity(context, board, targetFile)
        }

        return targetFile
    }

    fun shareBoard(context: Context, board: Board, format: ExportFormat, pageIndex: Int = board.activePageIndex) {
        val file = exportBoard(context, board, format, pageIndex)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, board.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Export ${board.title} as ${format.displayName}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun exportToPdf(board: Board, file: File) {
        val pdfDoc = PdfDocument()
        val width = 1920
        val height = 1080

        board.pages.forEachIndexed { index, page ->
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, index + 1).create()
            val pdfPage = pdfDoc.startPage(pageInfo)
            val canvas = pdfPage.canvas

            renderPageToCanvas(canvas, board.backgroundColor, page, width.toFloat(), height.toFloat())

            pdfDoc.finishPage(pdfPage)
        }

        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
    }

    private fun exportToPng(board: Board, pageIndex: Int, file: File) {
        val width = 1920
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val page = board.pages.getOrElse(pageIndex) { board.pages.firstOrNull() ?: BoardPage() }
        renderPageToCanvas(canvas, board.backgroundColor, page, width.toFloat(), height.toFloat())

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
    }

    private fun renderPageToCanvas(canvas: Canvas, backgroundColor: Long, page: BoardPage, width: Float, height: Float) {
        // 1. Background
        val bgPaint = Paint().apply {
            color = backgroundColor.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Strokes
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        page.strokes.forEach { stroke ->
            if (stroke.points.size < 2 || stroke.isEraser) return@forEach

            strokePaint.color = stroke.color.toInt()
            strokePaint.strokeWidth = stroke.strokeWidth

            when (stroke.tool) {
                DrawingTool.HIGHLIGHTER -> {
                    strokePaint.alpha = 90
                    strokePaint.strokeCap = Paint.Cap.SQUARE
                    strokePaint.strokeWidth = stroke.strokeWidth * 3f
                }
                DrawingTool.MARKER -> {
                    strokePaint.alpha = 255
                    strokePaint.strokeCap = Paint.Cap.ROUND
                    strokePaint.strokeWidth = stroke.strokeWidth * 2f
                }
                else -> {
                    strokePaint.alpha = 255
                    strokePaint.strokeCap = Paint.Cap.ROUND
                }
            }

            val path = android.graphics.Path()
            val points = stroke.points
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                path.quadTo(p0.x, p0.y, (p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
            }
            path.lineTo(points.last().x, points.last().y)
            canvas.drawPath(path, strokePaint)
        }

        // 3. Sticky Notes
        val notePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF1C1B1F.toInt()
            textSize = 20f
        }

        page.stickyNotes.forEach { note ->
            notePaint.color = note.color.toInt()
            val rect = RectF(note.x, note.y, note.x + note.width, note.y + note.height)
            canvas.drawRoundRect(rect, 16f, 16f, notePaint)
            if (note.text.isNotBlank()) {
                val lines = note.text.lines()
                var yOffset = note.y + 36f
                lines.forEach { line ->
                    if (yOffset < note.y + note.height - 12f) {
                        canvas.drawText(line, note.x + 16f, yOffset, textPaint)
                        yOffset += 26f
                    }
                }
            }
        }

        // 4. Text Elements
        val freeTextPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        page.textElements.forEach { txt ->
            freeTextPaint.color = txt.color.toInt()
            freeTextPaint.textSize = txt.fontSize
            canvas.drawText(txt.text, txt.x, txt.y + txt.fontSize, freeTextPaint)
        }
    }

    private fun exportToSvg(board: Board, pageIndex: Int, file: File) {
        val page = board.pages.getOrElse(pageIndex) { board.pages.firstOrNull() ?: BoardPage() }
        val width = 1920
        val height = 1080
        val bgHex = String.format("#%06X", 0xFFFFFF and board.backgroundColor.toInt())

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" width="$width" height="$height">""").append("\n")
        sb.append("""  <rect width="100%" height="100%" fill="$bgHex" />""").append("\n")

        // Strokes
        page.strokes.forEach { stroke ->
            if (stroke.points.size < 2 || stroke.isEraser) return@forEach
            val colorHex = String.format("#%06X", 0xFFFFFF and stroke.color.toInt())
            val opacity = if (stroke.tool == DrawingTool.HIGHLIGHTER) "0.35" else "1.0"
            val sw = stroke.strokeWidth * (if (stroke.tool == DrawingTool.HIGHLIGHTER) 3f else 1f)

            val d = buildSvgPathData(stroke.points)
            sb.append("""  <path d="$d" fill="none" stroke="$colorHex" stroke-width="$sw" stroke-linecap="round" stroke-linejoin="round" opacity="$opacity" />""").append("\n")
        }

        // Sticky notes
        page.stickyNotes.forEach { note ->
            val noteHex = String.format("#%06X", 0xFFFFFF and note.color.toInt())
            sb.append("""  <rect x="${note.x}" y="${note.y}" width="${note.width}" height="${note.height}" rx="12" fill="$noteHex" />""").append("\n")
            sb.append("""  <text x="${note.x + 16}" y="${note.y + 36}" font-size="20" fill="#1C1B1F" font-family="sans-serif">${escapeXml(note.text)}</text>""").append("\n")
        }

        // Text elements
        page.textElements.forEach { txt ->
            val colorHex = String.format("#%06X", 0xFFFFFF and txt.color.toInt())
            sb.append("""  <text x="${txt.x}" y="${txt.y + txt.fontSize}" font-size="${txt.fontSize}" fill="$colorHex" font-family="sans-serif">${escapeXml(txt.text)}</text>""").append("\n")
        }

        sb.append("</svg>\n")
        file.writeText(sb.toString())
    }

    private fun buildSvgPathData(points: List<Point>): String {
        if (points.isEmpty()) return ""
        val sb = StringBuilder()
        sb.append("M ${points[0].x} ${points[0].y}")
        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            sb.append(" Q ${p0.x} ${p0.y} $midX $midY")
        }
        sb.append(" L ${points.last().x} ${points.last().y}")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun exportToCreavity(context: Context, board: Board, file: File) {
        val bm = BoardManager(context)
        val tempSaved = bm.loadBoard(board.id)
        if (tempSaved != null) {
            val sourceFile = File(File(context.filesDir, "saved_boards"), "${board.id}.json")
            if (sourceFile.exists()) {
                sourceFile.copyTo(file, overwrite = true)
                return
            }
        }
        bm.saveBoard(board)
        val sourceFile = File(File(context.filesDir, "saved_boards"), "${board.id}.json")
        if (sourceFile.exists()) {
            sourceFile.copyTo(file, overwrite = true)
        }
    }
}
