package com.kd.creavity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.kd.creavity.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val AVAILABLE_GOOGLE_FONTS = listOf(
    "Google Sans Flex",
    "Caveat",
    "Pacifico",
    "Permanent Marker",
    "Patrick Hand",
    "Inter",
    "Orbitron"
)

// ROND=100 for fully rounded letterforms
private fun gsFont(weight: FontWeight, wght: Int) = Font(
    R.font.google_sans_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(wght),
        FontVariation.Setting("ROND", 100f)
    )
)

val AppFontFamily = FontFamily(
    gsFont(FontWeight.Thin,       100),
    gsFont(FontWeight.ExtraLight, 200),
    gsFont(FontWeight.Light,      300),
    gsFont(FontWeight.Normal,     400),
    gsFont(FontWeight.Medium,     500),
    gsFont(FontWeight.SemiBold,   600),
    gsFont(FontWeight.Bold,       700),
    gsFont(FontWeight.ExtraBold,  800),
    gsFont(FontWeight.Black,      900)
)


fun getGoogleFontFamily(fontName: String): FontFamily {
    if (fontName == "Google Sans Flex" || fontName == "DM Sans" || fontName == "Roboto Flex") return AppFontFamily
    val gf = GoogleFont(fontName)
    return FontFamily(
        androidx.compose.ui.text.googlefonts.Font(googleFont = gf, fontProvider = fontProvider)
    )
}

private val base = Typography()

val Typography = Typography(
    displayLarge  = base.displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall  = base.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge  = base.headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall  = base.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge  = base.titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    titleSmall  = base.titleSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    bodyLarge  = base.bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall  = base.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge  = base.labelLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = base.labelMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    labelSmall  = base.labelSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium)
)