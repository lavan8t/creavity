# Issue 05: KMP Desktop Google Sans Flex Synchronous Font Provider

## Status
resolved

## Description
Fix Google Sans Flex font loading on Desktop in Kotlin Multiplatform. The Compose Multiplatform resources library (`Font(Res.font.google_sans_flex)`) uses an asynchronous loader with an empty fallback dummy font on Skiko/Desktop, which fails to resolve in synchronous contexts and causes canvas text measuring and desktop Material 3 components to fall back to generic system sans-serif.

## Solution
1. Introduce expect/actual `getPlatformGoogleSansFontFamily(): FontFamily`:
   - `commonMain`: `expect fun getPlatformGoogleSansFontFamily(): FontFamily`
   - `androidMain`: `actual fun getPlatformGoogleSansFontFamily(): FontFamily` using native Compose resources.
   - `desktopMain`: `actual fun getPlatformGoogleSansFontFamily(): FontFamily` using `DesktopFontLoader` with `androidx.compose.ui.text.platform.Font` synchronously reading `google_sans_flex.ttf` bytes.
2. In `Type.kt`, wire `getAppFontFamily()` and `getDocFontFamily()` to use `getPlatformGoogleSansFontFamily()`.
3. In `desktopMain/Main.kt`, initialize `AppFontFamily` immediately at startup and register with AWT `GraphicsEnvironment`.
4. Verify with automated tests.
