# ADR 0002: Mutually Exclusive Peer Discovery, Stylus Palm Rejection, and High-Frequency Digitizer Sensitivity

## Status
Accepted

## Context
1. **Conflicting Discovery State**: In `PeerConnectScreen`, users could trigger both "Host" (broadcasting) and "Join" (scanning) simultaneously. This led to ambiguous Wi-Fi Direct / Nearby Connections states, conflicting advertising and discovery routines, and UI confusion.
2. **Missing Cancel Affordance**: Active broadcasting or scanning required re-tapping the highlighted button without a distinct, explicit "Cancel" label, while the alternative button remained active and clickable.
3. **Digitizer Sampling Sub-Frame Drops**: Android devices with active styluses (S-Pen, USI, Apple Pencil equivalents) and high-refresh displays sample at 120Hz–480Hz. Jetpack Compose batches sub-frame samples in `PointerInputChange.historical`. Because `toCanvasInputEvent()` discarded `change.historical`, up to 75% of raw points were dropped during fast drawing, resulting in corner cutting and sluggish handwriting.
4. **Accidental Palm Marks**: Writing with a stylus naturally places the side of the hand on the touchscreen. Without robust palm rejection, single-finger contact from resting palms created unwanted stray ink marks.
5. **Low-Force Pressure Deadzone**: Light initial pen contact sometimes failed to register cleanly without excessive force due to a linear pressure mapping, making natural sketching and feather-touch writing difficult.

## Decision
1. **Instant Mutually Exclusive Discovery Buttons & Connectivity Gate**:
   - In `PeerConnectScreen`, tapping "Host" or "Join" synchronously updates UI state so the opposite button is immediately greyed out and disabled with 0ms latency, while the tapped button becomes "Cancel".
   - If Bluetooth, Wi-Fi, or Nearby permissions are disabled, prompt the user with system intent launchers and show an actionable readiness banner.
   - Tapping "Cancel" halts discovery/advertising and re-enables both controls.
2. **Persistent Board Viewport Pan and Zoom**:
   - Save the active `panOffset` and `zoom` per board in `WhiteboardViewModel` and persist to `Board`.
   - When switching boards or navigating back from the boards library, restore the board's last panned view rather than resetting to `Offset.Zero`.
3. **Historical Digitizer Ingestion for Touch Sensitivity**:
   - Update `toCanvasInputEvent()` and `CanvasGestureController` to ingest all intermediate micro-points from `PointerInputChange.historical`.
   - Preserve sub-pixel coordinates and timestamps for every historical motion point, boosting touch sensitivity to native hardware rates (120Hz–480Hz).
4. **Strict Stylus Palm Rejection & Calibrated Pressure Curve**:
   - When a stylus pointer (`PointerType.Stylus` or `PointerType.Eraser`) is active or touching the canvas:
     - Single-finger touches are classified as palm contact and suppressed from emitting ink strokes.
     - Two-finger gestures remain dedicated to viewport navigation (pan and pinch-to-zoom).
   - Implement a calibrated non-linear power curve `P_calibrated = P_raw ^ 0.65` for stylus pressure.

## Consequences
- Guaranteed clean discovery state machine without concurrent advertising/scanning conflicts.
- Clear, unambiguous user control with prominent "Cancel" state and disabled secondary action.
- Fluid, ultra-responsive stroke rendering capturing high-speed handwriting without corner cutting.
- Natural resting palm support on tablet screens without accidental ink artifacts.
