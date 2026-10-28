# Issue 03: P2P Collaborative Classroom Chat Feature

## Status
resolved

## Description
Provide real-time peer-to-peer collaborative chat between teacher and students during sessions:
1. `ChatMessage` domain model (`id`, `senderId`, `senderName`, `text`, `timestampMs`).
2. `WhiteboardPacket.ChatMessage` in `PacketSerializer.kt` with binary serialization/deserialization.
3. Chat state (`chatMessages`, `unreadChatCount`, `isChatOpen`) in `WhiteboardUiState`.
4. Chat handling in `WhiteboardViewModel` (`SendChatMessage`, `ToggleChat`) and `WhiteboardSyncHandler` (mesh packet retransmission).
5. Floating/Docked Chat Drawer in `CanvasViewportScreen.kt` with top bar chat icon, unread badge counter, and responsive composer.

## Tasks
- [x] Define `ChatMessage` domain model.
- [x] Add `WhiteboardPacket.ChatMessage` and binary codec in `PacketSerializer.kt`.
- [x] Add chat state and actions to `WhiteboardUiState`, `BoardAction`, and `WhiteboardViewModel`.
- [x] Retransmit chat packets in `WhiteboardSyncHandler` for mesh relay.
- [x] Add Chat icon button with badge to `JamboardTopBar.kt`.
- [x] Build `CollaborativeChatPanel` in `CanvasViewportScreen.kt`.
