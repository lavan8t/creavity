# Issue 04: Folders Deprecation and Dynamic Multi-Select Labels Palette

## Status
Implemented and Verified

## Description
Remove all hierarchical folder views, tabs, maps, and dialogs from `BoardsLibraryScreen.kt`. Replace them with dynamic Material 3 Expressive `FilterChip`s populated from `board.labels` for fast, flat tag-based organization and filtering.

## Tasks
- [x] Remove Folders tab and TabRow from `BoardsLibraryScreen.kt`.
- [x] Remove `foldersMap`, folder grid items, `allFolderNames`, and `CreateFolderDialog`.
- [x] Extract `distinctLabels = boards.flatMap { it.labels }.distinct().sorted()`.
- [x] Add horizontally scrollable row of `FilterChip(border = null)` for each label.
- [x] Support toggling labels to filter boards matching selected tags.
- [x] Update board options menu to allow editing/adding labels directly to any board.
- [x] Add unit tests verifying label extraction, multi-tag filtering, and board mutation.
