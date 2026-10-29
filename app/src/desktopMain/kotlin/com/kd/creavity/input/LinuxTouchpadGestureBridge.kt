package com.kd.creavity.input

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import com.kd.creavity.presentation.ui.BoardAction
import java.awt.MouseInfo
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities
import kotlin.math.abs

/**
 * LinuxTouchpadGestureBridge
 *
 * Bridges native Linux touchpad pinch-to-zoom gestures (from libinput) directly to CreaVITy.
 * On Wayland (e.g. Hyprland, GNOME, KDE), XWayland only forwards pinch gestures via XInput 2.4,
 * which OpenJDK AWT does not process. This bridge captures native libinput pinch events
 * and synthesizes smooth focal-point zoom transformations on the canvas when the CreaVITy
 * window is active, focused, or hovered.
 */
object LinuxTouchpadGestureBridge {

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var workerThread: Thread? = null
    var logToFile: Boolean = false

    private val dispatchLock = Any()
    private var pendingScaleDelta = 1.0f
    private var isDispatchPending = false
    private var latestFocal: Offset = Offset.Zero

    // Universal matcher for GESTURE_PINCH_UPDATE across all libinput versions:
    // Handles format 1: 'event7 GESTURE_PINCH_UPDATE +1.234s 2 0.00/ 0.00 ( 0.00/ 0.00mm)  1.05 @  0.00'
    // Handles format 2: 'event8 GESTURE_PINCH_UPDATE +3.80s 2 0.00/-0.58 ( 0.00/-1.85 unaccelerated) 0.63 @-0.19'
    private val pinchRegexWithParens = Regex("""\)\s+([0-9.]+)\s+@""")

    fun parsePinchScale(line: String): Float? {
        if (!line.contains("GESTURE_PINCH_UPDATE")) return null
        val match = pinchRegexWithParens.find(line)
        if (match != null) {
            return match.groupValues[1].toFloatOrNull()
        }
        val after = line.substringAfter("GESTURE_PINCH_UPDATE")
        val atIndex = after.indexOf('@')
        if (atIndex != -1) {
            val beforeAt = after.substring(0, atIndex).trim()
            val lastWord = beforeAt.substringAfterLast(' ').trim()
            return lastWord.toFloatOrNull()
        }
        return null
    }

    private fun log(message: String) {
        if (!logToFile) return
        try {
            val logDir = File(System.getProperty("user.home"), ".local/share/creavity").apply { mkdirs() }
            val logFile = File(logDir, "touchpad_gestures.log")
            FileWriter(logFile, true).use {
                it.write("[${System.currentTimeMillis()}] $message\n")
            }
        } catch (_: Throwable) {}
    }

    fun isWindowEligible(window: ComposeWindow): Boolean {
        if (!window.isShowing) return false
        if (window.isFocused || window.isActive) return true

        // Check if mouse pointer is hovering within the window bounds
        return try {
            val pointerInfo = MouseInfo.getPointerInfo() ?: return false
            val loc = pointerInfo.location
            val bounds = window.bounds
            loc.x >= bounds.x && loc.x <= (bounds.x + bounds.width) &&
            loc.y >= bounds.y && loc.y <= (bounds.y + bounds.height)
        } catch (_: Throwable) {
            false
        }
    }

    fun getWindowFocalOffset(window: ComposeWindow, density: Float = 1.0f): Offset {
        return try {
            val pointerInfo = MouseInfo.getPointerInfo()
            val safeDensity = if (density > 0f) density else 1.0f
            if (pointerInfo != null) {
                val screenPoint = pointerInfo.location
                val winPoint = java.awt.Point(screenPoint)
                SwingUtilities.convertPointFromScreen(winPoint, window)
                Offset(winPoint.x.toFloat() / safeDensity, winPoint.y.toFloat() / safeDensity)
            } else {
                Offset((window.width / 2f) / safeDensity, (window.height / 2f) / safeDensity)
            }
        } catch (_: Throwable) {
            Offset(window.width / 2f, window.height / 2f)
        }
    }

