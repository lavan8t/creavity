package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.RoughnessStyle
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.presentation.ui.BoardAction

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FloatingSelectionDock(
    selectedStrokeIds: List<String>,
    selectedTextIds: List<String>,
    strokes: List<Stroke>,
    onAction: (BoardAction) -> Unit,
    onOpenEmbeddedTextEditor: (strokeId: String, currentText: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedStrokeIds.isEmpty() && selectedTextIds.isEmpty()) return

    val selectedStrokes = strokes.filter { selectedStrokeIds.contains(it.id) }
    val firstStroke = selectedStrokes.firstOrNull()

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Swatches, Stroke Widths, In-Shape Text, Layers, Duplicate, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Color Swatches
                val palette = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    Color(0xFFE53935), // Red
                    Color(0xFF43A047), // Green
                    Color(0xFF1E88E5), // Blue
                    Color(0xFFFB8C00), // Amber
                    MaterialTheme.colorScheme.onSurface
                )

                palette.forEach { swatch ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(swatch, CircleShape)
                            .clickable {
                                onAction(BoardAction.RecolorElements(selectedStrokeIds, selectedTextIds, swatch))
                            }
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // In-shape text button (if a single stroke is selected)
                if (selectedStrokeIds.size == 1 && firstStroke != null) {
                    IconButton(
                        onClick = {
                            onOpenEmbeddedTextEditor(firstStroke.id, firstStroke.embeddedText)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        MaterialSymbol(
                            name = "title",
                            size = 18.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Layer Reordering (Bring to front / Send to back)
                if (selectedStrokeIds.isNotEmpty()) {
                    IconButton(
                        onClick = { onAction(BoardAction.ReorderElements(selectedStrokeIds, bringToFront = true)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        MaterialSymbol(name = "arrow_upward", size = 18.dp)
                    }
                    IconButton(
                        onClick = { onAction(BoardAction.ReorderElements(selectedStrokeIds, bringToFront = false)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        MaterialSymbol(name = "arrow_downward", size = 18.dp)
                    }
                }

                // Duplicate
                IconButton(
                    onClick = { onAction(BoardAction.DuplicateElements(selectedStrokeIds, selectedTextIds)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    MaterialSymbol(name = "content_copy", size = 18.dp)
                }

                // Delete
                IconButton(
                    onClick = {
                        selectedStrokeIds.forEach { onAction(BoardAction.DeleteStroke(it)) }
                        selectedTextIds.forEach { onAction(BoardAction.DeleteText(it)) }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    MaterialSymbol(
                        name = "delete",
                        size = 18.dp,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Row 2: Fill Style & Roughness Style (if geometric shapes selected)
            if (firstStroke != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Fill Styles
                    Text("Fill:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FillStyle.entries.forEach { style ->
                        val isSelected = firstStroke.fillStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedStrokeIds.forEach { id ->
                                    onAction(BoardAction.UpdateStrokeProperties(id, fillStyle = style))
                                }
                            },
                            label = {
                                Text(
                                    when (style) {
                                        FillStyle.TRANSPARENT -> "Outline"
                                        FillStyle.SEMI_TRANSPARENT -> "Tint"
                                        FillStyle.SOLID -> "Solid"
                                        FillStyle.HACHURE -> "Hachure"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Roughness Styles
                    Text("Rough:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    RoughnessStyle.entries.forEach { rStyle ->
                        val isSelected = firstStroke.roughness == rStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedStrokeIds.forEach { id ->
                                    onAction(BoardAction.UpdateStrokeProperties(id, roughness = rStyle))
                                }
                            },
                            label = {
                                Text(
                                    when (rStyle) {
                                        RoughnessStyle.CLEAN -> "Clean"
                                        RoughnessStyle.SKETCHY -> "Sketchy"
                                        RoughnessStyle.CARTOON -> "Comic"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
}
