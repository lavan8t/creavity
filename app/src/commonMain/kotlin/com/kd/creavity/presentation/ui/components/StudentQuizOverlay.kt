package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.ActiveQuiz
import com.kd.creavity.domain.model.QuizSubmission
import com.kd.creavity.domain.model.QuizType

/**
 * Student Interactive Quiz & Live Poll Overlay (Mentimeter Style).
 * Adheres strictly to Material 3 Expressive guidelines:
 * - Uses Compose & Material 3 components ONLY.
 * - NO custom borders (border = null).
 * - Real-time progress bar, responsive option selection, and post-quiz answer distribution.
 */
@Composable
fun StudentQuizOverlay(
    activeQuiz: ActiveQuiz?,
    userAnswer: Int?,
    submissions: List<QuizSubmission>,
    timeRemainingSeconds: Int,
    onAnswer: (quizId: String, optionIndex: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeQuiz == null) return

    val totalVotes = submissions.size
    val isTimed = activeQuiz.type == QuizType.TIMED_QUIZ
    val duration = activeQuiz.durationSeconds.coerceAtLeast(1)
    val progress = if (activeQuiz.isEnded || activeQuiz.durationSeconds <= 0) {
        0f
    } else {
        (timeRemainingSeconds.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Type Chip, Timer Badge & Close Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (isTimed) "Timed Quiz" else "Live Poll",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            icon = {
                                MaterialSymbol(
                                    name = if (isTimed) "schedule" else "checklist",
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 16.dp
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )

                        if (isTimed && !activeQuiz.isEnded) {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "${timeRemainingSeconds}s remaining",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (timeRemainingSeconds <= 5) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    labelColor = if (timeRemainingSeconds <= 5) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                ),
                                border = null
                            )
                        } else if (activeQuiz.isEnded) {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text("Voting Concluded", style = MaterialTheme.typography.labelSmall)
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null
                            )
                        }
                    }

                    if (activeQuiz.isEnded) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            MaterialSymbol(
                                name = "close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }
                }

                // Timer Progress Bar
                if (isTimed && !activeQuiz.isEnded && activeQuiz.durationSeconds > 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (timeRemainingSeconds <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                }

                // Question Prompt
                Text(
                    text = activeQuiz.questionText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                )

                // Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeQuiz.options.forEachIndexed { index, option ->
                        val isSelected = userAnswer == index
                        val voteCount = submissions.count { it.selectedOptionIndex == index }
                        val votePercent = if (totalVotes > 0) (voteCount * 100f / totalVotes) else 0f
                        val isCorrectOption = activeQuiz.isEnded && activeQuiz.correctOptionIndex == index

                        if (!activeQuiz.isEnded) {
                            // Active Voting Button
                            FilledTonalButton(
                                onClick = { onAnswer(activeQuiz.quizId, index) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (isSelected) {
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "${('A'.code + index).toChar()}.",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (isSelected) {
                                        MaterialSymbol(
                                            name = "check",
                                            tint = MaterialTheme.colorScheme.primary,
                                            size = 20.dp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Concluded Quiz Result Option
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = when {
                                        isCorrectOption -> Color(0xFF1B5E20).copy(alpha = 0.12f)
                                        isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerLowest
                                    }
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${('A'.code + index).toChar()}.",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCorrectOption || isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                            if (isCorrectOption) {
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Correct", style = MaterialTheme.typography.labelSmall) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = Color(0xFF2E7D32).copy(alpha = 0.2f),
                                                        labelColor = Color(0xFF1B5E20)
                                                    ),
                                                    border = null
                                                )
                                            }
                                            if (isSelected && !isCorrectOption && activeQuiz.correctOptionIndex >= 0) {
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Your Choice", style = MaterialTheme.typography.labelSmall) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                                    ),
                                                    border = null
                                                )
                                            }
                                        }

                                        Text(
                                            text = "$voteCount (${votePercent.toInt()}%)",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { votePercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isCorrectOption) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer Status
                if (!activeQuiz.isEnded) {
                    Text(
                        text = if (userAnswer != null) "Response recorded! You may switch your answer until the timer ends." else "Select an option above to submit your answer.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
