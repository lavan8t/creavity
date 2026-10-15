package com.kd.creavity.data.storage

import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.util.getAppStorageDir
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BoardManager(
    val boardsDir: File = File(getAppStorageDir(), "boards").apply { mkdirs() }
) {
    private val foldersFile = File(boardsDir.parentFile ?: boardsDir, "folders.json")

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
        return try {
            if (!foldersFile.exists()) emptySet()
            else {
                val jsonArr = JSONArray(foldersFile.readText())
                (0 until jsonArr.length()).map { jsonArr.getString(it) }.toSet()
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun saveCustomFolders(folders: Set<String>) {
        try {
            val jsonArr = JSONArray(folders)
            foldersFile.writeText(jsonArr.toString())
        } catch (_: Exception) {}
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
                strokeObj.put("strokeWidth", stroke.strokeWidth.toDouble())
                strokeObj.put("tool", stroke.tool.name)
                strokeObj.put("isEraser", stroke.isEraser)
                strokeObj.put("author", stroke.author)
                strokeObj.put("createdAt", stroke.createdAt)

                val pointsArray = JSONArray()
                stroke.points.forEach { point ->
                    val ptObj = JSONObject()
                    ptObj.put("x", point.x.toDouble())
                    ptObj.put("y", point.y.toDouble())
                    ptObj.put("pressure", point.pressure.toDouble())
                    ptObj.put("timestampMs", point.timestampMs)
                    pointsArray.put(ptObj)
                }
                strokeObj.put("points", pointsArray)
                strokesArray.put(strokeObj)
            }
            pageObj.put("strokes", strokesArray)

            val textArray = JSONArray()
            page.textElements.forEach { textElem ->
                val txtObj = JSONObject()
                txtObj.put("id", textElem.id)
                txtObj.put("text", textElem.text)
                txtObj.put("x", textElem.x.toDouble())
                txtObj.put("y", textElem.y.toDouble())
                txtObj.put("color", textElem.color)
                txtObj.put("fontSize", textElem.fontSize.toDouble())
                txtObj.put("fontName", textElem.fontName)
                txtObj.put("author", textElem.author)
                txtObj.put("createdAt", textElem.createdAt)
                textArray.put(txtObj)
            }
            pageObj.put("textElements", textArray)

            val imgArray = JSONArray()
            page.imageElements.forEach { imgElem ->
                val imgObj = JSONObject()
                imgObj.put("id", imgElem.id)
                imgObj.put("path", imgElem.path)
                imgObj.put("x", imgElem.x.toDouble())
                imgObj.put("y", imgElem.y.toDouble())
                imgObj.put("width", imgElem.width.toDouble())
                imgObj.put("height", imgElem.height.toDouble())
                imgObj.put("author", imgElem.author)
                imgObj.put("createdAt", imgElem.createdAt)
                imgArray.put(imgObj)
            }
            pageObj.put("imageElements", imgArray)

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
        val title = json.getString("title")
        val activePageIndex = json.optInt("activePageIndex", 0)
        val backgroundColor = json.optLong("backgroundColor", 0xFFFBF8F6)
        val createdAt = json.optLong("createdAt", System.currentTimeMillis())
        val modifiedAt = json.optLong("modifiedAt", createdAt)
        val lastAccessedAt = json.optLong("lastAccessedAt", modifiedAt)
        val createdBy = json.optString("createdBy", "You")
        val ownerName = json.optString("ownerName", createdBy)
        val isReadOnly = json.optBoolean("isReadOnly", false)

        val pagesArray = json.optJSONArray("pages") ?: JSONArray()
        val pages = mutableListOf<BoardPage>()

        for (p in 0 until pagesArray.length()) {
            val pageObj = pagesArray.getJSONObject(p)
            val pageId = pageObj.optString("id", "")
            val pageName = pageObj.optString("name", "Page ${p + 1}")

            val strokesArray = pageObj.optJSONArray("strokes") ?: JSONArray()
            val strokes = mutableListOf<Stroke>()
            for (s in 0 until strokesArray.length()) {
                val strokeObj = strokesArray.getJSONObject(s)
                val ptsArray = strokeObj.optJSONArray("points") ?: JSONArray()
                val points = mutableListOf<Point>()
                for (pt in 0 until ptsArray.length()) {
                    val ptObj = ptsArray.getJSONObject(pt)
                    points.add(
                        Point(
                            x = ptObj.getDouble("x").toFloat(),
                            y = ptObj.getDouble("y").toFloat(),
                            pressure = ptObj.optDouble("pressure", 1.0).toFloat(),
                            timestampMs = ptObj.optLong("timestampMs", 0L)
                        )
                    )
                }

                val toolStr = strokeObj.optString("tool", DrawingTool.PEN.name)
                val tool = runCatching { DrawingTool.valueOf(toolStr) }.getOrDefault(DrawingTool.PEN)

                strokes.add(
                    Stroke(
                        id = strokeObj.getString("id"),
                        points = points,
                        color = strokeObj.optLong("color", 0xFF1B1B1F),
                        strokeWidth = strokeObj.optDouble("strokeWidth", 6.0).toFloat(),
                        tool = tool,
                        isEraser = strokeObj.optBoolean("isEraser", false),
                        author = strokeObj.optString("author", "You"),
                        createdAt = strokeObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val textArray = pageObj.optJSONArray("textElements") ?: JSONArray()
            val textElements = mutableListOf<TextElement>()
            for (t in 0 until textArray.length()) {
                val txtObj = textArray.getJSONObject(t)
                textElements.add(
                    TextElement(
                        id = txtObj.getString("id"),
                        text = txtObj.getString("text"),
                        x = txtObj.getDouble("x").toFloat(),
                        y = txtObj.getDouble("y").toFloat(),
                        color = txtObj.optLong("color", 0xFF1B1B1F),
                        fontSize = txtObj.optDouble("fontSize", 24.0).toFloat(),
                        fontName = txtObj.optString("fontName", "Google Sans Flex"),
                        author = txtObj.optString("author", "You"),
                        createdAt = txtObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val imgArray = pageObj.optJSONArray("imageElements") ?: JSONArray()
            val imageElements = mutableListOf<ImageElement>()
            for (m in 0 until imgArray.length()) {
                val imgObj = imgArray.getJSONObject(m)
                imageElements.add(
                    ImageElement(
                        id = imgObj.getString("id"),
                        path = imgObj.getString("path"),
                        x = imgObj.getDouble("x").toFloat(),
                        y = imgObj.getDouble("y").toFloat(),
                        width = imgObj.optDouble("width", 320.0).toFloat(),
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
