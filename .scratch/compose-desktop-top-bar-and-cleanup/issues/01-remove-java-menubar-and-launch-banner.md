# Issue 01: Remove Java AWT MenuBar and Launch Banner

## Status
closed

## Resolution
- Removed native AWT `MenuBar` from `Main.kt`.
- Removed `DesktopLaunchBanner` and any launch delay / overlay; app launches directly into `AdaptiveWhiteboardScreen`.
- Deleted `app/src/desktopMain/kotlin/com/kd/creavity/ui/components/DesktopLaunchBanner.kt`.

## Description
User requested: "no, that file edit should look like compose one not java one. and only visible inside a board. dawg. and taht banner i said, app launcing thing. remve it. not needed."

## Plan
1. In `Main.kt`:
   - Remove native AWT `MenuBar { ... }`.
   - Remove `DesktopLaunchBanner` and any launch delay / overlay. Launch directly into `AdaptiveWhiteboardScreen`.
   - Delete `DesktopLaunchBanner.kt`.
2. Verify tests and desktop build.
