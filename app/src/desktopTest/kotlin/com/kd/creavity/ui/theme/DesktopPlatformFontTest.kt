package com.kd.creavity.ui.theme

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.text.font.FontFamily

class DesktopPlatformFontTest {

    @Test
    fun testDesktopPlatformFontProviderLoadsBytes() {
        val bytes = PlatformFontProvider.fontBytes
        assertNotNull("PlatformFontProvider.fontBytes should not be null", bytes)
        assertTrue("PlatformFontProvider.fontBytes should be non-empty (loaded TTF bytes)", bytes.isNotEmpty())
        assertTrue("Font bytes should be at least 100KB", bytes.size > 100_000)
    }

    @Test
    fun testDesktopPlatformFontFamilyIsNotDefault() {
        val family = PlatformFontProvider.staticFontFamily
        assertNotNull("staticFontFamily should not be null", family)
        assertNotEquals("staticFontFamily should not fall back to FontFamily.Default", FontFamily.Default, family)
    }
}
