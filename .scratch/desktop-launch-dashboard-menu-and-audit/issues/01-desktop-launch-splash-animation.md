# Issue 01: Desktop Launch Splash Animation

## Status
resolved

## Description
User requested: "add in animation when desktop app also launching."
Currently, when the desktop app launches via `Main.kt`, the window immediately pops open with the dashboard without any branded entrance transition or splash animation.

## Plan
1. Create `DesktopLaunchSplash.kt` in `desktopMain/kotlin/com/kd/creavity/ui/components/`:
   - An expressive entrance animation sequence honoring Material 3 Expressive motion tokens.
   - Display brand icon (`icon.png`), glowing ambient pulsing halo, application title "CreaVITy", and tagline "Modern Offline Collaborative Whiteboard".
   - Smooth entrance (scale, opacity, spatial easing) over ~1000ms, followed by a smooth crossfade into the main application.
   - Interactive skip: clicking or pressing any key bypasses immediately.
2. Wire `DesktopLaunchSplash` into `Main.kt`.
3. Verify test compilation and desktop run.
