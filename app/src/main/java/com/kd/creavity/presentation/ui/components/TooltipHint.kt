package com.kd.creavity.presentation.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private val TooltipPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val screenEdgeMargin = 24
        val verticalMargin = 16

        val centeredX = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val y = if (anchorBounds.bottom + popupContentSize.height + verticalMargin > windowSize.height) {
            (anchorBounds.top - popupContentSize.height - verticalMargin).coerceAtLeast(screenEdgeMargin)
        } else {
            anchorBounds.bottom + verticalMargin
        }
        val x = centeredX.coerceIn(
            minimumValue = screenEdgeMargin,
            maximumValue = (windowSize.width - popupContentSize.width - screenEdgeMargin).coerceAtLeast(screenEdgeMargin)
        )

        return IntOffset(x, y)
    }
}

/**
 * Material 3 TooltipHint supporting long-press hints with haptic feedback
 * and adaptive above/below screen-bounds positioning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipHint(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tooltipState = rememberTooltipState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(tooltipState) {
        snapshotFlow { tooltipState.isVisible }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
    }

    TooltipBox(
        positionProvider = TooltipPositionProvider,
        tooltip = {
            PlainTooltip(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text(text)
            }
        },
        state = tooltipState,
        modifier = modifier
    ) {
        content()
    }
}
