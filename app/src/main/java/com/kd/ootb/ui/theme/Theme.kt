package com.kd.ootb.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.kd.ootb.domain.model.AppThemePreset

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryCharcoal,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryEmerald,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

fun getPresetColorScheme(preset: AppThemePreset, isDark: Boolean): ColorScheme {
    return when (preset) {
        AppThemePreset.STUDIO_AMBER -> darkColorScheme(
            primary = Color(0xFFDC5F00),
            primaryContainer = Color(0xFF8B3A00),
            tertiary = Color(0xFF00E676),
            tertiaryContainer = Color(0xFF006D39),
            surface = Color(0xFF18181C),
            surfaceContainer = Color(0xFF22252C),
            surfaceContainerHigh = Color(0xFF2B2F38),
            surfaceContainerHighest = Color(0xFF353A45),
            surfaceContainerLow = Color(0xFF1D1F25),
            surfaceContainerLowest = Color(0xFF141519),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFFB71C1C)
        )
        AppThemePreset.STUDIO_AQUA -> darkColorScheme(
            primary = Color(0xFF00B4D8),
            primaryContainer = Color(0xFF0077B6),
            tertiary = Color(0xFFFFB703),
            tertiaryContainer = Color(0xFFB8860B),
            surface = Color(0xFF141A22),
            surfaceContainer = Color(0xFF1A232E),
            surfaceContainerHigh = Color(0xFF222E3C),
            surfaceContainerHighest = Color(0xFF2B3A4C),
            surfaceContainerLow = Color(0xFF161E28),
            surfaceContainerLowest = Color(0xFF0F141B),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFFB71C1C)
        )
        AppThemePreset.STUDIO_EMERALD -> darkColorScheme(
            primary = Color(0xFF00E676),
            primaryContainer = Color(0xFF00C853),
            tertiary = Color(0xFF00B4D8),
            tertiaryContainer = Color(0xFF0077B6),
            surface = Color(0xFF121B16),
            surfaceContainer = Color(0xFF17241E),
            surfaceContainerHigh = Color(0xFF1E3028),
            surfaceContainerHighest = Color(0xFF263C32),
            surfaceContainerLow = Color(0xFF141F1A),
            surfaceContainerLowest = Color(0xFF0D1411),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFFB71C1C)
        )
        AppThemePreset.STUDIO_VIOLET -> darkColorScheme(
            primary = Color(0xFF9D4EDD),
            primaryContainer = Color(0xFF7B2CBF),
            tertiary = Color(0xFFFF007F),
            tertiaryContainer = Color(0xFFC70039),
            surface = Color(0xFF181422),
            surfaceContainer = Color(0xFF221C30),
            surfaceContainerHigh = Color(0xFF2C243E),
            surfaceContainerHighest = Color(0xFF372D4D),
            surfaceContainerLow = Color(0xFF1D172A),
            surfaceContainerLowest = Color(0xFF130F1C),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFFB71C1C)
        )
        AppThemePreset.STUDIO_CRIMSON -> darkColorScheme(
            primary = Color(0xFFFF3366),
            primaryContainer = Color(0xFFC9184A),
            tertiary = Color(0xFFFFB703),
            tertiaryContainer = Color(0xFFB8860B),
            surface = Color(0xFF201317),
            surfaceContainer = Color(0xFF2B1920),
            surfaceContainerHigh = Color(0xFF38202A),
            surfaceContainerHighest = Color(0xFF452734),
            surfaceContainerLow = Color(0xFF25151B),
            surfaceContainerLowest = Color(0xFF180D11),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFFB71C1C)
        )
        AppThemePreset.STUDIO_SLATE -> darkColorScheme(
            primary = Color(0xFF90A4AE),
            primaryContainer = Color(0xFF607D8B),
            tertiary = Color(0xFF00E5FF),
            tertiaryContainer = Color(0xFF0097A7),
            surface = Color(0xFF191C1E),
            surfaceContainer = Color(0xFF23272A),
            surfaceContainerHigh = Color(0xFF2D3236),
            surfaceContainerHighest = Color(0xFF383D42),
            surfaceContainerLow = Color(0xFF1E2124),
            surfaceContainerLowest = Color(0xFF131517),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFFB71C1C)
        )
        AppThemePreset.DYNAMIC -> DarkColorScheme
    }
}

@Composable
fun OutofTheBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: AppThemePreset = AppThemePreset.STUDIO_AMBER,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        themePreset == AppThemePreset.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getPresetColorScheme(themePreset, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}