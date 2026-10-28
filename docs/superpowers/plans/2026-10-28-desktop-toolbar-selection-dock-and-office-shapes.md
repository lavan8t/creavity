# Desktop Toolbar, Selection Dock & Shape Geometry Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix shape geometry (smooth circles without decagon/nonagon artifacts, precise vertex alignment), move drawing tools to a draggable Material 3 Expressive vertical dock on the left for desktop, and position the contextual selection dock directly around selected elements instead of pinned at top-center.

**Architecture:**
- Geometry: Ensure `WhiteboardCanvas` forwards `activeStroke.roughness` and `fillStyle` so `RoughnessStyle.CLEAN` renders true vector paths. Update `createRoughEllipse` in `RoughShapeGenerator` to use 4-arc cubic Bézier curves instead of 12-line polygons.
- Desktop Toolbar: Introduce `DesktopDraggableToolbar.kt` with Material 3 Expressive pill styling, drag handle, and gesture tracking clamped to viewport.
- Selection Dock: Compute bounding box screen coordinates from `selectionBounds` and position `FloatingSelectionDock` dynamically above/below the target element.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Skiko / Jetpack Ink math, Material 3 Expressive.

**Spec:** `.scratch/desktop-toolbar-selection-dock-and-shapes/issues/`

## Global Constraints
- Material 3 Expressive design tokens only; no arbitrary borders (`border = null`).
- Responsive: Draggable vertical toolbar applies when `isDesktopPlatform() == true`; mobile Android preserves bottom bar.
- Selection dock must dynamically adapt to zoom, pan, and canvas boundaries.
- All commits with `--no-gpg-sign`.

---

### Task 1: Fix Shape Geometry and Smooth Circle/Vertex Construction

**Files:**
- Modify: `app/src/commonMain/kotlin/com/kd/creavity/presentation/canvas/StrokeDrawer.kt`
- Modify: `app/src/commonMain/kotlin/com/kd/creavity/presentation/canvas/WhiteboardCanvas.kt`
- Modify: `app/src/commonMain/kotlin/com/kd/creavity/presentation/canvas/RoughShapeGenerator.kt`
- Test: `app/src/desktopTest/kotlin/com/kd/creavity/presentation/canvas/ShapeGeometryTest.kt`

**Interfaces:**
- `StrokeDrawer.drawStyledStroke(..., roughness = RoughnessStyle.CLEAN, ...)`
- `WhiteboardCanvas` passes `stroke.roughness` and `stroke.fillStyle`
- `RoughShapeGenerator.createRoughEllipse` produces cubic Bézier curves, never 12-segment polygons.

- [ ] **Step 1: Write the failing test for smooth circle and clean vertex geometry**

```kotlin
package com.kd.creavity.presentation.canvas

import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.RoughnessStyle
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ShapeGeometryTest {
    @Test
    fun testRoughEllipseProducesSmoothBezierCurvesNotCoarsePolygon() {
        val start = Point(100f, 100f)
        val end = Point(300f, 300f)
        val shape = RoughShapeGenerator.createRoughEllipse(
            start = start,
            end = end,
            strokeWidth = 4f,
            roughness = 1.0f,
            fillStyle = FillStyle.TRANSPARENT,
            random = Random(42)
        )
        assertNotNull(shape.strokePaths)
        assertTrue("Rough ellipse must have stroke paths", shape.strokePaths.isNotEmpty())
    }
}
```

- [ ] **Step 2: Run test to verify initial status**
Run: `./gradlew :app:desktopTest --tests "com.kd.creavity.presentation.canvas.ShapeGeometryTest"`

