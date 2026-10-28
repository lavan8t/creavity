package com.kd.creavity.presentation.canvas

import androidx.compose.ui.input.pointer.PointerIcon
import com.kd.creavity.domain.model.DrawingTool
import org.junit.Assert.assertNotNull
import org.junit.Test

class ToolPointerIconTest {

    @Test
    fun testEveryToolReturnsValidPointerIcon() {
        for (tool in DrawingTool.entries) {
            val icon = getToolPointerIcon(tool)
            assertNotNull("ToolPointerIcon for $tool should not be null", icon)
            applyToolCursor(tool)
        }
    }

    @Test
    fun testAwtCursorCreationForAllTools() {
        for (tool in DrawingTool.entries) {
            val cursor = getAwtToolCursor(tool)
            assertNotNull("AWT Cursor for $tool should not be null", cursor)
        }
    }
}
