# Issue 01: Minimal Loading Banner on Desktop Launch

## Status
resolved

## Description
User requested: "the startup splash, make it show that loading banner. not a fullscreen thing. no gradient no laoder simply just creavity and the lgoo and initialising only."

## Plan
1. In `DesktopLaunchSplash.kt` (or `DesktopLaunchBanner.kt`):
   - Remove the fullscreen gradient, background halo, version text, and progress bar.
   - Replace with a sleek, non-fullscreen floating banner (Material 3 Expressive pill `Surface` with `CircleShape` or `RoundedCornerShape(16.dp)`, `surfaceContainerHigh`, tonal elevation 6.dp, shadow elevation 8.dp).
   - Content:
     - CreaVITy brand logo (`icon.png` 24.dp).
     - "CreaVITy" (FontWeight.Bold).
     - Divider / Dot.
     - "Initialising" / "Initializing" in `onSurfaceVariant`.
   - Float at top center of the window over the workspace for ~900ms, then slide up and fade out cleanly.
   - The main workspace (`AdaptiveWhiteboardScreen`) is immediately visible and rendered underneath.
2. Update `Main.kt` so `AdaptiveWhiteboardScreen` is rendered immediately, with `DesktopLaunchBanner` floating on top until initialization completes.
3. Verify test compilation and desktop run.
