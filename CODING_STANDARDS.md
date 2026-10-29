# Coding Standards

Read by `/code-review` (the reviewer agent), not by the implementation agent. Only judgement calls
live here: anything a tool can decide belongs in `.github/workflows/ci.yml` or the Gradle build
instead, where it fails automatically.

Every rule below cites the defect that produced it. Keep that habit when adding a rule.

## Render-state caches are keyed by the geometry they cache

A cache over rendered or derived state must be invalidated by every input that changes its output,
and the key must live beside the cache rather than at the call sites. `StrokeGeometryCache` is the
reference: it derives its key from point count, endpoints, tool and width, so a shape that mutates
under a constant id still invalidates.

_Evidence: peer ink froze at its first point because the path cache keyed on `stroke.id` alone,
while a streamed remote stroke grows under one id._

## Streamed state is not settled state

A local stroke is immutable once committed. A remote element is not: it arrives incrementally under
a constant id, so treat "in flight" and "committed" as different states with different invariants.

_Evidence: same defect as above; the cache assumed creation-time immutability that collaboration
breaks._

## A handoff replaces, it never appends

When an element moves from a live representation into the document, the handoff removes the live
copy. One element id must resolve to one element in the page.

_Evidence: `StrokeEnd` drops the id from `remoteStrokesMap` and then appends the finished stroke
while the merged live copy is still in `page.strokes`, so every peer stroke persists twice._

## Tests drive the production seam

A test asserts what production actually consumes: the cache state, the rendered decision, the
handler's resulting page. A test that restates a helper's arithmetic passes even when the feature is
broken.

_Evidence: the first geometry test asserted only that a key-minting function returned different
strings; it would have stayed green with the cache still stale._

## Third-party versions live in the version catalog

`gradle/libs.versions.toml` holds the only copy of any dependency version. Build scripts reference
the `libs.*` alias.

_Evidence: `kotlinx-coroutines-test` was added with a hardcoded version while the catalog already
pinned `coroutines`._

## Dynamic background patterns are anchored to world coordinates, not viewport bounds

Any pattern rendered across an infinite or transformable canvas must compute column/row offsets from
absolute world coordinates (e.g., `y = r * rowHeight`, `offsetX = if (abs(r % 2) != 0) spacing * 0.5f else 0f`),
never from the visible viewport's top-left corner.

_Evidence: isometric dot grid calculated row parity from the visible `gridStartY` instead of absolute
world row. Panning vertically shifted `gridStartY` across row boundaries, flipping row parity and
jumping all dots 24px horizontally on even/odd transitions, appearing to move at a different pace from
whiteboard art._

## Dense canvas patterns are batched into GPU primitive arrays

Repetitive background or decorative canvas elements (dots, grid vertices, point clouds) must be
batched and drawn via a single call (`DrawScope.drawPoints` with `PointMode.Points`) rather than
thousands of per-element `drawCircle()` calls.

_Evidence: whiteboard canvas dot grid called `drawCircle` up to 20,000 times per frame via JNI to
Skia, causing 50–100ms frame times (10–15 FPS). Batching with `drawPoints` dropped render time to
<0.2ms._

## Hardware event bridges must coalesce updates before UI dispatch

High-frequency background event producers running faster than display refresh (e.g., `libinput` at
250 Hz) must coalesce delta factors and ensure at most one pending dispatch is queued in the UI event
loop at any time.

_Evidence: `LinuxTouchpadGestureBridge` dispatched `SwingUtilities.invokeLater` on every 4ms event tick,
flooding the AWT EventQueue with hundreds of state recompositions that played back sequentially long
after the gesture ended._

