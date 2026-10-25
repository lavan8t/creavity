package com.kd.creavity.data.binary

import com.kd.creavity.util.getAppStorageDir
import java.io.File

/**
 * Opt-in wire trace for diagnosing collaboration bugs.
 *
 * Off by default; set the environment variable `CREAVITY_PACKET_TRACE=1` (or the system property
 * `creavity.packetTrace=1`) to append one line per packet to `packet-trace.log` in the app storage
 * directory. Every line carries the direction, the packet type, the wire size and the stroke id
 * where there is one, which is exactly what is needed to tell "never sent" apart from "sent but
 * dropped" apart from "arrived but not applied".
 *
 * It is a diagnostic affordance, not a domain rule.
 */
object PacketTrace {

    enum class Direction { SEND, RECEIVE }

    private const val ENV_VAR = "CREAVITY_PACKET_TRACE"
    private const val SYSTEM_PROPERTY = "creavity.packetTrace"
    private const val FILE_NAME = "packet-trace.log"

    var isEnabled: Boolean =
        System.getenv(ENV_VAR) == "1" || System.getProperty(SYSTEM_PROPERTY) == "1"

    /** Replaceable so tests can capture lines instead of touching the filesystem. */
    internal var writeLine: (String) -> Unit = { line ->
        runCatching { traceFile()?.appendText(line) }
    }

    fun log(direction: Direction, packet: WhiteboardPacket, byteSize: Int) {
        if (!isEnabled) return
        val strokeId = when (packet) {
            is WhiteboardPacket.StrokeStart -> packet.strokeId
            is WhiteboardPacket.StrokePoint -> packet.strokeId
            is WhiteboardPacket.StrokeEnd -> packet.strokeId
            is WhiteboardPacket.StrokeDelete -> packet.strokeId
            else -> "-"
        }
        writeLine("${System.currentTimeMillis()} $direction ${packet.javaClass.simpleName} $byteSize $strokeId\n")
    }

    private fun traceFile(): File? = runCatching { File(getAppStorageDir(), FILE_NAME) }.getOrNull()
}
