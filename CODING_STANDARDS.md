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
