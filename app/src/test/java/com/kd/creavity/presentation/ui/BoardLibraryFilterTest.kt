package com.kd.creavity.presentation.ui

import com.kd.creavity.domain.model.Board
import com.kd.creavity.presentation.ui.screens.OwnershipFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardLibraryFilterTest {

    private val localUser = "You"

    private val board1 = Board(
        id = "b1",
        title = "My Local Board",
        createdBy = "You",
        ownerName = "You",
        labels = listOf("Sprint", "UI")
    )

    private val board2 = Board(
        id = "b2",
        title = "Team Diagram",
        createdBy = "Alice",
        ownerName = "Alice",
        labels = listOf("UI", "Architecture")
    )

    private val board3 = Board(
        id = "b3",
        title = "Math Notes",
        createdBy = "Bob",
        ownerName = "Bob",
        labels = listOf("Math")
    )

    private val board4 = Board(
        id = "b4",
        title = "Shared with me",
        createdBy = "Charlie",
        ownerName = "Charlie",
        labels = emptyList()
    )

    private val allBoards = listOf(board1, board2, board3, board4)

    @Test
    fun testOwnershipFilter_All_returnsAllBoards() {
        val filter = OwnershipFilter.ALL
        val result = when (filter) {
            OwnershipFilter.ALL -> allBoards
            OwnershipFilter.OWNED_BY_ME -> allBoards.filter { it.createdBy.equals(localUser, true) || it.ownerName.equals(localUser, true) }
            OwnershipFilter.SHARED_WITH_ME -> allBoards.filter { !it.createdBy.equals(localUser, true) && !it.ownerName.equals(localUser, true) }
        }
        assertEquals(4, result.size)
    }

    @Test
    fun testOwnershipFilter_OwnedByMe_filtersCorrectly() {
        val filter = OwnershipFilter.OWNED_BY_ME
        val result = when (filter) {
            OwnershipFilter.ALL -> allBoards
            OwnershipFilter.OWNED_BY_ME -> allBoards.filter { it.createdBy.equals(localUser, true) || it.ownerName.equals(localUser, true) }
            OwnershipFilter.SHARED_WITH_ME -> allBoards.filter { !it.createdBy.equals(localUser, true) && !it.ownerName.equals(localUser, true) }
        }
        assertEquals(1, result.size)
        assertEquals("b1", result.first().id)
    }

    @Test
    fun testOwnershipFilter_SharedWithMe_filtersCorrectly() {
        val filter = OwnershipFilter.SHARED_WITH_ME
        val result = when (filter) {
            OwnershipFilter.ALL -> allBoards
            OwnershipFilter.OWNED_BY_ME -> allBoards.filter { it.createdBy.equals(localUser, true) || it.ownerName.equals(localUser, true) }
            OwnershipFilter.SHARED_WITH_ME -> allBoards.filter { !it.createdBy.equals(localUser, true) && !it.ownerName.equals(localUser, true) }
        }
        assertEquals(3, result.size)
        assertTrue(result.none { it.id == "b1" })
    }

    @Test
    fun testDistinctLabelsExtraction_flattensAndSortsAlphabetically() {
        val distinctLabels = allBoards
            .flatMap { it.labels }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        assertEquals(listOf("Architecture", "Math", "Sprint", "UI"), distinctLabels)
    }

    @Test
    fun testMultiLabelFiltering_returnsMatchingSubset() {
        // Filter by "UI"
        val selectedLabels = setOf("UI")
        val result = allBoards.filter { b ->
            selectedLabels.all { sel -> b.labels.any { l -> l.equals(sel, ignoreCase = true) } }
        }
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "b1" })
        assertTrue(result.any { it.id == "b2" })

        // Filter by both "UI" and "Sprint"
        val multiSelected = setOf("UI", "Sprint")
        val multiResult = allBoards.filter { b ->
            multiSelected.all { sel -> b.labels.any { l -> l.equals(sel, ignoreCase = true) } }
        }
        assertEquals(1, multiResult.size)
        assertEquals("b1", multiResult.first().id)
    }
}
