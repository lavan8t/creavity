# Spec: Exclusive Peer Discovery, Stylus Palm Rejection & High-Frequency Digitizer Sensitivity

Status: ready-for-agent

## Problem Statement

Users of CreaVITy experience two key interaction hurdles:
1. **Conflicting Discovery Controls**: On the `PeerConnectScreen`, users could trigger "Host" (broadcasting) and "Join" (scanning) at the exact same time. The buttons lacked mutual exclusivity and clear "Cancel" affordances, causing ambiguous network states, race conditions, and confusing UI.
2. **Touch Sensitivity & Stylus Fidelity**: When inking rapidly or writing cursive on tablets/phones with styluses (such as Samsung S-Pen, USI pens, or active styluses):
   - High-frequency intermediate micro-points (120Hz–480Hz digitizer rate) were discarded by the canvas pipeline because `PointerInputChange.historical` was ignored, causing corner-cutting and segmented curves.
   - Resting palms on the glass generated accidental ink marks because the gesture controller did not enforce strict stylus palm rejection.
   - Delicate initial pen strokes required excessive physical pressure to register due to a linear pressure mapping.

## Solution

1. **Mutually Exclusive Discovery with Cancel Affordance & Connectivity Gate**:
   - Enforce that broadcasting and scanning can never occur simultaneously.
   - The moment (0ms) "Host" is tapped: start broadcasting, immediately update its label to "Cancel", and grey out/disable the "Join" button.
   - The moment (0ms) "Join" is tapped: start scanning, immediately update its label to "Cancel", and grey out/disable the "Host" button.
   - Tapping "Cancel" terminates the active operation, resetting both buttons to their default enabled states ("Host" and "Join").
   - Validate Bluetooth, Wi-Fi, and Nearby Devices readiness; if disabled, launch system enablement intents and notify the user.
2. **Persistent Board Viewport (Last Panned View)**:
   - Cache each board's last panned view (`panOffset` and `zoom`) in memory and persist across board switches.
   - When returning to the canvas or switching boards, restore the board's last panned view rather than resetting to `(0, 0)`.
3. **Historical Digitizer Ingestion for Peak Touch Sensitivity**:
   - Expand `CanvasInputEvent` and `toCanvasInputEvent()` to extract and process all sub-frame points from `PointerInputChange.historical`.
   - Ingest every intermediate micro-coordinate, timestamp, and pressure point into `CanvasGestureController` so rapid handwriting captures full hardware digitizer resolution (120Hz–480Hz).
4. **Strict Stylus Palm Rejection & Touch Disambiguation**:
   - When a stylus pointer (`PointerType.Stylus` or `PointerType.Eraser`) is detected:
     - Single-finger touches are classified as palm contact and suppressed from emitting ink strokes.
     - Two-finger touches remain exclusively dedicated to canvas viewport manipulation (pan and pinch-to-zoom).
5. **Calibrated Pressure Curve for Natural Response**:
   - Apply a non-linear power-law pressure transfer function `P_calibrated = P_raw ^ 0.65` in `StylusInputHandler`.
   - Gives immediate, feather-light response on initial pen touch, scaling smoothly to bold lines under deliberate pressure across all stroke tools.

## User Stories

1. As a whiteboard user hosting a session, I want the Host button to clearly say "Cancel" once broadcasting starts, so that I have an immediate and obvious way to stop broadcasting.
2. As a whiteboard user hosting a session, I want the Join button to be disabled and greyed out while broadcasting, so that I cannot accidentally trigger scanning and break my network state.
3. As a whiteboard user searching for sessions, I want the Join button to turn into a "Cancel" button and the Host button to be disabled while scanning, so that I can cancel scanning with a single tap.
4. As an artist or student writing with a stylus, I want all rapid strokes and cursive curves to be smoothly captured without angular corners, so that handwriting feels natural and responsive.
5. As a tablet user resting my hand on the screen while using a pen, I want my palm to be completely ignored by the inking engine, so that I don't leave accidental smudges on the whiteboard.
6. As a tablet user resting my hand on the screen, I want two-finger gestures to still zoom and pan smoothly without producing ink, so that navigating the whiteboard is effortless.
7. As a pen user making delicate sketches, I want light touches to leave crisp, fine lines immediately without having to press hard onto the glass, so that the pen feels sensitive and expressive.

## Implementation Decisions

- **UI Button Exclusivity in `PeerConnectScreen.kt`**:
  - `isAdvertising` active: Host button text is "Cancel" with close icon, Join button has `enabled = false` and reduced alpha/tonal surface color.
  - `isDiscovering` active: Join button text is "Cancel" with close icon, Host button has `enabled = false` and reduced alpha/tonal surface color.
  - Both idle: Both enabled with default "Host" and "Join" labels.
- **Historical Point Ingestion in `CanvasInputEvent.kt` & `WhiteboardCanvas.kt`**:
  - In `PointerEvent.toCanvasInputEvents(): List<CanvasInputEvent>` or `CanvasInputEvent(historicalPoints: List<Point>)`:
    - Iterate `change.historical` for the active drawing pointer to capture all intermediate points between the previous frame and the current frame.
    - Feed intermediate points to `CanvasGestureController` during `MOVE` so quadratic bezier smoothing connects all sub-frame digitizer points.
- **Stylus Palm Rejection in `CanvasGestureController.kt`**:
  - Track `hasStylusPointer` and `lastStylusTimestampMs`.
  - While stylus is active or has touched within recent palm-rejection window (e.g. 500ms):
    - Single-touch finger events are suppressed from starting or continuing ink strokes.
    - Multi-touch finger gestures continue to pan and zoom.
- **Stylus Pressure Calibration in `StylusInputHandler.kt`**:
  - Apply `val calibratedPressure = if (rawPressure > 0f) rawPressure.toDouble().pow(0.65).toFloat().coerceIn(0.05f, 1.0f) else 1.0f`.
  - Ensures responsive low-pressure drawing for soft stylus touches.

## Testing Decisions

- Unit test in `PeerConnectScreenTest` or ViewModel test verifying mutual exclusivity of discovery actions.
- Unit test in `CanvasGestureControllerTest` / `StylusInputHandlerTest` verifying that historical points are processed correctly and pressure mapping follows calibrated curve.
- Unit test verifying palm rejection suppresses single finger touches when stylus is active.
- Verify with `./gradlew :app:testDebugUnitTest` and assemble debug APK with zero errors.
