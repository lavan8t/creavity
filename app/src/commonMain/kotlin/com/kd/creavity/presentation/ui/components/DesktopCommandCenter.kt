package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.data.mesh.MeshRole
import com.kd.creavity.domain.model.ActiveQuiz
import com.kd.creavity.domain.model.EngagementState
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.QuizSubmission
import com.kd.creavity.domain.model.QuizType
import com.kd.creavity.domain.model.StudentWorksheet
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState

/**
 * Adaptive Desktop Command Cockpit (Expanded Screen Side-Deck).
 * Rendered on Windows, macOS, Linux, and wide tablets (>= 840dp).
 * Provides multi-tab real-time host control:
 * - Tab 0: Omniview Student Matrix (100-student live thumbnail grid)
 * - Tab 1: Synchronized Quiz & Live Poll Engine (Mentimeter-style response deck)
 * - Tab 2: Offline Relay Bridge & Mesh Health Diagnostics
 *
 * Adheres strictly to Material 3 Expressive guidelines:
 * - Compose & Material 3 components ONLY.
 * - NO custom borders (border = null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopCommandCenter(
    uiState: WhiteboardUiState,
    onAction: (BoardAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDelegateDialog by remember { mutableStateOf(false) }
    var delegateTitle by remember { mutableStateOf("") }
    var delegateQuestion by remember { mutableStateOf("") }
    var inspectedStudentId by remember { mutableStateOf<String?>(null) }

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cockpit Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        name = "hub",
                        tint = MaterialTheme.colorScheme.primary,
                        size = 24.dp
                    )
                    Text(
                        text = "Classroom Cockpit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${uiState.connectedPeers.size} Online", style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    MaterialSymbol(
                        name = "close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 20.dp
                    )
                }
            }

            // M3 Primary Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Omniview", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { MaterialSymbol(name = "grid_view", tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Quiz Deck", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { MaterialSymbol(name = "checklist", tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp) }
                )
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> OmniviewTabContent(
                        studentWorksheets = uiState.studentWorksheets,
                        connectedPeers = uiState.connectedPeers,
                        onOpenDelegateDialog = { showDelegateDialog = true },
                        onInspectStudent = { inspectedStudentId = it }
                    )
                    1 -> QuizDeckTabContent(
                        activeQuiz = uiState.activeQuiz,
                        submissions = uiState.quizSubmissions,
                        timeRemainingSeconds = uiState.quizTimeRemainingSeconds,
                        connectedPeers = uiState.connectedPeers,
                        onLaunchQuiz = { onAction(BoardAction.StartQuiz(it)) },
                        onEndQuiz = { qId, key -> onAction(BoardAction.EndQuiz(qId, key)) }
                    )
                }
            }
        }
    }

    // Delegate Worksheet Dialog
    if (showDelegateDialog) {
        AlertDialog(
            onDismissRequest = { showDelegateDialog = false },
            title = {
                Text(
                    text = "Delegate Classroom Worksheet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = delegateTitle,
                        onValueChange = { delegateTitle = it },
                        label = { Text("Worksheet Title") },
                        placeholder = { Text("e.g. In-Class Exercise") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = delegateQuestion,
                        onValueChange = { delegateQuestion = it },
                        label = { Text("Question Prompt") },
                        placeholder = { Text("e.g. Draw and explain the architecture diagram.") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        val title = delegateTitle.ifBlank { "Classroom Worksheet" }
                        onAction(BoardAction.DelegateWorksheet(title, delegateQuestion, ""))
                        showDelegateDialog = false
                        delegateTitle = ""
                        delegateQuestion = ""
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Assign to All Students")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelegateDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // Full Sheet Inspection Dialog
    if (inspectedStudentId != null) {
        val sheet = uiState.studentWorksheets[inspectedStudentId]
        if (sheet != null) {
            AlertDialog(
                onDismissRequest = { inspectedStudentId = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sheet.studentName.ifBlank { "Student Sheet" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = sheet.title,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(if (sheet.isSubmitted) "Submitted" else "Drafting") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (sheet.isSubmitted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            border = null
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (sheet.questionText.isNotBlank()) {
                            Text(
                                text = sheet.questionText,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            StudentSheetThumbnailCanvas(
                                strokes = sheet.strokes,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                confirmButton = {
                    FilledTonalButton(onClick = { inspectedStudentId = null }) {
                        Text("Close Inspection")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@Composable
private fun OmniviewTabContent(
    studentWorksheets: Map<String, StudentWorksheet>,
    connectedPeers: List<PeerDevice>,
    onOpenDelegateDialog: () -> Unit,
    onInspectStudent: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val submitted = studentWorksheets.values.count { it.isSubmitted }
            Text(
                text = "$submitted / ${studentWorksheets.size} Submitted",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            FilledTonalButton(
                onClick = onOpenDelegateDialog,
                shape = RoundedCornerShape(10.dp)
            ) {
                MaterialSymbol(name = "note_add", tint = MaterialTheme.colorScheme.onSecondaryContainer, size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delegate Sheet", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (studentWorksheets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(name = "checklist", tint = MaterialTheme.colorScheme.primary, size = 36.dp)
                    Text(
                        text = "No Worksheets Active",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Click 'Delegate Sheet' to distribute a problem to all connected devices.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(studentWorksheets.values.toList(), key = { it.studentId }) { sheet ->
                    val peer = connectedPeers.find { it.endpointId == sheet.studentId }
                    val engagement = peer?.engagementState ?: EngagementState.WATCHING

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInspectStudent(sheet.studentId) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sheet.studentName.ifBlank { "Student ${sheet.studentId.take(4)}" },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (engagement) {
                                                EngagementState.WATCHING -> Color(0xFF2E7D32)
                                                EngagementState.NOT_WATCHING -> Color(0xFFEF6C00)
                                                EngagementState.OFFLINE -> Color(0xFFC62828)
                                            }
                                        )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                StudentSheetThumbnailCanvas(
                                    strokes = sheet.strokes,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (sheet.isSubmitted) "Submitted" else "${sheet.strokes.size} strokes",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (sheet.isSubmitted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "Inspect",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizDeckTabContent(
    activeQuiz: ActiveQuiz?,
    submissions: List<QuizSubmission>,
    timeRemainingSeconds: Int,
    connectedPeers: List<PeerDevice>,
    onLaunchQuiz: (ActiveQuiz) -> Unit,
    onEndQuiz: (String, Int) -> Unit
) {
    if (activeQuiz == null || activeQuiz.isEnded) {
        var quizType by remember { mutableStateOf(QuizType.TIMED_QUIZ) }
        var questionText by remember { mutableStateOf("") }
        var durationSeconds by remember { mutableIntStateOf(30) }
        var correctIndex by remember { mutableIntStateOf(0) }
        val options = remember { mutableStateListOf("Option A", "Option B", "Option C", "Option D") }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = quizType == QuizType.TIMED_QUIZ,
                        onClick = { quizType = QuizType.TIMED_QUIZ },
                        label = { Text("Timed Quiz") },
                        border = null
                    )
                    FilterChip(
                        selected = quizType == QuizType.POLL,
                        onClick = { quizType = QuizType.POLL },
                        label = { Text("Live Poll") },
                        border = null
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question") },
                    placeholder = { Text("Ask a question...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (quizType == QuizType.TIMED_QUIZ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { s ->
                            FilterChip(
                                selected = durationSeconds == s,
                                onClick = { durationSeconds = s },
                                label = { Text("${s}s") },
                                border = null
                            )
                        }
                    }
                }
            }

            items(options.indices.toList()) { i ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = options[i],
                        onValueChange = { options[i] = it },
                        label = { Text("Option ${('A'.code + i).toChar()}") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (quizType == QuizType.TIMED_QUIZ) {
                        FilterChip(
                            selected = correctIndex == i,
                            onClick = { correctIndex = i },
                            label = { Text("Key") },
                            border = null
                        )
                    }
                }
            }

            item {
                FilledTonalButton(
                    onClick = {
                        val prompt = questionText.ifBlank { "Classroom Question" }
                        val quiz = ActiveQuiz(
                            questionText = prompt,
                            options = options.toList(),
                            correctOptionIndex = if (quizType == QuizType.TIMED_QUIZ) correctIndex else -1,
                            durationSeconds = if (quizType == QuizType.TIMED_QUIZ) durationSeconds else 0,
                            type = quizType,
                            startedAtMs = System.currentTimeMillis()
                        )
                        onLaunchQuiz(quiz)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Launch to Classroom")
                }
            }
        }
    } else {
        // Active Quiz Live View
        val total = submissions.size
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${activeQuiz.questionText}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                if (activeQuiz.type == QuizType.TIMED_QUIZ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${timeRemainingSeconds}s") },
                        border = null
                    )
                }
            }

            Text(
                text = "$total / ${connectedPeers.size} answered",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeQuiz.options.forEachIndexed { idx, opt ->
                    val count = submissions.count { it.selectedOptionIndex == idx }
                    val percent = if (total > 0) count * 100f / total else 0f
                    val isKey = activeQuiz.correctOptionIndex == idx

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${('A'.code + idx).toChar()}. $opt",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isKey) FontWeight.Bold else FontWeight.Normal)
                                )
                                Text(
                                    text = "$count (${percent.toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { percent / 100f },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (isKey) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = { onEndQuiz(activeQuiz.quizId, activeQuiz.correctOptionIndex) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("End Question & Show Leaderboard")
            }
        }
    }
}


