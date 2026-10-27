# Issue 04: Verification and Unit Tests

Status: resolved
Blockers: 01-exclusive-broadcast-scan-buttons.md, 03-stylus-palm-rejection-and-pressure-curve.md

## Context
Ensure all changes are verified by automated tests and compilation before marking the task complete.

## Acceptance Criteria
1. Unit tests verify mutual exclusivity of discovery and advertising triggers.
2. Unit tests verify that historical digitizer points are ingested and appended to active stroke points during `MOVE`.
3. Unit tests verify palm rejection behavior: single-finger touch is rejected when stylus is active, while two-finger pinch-to-zoom is preserved.
4. Unit tests verify calibrated pressure mapping gives elevated sensitivity at low pressure values.
5. `./gradlew :app:testDebugUnitTest` passes with 0 failures.
6. `./gradlew :app:assembleDebug` builds successfully.
