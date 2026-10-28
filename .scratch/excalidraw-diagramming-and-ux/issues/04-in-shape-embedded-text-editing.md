# Ticket 04: In-Shape Embedded Text Editing

Status: resolved
Blocked by: 01, 02

## What it delivers
- Embedded text attributes in `Stroke`:
  - `embeddedText: String? = null`
  - `embeddedTextColor: Long = 0L`
- Inline editing integration:
  - Double-tapping a rectangle, diamond, cylinder, or circle triggers inline text input centered within the shape.
  - Automatically wraps lines to fit within shape interior padding.
- Synchronous transformation:
  - When moving, resizing, or duplicating the shape, the embedded text moves and transforms with it as a single element.

## Verification
- Unit test in `EmbeddedTextTest.kt` verifying text assignment, bounding box inclusion, and synchronous translation with parent shape.
- `./gradlew :app:testDebugUnitTest` passes.
