# Live-stroke invisible until finger lift — bug report

## Status: Resolved

## Root Cause
In `WhiteboardCanvas.kt`, `drawInvalidator.longValue` was removed as a supposedly unused `dummy` variable during cleanup.
Because `controller.activePoints` and `controller.activePath` are plain mutable objects (not Compose `State`), Compose relies on reading `drawInvalidator.longValue` inside `Canvas`'s `DrawScope` to register a dependency on `drawInvalidator`.
Without this read, `requestInvalidate()` calls on finger down/move updated `drawInvalidator.longValue`, but Compose never scheduled a redraw of the `Canvas`.
Upon finger lift, `onStrokeCompleted` mutated `strokes` in the ViewModel, triggering recomposition and finally rendering the stroke.

## Fix
1. Restored `drawInvalidator.longValue` read inside the `Canvas` draw lambda in `WhiteboardCanvas.kt` with documentation explaining its necessity.
2. Verified `PacketSerializer.kt` wire trace logging for `PacketTraceTest`.
3. Verified both unit tests (`:app:testDebugUnitTest`) and desktop target compilation (`:app:compileKotlinDesktop`).
4. Verified on device with debug install.
