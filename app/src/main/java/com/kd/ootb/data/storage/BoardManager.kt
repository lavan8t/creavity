package com.kd.ootb.data.storage

import android.content.Context
import com.kd.ootb.domain.model.Board
import com.kd.ootb.domain.model.BoardPage
import com.kd.ootb.domain.model.DrawingTool
import com.kd.ootb.domain.model.ImageElement
import com.kd.ootb.domain.model.Point
import com.kd.ootb.domain.model.Stroke
import com.kd.ootb.domain.model.TextElement
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BoardManager(context: Context) {
    private val boardsDir = File(context.filesDir, "saved_boards").apply { mkdirs() }

    fun saveBoard(board: Board) {
        val file = File(boardsDir, "${board.id}.json")
        val json = boardToJson(board)
        file.writeText(json.toString())
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
        }.sortedByDescending { it.modifiedAt }
    }

    fun deleteBoard(boardId: String) {
        val file = File(boardsDir, "$boardId.json")
        if (file.exists()) file.delete()
    }

    fun cleanEmptyUntitledBoards(excludeBoardId: String? = null) {
        val files = boardsDir.listFiles { _, name -> name.endsWith(".json") } ?: return
        files.forEach { file ->
            try {
                val json = JSONObject(file.readText())
                val id = json.optString("id")
                val title = json.optString("title", "Untitled Board")
                val isUntitled = title.isBlank() || title == "Untitled Board" || title.startsWith("Untitled Board") || title == "My Board"
                if (id.isNotEmpty() && id != excludeBoardId && isUntitled) {
                    val pagesArray = json.optJSONArray("pages") ?: JSONArray()
                    var hasContent = false
                    for (i in 0 until pagesArray.length()) {
                        val pObj = pagesArray.getJSONObject(i)
                        val strokes = pObj.optJSONArray("strokes") ?: JSONArray()
                        val texts = pObj.optJSONArray("textElements") ?: JSONArray()
                        val images = pObj.optJSONArray("imageElements") ?: JSONArray()
                        if (strokes.length() > 0 || texts.length() > 0 || images.length() > 0) {
                            hasContent = true
                            break
                        }
                    }
                    if (!hasContent) {
                        file.delete()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun boardToJson(board: Board): JSONObject {
        val json = JSONObject()
        json.put("id", board.id)
        json.put("title", board.title)
        json.put("activePageIndex", board.activePageIndex)
        json.put("backgroundColor", board.backgroundColor)
        json.put("createdAt", board.createdAt)
        json.put("modifiedAt", board.modifiedAt)
        json.put("createdBy", board.createdBy)
        json.put("ownerName", board.ownerName)
        json.put("isReadOnly", board.isReadOnly)

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
                            timestampMs = ptObj.optLong("t", 0L)
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
                        fontName = txtObj.optString("fontName", "Roboto Flex"),
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

            pages.add(BoardPage(pageId, pageName, strokes, textElements, imageElements))
        }

        val safePages = if (pages.isEmpty()) listOf(BoardPage(name = "Page 1")) else pages
        val safeIndex = activePageIndex.coerceIn(0, safePages.size - 1)

        return Board(
            id = id,
            title = title,
            pages = safePages,
            activePageIndex = safeIndex,
            backgroundColor = backgroundColor,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            createdBy = createdBy,
            ownerName = ownerName,
            isReadOnly = isReadOnly
        )
    }
}
