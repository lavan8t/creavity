# Issue 07: Adaptive Modals on Desktop (No Bottom Sheets on Desktop)

## Status
resolved

## Description
The user specified: "no bottom sheets for desktops."
In desktop applications, mobile-style bottom sheets anchored to the screen bottom and stretching across 1080p+ widescreen monitors degrade user experience and violate desktop UI conventions.

## Solution
1. Introduce `AdaptiveModalSheet` in `com.kd.creavity.presentation.ui.components`:
   - Checks `isDesktopPlatform()`.
   - On Desktop: Renders as a centered Material 3 Expressive `Dialog` with `Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 6.dp, shadowElevation = 8.dp, widthIn(min = 380.dp, max = 680.dp))` without mobile drag handle.
   - On Android: Renders as standard `ModalBottomSheet` with swipe-to-dismiss and drag handle.
2. Replace all occurrences of `ModalBottomSheet` across the codebase:
   - `BirdsEyeViewSheet.kt`
   - `BoardActionSheets.kt` (Share, Export)
   - `BoardMetadataComponents.kt`
   - `CanvasCalloutCards.kt` (Sticky notes)
   - `CanvasDialogs.kt` (Google Slides, Rename board)
   - `FolderGridComponents.kt` (New folder)
   - `HostOmniviewMatrixSheet.kt`
   - `HostQuizControlDeck.kt`
   - `VersionHistorySheet.kt`
   - `PeerConnectScreen.kt` (Profile details)
   - `SettingsScreen.kt` (Theme, background, profile)
3. Add expect/actual `isDesktopPlatform(): Boolean` in `PlatformUtils.kt`.
4. Automated verification via unit test `DesktopContextMenuAndAdaptiveSheetTest`.