    fun start(window: ComposeWindow, density: Float = 1.0f, onAction: (BoardAction) -> Unit) {
        val os = System.getProperty("os.name")?.lowercase() ?: ""
        if (!os.contains("linux")) return

        if (!isRunning.compareAndSet(false, true)) return
        log("Starting LinuxTouchpadGestureBridge")

        workerThread = Thread({
            var restartCount = 0
            while (isRunning.get() && restartCount < 5) {
                try {
                    val cmd = if (File("/usr/bin/stdbuf").exists()) {
                        listOf("/usr/bin/stdbuf", "-oL", "libinput", "debug-events")
                    } else {
                        listOf("libinput", "debug-events")
                    }
                    val pb = ProcessBuilder(cmd)
                    pb.redirectErrorStream(true)
                    val proc = pb.start()
                    process = proc
                    log("Spawned libinput process PID=${proc.pid()}")

                    Runtime.getRuntime().addShutdownHook(Thread {
                        proc.destroyForcibly()
                    })

                    val reader = BufferedReader(InputStreamReader(proc.inputStream))
                    var lastScale = 1.0f

                    while (isRunning.get()) {
                        val line = reader.readLine() ?: break

                        if (line.contains("GESTURE_PINCH_BEGIN")) {
                            lastScale = 1.0f
                            synchronized(dispatchLock) {
                                pendingScaleDelta = 1.0f
                                isDispatchPending = false
                            }
                            log("Pinch begin")
                        } else if (line.contains("GESTURE_PINCH_UPDATE")) {
                            val currentScale = parsePinchScale(line)
                            if (currentScale != null && currentScale > 0.05f && currentScale < 20.0f) {
                                if (lastScale <= 0.05f) lastScale = 1.0f
                                val rawDelta = currentScale / lastScale
                                lastScale = currentScale

                                val deltaFactor = rawDelta.coerceIn(0.7f, 1.4f)
                                if (abs(deltaFactor - 1.0f) > 0.0001f) {
                                    if (isWindowEligible(window)) {
                                        val focal = getWindowFocalOffset(window, density)
                                        synchronized(dispatchLock) {
                                            pendingScaleDelta *= deltaFactor
                                            latestFocal = focal
                                            if (!isDispatchPending) {
                                                isDispatchPending = true
                                                SwingUtilities.invokeLater {
                                                    val factor: Float
                                                    val focalPoint: Offset
                                                    synchronized(dispatchLock) {
                                                        factor = pendingScaleDelta
                                                        focalPoint = latestFocal
                                                        pendingScaleDelta = 1.0f
                                                        isDispatchPending = false
                                                    }
                                                    if (abs(factor - 1.0f) > 0.0001f) {
                                                        onAction(BoardAction.TransformCanvas(factor, focalPoint, Offset.Zero))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (line.contains("GESTURE_PINCH_END")) {
                            lastScale = 1.0f
                            log("Pinch end")
                            synchronized(dispatchLock) {
                                val remainingDelta = pendingScaleDelta
                                val focalPoint = latestFocal
                                pendingScaleDelta = 1.0f
                                isDispatchPending = false
                                if (abs(remainingDelta - 1.0f) > 0.0001f && isWindowEligible(window)) {
                                    SwingUtilities.invokeLater {
                                        onAction(BoardAction.TransformCanvas(remainingDelta, focalPoint, Offset.Zero))
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    log("Worker exception: ${e.message}")
                } finally {
                    try {
                        process?.destroyForcibly()
                    } catch (_: Throwable) {}
                    process = null
                }
                restartCount++
                if (isRunning.get()) {
                    try { Thread.sleep(500) } catch (_: InterruptedException) { break }
                }
            }
            isRunning.set(false)
            log("Stopped LinuxTouchpadGestureBridge")
        }, "LinuxTouchpadGestureBridge-Worker").apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                process?.destroyForcibly()
            } catch (_: Throwable) {}
            process = null
            workerThread = null
        }
    }
}
