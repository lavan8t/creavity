package com.kd.creavity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.viewmodel.ThemeMode
import com.kd.creavity.presentation.viewmodel.WhiteboardViewModel
import com.kd.creavity.presentation.viewmodel.dispatch
import com.kd.creavity.ui.components.DesktopLaunchBanner
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

            MenuBar {
                Menu("File", mnemonic = 'F') {
                    Item("New Board", shortcut = KeyShortcut(Key.N, ctrl = true), onClick = {
                        viewModel.dispatch(BoardAction.CreateNewBoard(""))
                    })
                    Separator()
                    Item("Exit", shortcut = KeyShortcut(Key.Q, ctrl = true), onClick = ::exitApplication)
                }
                Menu("Edit", mnemonic = 'E') {
                    Item("Undo", shortcut = KeyShortcut(Key.Z, ctrl = true), onClick = {
                        viewModel.dispatch(BoardAction.Undo)
                    })
                    Item("Redo", shortcut = KeyShortcut(Key.Y, ctrl = true), onClick = {
                        viewModel.dispatch(BoardAction.Redo)
                    })
                }
                Menu("View", mnemonic = 'V') {
                    Item("Zoom In", shortcut = KeyShortcut(Key.Equals, ctrl = true), onClick = {
                        val z = (uiState.zoom * 1.25f).coerceAtMost(5.0f)
                        viewModel.dispatch(BoardAction.SetCanvasTransform(z, uiState.panOffset))
                    })
                    Item("Zoom Out", shortcut = KeyShortcut(Key.Minus, ctrl = true), onClick = {
                        val z = (uiState.zoom / 1.25f).coerceAtLeast(0.1f)
                        viewModel.dispatch(BoardAction.SetCanvasTransform(z, uiState.panOffset))
                    })
                    Item("Reset Zoom (100%)", shortcut = KeyShortcut(Key.Zero, ctrl = true), onClick = {
                        viewModel.dispatch(BoardAction.SetCanvasTransform(1.0f, Offset.Zero))
                    })
                }
                Menu("Tools", mnemonic = 'T') {
                    Item("Pen (P)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.PEN)) })
                    Item("Highlighter (H)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.HIGHLIGHTER)) })
                    Item("Eraser (E)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.ERASER)) })
                    Item("Shapes (R)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.RECTANGLE)) })
                    Item("Select (V)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.SELECT)) })
                    Item("Sticky Note (S)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.STICKY_NOTE)) })
                    Item("Text (T)", onClick = { viewModel.dispatch(BoardAction.SelectTool(DrawingTool.TEXT)) })
                }
            }

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
                    Box(modifier = Modifier.fillMaxSize()) {
                        AdaptiveWhiteboardScreen(
                            uiState = uiState,
                            onAction = { action -> viewModel.dispatch(action) }
                        )

                        var showBanner by remember { mutableStateOf(true) }
                        AnimatedVisibility(
                            visible = showBanner,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(300)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(),
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                        ) {
                            DesktopLaunchBanner(onDismiss = { showBanner = false })
                        }
                    }
                }
            }
        }
    }
}
