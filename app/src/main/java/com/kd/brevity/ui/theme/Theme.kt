package com.kd.brevity.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val OrangeColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFDC5F00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8B3A00),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF373A40),
    onSecondary = Color(0xFFEEEEEE),
    secondaryContainer = Color(0xFF22252C),
    onSecondaryContainer = Color(0xFFE2E2E9),
    tertiary = Color(0xFF00E676),
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF006D39),
    onTertiaryContainer = Color(0xFF6CFFA0),
    background = Color(0xFF18181C),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF18181C),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF2B2F38),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF141519),
    surfaceContainerLow = Color(0xFF1D1F25),
    surfaceContainer = Color(0xFF22252C),
    surfaceContainerHigh = Color(0xFF2B2F38),
    surfaceContainerHighest = Color(0xFF353A45),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFFF5252),
    errorContainer = Color(0xFFB71C1C)
)

val ExpressiveShapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OutofTheBoxTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        OrangeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}