- [ ] **Step 3: Update `StrokeDrawer.kt`, `WhiteboardCanvas.kt`, and `RoughShapeGenerator.kt`**
- Set default `roughness = RoughnessStyle.CLEAN` in `StrokeDrawer.drawStyledStroke`.
- In `WhiteboardCanvas.kt` (lines 331 and 471), pass `roughness = activeStroke.roughness`, `fillStyle = activeStroke.fillStyle`, `strokeId = activeStroke.id`.
- In `RoughShapeGenerator.kt`, refactor `createRoughEllipse` to generate smooth cubic curves using 4 quadrants with cubic control points (`cubicTo`), eliminating 12-segment polygon lines.
- For rectangles and triangles, ensure vertices connect without overshoots when roughness is clean.

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew :app:desktopTest`

- [ ] **Step 5: Commit**
```bash
git add app/src/commonMain/kotlin/com/kd/creavity/presentation/canvas/ app/src/desktopTest/
git commit --no-gpg-sign -m "fix(shapes): replace polygon circles with smooth bezier curves and wire clean roughness"
```

---

### Task 2: Build Draggable Desktop Material 3 Expressive Toolbar

**Files:**
- Create: `app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/components/DesktopDraggableToolbar.kt`
- Modify: `app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/screens/CanvasViewportScreen.kt`

**Interfaces:**
- `DesktopDraggableToolbar(uiState, activeCalloutTool, onAction, onToggleCallout, onOpenStickyNote, onOpenTextDialog, ...)`
- Draggable state `toolbarOffset: Offset` with boundary clamping.

- [ ] **Step 1: Create `DesktopDraggableToolbar.kt`**
- Material 3 Expressive vertical dock styled with `RoundedCornerShape(24.dp)`, `surfaceContainerHigh`, tonal elevation 6.dp, shadow elevation 8.dp.
- Distinctive drag handle at the top (`detectDragGestures` modifying `offset`).
- Vertical stack of tool buttons: Pen, Marker, Highlighter, Brush, Eraser, Shapes, Select, Sticky Note, Text, Undo, Redo.

- [ ] **Step 2: Wire in `CanvasViewportScreen.kt`**
- In `CanvasViewportScreen.kt`, check `if (isDesktopPlatform()) DesktopDraggableToolbar(...) else CanvasBottomToolbar(...)`.
- Anchor shape callout cards and pen callout cards directly adjacent to the draggable dock.

- [ ] **Step 3: Test compilation and verify desktop build**
Run: `./gradlew :app:compileKotlinDesktop`

- [ ] **Step 4: Commit**
```bash
git add app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/components/DesktopDraggableToolbar.kt app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/screens/CanvasViewportScreen.kt
git commit --no-gpg-sign -m "feat(desktop): add draggable material 3 expressive vertical toolbar"
```

---

### Task 3: Contextual Selection Dock Relative Positioning Around Curves/Shapes

**Files:**
- Modify: `app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/screens/CanvasViewportScreen.kt`
- Create: `app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/components/SelectionDockPositioner.kt`
- Test: `app/src/desktopTest/kotlin/com/kd/creavity/presentation/ui/components/SelectionDockPositionerTest.kt`

**Interfaces:**
- `SelectionDockPositioner.calculateDockPosition(bounds, zoom, panOffset, canvasSize, dockSize): IntOffset`

- [ ] **Step 1: Write test for selection dock relative positioning**

```kotlin
package com.kd.creavity.presentation.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionDockPositionerTest {
    @Test
    fun testDockPositionsDirectlyAboveSelectionBounds() {
        val bounds = Rect(200f, 300f, 400f, 500f)
        val pos = SelectionDockPositioner.calculateDockPosition(
            selectionBounds = bounds,
            zoom = 1.0f,
            panOffset = Offset.Zero,
            canvasSize = IntSize(1920, 1080),
            dockWidth = 400,
            dockHeight = 60
        )
        // Center X = 300, dock starts at 300 - 200 = 100
        assertEquals(100, pos.x)
        // Top = 300, dock placed above: 300 - 60 - 16 = 224
        assertEquals(224, pos.y)
    }

    @Test
    fun testDockFlipsBelowWhenNearTopEdge() {
        val bounds = Rect(200f, 20f, 400f, 100f)
        val pos = SelectionDockPositioner.calculateDockPosition(
            selectionBounds = bounds,
            zoom = 1.0f,
            panOffset = Offset.Zero,
            canvasSize = IntSize(1920, 1080),
            dockWidth = 400,
            dockHeight = 60
        )
        // Top edge is too close to screen top (< 72px), so dock flips below: bottom (100) + 16 = 116
        assertEquals(116, pos.y)
    }
}
```

- [ ] **Step 2: Implement `SelectionDockPositioner.kt`**
- Calculate target `(x, y)` relative to `selectionBounds`.
- Clamp within `[16, canvasWidth - dockWidth - 16]` and `[64, canvasHeight - dockHeight - 16]`.

- [ ] **Step 3: Update `CanvasViewportScreen.kt`**
- Replace hardcoded `.align(Alignment.TopCenter)` with dynamic `.offset { dockOffset }`.

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew :app:desktopTest`

- [ ] **Step 5: Commit**
```bash
git add app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/ app/src/desktopTest/
git commit --no-gpg-sign -m "feat(selection): dynamically position selection dock around selected curves and shapes"
```

---

### Task 4: Full Verification, ADR 0008, and Release Distributable Update

**Files:**
- Modify: `docs/adr/0008-draggable-desktop-toolbar-selection-dock-and-shape-bezier-geometry.md`
- Modify: `GLOSSARY.md`

- [ ] **Step 1: Run full test suite across Desktop JVM and Android**
Run: `./gradlew testDebugUnitTest :app:desktopTest`

- [ ] **Step 2: Build release distributable**
Run: `./gradlew :app:createReleaseDistributable`

- [ ] **Step 3: Sync to `~/.local/share/creavity/`**
Run: `rsync -a --delete app/build/compose/binaries/main-release/app/CreaVITy/ ~/.local/share/creavity/`

- [ ] **Step 4: Push to origin main**
Run: `git push origin main`
