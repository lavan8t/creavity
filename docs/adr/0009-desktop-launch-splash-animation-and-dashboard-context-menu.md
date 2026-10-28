# 0009. Desktop Launch Splash Animation and Dashboard Context Menu

Date: 2026-10-28

## Status
Accepted

## Context
When running CreaVITy on desktop systems (Linux Fedora, Windows, macOS):
1. **Desktop App Launching**: The app window opened abruptly into the board library without an expressive entrance transition or brand introduction typical of desktop software like Photoshop and Figma.
2. **Dashboard Context Menus**: While the infinite canvas had right-click context menu capabilities (ADR 0007), the main dashboard (`BoardsLibraryScreen`) was unresponsive to secondary pointer clicks. Users expecting right-click manipulation on board cards (Open, Duplicate, Star, Rename, Export, Delete) and on empty canvas background (Create Board, Change View Mode, Sort By, Filter By) were forced to use multi-step clicks or mobile sheets.

## Decision

1. **Material 3 Expressive Launch Splash Animation**:
   - Implemented `DesktopLaunchSplash.kt` in `desktopMain/kotlin/com/kd/creavity/ui/components/`.
   - Built a branded entrance sequence using Compose animation springs:
     - Scaled, rounded brand logo mark (`icon.png`) with elevation and subtle ambient radial glow pulse.
     - Animated brand typography ("CreaVITy", "Offline Collaborative Whiteboard").
     - Progressive linear loading sweep over 1100ms.
     - Smooth `Crossfade(animationSpec = tween(400))` into `AdaptiveWhiteboardScreen`.
     - Instant interactive bypass: clicking anywhere or pressing a key dismisses the splash immediately.
   - Integrated into `Main.kt`.

2. **Dashboard Right-Click Context Menus**:
   - Implemented `onDesktopRightClick` pointer modifier in `DesktopLibraryContextMenu.kt` using `PointerEventPass.Initial` to capture secondary button presses without interfering with primary taps or long-press gestures.
   - Built `DesktopBoardCardContextMenu`:
     - Actions: Open Board (`Enter`), Duplicate (`Ctrl+D`), Favourite / Unstar (`S`), Rename / Edit Details (`F2`), Export Board (`Ctrl+E`), Move to Bin (`Del`).
   - Built `DesktopDashboardBackgroundContextMenu`:
     - Actions: New Board (`Ctrl+N`), View Mode (Grid / List), Sort by (Last Accessed, Last Modified, Title), Filter by (All, Owned by me, Shared with me).
   - Wired `onCardRightClick` and `onRowRightClick` into `BoardGridCard` and `BoardListRow` in `BoardCardComponents.kt`.
   - Wired background right-click detection on `BoardsLibraryScreen.kt`.

3. **Software Engineering & UI/UX Audit**:
   - Formulated a detailed technical benchmark audit against Adobe Photoshop, Excalidraw, and Figma, identifying gaps across native `MenuBar`, dynamic `pointerHoverIcon` feedback, Spacebar-hold temporary pan, external file drag-and-drop, and window state persistence.

## Consequences
- Desktop app launch feels polished, expressive, and branded, with zero lag or blocking.
- Dashboard board cards and empty spaces have complete right-click parity with professional desktop creative software.
- Full test pass across Desktop JVM and Android unit test suites.
