package com.kd.creavity

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kd.creavity.presentation.canvas.applyToolCursor
import com.kd.creavity.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.creavity.presentation.viewmodel.ThemeMode
import com.kd.creavity.presentation.viewmodel.WhiteboardViewModel
import com.kd.creavity.presentation.viewmodel.dispatch
import com.kd.creavity.ui.theme.boardTheme
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import kotlin.system.exitProcess

private var singleInstanceFileLock: FileLock? = null
private var singleInstanceChannel: FileChannel? = null

private fun acquireSingleInstanceLock(): Boolean {
    return try {
        val appDir = File(System.getProperty("user.home"), ".local/share/creavity").apply { mkdirs() }
        val lockFile = File(appDir, ".creavity.lock")
        val channel = RandomAccessFile(lockFile, "rw").channel
        val lock = channel.tryLock()
        if (lock != null) {
            singleInstanceFileLock = lock
            singleInstanceChannel = channel
            Runtime.getRuntime().addShutdownHook(Thread {
                runCatching {
                    singleInstanceFileLock?.release()
                    singleInstanceChannel?.close()
                }
            })
            true
        } else {
            false
        }
    } catch (_: Throwable) {
        try {
            val lockFile = File(System.getProperty("java.io.tmpdir"), "creavity_single_instance.lock")
            val channel = RandomAccessFile(lockFile, "rw").channel
            val lock = channel.tryLock()
            if (lock != null) {
                singleInstanceFileLock = lock
                singleInstanceChannel = channel
                Runtime.getRuntime().addShutdownHook(Thread {
                    runCatching {
                        singleInstanceFileLock?.release()
                        singleInstanceChannel?.close()
                    }
                })
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            true
        }
    }
}

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
    if (!acquireSingleInstanceLock()) {
        System.err.println("CreaVITy is already running. Only 1 instance can be open at a time.")
        exitProcess(0)
    }
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

            LaunchedEffect(uiState.selectedTool) {
                applyToolCursor(uiState.selectedTool)
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
                    AdaptiveWhiteboardScreen(
                        uiState = uiState,
                        onAction = { action -> viewModel.dispatch(action) }
                    )
                }
            }
        }
    }
}

