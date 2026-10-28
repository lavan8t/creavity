# 0007. Desktop Synchronous Font Provider, M3 Expressive Context Menu, and Adaptive Modals

Date: 2026-10-28

## Status
Accepted

## Context
When running CreaVITy as a native desktop application on Linux/Fedora:
1. Compose Multiplatform's default `org.jetbrains.compose.resources.Font` on Skiko/Desktop loads font files asynchronously using `rememberResourceState` with a 1-glyph fallback empty font (`org.jetbrains.compose.emptyFont`). Canvas `TextMeasurer` and early composition stages evaluate against this dummy fallback, breaking text measurements and falling back to generic system sans-serif.
2. Desktop power users expect right-click context menus (`DropdownMenu`) directly at pointer coordinates on the infinite canvas for instant object duplication, reordering, deletion, and quick insertion of sticky notes, text, and office-grade shapes.
3. Mobile-centric `ModalBottomSheet` controls anchored to screen bottom and stretching across 1080p+ widescreen monitors look awkward, violating desktop design paradigms ("no bottom sheets for desktops").

## Decision

1. **Synchronous Desktop Font Provider**:
   - Implemented `PlatformFontProvider` (`expect`/`actual`).
   - On Desktop JVM, synchronously reads `google_sans_flex.ttf` raw bytes from the resource stream at class-loading time, registers it with local AWT `GraphicsEnvironment`, and instantiates `FontFamily` using `androidx.compose.ui.text.platform.Font(identity, bytes, weight)` for Light, Normal, Medium, SemiBold, and Bold.
   - `AppFontFamily`, `LocalAppFontFamily`, and default `Typography` eagerly use `PlatformFontProvider.staticFontFamily`, guaranteeing 0ms fallback delay and zero dummy font glitches.
   - Unit tests on Desktop target (`DesktopPlatformFontTest`) verify font bytes (> 100KB) and non-default font family.

2. **Desktop Material 3 Expressive Context Menu**:
   - In `CanvasGestureController.kt`, tracked secondary pointer release without drag (`secondaryDragDist <= 8f`).
   - On right-click, hit-tests elements at click position. If an element is hit, selects it and invokes `onContextMenu(screenOffset, worldPoint, hasSelection = true)`. On empty canvas, invokes with `hasSelection = false`.
   - Built `DesktopCanvasContextMenu.kt` with Material 3 Expressive styling (`RoundedCornerShape(16.dp)`, `surfaceContainerHigh`, tonal elevation 6.dp, shadow elevation 8.dp).
   - Selection menu actions: Duplicate (`Ctrl+D`), Bring to Front (`Ctrl+]`), Send to Back (`Ctrl+[`), Delete (`Del`), Deselect (`Esc`).
   - Canvas menu actions: Add Sticky Note (`S`), Insert Text (`T`), Insert Rectangle (`R`), Insert Circle (`C`), Select All (`Ctrl+A`), Reset Zoom (`Ctrl+0`), Clear Canvas.

3. **Adaptive Modal Architecture ("No Bottom Sheets on Desktop")**:
   - Created `AdaptiveModalSheet.kt` in `presentation/ui/components/`.
   - On Desktop (`isDesktopPlatform() == true`), renders centered Material 3 `Dialog` with floating `Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 6.dp, shadowElevation = 8.dp, widthIn(min = 380.dp, max = 680.dp))` and suppresses mobile drag handles.
   - On Android (`isDesktopPlatform() == false`), preserves standard `ModalBottomSheet`.
   - Replaced all 11 instances of `ModalBottomSheet` across whiteboard sheets, version history, omniview, quiz deck, and settings.

## Consequences
- 100% synchronous Google Sans Flex font rendering on Desktop KMP with zero font pops or layout jump.
- Native, desktop-first right-click workflow on Fedora/Linux.
- Complete removal of bottom sheets on desktop in favor of centered M3 Expressive dialog cards.
- All desktop and Android unit tests pass cleanly.
