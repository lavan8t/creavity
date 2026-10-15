package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.canvas.WhiteboardCanvas
import com.kd.creavity.presentation.ui.OotbAction
import com.kd.creavity.presentation.ui.motion.BrevityMotion
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import com.kd.creavity.util.PlatformWebView

@Composable
fun CanvasPresentationOverlay(
    uiState: WhiteboardUiState,
    onAction: (OotbAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = uiState.isPresentationMode,
        enter = BrevityMotion.enterFadeScale(),
        exit = BrevityMotion.exitFadeScale(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val hasGoogleSlides = !uiState.presentationUrl.isNullOrBlank()

            if (hasGoogleSlides) {
                PlatformWebView(
                    url = uiState.presentationUrl.orEmpty(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val currentSlidePage = uiState.currentBoard.pages.getOrElse(uiState.currentBoard.activePageIndex) { null }
                if (currentSlidePage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shadowElevation = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            WhiteboardCanvas(
                                strokes = currentSlidePage.strokes,
                                textElements = currentSlidePage.textElements,
                                imageElements = currentSlidePage.imageElements,
                                peerPresences = uiState.livePeerPresences,
                                currentColor = uiState.currentColor,
                                currentWidth = uiState.currentWidth,
                                selectedTool = uiState.selectedTool,
                                backgroundColor = Color(uiState.currentBoard.backgroundColor),
                                backgroundPattern = com.kd.creavity.presentation.canvas.CanvasBackgroundPattern.BLANK,
                                hapticsEnabled = uiState.hapticsEnabled,
                                zoom = 1.0f,
                                panOffset = Offset.Zero,
                                isInfiniteCanvas = false,
                                canvasWidth = uiState.currentBoard.canvasWidth,
                                canvasHeight = uiState.currentBoard.canvasHeight,
                                onTransform = { _, _, _ -> },
                                onStrokeCompleted = { onAction(OotbAction.StrokeCompleted(it)) },
                                onStrokeBroadcastStart = { id, c, w, e, pt -> onAction(OotbAction.StrokeBroadcastStart(id, c, w, e, pt)) },
                                onStrokeBroadcastPoint = { id, pt -> onAction(OotbAction.StrokeBroadcastPoint(id, pt)) },
                                onStrokeBroadcastEnd = { onAction(OotbAction.StrokeBroadcastEnd(it)) },
                                onDeleteStroke = { onAction(OotbAction.DeleteStroke(it)) },
                                onDeleteText = { onAction(OotbAction.DeleteText(it)) },
                                onDeleteImage = { onAction(OotbAction.DeleteImage(it)) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // If Google Slides: Inking layer on top for live annotations & laser pointer
            if (hasGoogleSlides) {
                WhiteboardCanvas(
                    strokes = uiState.strokes,
                    textElements = uiState.textElements,
                    imageElements = uiState.imageElements,
                    peerPresences = uiState.livePeerPresences,
                    currentColor = uiState.currentColor,
                    currentWidth = uiState.currentWidth,
                    selectedTool = uiState.selectedTool,
                    backgroundColor = Color.Transparent,
                    backgroundPattern = com.kd.creavity.presentation.canvas.CanvasBackgroundPattern.BLANK,
                    hapticsEnabled = uiState.hapticsEnabled,
                    zoom = 1.0f,
                    panOffset = Offset.Zero,
                    isInfiniteCanvas = true,
                    onTransform = { _, _, _ -> },
                    onStrokeCompleted = { onAction(OotbAction.StrokeCompleted(it)) },
                    onStrokeBroadcastStart = { id, c, w, e, pt -> onAction(OotbAction.StrokeBroadcastStart(id, c, w, e, pt)) },
                    onStrokeBroadcastPoint = { id, pt -> onAction(OotbAction.StrokeBroadcastPoint(id, pt)) },
                    onStrokeBroadcastEnd = { onAction(OotbAction.StrokeBroadcastEnd(it)) },
                    onDeleteStroke = { onAction(OotbAction.DeleteStroke(it)) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Exit and Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaterialSymbol(name = "slideshow", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                        Text(
                            text = if (hasGoogleSlides) "Google Slides" else "Slide ${uiState.currentBoard.activePageIndex + 1}/${uiState.currentBoard.pages.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                TooltipHint(text = "Exit presentation") {
                    IconButton(
                        onClick = { onAction(OotbAction.SetPresentationMode(false)) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f))
                    ) {
                        MaterialSymbol(name = "close", tint = MaterialTheme.colorScheme.onSurface, size = 22.dp)
                    }
                }
            }

            // Bottom Floating Presentation Controls Pill
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Slide
                    TooltipHint(text = "Previous slide") {
                        IconButton(
                            onClick = {
                                if (uiState.currentBoard.activePageIndex > 0) {
                                    onAction(OotbAction.SwitchPage(uiState.currentBoard.activePageIndex - 1))
                                }
                            },
                            enabled = !hasGoogleSlides && uiState.currentBoard.activePageIndex > 0,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "chevron_left",
                                tint = if (!hasGoogleSlides && uiState.currentBoard.activePageIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 22.dp
                            )
                        }
                    }

                    // Next Slide
                    TooltipHint(text = "Next slide") {
                        IconButton(
                            onClick = {
                                if (uiState.currentBoard.activePageIndex < uiState.currentBoard.pages.size - 1) {
                                    onAction(OotbAction.SwitchPage(uiState.currentBoard.activePageIndex + 1))
                                }
                            },
                            enabled = !hasGoogleSlides && uiState.currentBoard.activePageIndex < uiState.currentBoard.pages.size - 1,
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(
                                name = "chevron_right",
                                tint = if (!hasGoogleSlides && uiState.currentBoard.activePageIndex < uiState.currentBoard.pages.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 22.dp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Laser Pointer Tool
                    TooltipHint(text = "Laser pointer") {
                        IconButton(
                            onClick = {
                                onAction(OotbAction.SelectTool(DrawingTool.LASER))
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (uiState.selectedTool == DrawingTool.LASER) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        ) {
                            MaterialSymbol(
                                name = "flare",
                                tint = if (uiState.selectedTool == DrawingTool.LASER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }

                    // Pen Tool
                    TooltipHint(text = "Pen") {
                        IconButton(
                            onClick = {
                                onAction(OotbAction.SelectTool(DrawingTool.PEN))
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (uiState.selectedTool == DrawingTool.PEN) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        ) {
                            MaterialSymbol(
                                name = "draw",
                                tint = if (uiState.selectedTool == DrawingTool.PEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }

                    // Highlighter Tool
                    TooltipHint(text = "Highlighter") {
                        IconButton(
                            onClick = {
                                onAction(OotbAction.SelectTool(DrawingTool.HIGHLIGHTER))
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (uiState.selectedTool == DrawingTool.HIGHLIGHTER) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        ) {
                            MaterialSymbol(
                                name = "ink_highlighter",
                                tint = if (uiState.selectedTool == DrawingTool.HIGHLIGHTER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 20.dp
                            )
                        }
                    }

                    // Clear Slide Annotations
                    TooltipHint(text = "Clear slide annotations") {
                        IconButton(
                            onClick = { onAction(OotbAction.ClearCanvas) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            MaterialSymbol(name = "delete_sweep", tint = MaterialTheme.colorScheme.error, size = 20.dp)
                        }
                    }
                }
            }
        }
    }
}
