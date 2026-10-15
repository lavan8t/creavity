package com.kd.creavity

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kd.creavity.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.creavity.presentation.viewmodel.ThemeMode
import com.kd.creavity.presentation.viewmodel.WhiteboardViewModel
import com.kd.creavity.presentation.viewmodel.dispatch
import com.kd.creavity.ui.theme.OutofTheBoxTheme

fun main() = application {
    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
    val viewModel = remember { WhiteboardViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CreaVITy",
        state = windowState
    ) {
        val uiState by viewModel.uiState.collectAsState()

        val isDark = when (uiState.themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        OutofTheBoxTheme(darkTheme = isDark) {
            AdaptiveWhiteboardScreen(
                uiState = uiState,
                onAction = { action -> viewModel.dispatch(action) }
            )
        }
    }
}
