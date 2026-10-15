package com.kd.creavity.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.presentation.ui.motion.BrevityMotion

@Composable
fun CanvasSelectionBar(
    selectedCount: Int,
    onDuplicate: () -> Unit,
    onRecolor: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = BrevityMotion.enterSlideDown(),
        exit = BrevityMotion.exitSlideUp(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedCount selected",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TooltipHint(text = "Duplicate") {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDuplicate()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(name = "copy", tint = MaterialTheme.colorScheme.onSurface, size = 18.dp)
                    }
                }

                TooltipHint(text = "Recolor") {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onRecolor()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(name = "palette", tint = MaterialTheme.colorScheme.onSurface, size = 18.dp)
                    }
                }

                TooltipHint(text = "Delete") {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        MaterialSymbol(name = "delete", tint = MaterialTheme.colorScheme.error, size = 18.dp)
                    }
                }
            }
        }
    }
}
