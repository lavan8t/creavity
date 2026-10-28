package com.kd.creavity.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

expect object PlatformFontProvider {
    val staticFontFamily: FontFamily

    @Composable
    fun getFontFamily(): FontFamily
}
