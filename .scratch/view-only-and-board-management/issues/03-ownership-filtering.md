# Issue 03: Board Ownership Scope Filtering (All / Owned by me / Shared with me)

## Status
Implemented and Verified

## Description
Provide instant ownership filtering in the Boards Library screen so users can filter between boards they created locally versus boards shared or received from peer collaboration sessions.

## Tasks
- [x] Define `enum class OwnershipFilter { ALL, OWNED_BY_ME, SHARED_WITH_ME }`.
- [x] Implement filter predicate:
  - `OWNED_BY_ME`: `board.createdBy.equals("You", ignoreCase = true) || board.ownerName.equals("You", ignoreCase = true) || board.createdBy == localPeerName`.
  - `SHARED_WITH_ME`: Boards where owner/creator is another peer or host.
  - `ALL`: All boards.
- [x] Add Material 3 Expressive `FilterChip` group (`border = null`) in `BoardsLibraryScreen.kt` alongside search.
- [x] Ensure empty states gracefully indicate "No boards owned by you" or "No shared boards yet".
- [x] Add unit tests verifying filtering logic across different board ownership combinations.
