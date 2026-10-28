# Issue 02: Interactive Version History Sheet and Snapshot Rollback

## Status
Implemented and Verified

## Description
Provide an interactive Material 3 Expressive Version History bottom sheet / side drawer allowing users to create manual snapshots, view timestamped versions of the active board, and restore past versions.

## Tasks
- [x] Inspect existing `BoardVersionSnapshot` model in `WhiteboardModels.kt` and `WhiteboardViewModel` methods (`createVersionSnapshot`, `restoreVersionSnapshot`).
- [x] Build `VersionHistorySheet.kt` using `ModalBottomSheet`:
  - Header with board title and "Create Snapshot" `FilledTonalButton(border = null)`.
  - Empty state when no snapshots exist yet.
  - List of `BoardVersionSnapshot` cards (`ElevatedCard(border = null)`):
    - Snapshot name/label, formatted timestamp, author name.
    - Component summary pills: strokes count, text count, sticky notes count.
    - "Restore" button triggering confirmation and rollback.
- [x] Add "Version History" option in top app bar dropdown menu in `CanvasViewportScreen.kt`.
- [x] Implement manual snapshot dialog allowing user to enter a name (e.g. "Milestone 1").
- [x] Add unit tests verifying snapshot creation and rollback state integrity.
