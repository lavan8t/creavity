# Issue 03: Stylus Palm Rejection and Calibrated Pressure Curve

Status: resolved
Blockers: 02-historical-digitizer-points-ingestion.md

## Context
Resting a palm on the screen while writing with a stylus produces stray ink marks if the digitizer detects single-finger touch before or during pen strokes. Furthermore, default linear pressure mapping requires excessive force for delicate initial pen contact.

## Acceptance Criteria
1. When a stylus (`PointerType.Stylus` or `PointerType.Eraser`) is touching or has touched within the active palm-rejection window, single-finger touches are treated as palm contact and strictly ignored for inking.
2. Two-finger touch gestures remain fully functional for viewport panning and pinch-to-zoom even when palm rejection is active.
3. Apply a calibrated non-linear power curve (`rawPressure ^ 0.65`) in `StylusInputHandler` so that light touches render immediate fine lines, scaling smoothly to full width under firmer pressure.
