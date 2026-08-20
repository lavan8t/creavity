package com.kd.ootb.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanToolAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MaterialSymbol(
    name: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    val imageVector: ImageVector = when (name) {
        "draw", "pen", "brush" -> Icons.Filled.Brush
        "pencil", "edit" -> Icons.Filled.Edit
        "highlighter" -> Icons.Filled.BorderColor
        "line" -> Icons.Filled.HorizontalRule
        "arrow" -> Icons.AutoMirrored.Filled.ArrowForward
        "rectangle", "rect_select", "box_select" -> Icons.Filled.CropSquare
        "circle" -> Icons.Filled.RadioButtonUnchecked
        "text" -> Icons.Filled.TextFields
        "select_erase" -> Icons.Filled.HighlightOff
        "select_move", "move", "select" -> Icons.Filled.PanToolAlt
        "lasso", "lasso_select", "gesture", "freeform" -> Icons.Filled.Gesture
        "ink_eraser", "eraser" -> Icons.Filled.CleaningServices
        "delete" -> Icons.Filled.Delete
        "auto_awesome" -> Icons.Filled.AutoAwesome
        "tune" -> Icons.Filled.Tune
        "palette" -> Icons.Filled.Palette
        "format_color_fill" -> Icons.Filled.FormatColorFill
        "sensors" -> Icons.Filled.Sensors
        "groups" -> Icons.Filled.Groups
        "dashboard" -> Icons.Filled.Dashboard
        "home" -> Icons.Filled.Home
        "settings" -> Icons.Filled.Settings
        "copy", "content_copy" -> Icons.Filled.ContentCopy
        "vibration" -> Icons.Filled.Vibration
        "share" -> Icons.Filled.Share
        "devices" -> Icons.Filled.Devices
        "add" -> Icons.Filled.Add
        "close" -> Icons.Filled.Close
        "chevron_left" -> Icons.Filled.ChevronLeft
        "chevron_right" -> Icons.Filled.ChevronRight
        "arrow_back", "back" -> Icons.AutoMirrored.Filled.ArrowBack
        "undo" -> Icons.AutoMirrored.Filled.Undo
        "redo" -> Icons.AutoMirrored.Filled.Redo
        "history", "restore" -> Icons.Filled.History
        "person", "account" -> Icons.Filled.Person
        "lock" -> Icons.Filled.Lock
        "info" -> Icons.Filled.Info
        "visibility", "eye", "view" -> Icons.Filled.Visibility
        "visibility_off" -> Icons.Filled.VisibilityOff
        "restart_alt" -> Icons.Filled.RestartAlt
        "map", "minimap", "radar", "near_me" -> Icons.Filled.NearMe
        "layers", "pages" -> Icons.Filled.Dashboard
        else -> Icons.Filled.Tune
    }

    Icon(
        imageVector = imageVector,
        contentDescription = name,
        tint = tint,
        modifier = modifier
    )
}
