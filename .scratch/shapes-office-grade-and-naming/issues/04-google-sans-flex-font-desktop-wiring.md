# Issue 04: Google Sans Flex Desktop Typography Wiring

## Status
resolved

## Description
Wire `Google Sans Flex` font properly for the Desktop application on Linux / JVM so the desktop app displays the font across all UI components and canvas text measuring without falling back to system sans-serif.

## Tasks
- [x] Ensure `google_sans_flex.ttf` is bundled in `app/src/desktopMain/resources/font/google_sans_flex.ttf`.
- [x] In `Main.kt`, load and register `google_sans_flex.ttf` with Java AWT `GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(...)`.
- [x] In `Type.kt` / desktop platform font provider, ensure `FontFamily` resolves `Google Sans Flex` synchronously on Desktop.
- [x] Verify that canvas `textMeasurer` and Material 3 Expressive typography pick up the font on desktop.
- [x] Add verification test for desktop font loading.
