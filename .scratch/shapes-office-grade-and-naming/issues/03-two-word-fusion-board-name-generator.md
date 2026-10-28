# Issue 03: Two-Word Fusion Board Name Generator

## Status
resolved

## Description
Replace default "Untitled Board" with an automatic two-word fusion generator (e.g. "Cosmic Canvas", "Solar Drift", "Amber Horizon", "Velvet Echo"). When a user creates a new board or leaves the title blank, the app assigns an evocative two-word title.

## Tasks
- [x] Create `BoardNameGenerator` domain object with rich, curated lists of evocative adjectives and nouns.
- [x] Implement `BoardNameGenerator.generate()` returning a 2-word title (e.g., "Cosmic Canvas").
- [x] Update `Board` model default title in `WhiteboardModels.kt` to call `BoardNameGenerator.generate()`.
- [x] Update `WhiteboardViewModel.createNewBoard(title: String = "")` to fallback to `BoardNameGenerator.generate()`.
- [x] Update `BoardsLibraryScreen.kt` and `PeerConnectScreen.kt` "New Board" / "Create Board" buttons.
- [x] Update blank-title fallback in board rename dialogs.
- [x] Add unit tests verifying `BoardNameGenerator` generates valid two-word titles and uniqueness.
