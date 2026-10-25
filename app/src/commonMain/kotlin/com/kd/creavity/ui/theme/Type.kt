package com.kd.creavity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import creavity.app.generated.resources.Res
import creavity.app.generated.resources.google_sans_flex
import org.jetbrains.compose.resources.Font

val LocalAppFontFamily = compositionLocalOf<FontFamily> { FontFamily.Default }

var AppFontFamily: FontFamily = FontFamily.Default

@Composable
fun getAppFontFamily(): FontFamily {
    val family = FontFamily(
        Font(Res.font.google_sans_flex, weight = FontWeight.Light),
        Font(Res.font.google_sans_flex, weight = FontWeight.Normal),
        Font(Res.font.google_sans_flex, weight = FontWeight.Medium),
        Font(Res.font.google_sans_flex, weight = FontWeight.SemiBold),
        Font(Res.font.google_sans_flex, weight = FontWeight.Bold)
    )
    AppFontFamily = family
    return family
}

fun getDocFontFamily(fontName: String): FontFamily = AppFontFamily

fun createAppTypography(fontFamily: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium)
    )
}

val Typography = createAppTypography(FontFamily.Default)