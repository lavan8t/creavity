package com.kd.creavity.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import java.awt.Font as AwtFont
import java.awt.GraphicsEnvironment

actual object PlatformFontProvider {
    val fontBytes: ByteArray by lazy {
        val classLoader = Thread.currentThread().contextClassLoader ?: PlatformFontProvider::class.java.classLoader
        val stream = classLoader.getResourceAsStream("font/google_sans_flex.ttf")
            ?: PlatformFontProvider::class.java.getResourceAsStream("/font/google_sans_flex.ttf")
            ?: PlatformFontProvider::class.java.getResourceAsStream("/composeResources/creavity.app.generated.resources/font/google_sans_flex.ttf")
        val bytes = stream?.readBytes() ?: ByteArray(0)
        if (bytes.isNotEmpty()) {
            try {
                val awtFont = AwtFont.createFont(AwtFont.TRUETYPE_FONT, bytes.inputStream())
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(awtFont)
            } catch (_: Throwable) {}
        }
        bytes
    }

    private val loadedFamily: FontFamily by lazy {
        val bytes = fontBytes
        if (bytes.isNotEmpty()) {
            FontFamily(
                Font("google_sans_flex_light", bytes, FontWeight.Light),
                Font("google_sans_flex_normal", bytes, FontWeight.Normal),
                Font("google_sans_flex_medium", bytes, FontWeight.Medium),
                Font("google_sans_flex_semibold", bytes, FontWeight.SemiBold),
                Font("google_sans_flex_bold", bytes, FontWeight.Bold)
            )
        } else {
            FontFamily.Default
        }
    }

    actual val staticFontFamily: FontFamily
        get() = loadedFamily

    @Composable
    actual fun getFontFamily(): FontFamily {
        return loadedFamily
    }
}
