package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowRightAlt
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanToolAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowRightAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.ChangeHistory
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flare
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PanToolAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kd.creavity.presentation.ui.symbols.ink_eraser
import com.kd.creavity.presentation.ui.symbols.ink_eraser_filled
import com.kd.creavity.presentation.ui.symbols.ink_highlighter
import com.kd.creavity.presentation.ui.symbols.ink_highlighter_filled

@Composable
fun MaterialSymbol(
    name: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    val imageVector: ImageVector = when (name) {
        "draw", "pen" -> if (filled) Icons.Filled.Draw else Icons.Outlined.Draw
        "brush" -> if (filled) Icons.Filled.Brush else Icons.Outlined.Brush
        "edit", "pencil" -> if (filled) Icons.Filled.Edit else Icons.Outlined.Edit
        "calligraphy", "history_edu", "fountain_pen", "border_color" -> if (filled) Icons.Filled.BorderColor else Icons.Outlined.BorderColor
        "edit_outline" -> Icons.Outlined.Edit
        "highlighter", "ink_highlighter" -> if (filled) ink_highlighter_filled else ink_highlighter
        "line", "horizontal_rule" -> if (filled) Icons.Filled.HorizontalRule else Icons.Outlined.HorizontalRule
        "arrow", "arrow_forward" -> if (filled) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Outlined.ArrowForward
        "arrow_right_alt" -> if (filled) Icons.AutoMirrored.Filled.ArrowRightAlt else Icons.AutoMirrored.Outlined.ArrowRightAlt
        "arrow_upward", "north" -> Icons.Default.ArrowUpward
        "arrow_downward", "south" -> Icons.Default.ArrowDownward
        "rectangle", "crop_square", "filter_none" -> if (filled) Icons.Filled.Rectangle else Icons.Outlined.Rectangle
        "circle", "radio_button_unchecked" -> if (filled) Icons.Filled.Circle else Icons.Outlined.Circle
        "triangle", "change_history" -> if (filled) Icons.Filled.ChangeHistory else Icons.Outlined.ChangeHistory
        "text", "title" -> if (filled) Icons.Filled.Title else Icons.Outlined.Title
        "text_fields" -> if (filled) Icons.Filled.TextFields else Icons.Outlined.TextFields
        "text_format" -> if (filled) Icons.Filled.TextFormat else Icons.Outlined.TextFormat
        "eraser", "ink_eraser" -> if (filled) ink_eraser_filled else ink_eraser
        "cleaning_services" -> if (filled) Icons.Filled.CleaningServices else Icons.Outlined.CleaningServices
        "select_erase", "highlight_off" -> if (filled) Icons.Filled.HighlightOff else Icons.Outlined.HighlightOff
        "rect_select", "crop_free", "box_select", "fit_screen", "filter_center_focus" -> if (filled) Icons.Filled.CropFree else Icons.Outlined.CropFree
        "select_move", "move", "select", "pan_tool_alt" -> if (filled) Icons.Filled.PanToolAlt else Icons.Outlined.PanToolAlt
        "lasso", "lasso_select", "gesture", "freeform", "all_inclusive" -> if (filled) Icons.Filled.Gesture else Icons.Outlined.Gesture
        "select_all" -> Icons.Default.SelectAll
        "deselect" -> Icons.Default.Deselect
        "checklist" -> Icons.Default.Checklist
        "check_circle" -> Icons.Filled.CheckCircle
        "menu" -> if (filled) Icons.Filled.Menu else Icons.Outlined.Menu
        "undo" -> if (filled) Icons.AutoMirrored.Filled.Undo else Icons.AutoMirrored.Outlined.Undo
        "redo" -> if (filled) Icons.AutoMirrored.Filled.Redo else Icons.AutoMirrored.Outlined.Redo
        "restart_alt" -> if (filled) Icons.Filled.RestartAlt else Icons.Outlined.RestartAlt
        "chevron_left" -> if (filled) Icons.Filled.ChevronLeft else Icons.Outlined.ChevronLeft
        "chevron_right" -> if (filled) Icons.Filled.ChevronRight else Icons.Outlined.ChevronRight
        "add" -> if (filled) Icons.Filled.Add else Icons.Outlined.Add
        "delete", "bin", "trash", "delete_outline" -> if (filled) Icons.Filled.Delete else Icons.Outlined.Delete
        "delete_sweep" -> if (filled) Icons.Filled.DeleteSweep else Icons.Outlined.DeleteSweep
        "close" -> if (filled) Icons.Filled.Close else Icons.Outlined.Close
        "arrow_back", "back" -> if (filled) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Outlined.ArrowBack
        "copy", "content_copy" -> if (filled) Icons.Filled.ContentCopy else Icons.Outlined.ContentCopy
        "palette" -> if (filled) Icons.Filled.Palette else Icons.Outlined.Palette
        "settings", "tune" -> if (filled) Icons.Filled.Settings else Icons.Outlined.Settings
        "map", "minimap", "radar", "near_me" -> if (filled) Icons.Filled.Map else Icons.Outlined.Map
        "format_color_fill" -> if (filled) Icons.Filled.FormatColorFill else Icons.Outlined.FormatColorFill
        "folder", "folder_off" -> if (filled) Icons.Filled.Folder else Icons.Outlined.Folder
        "folder_open" -> Icons.Default.FolderOpen
        "create_new_folder", "new_folder" -> Icons.Default.CreateNewFolder
        "schedule", "recent", "time" -> Icons.Default.Schedule
        "cell_tower" -> Icons.Default.CellTower
        "sensors" -> if (filled) Icons.Filled.Sensors else Icons.Outlined.Sensors
        "search" -> if (filled) Icons.Filled.Search else Icons.Outlined.Search
        "person", "account" -> if (filled) Icons.Filled.Person else Icons.Outlined.Person
        "groups", "group" -> if (filled) Icons.Filled.Groups else Icons.Outlined.Groups
        "home" -> if (filled) Icons.Filled.Home else Icons.Outlined.Home
        "share", "link" -> if (filled) Icons.Filled.Share else Icons.Outlined.Share
        "devices", "hub" -> if (filled) Icons.Filled.Devices else Icons.Outlined.Devices
        "lock" -> if (filled) Icons.Filled.Lock else Icons.Outlined.Lock
        "info" -> if (filled) Icons.Filled.Info else Icons.Outlined.Info
        "visibility", "eye", "view" -> if (filled) Icons.Filled.Visibility else Icons.Outlined.Visibility
        "visibility_off" -> if (filled) Icons.Filled.VisibilityOff else Icons.Outlined.VisibilityOff
        "history", "restore" -> if (filled) Icons.Filled.History else Icons.Outlined.History
        "vibration" -> if (filled) Icons.Filled.Vibration else Icons.Outlined.Vibration
        "auto_awesome" -> if (filled) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
        "more_vert", "dots", "more" -> if (filled) Icons.Filled.MoreVert else Icons.Outlined.MoreVert
        "star", "favorite", "star_outline" -> if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline
        "note_add", "file_present" -> if (filled) Icons.AutoMirrored.Filled.NoteAdd else Icons.AutoMirrored.Outlined.NoteAdd
        "sticky_note_2" -> if (filled) Icons.AutoMirrored.Filled.StickyNote2 else Icons.AutoMirrored.Outlined.StickyNote2
        "image" -> if (filled) Icons.Filled.Image else Icons.Outlined.Image
        "slideshow" -> if (filled) Icons.Filled.Slideshow else Icons.Outlined.Slideshow
        "flare" -> if (filled) Icons.Filled.Flare else Icons.Outlined.Flare
        "grid_view", "view_module" -> if (filled) Icons.Filled.GridView else Icons.Outlined.GridView
        "view_list" -> if (filled) Icons.AutoMirrored.Filled.ViewList else Icons.AutoMirrored.Outlined.ViewList
        "check" -> if (filled) Icons.Filled.Check else Icons.Outlined.Check
        else -> if (filled) Icons.Filled.Circle else Icons.Outlined.Circle
    }

    Icon(
        imageVector = imageVector,
        contentDescription = name,
        tint = tint,
        modifier = modifier.size(size)
    )
}
