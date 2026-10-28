package com.kd.creavity.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import creavity.app.generated.resources.Res
import creavity.app.generated.resources.google_sans_flex
import org.jetbrains.compose.resources.Font

actual object PlatformFontProvider {
    actual val staticFontFamily: FontFamily = FontFamily.Default

    @Composable
    actual fun getFontFamily(): FontFamily {
        return FontFamily(
            Font(Res.font.google_sans_flex, weight = FontWeight.Light),
            Font(Res.font.google_sans_flex, weight = FontWeight.Normal),
            Font(Res.font.google_sans_flex, weight = FontWeight.Medium),
            Font(Res.font.google_sans_flex, weight = FontWeight.SemiBold),
            Font(Res.font.google_sans_flex, weight = FontWeight.Bold)
        )
    }
}
