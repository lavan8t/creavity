package com.kd.brevity.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kd.brevity.presentation.ui.symbols.*

@Composable
fun MaterialSymbol(
    name: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    val imageVector: ImageVector = when (name) {
        "draw", "pen" -> if (filled) draw_filled else draw
        "brush" -> if (filled) brush_filled else brush
        "pencil", "edit" -> if (filled) edit_filled else edit
        "highlighter", "ink_highlighter" -> if (filled) ink_highlighter_filled else ink_highlighter
        "border_color" -> if (filled) border_color_filled else border_color
        "line", "horizontal_rule" -> if (filled) horizontal_rule_filled else horizontal_rule
        "arrow", "arrow_forward" -> if (filled) arrow_forward_filled else arrow_forward
        "arrow_right_alt" -> if (filled) arrow_right_alt_filled else arrow_right_alt
        "rectangle", "crop_square" -> if (filled) rectangle_filled else rectangle
        "circle", "radio_button_unchecked" -> if (filled) circle_filled else circle
        "triangle", "change_history" -> if (filled) change_history_filled else change_history
        "text", "title" -> if (filled) title_filled else title
        "text_fields" -> if (filled) text_fields_filled else text_fields
        "text_format" -> if (filled) text_format_filled else text_format
        "eraser", "ink_eraser" -> if (filled) ink_eraser_filled else ink_eraser
        "cleaning_services" -> if (filled) cleaning_services_filled else cleaning_services
        "select_erase", "highlight_off" -> if (filled) highlight_off_filled else highlight_off
        "rect_select", "crop_free", "box_select" -> if (filled) crop_free_filled else crop_free
        "select_move", "move", "select", "pan_tool_alt" -> if (filled) pan_tool_alt_filled else pan_tool_alt
        "lasso", "lasso_select", "gesture", "freeform" -> if (filled) gesture_filled else gesture
        "menu" -> if (filled) menu_filled else menu
        "undo" -> if (filled) undo_filled else undo
        "redo" -> if (filled) redo_filled else redo
        "restart_alt" -> if (filled) restart_alt_filled else restart_alt
        "chevron_left" -> if (filled) chevron_left_filled else chevron_left
        "chevron_right" -> if (filled) chevron_right_filled else chevron_right
        "add" -> if (filled) add_filled else add
        "delete" -> if (filled) delete_filled else delete
        "delete_sweep" -> if (filled) delete_sweep_filled else delete_sweep
        "close" -> if (filled) close_filled else close
        "arrow_back", "back" -> if (filled) arrow_back_filled else arrow_back
        "copy", "content_copy" -> if (filled) content_copy_filled else content_copy
        "palette" -> if (filled) palette_filled else palette
        "tune" -> if (filled) tune_filled else tune
        "map", "minimap", "radar", "near_me" -> if (filled) map_filled else map
        "format_color_fill" -> if (filled) format_color_fill_filled else format_color_fill
        "folder" -> if (filled) folder_filled else folder
        "sensors" -> if (filled) sensors_filled else sensors
        "settings" -> if (filled) settings_filled else settings
        "search" -> if (filled) search_filled else search
        "person", "account" -> if (filled) person_filled else person
        "groups" -> if (filled) groups_filled else groups
        "home" -> if (filled) home_filled else home
        "share" -> if (filled) share_filled else share
        "devices" -> if (filled) devices_filled else devices
        "lock" -> if (filled) lock_filled else lock
        "info" -> if (filled) info_filled else info
        "visibility", "eye", "view" -> if (filled) visibility_filled else visibility
        "visibility_off" -> if (filled) visibility_off_filled else visibility_off
        "history", "restore" -> if (filled) history_filled else history
        "vibration" -> if (filled) vibration_filled else vibration
        "auto_awesome" -> if (filled) auto_awesome_filled else auto_awesome
        "more_vert", "dots", "more" -> if (filled) more_vert_filled else more_vert
        "star" -> if (filled) star_filled else star
        "note_add" -> if (filled) note_add_filled else note_add
        "sticky_note_2" -> if (filled) sticky_note_2_filled else sticky_note_2
        "image" -> if (filled) image_filled else image
        "slideshow" -> if (filled) slideshow_filled else slideshow
        "flare" -> if (filled) flare_filled else flare
        "grid_view", "view_module" -> if (filled) grid_view_filled else grid_view
        "view_list" -> if (filled) view_list_filled else view_list
        "check" -> if (filled) check_filled else check
        else -> if (filled) tune_filled else tune
    }

    Icon(
        imageVector = imageVector,
        contentDescription = name,
        tint = tint,
        modifier = modifier.size(size)
    )
}
