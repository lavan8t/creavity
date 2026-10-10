package com.kd.creavity.domain.mutation

import com.kd.creavity.domain.model.BoardPage

class MutationHistory(private val maxStackSize: Int = 100) {
    private val undoStack = mutableListOf<BoardMutation>()
    private val redoStack = mutableListOf<BoardMutation>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun record(mutation: BoardMutation) {
        if (undoStack.size >= maxStackSize) {
            undoStack.removeAt(0)
        }
        undoStack.add(mutation)
        redoStack.clear()
    }

    fun undo(currentPage: BoardPage): Pair<BoardPage, BoardMutation?> {
        if (undoStack.isEmpty()) return Pair(currentPage, null)
        val mutation = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(mutation)
        val revertedPage = mutation.revert(currentPage)
        return Pair(revertedPage, mutation)
    }

    fun redo(currentPage: BoardPage): Pair<BoardPage, BoardMutation?> {
        if (redoStack.isEmpty()) return Pair(currentPage, null)
        val mutation = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(mutation)
        val appliedPage = mutation.apply(currentPage)
        return Pair(appliedPage, mutation)
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
