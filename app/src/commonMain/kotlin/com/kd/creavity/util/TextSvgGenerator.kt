package com.kd.creavity.util

expect object TextSvgGenerator {
    /**
     * Generates exact SVG vector path outline data (`d="M... Z"`) for the given text
     * rendered with [fontName] at [fontSize].
     * Returns null if vector path generation is unavailable or fails.
     */
    fun generateSvgData(text: String, fontName: String, fontSize: Float): String?

    /**
     * Checks if the requested font family is available locally on this device.
     */
    fun isFontAvailable(fontName: String): Boolean
}
