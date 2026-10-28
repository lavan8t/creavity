# Feature Spec: Select Cursor, Zoom & Pan Gestures, and Collaborative Chat

## Summary
This feature addresses 4 key areas:
1. **Fix Board Card Footer Layout**: Fix layout in `BoardCardComponents.kt` where the "Shared by {owner}" badge and card actions (star, 3-dots) collided / were cramped at the bottom edge. Place Title and Actions in a top row, and Metadata Badges neatly below with comfortable padding.
2. **Select Tool Official Cursor & Auto-Dismissing Floating Dock**:
   - Provide official Material Symbol `arrow_selector_tool` (cursor pointer) in `symbols/generated/`.
   - Update `MaterialSymbol.kt`, `CanvasBottomToolbar.kt`, `DesktopDraggableToolbar.kt`, and `ToolPointerIcon.desktop.kt` to use the cursor pointer icon instead of `pan_tool_alt`.
   - Add "Done" button to `FloatingSelectionDock` and automatically dismiss the dock when switching away from selection tools or pressing Escape.
3. **Critical Canvas Navigation Fix (Zoom & Pan Gestures)**:
   - Fix `toCanvasInputEvent()` in `CanvasInputEvent.kt` which was cancelling mouse events on middle/secondary clicks and dropping `Shift`, `Alt`, and `Meta` modifier keys.
   - Support `Shift + Scroll Wheel` for horizontal scrolling (left/right).
   - Support touchpad/wheel `hScroll` for horizontal scrolling.
   - Support `Ctrl + Scroll`, `Meta + Scroll`, and `Alt + Scroll` for zooming centered at pointer position.
   - Support middle-mouse button (`isTertiaryPressed`) drag for omnidirectional panning (left, right, up, down).
   - Add on-screen zoom control pill (`[-] 100% [+]`) in `JamboardTopBar.kt` and keyboard shortcuts (`Ctrl++/=`, `Ctrl+-`, `Ctrl+0`).
4. **Local-First P2P Collaborative Chat**:
   - Define `ChatMessage` domain model and `WhiteboardPacket.ChatMessage` in binary packet protocol.
   - Update `PacketSerializer.kt` to serialize/deserialize chat messages.
   - Wire incoming/outgoing chat messages in `WhiteboardViewModel` and `WhiteboardSyncHandler` with peer forwarding.
   - Add Material 3 Expressive Chat Drawer / Flyout in `CanvasViewportScreen.kt` with message history, unread badge on top bar chat icon, and instant message composer.
