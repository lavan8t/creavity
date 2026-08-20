package com.kd.ootb.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.kd.ootb.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val AVAILABLE_GOOGLE_FONTS = listOf(
    "Roboto Flex",
    "Caveat",
    "Pacifico",
    "Permanent Marker",
    "Patrick Hand",
    "Inter",
    "Orbitron"
)

@OptIn(ExperimentalTextApi::class)
val robotoFlexAppFontFamily = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("wght", 500f),
            FontVariation.Setting("wdth", 100f),
            FontVariation.Setting("slnt", 0f),
            FontVariation.Setting("GRAD", 150f),
            FontVariation.Setting("XOPQ", 96f),
            FontVariation.Setting("XTRA", 603f),
            FontVariation.Setting("YOPQ", 79f),
            FontVariation.Setting("YTAS", 788f),
            FontVariation.Setting("YTDE", -203f),
            FontVariation.Setting("YTFI", 560f),
            FontVariation.Setting("YTLC", 570f),
            FontVariation.Setting("YTUC", 760f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val robotoFlexOotbTitleFontFamily = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("wght", 1000f),
            FontVariation.Setting("wdth", 151f),
            FontVariation.Setting("slnt", 0f),
            FontVariation.Setting("GRAD", 98f),
            FontVariation.Setting("XOPQ", 27f),
            FontVariation.Setting("XTRA", 603f),
            FontVariation.Setting("YOPQ", 25f),
            FontVariation.Setting("YTDE", -203f),
            FontVariation.Setting("YTFI", 738f),
            FontVariation.Setting("YTLC", 514f),
            FontVariation.Setting("YTUC", 528f)
        )
    )
)

fun getGoogleFontFamily(fontName: String): FontFamily {
    if (fontName == "Roboto Flex") return robotoFlexAppFontFamily
    val googleFont = GoogleFont(fontName)
    return FontFamily(
        androidx.compose.ui.text.googlefonts.Font(
            googleFont = googleFont,
            fontProvider = fontProvider
        )
    )
}

val RobotoFlexAppHeading = TextStyle(
    fontFamily = robotoFlexAppFontFamily,
    fontSize = 28.sp,
    lineHeight = 34.sp
)

val RobotoFlexOotbTitle = TextStyle(
    fontFamily = robotoFlexOotbTitleFontFamily,
    fontSize = 24.sp,
    lineHeight = 30.sp
)

val Typography = Typography(
    headlineLarge = RobotoFlexAppHeading,
    headlineMedium = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = RobotoFlexAppHeading,
    titleMedium = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = robotoFlexAppFontFamily,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)