package com.kd.brevity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.presentation.ui.motion.BrevityMotion

// Jamboard 6-Color Palette
val JAMBOARD_PEN_COLORS = listOf(
    Color(0xFF202124), // Black / Dark
    Color(0xFF1A73E8), // Google Blue
    Color(0xFF1E8E3E), // Google Green
    Color(0xFFF9AB00), // Google Yellow / Amber
    Color(0xFFD93025), // Google Red
    Color(0xFFFFFFFF)  // White
)

// Jamboard Sticky Note Colors
val JAMBOARD_STICKY_COLORS = listOf(
    Color(0xFFFFF176), // Classic Jamboard Yellow
    Color(0xFFAED581), // Jamboard Green
    Color(0xFF81D4FA), // Jamboard Blue
    Color(0xFFF48FB1), // Jamboard Pink
    Color(0xFFFFB74D), // Jamboard Orange
    Color(0xFFFFFFFF)  // Clean White
)

/**
 * Sticky Note Creation Dialog (Iconic Jamboard yellow, green, blue, pink, orange notes).
 */
@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamboardStickyNoteDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (text: String, color: Long) -> Unit
) {
    if (!visible) return

    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(JAMBOARD_STICKY_COLORS[0]) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sticky Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Color swatches row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                JAMBOARD_STICKY_COLORS.forEach { col ->
                    val isSelected = selectedColor == col
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(col)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedColor = col }
                    )
                }
            }

            // Card Preview with text input
            Surface(
                color = selectedColor,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Write note...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSave(text, selectedColor.value.toLong())
                            onDismiss()
                        }
                    },
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Compact Callout Card for Pen / Drawing Tools (tips, stroke width slider, color row).
 * Opens on long-press of Pen tool. Rounded rectangle shape (14dp).
 */
@Composable
fun PenCalloutCard(
    visible: Boolean,
    selectedTool: DrawingTool,
    currentWidth: Float,
    currentColor: Color,
    onSelectTool: (DrawingTool) -> Unit,
    onChangeWidth: (Float) -> Unit,
    onSelectColor: (Color) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = BrevityMotion.enterSlideUp(),
        exit = BrevityMotion.exitSlideDown(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.width(310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tips = listOf(
                        DrawingTool.PEN to ("draw" to "Pen"),
                        DrawingTool.PENCIL to ("pencil" to "Pencil"),
                        DrawingTool.CALLIGRAPHY to ("calligraphy" to "Calligraphy"),
                        DrawingTool.MARKER to ("edit" to "Marker"),
                        DrawingTool.HIGHLIGHTER to ("ink_highlighter" to "Highlighter"),
                        DrawingTool.BRUSH to ("brush" to "Brush")
                    )
                    tips.forEach { (tool, iconAndName) ->
                        val isSelected = selectedTool == tool
                        TooltipHint(text = iconAndName.second) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val w = when (tool) {
                                            DrawingTool.PEN -> 5f
                                            DrawingTool.PENCIL -> 4f
                                            DrawingTool.CALLIGRAPHY -> 10f
                                            DrawingTool.MARKER -> 12f
                                            DrawingTool.HIGHLIGHTER -> 28f
                                            DrawingTool.BRUSH -> 18f
                                            else -> 5f
                                        }
                                        onChangeWidth(w)
                                        onSelectTool(tool)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialSymbol(
                                    name = iconAndName.first,
                                    filled = isSelected,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 20.dp
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Width slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${currentWidth.toInt()}px",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(36.dp)
                    )
                    Slider(
                        value = currentWidth,
                        onValueChange = onChangeWidth,
                        valueRange = 1f..40f,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Colors row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JAMBOARD_PEN_COLORS.forEach { col ->
                        val isSelected = currentColor == col
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectColor(col)
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact Callout Card for Eraser Tools (Stroke Eraser, Object Eraser, Clear Canvas).
 * Opens on long-press of Eraser tool. Rounded rectangle shape (14dp).
 */
@Composable
fun EraserCalloutCard(
    visible: Boolean,
    selectedTool: DrawingTool,
    onSelectTool: (DrawingTool) -> Unit,
    onClearCanvas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = BrevityMotion.enterSlideUp(),
        exit = BrevityMotion.exitSlideDown(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stroke Eraser
                val isStroke = selectedTool == DrawingTool.ERASER
                TooltipHint(text = "Stroke Eraser") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isStroke) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectTool(DrawingTool.ERASER)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MaterialSymbol(
                                name = "ink_eraser",
                                filled = isStroke,
                                tint = if (isStroke) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                            Text(
                                text = "Stroke",
                                fontSize = 13.sp,
                                fontWeight = if (isStroke) FontWeight.Bold else FontWeight.Normal,
                                color = if (isStroke) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Object Eraser
                val isSelect = selectedTool == DrawingTool.SELECT_ERASE
                TooltipHint(text = "Object Eraser") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelect) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectTool(DrawingTool.SELECT_ERASE)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MaterialSymbol(
                                name = "highlight_off",
                                filled = isSelect,
                                tint = if (isSelect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                            Text(
                                text = "Object",
                                fontSize = 13.sp,
                                fontWeight = if (isSelect) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Clear Board
                TooltipHint(text = "Clear Board") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClearCanvas()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MaterialSymbol(
                                name = "delete_sweep",
                                tint = MaterialTheme.colorScheme.error,
                                size = 20.dp
                            )
                            Text(
                                text = "Clear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Callout Card for Selection Tools (Move, Rect Select, Lasso Select).
 * Opens on long-press of Select tool. Rounded rectangle shape (14dp).
 */
@Composable
fun SelectCalloutCard(
    visible: Boolean,
    selectedTool: DrawingTool,
    onSelectTool: (DrawingTool) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = BrevityMotion.enterSlideUp(),
        exit = BrevityMotion.exitSlideDown(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val modes = listOf(
                    Triple(DrawingTool.SELECT_MOVE, "pan_tool_alt", "Move"),
                    Triple(DrawingTool.RECT_SELECT, "crop_free", "Rect"),
                    Triple(DrawingTool.LASSO_SELECT, "gesture", "Lasso")
                )
                modes.forEach { (tool, icon, label) ->
                    val isSelected = selectedTool == tool
                    TooltipHint(text = label) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectTool(tool)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MaterialSymbol(
                                    name = icon,
                                    filled = isSelected,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 20.dp
                                )
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Callout Card for Undo / Redo history actions.
 * Opens on long-press of Undo/Redo merged button. Rounded rectangle shape (14dp).
 */
@Composable
fun UndoRedoCalloutCard(
    visible: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = BrevityMotion.enterSlideUp(),
        exit = BrevityMotion.exitSlideDown(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TooltipHint(text = "Undo") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = canUndo) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onUndo()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MaterialSymbol(
                                name = "undo",
                                tint = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 20.dp
                            )
                            Text(
                                text = "Undo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                TooltipHint(text = "Redo") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = canRedo) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRedo()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MaterialSymbol(
                                name = "redo",
                                tint = if (canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 20.dp
                            )
                            Text(
                                text = "Redo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            }
        }
    }
}
