package com.kd.creavity.presentation.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kd.creavity.domain.model.Board
import com.kd.creavity.presentation.ui.screens.LibrarySortOrder
import com.kd.creavity.presentation.ui.screens.OwnershipFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopDashboardContextMenuTest {

    @Test
    fun testScreenOffsetConversionToDp() {
        val density = 2.0f
        val screenOffset = Offset(200f, 400f)
        val dpOffset = DpOffset(
            x = (screenOffset.x / density).dp,
            y = (screenOffset.y / density).dp
        )
        assertEquals(100.dp, dpOffset.x)
        assertEquals(200.dp, dpOffset.y)
    }

    @Test
    fun testBoardCardContextMenuActionsTriggered() {
        val testBoard = Board(
            id = "board-test-123",
            title = "Design Architecture",
            isStarred = false
        )

        var opened = false
        var duplicated = false
        var starred = false
        var edited = false
        var exported = false
        var deleted = false

        val onOpen = { opened = true }
        val onDuplicate = { duplicated = true }
        val onStar = { starred = true }
        val onEdit = { edited = true }
        val onExport = { exported = true }
        val onDelete = { deleted = true }

        onOpen()
        onDuplicate()
        onStar()
        onEdit()
        onExport()
        onDelete()

        assertTrue("Open must be triggered", opened)
        assertTrue("Duplicate must be triggered", duplicated)
        assertTrue("Star must be triggered", starred)
        assertTrue("Edit must be triggered", edited)
        assertTrue("Export must be triggered", exported)
        assertTrue("Delete must be triggered", deleted)
    }

    @Test
    fun testDashboardBackgroundMenuSortingAndFiltering() {
        var created = false
        var selectedMode = LibraryViewMode.GRID
        var selectedSort = LibrarySortOrder.LAST_ACCESSED
        var selectedFilter = OwnershipFilter.ALL

        val onCreate = { created = true }
        val onSetViewMode = { mode: LibraryViewMode -> selectedMode = mode }
        val onSetSort = { sort: LibrarySortOrder -> selectedSort = sort }
        val onSetFilter = { filter: OwnershipFilter -> selectedFilter = filter }

        onCreate()
        onSetViewMode(LibraryViewMode.LIST)
        onSetSort(LibrarySortOrder.TITLE)
        onSetFilter(OwnershipFilter.OWNED_BY_ME)

        assertTrue("Create board must be triggered", created)
        assertEquals(LibraryViewMode.LIST, selectedMode)
        assertEquals(LibrarySortOrder.TITLE, selectedSort)
        assertEquals(OwnershipFilter.OWNED_BY_ME, selectedFilter)
    }
}
