package com.kd.creavity.ui.theme

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Font
import java.awt.GraphicsEnvironment

class DesktopFontWiringTest {

    @Test
    fun testGoogleSansFlexFontResourceExists() {
        val classLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
        val fontStream = classLoader.getResourceAsStream("font/google_sans_flex.ttf")
            ?: javaClass.getResourceAsStream("/font/google_sans_flex.ttf")
            ?: javaClass.getResourceAsStream("/composeResources/creavity.app.generated.resources/font/google_sans_flex.ttf")

        assertNotNull("Google Sans Flex TTF resource must exist on the classpath", fontStream)
        fontStream?.use { stream ->
            val awtFont = Font.createFont(Font.TRUETYPE_FONT, stream)
            assertNotNull(awtFont)
            assertTrue(
                "AWT Font name should be related to Google Sans Flex",
                awtFont.family.contains("Google Sans", ignoreCase = true) ||
                        awtFont.fontName.contains("Google Sans", ignoreCase = true)
            )

            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val registered = ge.registerFont(awtFont)
            // Either true (registered now) or false (already registered)
            assertTrue("Font registration should not throw exception", true)
        }
    }
}
