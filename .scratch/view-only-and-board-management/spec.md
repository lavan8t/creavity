# Specification: View-Only Gating, Version History, Ownership Scope, and Label-Based Taxonomy

## Objective
Enhance CreaVITy's whiteboard and library experience with:
1. Strict view-only mode toolbar suppression with a Material 3 Expressive read-only status pill.
2. Full interactive Version History sheet supporting milestone snapshots and seamless rollback.
3. Clean board ownership classification chips (`All`, `Owned by me`, `Shared with me`).
4. Total elimination of folders in favor of dynamic, multi-select Material 3 Expressive label chips.

## User Stories & Functional Requirements

### 1. View-Only Gating
- When a board is read-only (`isCanvasReadOnly == true`):
  - `CanvasBottomToolbar` is not rendered.
  - Floating contextual property dock is not rendered.
  - Tool selection menus and drawing FABs are suppressed.
  - A discreet `ElevatedCard(shape = CircleShape, border = null)` pill with `Icons.Default.Visibility` / `Lock` and text `"View Only"` is anchored at the bottom-center of the canvas.
  - Panning, zooming, fitting to content, and laser pointer tools remain fully accessible and responsive.

### 2. Board Version History
- An entry point in the canvas top bar options menu ("Version History") opens `VersionHistorySheet.kt`.
- The sheet presents:
  - Header with board name and a "Create Snapshot" button (`FilledTonalButton`).
  - Scrollable list of `BoardVersionSnapshot` items:
    - Snapshot title / name (e.g., "Initial Draft", "Before Brainstorming").
    - Relative or formatted timestamp.
    - Author attribution.
    - Component metrics: stroke count, text count, sticky note count.
    - A "Restore" button (`OutlinedButton` or `FilledTonalButton`, `border = null`) that updates the active board state to the snapshot contents.
- Snapshot creation prompts a simple Material 3 `AlertDialog` for an optional custom snapshot label.

### 3. Board Ownership Scope
- In `BoardsLibraryScreen.kt`:
  - Add an `OwnershipFilter` enum: `ALL`, `OWNED_BY_ME`, `SHARED_WITH_ME`.
  - Filter chips displayed in the search and filter header bar using `FilterChip(border = null)`.
  - Boards are classified based on `board.ownerName` and `board.createdBy`:
    - `OWNED_BY_ME`: `ownerName == "You" || createdBy == "You"` (case-insensitive) or matches local peer name.
    - `SHARED_WITH_ME`: remote host or peer boards where `ownerName != "You"` and `createdBy != "You"`.
    - `ALL`: unconstrained.

### 4. Folders Deprecation & Dynamic Labels Bar
- Remove all folder-specific UI from `BoardsLibraryScreen.kt`:
  - Eliminate the "Folders" tab.
  - Eliminate `foldersMap`, folder grid items, and folder creation dialogs.
  - Remove folder icons from boards.
- Promote `board.labels`:
  - Compute distinct labels across all available boards.
  - Render an expressive horizontal row of `FilterChip(border = null)`s for each label.
  - Selecting a label filters the boards list to only include boards containing that label (supports multi-select tag filtering).
  - Provide a label management dialog or action in the Board Options menu allowing users to add/remove tags on any board.

## Non-Functional Requirements
- **Design System**: Material 3 Expressive components only (`ElevatedCard`, `FilterChip`, `SuggestionChip`, `ModalBottomSheet`, `FilledTonalButton`).
- **No Borders**: Explicitly set `border = null` on all cards, buttons, and chips.
- **Performance**: Zero re-render lag when toggling filters or opening version history.
- **Verification**: Unit tests covering filtering logic, snapshot restore, and view-only state assertions.
