package com.kd.creavity.data.storage

import android.content.Context
import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BoardManager(private val context: Context) {
    private val boardsDir: File by lazy {
        val publicDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "CreaVITy").apply {
            try { mkdirs() } catch (_: Exception) {}
        }
        val targetDir = if (publicDocs.exists() && publicDocs.canWrite()) {
            publicDocs
        } else {
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)?.let { File(it, "boards").apply { mkdirs() } }
                ?: File(context.filesDir, "saved_boards").apply { mkdirs() }
        }
        // Migrate legacy boards from Brevity Documents folder
        val legacyBrevityDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Brevity")
        if (legacyBrevityDocs.exists() && legacyBrevityDocs != targetDir) {
            legacyBrevityDocs.listFiles { _, name -> name.endsWith(".json") }?.forEach { f ->
                val dest = File(targetDir, f.name)
                if (!dest.exists()) {
                    try { f.copyTo(dest, overwrite = true) } catch (_: Exception) {}
                }
            }
        }
        // Migrate legacy boards from context.filesDir/saved_boards
        val legacyDir = File(context.filesDir, "saved_boards")
        if (legacyDir.exists() && legacyDir != targetDir) {
            legacyDir.listFiles { _, name -> name.endsWith(".json") }?.forEach { f ->
                val dest = File(targetDir, f.name)
                if (!dest.exists()) {
                    try { f.copyTo(dest, overwrite = true) } catch (_: Exception) {}
                }
            }
        }
        targetDir
    }

    @Synchronized
    fun saveBoard(board: Board) {
        if (board.id.isBlank()) return
        val targetFile = File(boardsDir, "${board.id}.json")
        val tempFile = File(boardsDir, "${board.id}.json.tmp")
        try {
            val json = boardToJson(board)
            tempFile.writeText(json.toString())
            if (!tempFile.renameTo(targetFile)) {
                targetFile.writeText(json.toString())
                tempFile.delete()
            }
        } catch (_: Exception) {}
    }

    fun loadBoard(boardId: String): Board? {
        if (boardId.isBlank()) return null
        val file = File(boardsDir, "$boardId.json")
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            jsonToBoard(json)
        } catch (_: Exception) {
            null
        }
    }

    fun loadAllBoards(): List<Board> {
        val files = boardsDir.listFiles { _, name -> name.endsWith(".json") } ?: return emptyList()
        return files.mapNotNull { file ->
            try {
                val json = JSONObject(file.readText())
                jsonToBoard(json)
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.lastAccessedAt }
    }

    fun deleteBoard(boardId: String) {
        val file = File(boardsDir, "$boardId.json")
        if (file.exists()) file.delete()
        val tempFile = File(boardsDir, "$boardId.json.tmp")
        if (tempFile.exists()) tempFile.delete()
    }

    fun cleanEmptyUntitledBoards(excludeBoardId: String? = null) {
        // Deliberately no-op: never silently delete boards behind user back
    }

    fun getCustomFolders(): Set<String> {
        val prefs = context.getSharedPreferences("creavity_folders", Context.MODE_PRIVATE)
        return prefs.getStringSet("folders", emptySet()) ?: emptySet()
    }

    fun saveCustomFolders(folders: Set<String>) {
        val prefs = context.getSharedPreferences("creavity_folders", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("folders", folders).apply()
    }

    private fun boardToJson(board: Board): JSONObject {
        val json = JSONObject()
        json.put("id", board.id)
        json.put("title", board.title)
        json.put("activePageIndex", board.activePageIndex)
        json.put("backgroundColor", board.backgroundColor)
        json.put("createdAt", board.createdAt)
        json.put("modifiedAt", board.modifiedAt)
        json.put("lastAccessedAt", board.lastAccessedAt)
        json.put("createdBy", board.createdBy)
        json.put("ownerName", board.ownerName)
        json.put("isReadOnly", board.isReadOnly)
        json.put("isStarred", board.isStarred)
        json.put("isInBin", board.isInBin)
        board.binnedAt?.let { json.put("binnedAt", it) }
        json.put("isInfiniteCanvas", board.isInfiniteCanvas)
        json.put("canvasWidth", board.canvasWidth.toDouble())
        json.put("canvasHeight", board.canvasHeight.toDouble())
        board.presentationUrl?.let { json.put("presentationUrl", it) }
        json.put("venue", board.venue)
        json.put("group", board.group)
        json.put("labels", JSONArray(board.labels))

        val pagesArray = JSONArray()
        board.pages.forEach { page ->
            val pageObj = JSONObject()
            pageObj.put("id", page.id)
            pageObj.put("name", page.name)

            val strokesArray = JSONArray()
            page.strokes.forEach { stroke ->
                val strokeObj = JSONObject()
                strokeObj.put("id", stroke.id)
                strokeObj.put("color", stroke.color)
                strokeObj.put("strokeWidth", stroke.strokeWidth)
                strokeObj.put("tool", stroke.tool.name)
                strokeObj.put("isEraser", stroke.isEraser)
                strokeObj.put("author", stroke.author)
                strokeObj.put("createdAt", stroke.createdAt)

                val pointsArray = JSONArray()
                stroke.points.forEach { pt ->
                    val ptObj = JSONObject()
                    ptObj.put("x", pt.x.toDouble())
                    ptObj.put("y", pt.y.toDouble())
                    ptObj.put("p", pt.pressure.toDouble())
                    ptObj.put("t", pt.timestampMs)
                    if (pt.tilt != 0f) ptObj.put("tilt", pt.tilt.toDouble())
                    if (pt.orientation != 0f) ptObj.put("ori", pt.orientation.toDouble())
                    pointsArray.put(ptObj)
                }
                strokeObj.put("points", pointsArray)
                strokesArray.put(strokeObj)
            }
            pageObj.put("strokes", strokesArray)

            val textArray = JSONArray()
            page.textElements.forEach { txt ->
                val txtObj = JSONObject()
                txtObj.put("id", txt.id)
                txtObj.put("text", txt.text)
                txtObj.put("x", txt.x.toDouble())
                txtObj.put("y", txt.y.toDouble())
                txtObj.put("color", txt.color)
                txtObj.put("fontSize", txt.fontSize.toDouble())
                txtObj.put("fontName", txt.fontName)
                txtObj.put("author", txt.author)
                txtObj.put("createdAt", txt.createdAt)
                textArray.put(txtObj)
            }
            pageObj.put("textElements", textArray)

            val imageArray = JSONArray()
            page.imageElements.forEach { img ->
                val imgObj = JSONObject()
                imgObj.put("id", img.id)
                imgObj.put("path", img.path)
                imgObj.put("x", img.x.toDouble())
                imgObj.put("y", img.y.toDouble())
                imgObj.put("width", img.width.toDouble())
                imgObj.put("height", img.height.toDouble())
                imgObj.put("author", img.author)
                imgObj.put("createdAt", img.createdAt)
                imageArray.put(imgObj)
            }
            pageObj.put("imageElements", imageArray)

            val stickyArray = JSONArray()
            page.stickyNotes.forEach { sn ->
                val snObj = JSONObject()
                snObj.put("id", sn.id)
                snObj.put("text", sn.text)
                snObj.put("x", sn.x.toDouble())
                snObj.put("y", sn.y.toDouble())
                snObj.put("width", sn.width.toDouble())
                snObj.put("height", sn.height.toDouble())
                snObj.put("color", sn.color)
                snObj.put("author", sn.author)
                snObj.put("createdAt", sn.createdAt)
                stickyArray.put(snObj)
            }
            pageObj.put("stickyNotes", stickyArray)

            pagesArray.put(pageObj)
        }
        json.put("pages", pagesArray)
        return json
    }

    private fun jsonToBoard(json: JSONObject): Board {
        val id = json.getString("id")
        val title = json.optString("title", "Untitled Board")
        val activePageIndex = json.optInt("activePageIndex", 0)
        val backgroundColor = json.optLong("backgroundColor", 0xFF18181C)
        val createdAt = json.optLong("createdAt", System.currentTimeMillis())
        val modifiedAt = json.optLong("modifiedAt", System.currentTimeMillis())
        val lastAccessedAt = json.optLong("lastAccessedAt", modifiedAt)
        val createdBy = json.optString("createdBy", "You")
        val ownerName = json.optString("ownerName", "You")
        val isReadOnly = json.optBoolean("isReadOnly", false)

        val pagesArray = json.optJSONArray("pages") ?: JSONArray()
        val pages = mutableListOf<BoardPage>()

        for (i in 0 until pagesArray.length()) {
            val pageObj = pagesArray.getJSONObject(i)
            val pageId = pageObj.getString("id")
            val pageName = pageObj.optString("name", "Page ${i + 1}")
            val strokesArray = pageObj.optJSONArray("strokes") ?: JSONArray()
            val strokes = mutableListOf<Stroke>()

            for (j in 0 until strokesArray.length()) {
                val strokeObj = strokesArray.getJSONObject(j)
                val strokeId = strokeObj.getString("id")
                val color = strokeObj.optLong("color", 0xFFFFFFFF)
                val width = strokeObj.optDouble("strokeWidth", 5.0).toFloat()
                val toolName = strokeObj.optString("tool", "PEN")
                val tool = try { DrawingTool.valueOf(toolName) } catch (_: Exception) { DrawingTool.PEN }
                val isEraser = strokeObj.optBoolean("isEraser", tool == DrawingTool.ERASER)
                val author = strokeObj.optString("author", "You")
                val strokeCreatedAt = strokeObj.optLong("createdAt", System.currentTimeMillis())

                val pointsArray = strokeObj.optJSONArray("points") ?: JSONArray()
                val points = mutableListOf<Point>()
                for (k in 0 until pointsArray.length()) {
                    val ptObj = pointsArray.getJSONObject(k)
                    points.add(
                        Point(
                            x = ptObj.getDouble("x").toFloat(),
                            y = ptObj.getDouble("y").toFloat(),
                            pressure = ptObj.optDouble("p", 1.0).toFloat(),
                            timestampMs = ptObj.optLong("t", 0L),
                            tilt = ptObj.optDouble("tilt", 0.0).toFloat(),
                            orientation = ptObj.optDouble("ori", 0.0).toFloat()
                        )
                    )
                }
                strokes.add(Stroke(strokeId, points, color, width, tool, isEraser, author, strokeCreatedAt))
            }

            val textArray = pageObj.optJSONArray("textElements") ?: JSONArray()
            val textElements = mutableListOf<TextElement>()
            for (t in 0 until textArray.length()) {
                val txtObj = textArray.getJSONObject(t)
                textElements.add(
                    TextElement(
                        id = txtObj.getString("id"),
                        text = txtObj.optString("text", ""),
                        x = txtObj.optDouble("x", 0.0).toFloat(),
                        y = txtObj.optDouble("y", 0.0).toFloat(),
                        color = txtObj.optLong("color", 0xFFFFFFFF),
                        fontSize = txtObj.optDouble("fontSize", 24.0).toFloat(),
                        fontName = txtObj.optString("fontName", "Google Sans Flex"),
                        author = txtObj.optString("author", "You"),
                        createdAt = txtObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val imageArray = pageObj.optJSONArray("imageElements") ?: JSONArray()
            val imageElements = mutableListOf<ImageElement>()
            for (im in 0 until imageArray.length()) {
                val imgObj = imageArray.getJSONObject(im)
                imageElements.add(
                    ImageElement(
                        id = imgObj.getString("id"),
                        path = imgObj.optString("path", ""),
                        x = imgObj.optDouble("x", 0.0).toFloat(),
                        y = imgObj.optDouble("y", 0.0).toFloat(),
                        width = imgObj.optDouble("width", 240.0).toFloat(),
                        height = imgObj.optDouble("height", 240.0).toFloat(),
                        author = imgObj.optString("author", "You"),
                        createdAt = imgObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val stickyArray = pageObj.optJSONArray("stickyNotes") ?: JSONArray()
            val stickyNotes = mutableListOf<StickyNote>()
            for (s in 0 until stickyArray.length()) {
                val snObj = stickyArray.getJSONObject(s)
                stickyNotes.add(
                    StickyNote(
                        id = snObj.getString("id"),
                        text = snObj.optString("text", ""),
                        x = snObj.optDouble("x", 0.0).toFloat(),
                        y = snObj.optDouble("y", 0.0).toFloat(),
                        width = snObj.optDouble("width", 220.0).toFloat(),
                        height = snObj.optDouble("height", 220.0).toFloat(),
                        color = snObj.optLong("color", 0xFFFFF176),
                        author = snObj.optString("author", "You"),
                        createdAt = snObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            pages.add(BoardPage(pageId, pageName, strokes, textElements, imageElements, stickyNotes))
        }

        val safePages = if (pages.isEmpty()) listOf(BoardPage(name = "Page 1")) else pages
        val safeIndex = activePageIndex.coerceIn(0, safePages.size - 1)

        val isStarred = json.optBoolean("isStarred", false)
        val isInBin = json.optBoolean("isInBin", false)
        val binnedAt = if (json.has("binnedAt") && !json.isNull("binnedAt")) json.getLong("binnedAt") else null
        val isInfiniteCanvas = json.optBoolean("isInfiniteCanvas", true)
        val canvasWidth = json.optDouble("canvasWidth", 1920.0).toFloat()
        val canvasHeight = json.optDouble("canvasHeight", 1080.0).toFloat()
        val presentationUrl = if (json.has("presentationUrl") && !json.isNull("presentationUrl")) json.getString("presentationUrl") else null
        val venue = json.optString("venue", "")
        val group = json.optString("group", "")
        val labelsArray = json.optJSONArray("labels")
        val labels = if (labelsArray != null) {
            (0 until labelsArray.length()).map { labelsArray.optString(it, "") }
        } else {
            emptyList()
        }

        return Board(
            id = id,
            title = title,
            pages = safePages,
            activePageIndex = safeIndex,
            backgroundColor = backgroundColor,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            lastAccessedAt = lastAccessedAt,
            createdBy = createdBy,
            ownerName = ownerName,
            isStarred = isStarred,
            isReadOnly = isReadOnly,
            isInBin = isInBin,
            binnedAt = binnedAt,
            isInfiniteCanvas = isInfiniteCanvas,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            presentationUrl = presentationUrl,
            labels = labels,
            venue = venue,
            group = group
        )
    }
}
