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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.data.export.ExportFormat
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import com.kd.creavity.presentation.canvas.CanvasExportManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasExportDialog(
    boardTitle: String,
    strokes: List<Stroke>,
    texts: List<TextElement>,
    stickyNotes: List<StickyNote>,
    selectedStrokeIds: List<String> = emptyList(),
    backgroundColor: Long = 0xFFFFFFFF,
    onDismiss: () -> Unit,
    onExportFile: (format: ExportFormat, isTransparent: Boolean, isSelectionOnly: Boolean) -> Unit
) {
    var isSelectionOnly by remember { mutableStateOf(selectedStrokeIds.isNotEmpty()) }
    var isTransparentBg by remember { mutableStateOf(false) }
    var copiedToClipboard by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    val exportStrokes = if (isSelectionOnly && selectedStrokeIds.isNotEmpty()) {
        strokes.filter { selectedStrokeIds.contains(it.id) }
    } else strokes

    val exportTexts = if (isSelectionOnly) emptyList() else texts
    val exportNotes = if (isSelectionOnly) emptyList() else stickyNotes

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MaterialSymbol(name = "share", size = 26.dp, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Export Diagram & Canvas",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scope selector (if selection is present)
                if (selectedStrokeIds.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Export Scope",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !isSelectionOnly,
                                onClick = { isSelectionOnly = false },
                                label = { Text("Full Canvas") },
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = isSelectionOnly,
                                onClick = { isSelectionOnly = true },
                                label = { Text("Selected Elements (${selectedStrokeIds.size})") },
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Background Style
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Canvas Background",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isTransparentBg,
                            onClick = { isTransparentBg = false },
                            label = { Text("Canvas Color") },
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = isTransparentBg,
                            onClick = { isTransparentBg = true },
                            label = { Text("Transparent") },
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Copy to Clipboard (Vector SVG)
                FilledTonalButton(
                    onClick = {
                        val svg = CanvasExportManager.exportToSvg(
                            strokes = exportStrokes,
                            texts = exportTexts,
                            stickyNotes = exportNotes,
                            isTransparentBg = isTransparentBg,
                            backgroundColor = backgroundColor
                        )
                        clipboardManager.setText(AnnotatedString(svg))
                        copiedToClipboard = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MaterialSymbol(
                        name = if (copiedToClipboard) "check" else "content_copy",
                        size = 18.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (copiedToClipboard) "Copied SVG to Clipboard!" else "Copy Vector SVG to Clipboard")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExportFile(ExportFormat.SVG, isTransparentBg, isSelectionOnly)
                    onDismiss()
                }
            ) {
                Text("Export SVG")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
