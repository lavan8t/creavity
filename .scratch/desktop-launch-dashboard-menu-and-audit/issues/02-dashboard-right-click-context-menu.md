# Issue 02: Dashboard Right-Click Context Menu

## Status
resolved

## Description
User requested: "add right clik context menu to main dashboard too. it has to feel like a professional software like photoshop, excalidraw."
Currently, right-clicking on the dashboard (`BoardsLibraryScreen.kt`), whether on a board card or empty grid space, does nothing.

## Plan
1. Create `DesktopDashboardContextMenu.kt` in `presentation/ui/components/`:
   - Material 3 Expressive `DropdownMenu` (`RoundedCornerShape(16.dp)`, `surfaceContainerHigh`, tonal elevation 6.dp, shadow elevation 8.dp).
   - Card context menu: Open, Duplicate (`Ctrl+D`), Favourite / Unstar (`S`), Rename / Details (`F2`), Export... (`Ctrl+E`), Move to Bin (`Del`).
   - Background context menu: New Board (`Ctrl+N`), View Mode (Grid / List), Sort By (Last Accessed, Last Modified, Title), Filter (All, Owned, Shared).
2. Wire right-click detection on `BoardGridCard` and `BoardListRow` in `BoardCardComponents.kt`.
3. Wire right-click detection on empty background space in `BoardsLibraryScreen.kt`.
4. Add desktop unit tests verifying menu states and action triggering.
