package com.kd.creavity.domain.mutation

import com.kd.creavity.domain.model.BoardPage

class MutationHistory(private val maxStackSize: Int = 100) {
    private val undoStack = ArrayDeque<BoardMutation>()
    private val redoStack = ArrayDeque<BoardMutation>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun record(mutation: BoardMutation) {
        if (undoStack.size >= maxStackSize) {
            undoStack.removeFirst()
        }
        undoStack.addLast(mutation)
        redoStack.clear()
    }

    fun undo(currentPage: BoardPage): Pair<BoardPage, BoardMutation?> {
        if (undoStack.isEmpty()) return Pair(currentPage, null)
        val mutation = undoStack.removeLast()
        redoStack.addLast(mutation)
        val revertedPage = mutation.revert(currentPage)
        return Pair(revertedPage, mutation)
    }

    fun redo(currentPage: BoardPage): Pair<BoardPage, BoardMutation?> {
        if (redoStack.isEmpty()) return Pair(currentPage, null)
        val mutation = redoStack.removeLast()
        undoStack.addLast(mutation)
        val appliedPage = mutation.apply(currentPage)
        return Pair(appliedPage, mutation)
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
