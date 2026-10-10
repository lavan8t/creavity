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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.presentation.ui.motion.BrevityMotion

/**
 * Official Material 3 (M3) 24-Color Expressive Swatches Palette.
 * 4x6 Matrix covering Neutrals, Warm Primaries, Cool Nature/Cyans, and Earth/Amber Accents.
 */
val M3_PEN_PALETTE_24: List<Color> = listOf(
    // Row 1: Neutrals & Grayscale (Black to Pure White)
    Color(0xFF000000), // Pure Black
    Color(0xFF424242), // Charcoal / Grey 800
    Color(0xFF757575), // Medium Grey / Grey 600
    Color(0xFF9E9E9E), // Grey 500
    Color(0xFFE0E0E0), // Light Grey / Grey 300
    Color(0xFFFFFFFF), // Pure White

    // Row 2: Warm Primaries & Purples (Material 500/600 Hues)
    Color(0xFFD93025), // Google Red (Red 600)
    Color(0xFFE91E63), // Material Pink 500
    Color(0xFF9C27B0), // Material Purple 500
    Color(0xFF673AB7), // Material Deep Purple 500
    Color(0xFF3F51B5), // Material Indigo 500
    Color(0xFF1A73E8), // Google Blue (Blue 600)

    // Row 3: Cool Nature & Cyans
    Color(0xFF03A9F4), // Material Light Blue 500
    Color(0xFF00BCD4), // Material Cyan 500
    Color(0xFF009688), // Material Teal 500
    Color(0xFF1E8E3E), // Google Green (Green 600)
    Color(0xFF8BC34A), // Material Light Green 500
    Color(0xFFCDDC39), // Material Lime 500

    // Row 4: Warm Earth & Ambers
    Color(0xFFF9AB00), // Google Yellow / Amber 600
    Color(0xFFFFB300), // Material Amber 500
    Color(0xFFFF9800), // Material Orange 500
    Color(0xFFF4511E), // Material Deep Orange 500
    Color(0xFF795548), // Material Brown 500
    Color(0xFF607D8B)  // Material Blue Grey / Slate 500
)

// Legacy alias for backwards compatibility
val JAMBOARD_PEN_COLORS: List<Color> = M3_PEN_PALETTE_24

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
            modifier = Modifier.width(300.dp)
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

                // Official Material 3 24-Color Swatches Matrix (4 rows of 6)
                M3ColorSwatchesGrid(
                    selectedColor = currentColor,
                    onColorSelected = onSelectColor,
                    swatchSize = 28.dp,
                    spacing = 6.dp
                )
            }
        }
    }
}

/**
 * Official Material 3 24-Color Swatches Grid (4 rows x 6 columns).
 */
@Composable
fun M3ColorSwatchesGrid(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    swatchSize: Dp = 28.dp,
    spacing: Dp = 6.dp
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        M3_PEN_PALETTE_24.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowColors.forEach { col ->
                    val isSelected = selectedColor == col
                    val isLight = col.luminance() > 0.75f
                    val checkTint = if (col.luminance() > 0.5f) Color.Black else Color.White
                    Box(
                        modifier = Modifier
                            .size(swatchSize)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else if (isLight) MaterialTheme.colorScheme.outlineVariant
                                        else Color.Black.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onColorSelected(col)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            MaterialSymbol(
                                name = "check",
                                tint = checkTint,
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet for Recoloring Selected Canvas Elements using the official 24 M3 Swatches.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecolorModalBottomSheet(
    visible: Boolean,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
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
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recolor Elements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            M3ColorSwatchesGrid(
                selectedColor = currentColor,
                onColorSelected = { col ->
                    onColorSelected(col)
                    onDismiss()
                },
                swatchSize = 36.dp,
                spacing = 10.dp
            )
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
