package com.kd.creavity.presentation.ui

import com.kd.creavity.domain.model.Board
import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.BoardVersionSnapshot
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.domain.mutation.BoardMutation
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewOnlyAndVersionHistoryTest {

    @Test
    fun testIsCanvasReadOnly_whenBoardIsReadOnly_returnsTrue() {
        val board = Board(id = "b1", title = "Read Only Board", isReadOnly = true)
        val state = WhiteboardUiState(currentBoard = board, myAccessReadOnly = false)
        assertTrue(state.isCanvasReadOnly)
    }

    @Test
    fun testIsCanvasReadOnly_whenPeerAccessReadOnly_returnsTrue() {
        val board = Board(id = "b1", title = "Editable Board", isReadOnly = false)
        val state = WhiteboardUiState(currentBoard = board, myAccessReadOnly = true)
        assertTrue(state.isCanvasReadOnly)
    }

    @Test
    fun testIsCanvasReadOnly_whenNeitherReadOnly_returnsFalse() {
        val board = Board(id = "b1", title = "Editable Board", isReadOnly = false)
        val state = WhiteboardUiState(currentBoard = board, myAccessReadOnly = false)
        assertFalse(state.isCanvasReadOnly)
    }

    @Test
    fun testBoardVersionSnapshot_capturesAllElementTypes() {
        val stroke = Stroke(id = "s1", points = listOf(Point(10f, 10f), Point(20f, 20f)))
        val text = TextElement(id = "t1", text = "Architecture Plan", x = 100f, y = 100f)
        val note = StickyNote(id = "n1", text = "Note 1", x = 50f, y = 50f)

        val snapshot = BoardVersionSnapshot(
            id = "snap-1",
            name = "Sprint 1 Snapshot",
            author = "Alice",
            strokes = listOf(stroke),
            textElements = listOf(text),
            stickyNotes = listOf(note)
        )

        assertEquals("snap-1", snapshot.id)
        assertEquals("Sprint 1 Snapshot", snapshot.name)
        assertEquals("Alice", snapshot.author)
        assertEquals(1, snapshot.strokes.size)
        assertEquals(1, snapshot.textElements.size)
        assertEquals(1, snapshot.stickyNotes.size)
    }

    @Test
    fun testRestoreVersionMutation_restoresAllElementsAndReverts() {
        val originalStroke = Stroke(id = "orig_s", points = listOf(Point(0f, 0f)))
        val originalText = TextElement(id = "orig_t", text = "Old Text", x = 0f, y = 0f)

        val newStroke = Stroke(id = "snap_s", points = listOf(Point(50f, 50f)))
        val newText = TextElement(id = "snap_t", text = "New Restored Text", x = 50f, y = 50f)
        val newNote = StickyNote(id = "snap_n", text = "New Note", x = 10f, y = 10f)

        val initialPage = BoardPage(
            id = "p1",
            strokes = listOf(originalStroke),
            textElements = listOf(originalText)
        )

        val mutation = BoardMutation.RestoreVersion(
            previousStrokes = initialPage.strokes,
            newStrokes = listOf(newStroke),
            previousTexts = initialPage.textElements,
            newTexts = listOf(newText),
            previousStickyNotes = initialPage.stickyNotes,
            newStickyNotes = listOf(newNote)
        )

        // Apply mutation
        val restoredPage = mutation.apply(initialPage)
        assertEquals(1, restoredPage.strokes.size)
        assertEquals("snap_s", restoredPage.strokes.first().id)
        assertEquals(1, restoredPage.textElements.size)
        assertEquals("snap_t", restoredPage.textElements.first().id)
        assertEquals(1, restoredPage.stickyNotes.size)
        assertEquals("snap_n", restoredPage.stickyNotes.first().id)

        // Revert mutation (undo)
        val revertedPage = mutation.revert(restoredPage)
        assertEquals(1, revertedPage.strokes.size)
        assertEquals("orig_s", revertedPage.strokes.first().id)
        assertEquals(1, revertedPage.textElements.size)
        assertEquals("orig_t", revertedPage.textElements.first().id)
        assertTrue(revertedPage.stickyNotes.isEmpty())
    }
}
