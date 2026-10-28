package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kd.creavity.presentation.ui.symbols.generated.*

/**
 * Material Symbol rendering component using official Google Material Symbols Rounded
 * (optical size: 24dp, weight: 700, fill: 1, round: 50).
 */
@Composable
fun MaterialSymbol(
    name: String,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    val imageVector: ImageVector = when (name) {
        "draw", "pen" -> draw
        "brush" -> brush
        "edit", "pencil", "edit_outline" -> edit
        "calligraphy", "history_edu", "fountain_pen", "border_color" -> edit
        "highlighter", "ink_highlighter" -> ink_highlighter
        "line", "horizontal_rule" -> arrow_right_alt
        "arrow", "arrow_forward" -> arrow_forward
        "arrow_right_alt" -> arrow_right_alt
        "arrow_upward", "north" -> arrow_upward
        "arrow_downward", "south" -> arrow_downward
        "arrow_back", "back" -> arrow_back
        "rectangle", "crop_square", "filter_none" -> rectangle
        "circle", "radio_button_unchecked" -> circle
        "shapes", "category" -> category
        "triangle", "change_history" -> change_history
        "diamond" -> diamond
        "cylinder", "database", "storage" -> storage
        "text", "title" -> title
        "text_fields" -> text_fields
        "text_format" -> text_format
        "eraser", "ink_eraser" -> ink_eraser
        "cleaning_services" -> cleaning_services
        "select_erase", "highlight_off" -> highlight_off
        "rect_select", "crop_free", "box_select", "fit_screen", "filter_center_focus" -> crop_free
        "select_move", "move", "select", "pan_tool_alt" -> pan_tool_alt
        "lasso", "lasso_select", "gesture", "freeform", "all_inclusive" -> gesture
        "select_all" -> select_all
        "deselect" -> deselect
        "checklist" -> check_circle
        "check_circle" -> check_circle
        "check" -> check
        "menu" -> menu
        "undo" -> undo
        "redo" -> redo
        "restart_alt" -> restart_alt
        "chevron_left" -> chevron_left
        "chevron_right" -> chevron_right
        "add" -> add
        "delete", "bin", "trash", "delete_outline" -> delete
        "delete_sweep" -> delete_sweep
        "close" -> close
        "copy", "content_copy" -> content_copy
        "palette" -> palette
        "settings", "tune" -> settings
        "map", "minimap", "radar", "near_me" -> sensors
        "format_color_fill" -> format_color_fill
        "folder", "folder_off" -> folder
        "folder_open" -> folder_open
        "create_new_folder", "new_folder" -> folder_open
        "schedule", "recent", "time" -> schedule
        "cell_tower" -> cell_tower
        "sensors" -> sensors
        "search" -> search
        "person", "account" -> person
        "groups", "group" -> groups
        "home" -> home
        "share", "link" -> share
        "devices", "hub" -> devices
        "lock" -> lock
        "info" -> info
        "visibility", "eye", "view" -> visibility
        "visibility_off" -> visibility_off
        "history", "restore" -> history
        "vibration" -> sensors
        "auto_awesome", "flare" -> flare
        "more_vert", "dots", "more" -> more_vert
        "star", "favorite", "star_outline" -> star
        "note_add", "file_present" -> note_add
        "sticky_note_2" -> sticky_note_2
        "image" -> image
        "slideshow" -> slideshow
        "grid_view", "view_module" -> grid_view
        "view_list" -> view_list
        else -> circle
    }

    Icon(
        imageVector = imageVector,
        contentDescription = name,
        tint = tint,
        modifier = modifier.size(size)
    )
}
