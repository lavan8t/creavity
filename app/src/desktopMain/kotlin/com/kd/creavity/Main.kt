package com.kd.creavity

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kd.creavity.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.creavity.presentation.viewmodel.ThemeMode
import com.kd.creavity.presentation.viewmodel.WhiteboardViewModel
import com.kd.creavity.presentation.viewmodel.dispatch
import com.kd.creavity.ui.components.DesktopLaunchSplash
import com.kd.creavity.ui.theme.boardTheme
import java.awt.GraphicsEnvironment
import java.awt.Toolkit

fun getSystemScale(): Float {
    System.getProperty("sun.java2d.uiScale")?.toFloatOrNull()?.let { return it }
    System.getenv("GDK_SCALE")?.toFloatOrNull()?.let { return it }
    System.getenv("QT_SCALE_FACTOR")?.toFloatOrNull()?.let { return it }

    try {
        val dpi = Toolkit.getDefaultToolkit().screenResolution
        if (dpi > 0) {
            val scale = dpi.toFloat() / 96f
            if (scale >= 1.0f) return scale
        }
    } catch (_: Throwable) {}

    try {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val transform = ge.defaultScreenDevice.defaultConfiguration.defaultTransform
        if (transform.scaleX > 0) {
            return transform.scaleX.toFloat()
        }
    } catch (_: Throwable) {}

    return 1.0f
}

fun initDesktopFonts() {
    try {
        val family = com.kd.creavity.ui.theme.PlatformFontProvider.staticFontFamily
        com.kd.creavity.ui.theme.AppFontFamily = family
    } catch (e: Throwable) {
        println("Note: Could not register Google Sans Flex Compose FontFamily: ${e.message}")
    }
}

fun main() {
    initDesktopFonts()
    if (System.getProperty("sun.java2d.uiScale.enabled") == null) {
        System.setProperty("sun.java2d.uiScale.enabled", "true")
    }

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
        val viewModel = remember { WhiteboardViewModel() }

        Window(
            onCloseRequest = ::exitApplication,
            title = "CreaVITy",
            icon = painterResource("icon.png"),
            state = windowState
        ) {
            val uiState by viewModel.uiState.collectAsState()

            val isDark = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val baseDensity = LocalDensity.current
            val detectedScale = remember { getSystemScale() }
            val effectiveScale = maxOf(baseDensity.density, detectedScale)
            val density = remember(effectiveScale, baseDensity.fontScale) {
                Density(density = effectiveScale, fontScale = baseDensity.fontScale)
            }

            CompositionLocalProvider(LocalDensity provides density) {
                boardTheme(darkTheme = isDark) {
                    var isLaunching by remember { mutableStateOf(true) }

                    Crossfade(
                        targetState = isLaunching,
                        animationSpec = tween(durationMillis = 400)
                    ) { launching ->
                        if (launching) {
                            DesktopLaunchSplash(
                                onSplashFinished = { isLaunching = false }
                            )
                        } else {
                            AdaptiveWhiteboardScreen(
                                uiState = uiState,
                                onAction = { action -> viewModel.dispatch(action) }
                            )
                        }
                    }
                }
            }
        }
    }
}
