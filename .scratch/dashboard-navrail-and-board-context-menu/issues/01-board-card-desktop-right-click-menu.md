# Issue 01: Board Card Desktop Right-Click Context Menu

## Status
closed

## Resolution
- Modified `onDesktopRightClick` in `DesktopLibraryContextMenu.kt` to use `PointerEventPass.Main` so child cards evaluate and consume secondary click events before the background container.
- Directly anchored `DesktopBoardCardContextMenu` in `BoardGridCard` and `BoardListRow` with `DropdownMenu(offset = dpOffset)` relative to the card's local coordinates.
- Wired all context actions: Open Board, Duplicate, Toggle Star, Rename/Edit Details, Export Board, Move to Bin.
- Wired 3-dots button on desktop to open the same context menu.

## Description
User requested: "rgith click menu for each board in home also it has to work."
Previously, right-clicking on board cards in the home dashboard failed because `onDesktopRightClick` used `PointerEventPass.Initial` on the parent `BoxWithConstraints`, causing the parent background to consume secondary clicks before any child card could receive them. Additionally, card menu offsets were local to the card but rendered at the global screen level, mispositioning the popup.

## Plan
1. In `DesktopLibraryContextMenu.kt`:
   - Change `onDesktopRightClick` to use `PointerEventPass.Main` so child cards evaluate and consume the event before the background container.
2. In `BoardCardComponents.kt`:
   - Anchor `DesktopBoardCardContextMenu` directly in `BoardGridCard` and `BoardListRow` inside each card's root `Box`.
   - Wire all context actions: Open Board, Duplicate, Toggle Favourite, Rename/Edit Details, Export Board, Move to Bin.
   - Wire 3-dots button on desktop to open the same context menu.
3. In `BoardsLibraryScreen.kt`:
   - Pass action callbacks (`onOpenBoard`, `onDuplicateBoard`, `onEditMetadata`, `onExportBoard`, `onDeleteBoard`) to `BoardGridCard` and `BoardListRow`.
   - Remove root card context menu overlay since it is now natively anchored to each card.
