package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.EngagementState
import com.kd.creavity.domain.model.PeerDevice
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.StudentWorksheet
import kotlin.math.max
import kotlin.math.min

/**
 * Host-Only Omniview Matrix Modal Bottom Sheet.
 * Displays live thumbnail previews of all connected student sheets, their real-time engagement telemetry,
 * submission state, and lets the host delegate questions and inspect individual work in full screen.
 *
 * Adheres strictly to Material 3 Expressive guidelines:
 * - Uses Compose & Material 3 components ONLY.
 * - NO custom borders (border = null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostOmniviewMatrixSheet(
    visible: Boolean,
    isHost: Boolean,
    studentWorksheets: Map<String, StudentWorksheet>,
    connectedPeers: List<PeerDevice>,
    inspectedStudentId: String?,
    onDismiss: () -> Unit,
    onDelegateWorksheet: (title: String, question: String, targetStudentId: String) -> Unit,
    onInspectStudent: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || !isHost) return

    var showDelegateDialog by remember { mutableStateOf(false) }
    var delegateTitle by remember { mutableStateOf("") }
    var delegateQuestion by remember { mutableStateOf("") }

    val submittedCount = studentWorksheets.values.count { it.isSubmitted }
    val totalSheets = studentWorksheets.size

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
            // Header Bar
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
                        name = "grid_view",
                        tint = MaterialTheme.colorScheme.primary,
                        size = 28.dp
                    )
                    Column {
                        Text(
                            text = "Omniview Matrix",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "$submittedCount of $totalSheets submitted • ${connectedPeers.size} students connected",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showDelegateDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        MaterialSymbol(
                            name = "note_add",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            size = 18.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delegate Sheet")
                    }

                    IconButton(onClick = onDismiss) {
                        MaterialSymbol(
                            name = "close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 22.dp
                        )
                    }
                }
            }

            // Sheets Grid or Empty State
            if (studentWorksheets.isEmpty()) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MaterialSymbol(
                            name = "checklist",
                            tint = MaterialTheme.colorScheme.primary,
                            size = 48.dp
                        )
                        Text(
                            text = "No Worksheets Delegated Yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Delegate a private worksheet with a question prompt. Students will complete answers independently while you observe live progress.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        )
                        FilledTonalButton(
                            onClick = { showDelegateDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create & Delegate Sheet")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studentWorksheets.values.toList(), key = { it.studentId }) { sheet ->
                        val peer = connectedPeers.find { it.endpointId == sheet.studentId }
                        val engagement = peer?.engagementState ?: EngagementState.WATCHING

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInspectStudent(sheet.studentId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Student Info & Engagement
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sheet.studentName.ifBlank { "Student ${sheet.studentId.take(4)}" },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Engagement Chip
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = when (engagement) {
                                                    EngagementState.WATCHING -> "Watching"
                                                    EngagementState.NOT_WATCHING -> "Away"
                                                    EngagementState.OFFLINE -> "Offline"
                                                },
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = when (engagement) {
                                                EngagementState.WATCHING -> Color(0xFF1B5E20).copy(alpha = 0.15f)
                                                EngagementState.NOT_WATCHING -> Color(0xFFE65100).copy(alpha = 0.15f)
                                                EngagementState.OFFLINE -> Color(0xFFB71C1C).copy(alpha = 0.15f)
                                            },
                                            labelColor = when (engagement) {
                                                EngagementState.WATCHING -> Color(0xFF2E7D32)
                                                EngagementState.NOT_WATCHING -> Color(0xFFEF6C00)
                                                EngagementState.OFFLINE -> Color(0xFFC62828)
                                            }
                                        ),
                                        border = null
                                    )
                                }

                                // Submission Status Chip
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = if (sheet.isSubmitted) "Submitted" else "Drafting (${sheet.strokes.size} strokes)",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (sheet.isSubmitted) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerHighest
                                            },
                                            labelColor = if (sheet.isSubmitted) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        ),
                                        border = null
                                    )
                                }

                                // Live Mini-Canvas Thumbnail Preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 10f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    if (sheet.strokes.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No strokes yet",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            )
                                        }
                                    } else {
                                        StudentSheetThumbnailCanvas(
                                            strokes = sheet.strokes,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Inspect Action
                                FilledTonalButton(
                                    onClick = { onInspectStudent(sheet.studentId) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    MaterialSymbol(
                                        name = "visibility",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        size = 16.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Inspect Full Sheet",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Delegating a New Worksheet
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
                        placeholder = { Text("e.g. Calculus Quiz 1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = delegateQuestion,
                        onValueChange = { delegateQuestion = it },
                        label = { Text("Question Prompt") },
                        placeholder = { Text("e.g. Solve dy/dx = 3x^2 + 2x and show all steps.") },
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
                        onDelegateWorksheet(title, delegateQuestion, "")
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

    // Full Screen Inspection Dialog
    if (inspectedStudentId != null) {
        val sheet = studentWorksheets[inspectedStudentId]
        if (sheet != null) {
            AlertDialog(
                onDismissRequest = { onInspectStudent(null) },
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
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp))
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
                    FilledTonalButton(onClick = { onInspectStudent(null) }) {
                        Text("Close Inspection")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

/**
 * Thumbnail canvas rendering student strokes scaled to fit the bounding container.
 */
@Composable
fun StudentSheetThumbnailCanvas(
    strokes: List<Stroke>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (strokes.isEmpty()) return@Canvas

        // Compute bounding box
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (stroke in strokes) {
            for (p in stroke.points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
        }

        val padding = 20f
        val contentWidth = max(maxX - minX, 100f) + padding * 2
        val contentHeight = max(maxY - minY, 100f) + padding * 2

        val scaleX = size.width / contentWidth
        val scaleY = size.height / contentHeight
        val scale = min(scaleX, scaleY)

        val offsetX = (size.width - contentWidth * scale) / 2f + padding * scale - minX * scale
        val offsetY = (size.height - contentHeight * scale) / 2f + padding * scale - minY * scale

        for (stroke in strokes) {
            if (stroke.points.size < 2) continue
            val path = Path()
            val first = stroke.points.first()
            path.moveTo(first.x * scale + offsetX, first.y * scale + offsetY)

            for (i in 1 until stroke.points.size) {
                val pt = stroke.points[i]
                path.lineTo(pt.x * scale + offsetX, pt.y * scale + offsetY)
            }

            val strokeColor = if (stroke.color == 0L || (stroke.color and 0xFF000000L) == 0L) {
                Color(0xFF1B1B1F)
            } else {
                Color(stroke.color)
            }

            drawPath(
                path = path,
                color = strokeColor,
                style = DrawStroke(
                    width = max(stroke.strokeWidth * scale, 1.5f),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
