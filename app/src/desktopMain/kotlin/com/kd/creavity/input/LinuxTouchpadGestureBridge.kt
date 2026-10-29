package com.kd.creavity.input

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import com.kd.creavity.presentation.ui.BoardAction
import java.awt.MouseInfo
import java.io.BufferedReader
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
 * window is active and focused.
 */
object LinuxTouchpadGestureBridge {

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var workerThread: Thread? = null

    // Pattern: <device> GESTURE_PINCH_UPDATE <time> <fingers> <dx>/<dy> ( <dx_unacc>/<dy_unacc> unaccelerated) <scale> @<angle>
    private val pinchUpdateRegex = Regex("""GESTURE_PINCH_UPDATE\s+\S+\s+(\d+)\s+.*?([0-9.]+)\s+@[-\d.]+""")

    fun parsePinchScale(line: String): Float? {
        val match = pinchUpdateRegex.find(line) ?: return null
        return match.groupValues[2].toFloatOrNull()
    }

    fun start(window: ComposeWindow, onAction: (BoardAction) -> Unit) {
        val os = System.getProperty("os.name")?.lowercase() ?: ""
        if (!os.contains("linux")) return

        if (!isRunning.compareAndSet(false, true)) return

        workerThread = Thread({
            try {
                val pb = ProcessBuilder("libinput", "debug-events")
                pb.redirectErrorStream(true)
                val proc = pb.start()
                process = proc

                Runtime.getRuntime().addShutdownHook(Thread {
                    proc.destroyForcibly()
                })

                val reader = BufferedReader(InputStreamReader(proc.inputStream))
                var lastScale = 1.0f

                while (isRunning.get()) {
                    val line = reader.readLine() ?: break

                    if (line.contains("GESTURE_PINCH_BEGIN")) {
                        lastScale = 1.0f
                    } else if (line.contains("GESTURE_PINCH_UPDATE")) {
                        val currentScale = parsePinchScale(line)
                        if (currentScale != null && currentScale > 0.05f && currentScale < 20.0f) {
                            val deltaFactor = currentScale / lastScale
                            lastScale = currentScale

                            // Verify CreaVITy window is focused before handling gesture
                            if (window.isFocused && abs(deltaFactor - 1.0f) > 0.001f && deltaFactor in 0.6f..1.6f) {
                                val focalPoint = getWindowFocalOffset(window)
                                SwingUtilities.invokeLater {
                                    onAction(BoardAction.TransformCanvas(deltaFactor, focalPoint, Offset.Zero))
                                }
                            }
                        }
                    } else if (line.contains("GESTURE_PINCH_END")) {
                        lastScale = 1.0f
                    }
                }
            } catch (e: Throwable) {
                // If libinput is not installed or permissions are unavailable, silently fallback
            } finally {
                stop()
            }
        }, "LinuxTouchpadGestureBridge-Worker").apply {
            isDaemon = true
            start()
        }
    }

    private fun getWindowFocalOffset(window: ComposeWindow): Offset {
        return try {
            val pointerInfo = MouseInfo.getPointerInfo()
            if (pointerInfo != null) {
                val screenPoint = pointerInfo.location
                val winPoint = java.awt.Point(screenPoint)
                SwingUtilities.convertPointFromScreen(winPoint, window)
                Offset(winPoint.x.toFloat(), winPoint.y.toFloat())
            } else {
                Offset(window.width / 2f, window.height / 2f)
            }
        } catch (_: Throwable) {
            Offset(window.width / 2f, window.height / 2f)
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
