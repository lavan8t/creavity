package com.kd.creavity.presentation.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ExpressiveLoadingIndicator(
    modifier: Modifier,
    color: Color
) {
    LoadingIndicator(
        modifier = modifier,
        color = color
    )
}
