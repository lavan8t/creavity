# ADR 0005: View-Only Gating, Board Version History, Ownership Scope, and Label-Based Taxonomy

## Status
Accepted

## Context
1. **View-Only Toolbar Gating**: When a whiteboard session is locked by the host or when a user connects with view-only permissions (`isCanvasReadOnly == true`), drawing and creation tools should be completely suppressed. Displaying non-functional or disabled buttons leads to visual confusion, accidental touch interaction failures, and poor user trust. A clear, non-intrusive status pill is required instead.
2. **Board Version History & Rollback**: Whiteboards undergo continuous collaborative edits. Accidental deletions or design iterations need explicit version snapshots. The underlying data model already includes `BoardVersionSnapshot` and `Board.versionHistory`, and `WhiteboardViewModel` has baseline snapshot/restore methods, but there is no dedicated Material 3 Expressive UI exposing this functionality to users.
3. **Board Ownership Clarity**: As boards are shared and synced across local networks and collaborative sessions, users need clear clarity on board origins. A clean classification between "Owned by me" and "Shared with me" helps participants navigate large libraries without guessing provenance.
4. **Simplification: Deprecating Folders for Dynamic Labels**: Hierarchical folder structures introduce friction, rigid navigation stacks, and unnecessary UI clutter on tablets and mobile screens. A flat, multi-tag label taxonomy (`FilterChip`s populated dynamically from `board.labels`) provides immediate discovery, flexible multi-category filtering, and a modern Material 3 Expressive aesthetic without rigid parent-child folder structures.

## Decision

1. **Read-Only Viewport Gate**:
   - In `CanvasViewportScreen.kt`, conditionally hide `CanvasBottomToolbar`, floating property docks, context edit menus, and tool FABs when `uiState.isCanvasReadOnly` is `true`.
   - Render a discreet Material 3 Expressive status indicator pill using `ElevatedCard(shape = CircleShape, border = null)` with an eye/lock icon and "View Only" text pill anchored at the bottom center.
   - Retain full canvas navigation (panning, pinch-to-zoom, fit-to-screen) and presentation laser pointer capabilities.

2. **Interactive Version History Sheet**:
   - Implement `VersionHistorySheet.kt` using Material 3 Expressive `ModalBottomSheet` on compact screens and side drawer on wide screens.
   - Display timestamped history entries with snapshot title, author name, timestamp formatted gracefully, and metrics badges (strokes count, text elements count, shape count).
   - Provide a "Save Current Version" action to capture a new named snapshot on demand.
   - Provide a "Restore Version" action with immediate rollback via `WhiteboardViewModel.restoreVersionSnapshot(...)`.

3. **Ownership Scope Filtering**:
   - Define `OwnershipFilter { ALL, OWNED_BY_ME, SHARED_WITH_ME }`.
   - Filter boards in `BoardsLibraryScreen.kt`:
     - `OWNED_BY_ME`: `board.createdBy == "You" || board.ownerName == "You"` (or matching local peer display name).
     - `SHARED_WITH_ME`: Boards synced from peers or remote hosts where owner != "You".
     - `ALL`: Display all available boards.
   - Render using borderless Material 3 `FilterChip(border = null)`.

4. **Folders Elimination & Dynamic Labels Palette**:
   - Completely remove `foldersMap`, folder grid views, `allFolderNames`, and folder creation dialogs from `BoardsLibraryScreen.kt`.
   - Compute `distinctLabels = boards.flatMap { it.labels }.distinct().sorted()`.
   - Present labels in a horizontally scrollable row of Material 3 Expressive `FilterChip(border = null)`.
   - Allow toggling multiple labels simultaneously to filter boards satisfying active labels.
   - Enable direct label addition/editing on boards within the board options bottom sheet.

## Consequences

- **Positive**: Clean view-only UX prevents viewer frustration and accidental touch errors; robust version history allows safe rollbacks and milestone tagging; clear ownership distinction resolves collaboration ambiguity; flat label taxonomy significantly reduces cognitive load and removes hierarchical navigation bugs.
- **Negative**: Users accustomed to nested folder hierarchies will now categorize with tags; existing boards with folder metadata will have their folder names converted or merged into labels.
