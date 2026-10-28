# Issue 02: Persistent NavRail Across Non-Canvas Pages & Vertical Slide Navigation

## Status
closed

## Resolution
- Updated `LibraryNavDrawer.kt` to hide rail exclusively on `NavDestination.CANVAS`, keeping it visible across all other pages on wide screens.
- Wrapped `NavHost` in `LibraryNavDrawer` in `AdaptiveWhiteboardScreen.kt` so `NavigationRail` stays permanently mounted on the left across `BOARDS`, `PEERS`, `SETTINGS`, `BIN`, `ABOUT`.
- Implemented vertical slide transitions (`BrevityMotion.enterSlideUp`, `enterSlideDown`, `exitSlideUp`, `exitSlideDown`) between dashboard pages based on relative vertical order in navigation.
- Removed duplicate `LibraryNavDrawer` from `BoardsLibraryScreen.kt`.

## Description
User requested: "and the naviatin btwn conenct, sttigns about chnage shsould be liek slide up donw and all pages the navrail msut stay except when in a board. fix it"

Currently:
1. `LibraryNavDrawer` only wrapped `BoardsLibraryScreen`, so when navigating to `PeerConnectScreen`, `SettingsScreen`, `BinScreen`, or `AboutScreen`, the navigation rail vanished.
2. The transitions between screens in `AdaptiveWhiteboardScreen` were horizontal (`slideInHorizontally` / `slideOutHorizontally`).

## Plan
1. In `LibraryNavDrawer.kt`:
   - Support `currentDestination == NavDestination.CANVAS` check: on wide screen / desktop, show `NavigationRail` whenever `currentDestination != NavDestination.CANVAS`.
   - On canvas, hide the rail completely so whiteboard canvas has 100% of the screen.
2. In `AdaptiveWhiteboardScreen.kt`:
   - Wrap `NavHost` inside `LibraryNavDrawer` so the `NavigationRail` stays permanently mounted on the left across all dashboard pages (`BOARDS`, `PEERS`, `SETTINGS`, `BIN`, `ABOUT`).
   - Replace horizontal screen transitions with vertical slide up / slide down transitions using `BrevityMotion.enterSlideUp()`, `exitSlideUp()`, `enterSlideDown()`, `exitSlideDown()`.
   - Calculate vertical slide direction based on relative index of destination in the navigation rail.
3. In `BoardsLibraryScreen.kt`:
   - Remove duplicate inner `LibraryNavDrawer` wrapper.
   - Accept `onOpenDrawer: () -> Unit` for mobile hamburger menu.
