package com.kd.creavity.domain.mutation

import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MutationHistoryTest {

    @Test
    fun addStroke_applyAndUndo() {
        val history = MutationHistory()
        val page = BoardPage(name = "Test Page")
        val stroke = Stroke(
            id = "stroke-1",
            points = listOf(Point(10f, 10f), Point(20f, 20f)),
            tool = DrawingTool.PEN
        )

        val mutation = BoardMutation.AddStroke(stroke)
        val appliedPage = mutation.apply(page)
        history.record(mutation)

        assertEquals(1, appliedPage.strokes.size)
        assertEquals("stroke-1", appliedPage.strokes.first().id)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)

        val (revertedPage, undoneMutation) = history.undo(appliedPage)
        assertEquals(0, revertedPage.strokes.size)
        assertEquals(mutation, undoneMutation)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)

        val (redonePage, redoneMutation) = history.redo(revertedPage)
        assertEquals(1, redonePage.strokes.size)
        assertEquals(mutation, redoneMutation)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun deleteText_applyAndUndo() {
        val text = TextElement(id = "text-1", text = "Hello World")
        val page = BoardPage(name = "Test Page", textElements = listOf(text))
        val history = MutationHistory()

        val mutation = BoardMutation.DeleteText(text)
        val appliedPage = mutation.apply(page)
        history.record(mutation)

        assertEquals(0, appliedPage.textElements.size)

        val (revertedPage, _) = history.undo(appliedPage)
        assertEquals(1, revertedPage.textElements.size)
        assertEquals("Hello World", revertedPage.textElements.first().text)
    }

    @Test
    fun clearPage_applyAndUndo() {
        val stroke = Stroke(id = "s1")
        val text = TextElement(id = "t1", text = "Note")
        val page = BoardPage(strokes = listOf(stroke), textElements = listOf(text))
        val history = MutationHistory()

        val mutation = BoardMutation.ClearPage(listOf(stroke), listOf(text), emptyList())
        val appliedPage = mutation.apply(page)
        history.record(mutation)

        assertTrue(appliedPage.strokes.isEmpty())
        assertTrue(appliedPage.textElements.isEmpty())

        val (revertedPage, _) = history.undo(appliedPage)
        assertEquals(1, revertedPage.strokes.size)
        assertEquals(1, revertedPage.textElements.size)
    }
}
