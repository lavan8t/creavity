package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.StudentWorksheet

/**
 * Pinned Top Banner for Active Student Delegated Worksheet.
 * Adheres strictly to Material 3 Expressive guidelines:
 * - Uses ElevatedCard with containerColor token.
 * - NO custom borders (border = null).
 * - Live submission and drafting status with Material 3 SuggestionChips.
 */
@Composable
fun StudentWorksheetBanner(
    worksheet: StudentWorksheet?,
    visible: Boolean,
    onSubmit: (String) -> Unit,
    onCloseOrToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (worksheet == null) return

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row: Title, Status Chip, and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        MaterialSymbol(
                            name = "checklist",
                            tint = MaterialTheme.colorScheme.primary,
                            size = 24.dp
                        )
                        Text(
                            text = worksheet.title.ifBlank { "Assigned Worksheet" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (worksheet.isSubmitted) "Submitted" else "In Progress",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (worksheet.isSubmitted) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                labelColor = if (worksheet.isSubmitted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            ),
                            border = null
                        )
                    }

                    IconButton(
                        onClick = onCloseOrToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(
                            name = "close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 20.dp
                        )
                    }
                }

                // Question Prompt
                if (worksheet.questionText.isNotBlank()) {
                    Text(
                        text = worksheet.questionText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Footer Controls: Submission & Switch View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCloseOrToggle) {
                        Text(
                            text = "Toggle Whiteboard View",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (!worksheet.isSubmitted) {
                        FilledTonalButton(
                            onClick = { onSubmit(worksheet.sheetId) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            MaterialSymbol(
                                name = "check",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                size = 18.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Submit Answers",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "Locked & Submitted",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            icon = {
                                MaterialSymbol(
                                    name = "check_circle",
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 18.dp
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }
        }
    }
}
