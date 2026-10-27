package com.kd.creavity.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Material 3 Expressive Loading Indicator that animates and morphs between Material shapes
 * (SoftBurst, Cookie, Pentagon, Pill, Sunny, Oval) using the official Material 3 Expressive component.
 */
@Composable
expect fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
)
