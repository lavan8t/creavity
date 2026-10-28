package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.ActiveQuiz
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.QuizSubmission
import com.kd.creavity.domain.model.QuizType

/**
 * Host Quiz & Poll Control Deck (Mentimeter Style).
 * - Create and launch synchronized timed quizzes and open polls.
 * - Monitor live response distribution bars in real time.
 * - End questions and review ranked class leaderboards.
 *
 * Adheres strictly to Material 3 Expressive guidelines:
 * - Uses Compose & Material 3 components ONLY.
 * - NO custom borders (border = null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostQuizControlDeck(
    visible: Boolean,
    isHost: Boolean,
    activeQuiz: ActiveQuiz?,
    submissions: List<QuizSubmission>,
    timeRemainingSeconds: Int,
    connectedPeers: List<PeerDevice>,
    onLaunchQuiz: (ActiveQuiz) -> Unit,
    onEndQuiz: (quizId: String, correctOptionIndex: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || !isHost) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MaterialSymbol(
                        name = "checklist",
                        tint = MaterialTheme.colorScheme.primary,
                        size = 28.dp
                    )
                    Column {
                        Text(
                            text = "Interactive Quiz & Poll Deck",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "${connectedPeers.size} students connected",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    MaterialSymbol(
                        name = "close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 22.dp
                    )
                }
            }

            if (activeQuiz == null || activeQuiz.isEnded) {
                // Creation Deck Form
                QuizCreationForm(
                    lastEndedQuiz = activeQuiz,
                    submissions = submissions,
                    onLaunch = onLaunchQuiz
                )
            } else {
                // Active Live Deck
                ActiveQuizLiveDeck(
                    activeQuiz = activeQuiz,
                    submissions = submissions,
                    timeRemainingSeconds = timeRemainingSeconds,
                    totalStudents = connectedPeers.size,
                    onEndQuiz = onEndQuiz
                )
            }
        }
    }
}

@Composable
private fun QuizCreationForm(
    lastEndedQuiz: ActiveQuiz?,
    submissions: List<QuizSubmission>,
    onLaunch: (ActiveQuiz) -> Unit
) {
    var quizType by remember { mutableStateOf(QuizType.TIMED_QUIZ) }
    var questionText by remember { mutableStateOf("") }
    var durationSeconds by remember { mutableIntStateOf(30) }
    var correctIndex by remember { mutableIntStateOf(0) }

    val options = remember {
        mutableStateListOf(
            "Option A",
            "Option B",
            "Option C",
            "Option D"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // If there was a previous ended quiz, show previous leaderboard summary button/card
        if (lastEndedQuiz != null && submissions.isNotEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Last Assessment: ${lastEndedQuiz.questionText}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${submissions.size} responses • Top Student: ${submissions.firstOrNull { it.isCorrect }?.studentName ?: "None"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        // Quiz Type Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = quizType == QuizType.TIMED_QUIZ,
                    onClick = { quizType = QuizType.TIMED_QUIZ },
                    label = { Text("Timed Quiz (with Answer)") },
                    border = null,
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = quizType == QuizType.POLL,
                    onClick = { quizType = QuizType.POLL },
                    label = { Text("Live Poll") },
                    border = null,
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
        }

        // Question Input
        item {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Prompt") },
                placeholder = { Text("e.g. Which algorithm has O(N log N) worst-case time complexity?") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Timer Duration Chips (for Timed Quiz)
        if (quizType == QuizType.TIMED_QUIZ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Time Limit",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { seconds ->
                            FilterChip(
                                selected = durationSeconds == seconds,
                                onClick = { durationSeconds = seconds },
                                label = { Text("${seconds}s") },
                                border = null
                            )
                        }
                    }
                }
            }
        }

        // Options List
        items(options.indices.toList()) { index ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = options[index],
                    onValueChange = { options[index] = it },
                    label = { Text("Option ${('A'.code + index).toChar()}") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                if (quizType == QuizType.TIMED_QUIZ) {
                    FilterChip(
                        selected = correctIndex == index,
                        onClick = { correctIndex = index },
                        label = { Text("Correct") },
                        border = null
                    )
                }

                if (options.size > 2) {
                    IconButton(onClick = { options.removeAt(index) }) {
                        MaterialSymbol(
                            name = "close",
                            tint = MaterialTheme.colorScheme.error,
                            size = 18.dp
                        )
                    }
                }
            }
        }

        // Add Option Action
        if (options.size < 6) {
            item {
                TextButton(onClick = { options.add("Option ${('A'.code + options.size).toChar()}") }) {
                    MaterialSymbol(name = "add", tint = MaterialTheme.colorScheme.primary, size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Option")
                }
            }
        }

        // Launch Action Button
        item {
            FilledTonalButton(
                onClick = {
                    val prompt = questionText.ifBlank { "Classroom Question" }
                    val newQuiz = ActiveQuiz(
                        questionText = prompt,
                        options = options.toList(),
                        correctOptionIndex = if (quizType == QuizType.TIMED_QUIZ) correctIndex else -1,
                        durationSeconds = if (quizType == QuizType.TIMED_QUIZ) durationSeconds else 0,
                        type = quizType,
                        startedAtMs = System.currentTimeMillis()
                    )
                    onLaunch(newQuiz)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                MaterialSymbol(
                    name = "arrow_forward",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    size = 18.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch ${if (quizType == QuizType.TIMED_QUIZ) "Quiz" else "Poll"} to Class",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun ActiveQuizLiveDeck(
    activeQuiz: ActiveQuiz,
    submissions: List<QuizSubmission>,
    timeRemainingSeconds: Int,
    totalStudents: Int,
    onEndQuiz: (String, Int) -> Unit
) {
    val totalResponses = submissions.size
    val isTimed = activeQuiz.type == QuizType.TIMED_QUIZ
    val duration = activeQuiz.durationSeconds.coerceAtLeast(1)
    val progress = (timeRemainingSeconds.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Row
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
                    label = { Text("Live In Progress") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null
                )
                if (isTimed) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${timeRemainingSeconds}s") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }
            }

            Text(
                text = "$totalResponses of $totalStudents answered",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Timer Progress Bar
        if (isTimed && activeQuiz.durationSeconds > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }

        // Question Text
        Text(
            text = activeQuiz.questionText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        // Real-Time Distribution Bars
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activeQuiz.options.forEachIndexed { index, option ->
                val count = submissions.count { it.selectedOptionIndex == index }
                val percent = if (totalResponses > 0) (count * 100f / totalResponses) else 0f
                val isCorrect = activeQuiz.correctOptionIndex == index

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (isCorrect) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Correct Key", style = MaterialTheme.typography.labelSmall) },
                                        border = null
                                    )
                                }
                            }

                            Text(
                                text = "$count (${percent.toInt()}%)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    }
                }
            }
        }

        // Action to end quiz manually
        FilledTonalButton(
            onClick = { onEndQuiz(activeQuiz.quizId, activeQuiz.correctOptionIndex) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("End Question & Show Final Results")
        }
    }
}
