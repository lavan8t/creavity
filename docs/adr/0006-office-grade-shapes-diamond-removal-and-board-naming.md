# ADR 0006: Office-Grade Shapes Tooling, Diamond Removal, 2-Word Fusion Board Naming & Desktop Typography

## Status
Accepted

## Context
1. **Diamond Tool**: The standalone Diamond tool was awkward and cluttered the primary drawing toolkit. Users requested its complete removal in favor of a cohesive, first-class **Shapes** tool option.
2. **Office-Grade Shapes**: Existing shape drawing defaulted to sketchy, wobbly multi-line hand-drawn primitives (`RoughnessStyle.SKETCHY`). Users requested professional shape fidelity matching Microsoft Word, PowerPoint, Google Slides, and Figma: clean, crisp vector paths, live drag bounding box, and interactive resize handles.
3. **Board Naming**: Default "Untitled Board" naming lacked memorability. Users requested that board names be generated automatically as an evocative fusion of two words (e.g., "Cosmic Canvas", "Velvet Echo", "Solar Drift").
4. **Desktop Typography**: On Compose Desktop / Linux, `Google Sans Flex` was not registered with the Java AWT `GraphicsEnvironment` or wired statically for canvas text measuring and Material 3 typography, resulting in system font fallbacks.

## Decision
1. **Remove Diamond & Add Dedicated Shapes Option**:
   - Removed `DrawingTool.DIAMOND` from all toolbars, FAB menus, and keyboard shortcuts.
   - Introduced a dedicated **Shapes** button (`ToolbarIconButton`) in `CanvasBottomToolbar` anchored between Eraser and Select.
   - Consolidated individual shapes in the expandable FAB menu into a single "Shapes" option.
   - Created `ShapeCalloutCard`: An M3 Expressive card providing one-tap access to Rectangle, Circle/Oval, Triangle, Arrow, Line, Star, and Cylinder.
2. **Office-Grade Clean Geometry & 8-Point Handle Resizing**:
   - Set default `roughness` in `Stroke` model to `RoughnessStyle.CLEAN`.
   - Render clean vector geometry directly via Compose Canvas vector primitives (`drawRect`, `drawOval`, `drawPath`) with smooth anti-aliased geometry when `roughness == CLEAN`.
   - Added interactive 8-point resize handles (NW, N, NE, E, SE, S, SW, W) in `CanvasGestureController` and `WhiteboardCanvas`, allowing users to drag handles to scale and reshape objects directly on canvas.
3. **Two-Word Fusion Board Name Generator**:
   - Created `BoardNameGenerator` domain service featuring a curated dictionary of evocative adjectives and canvas/nature nouns.
   - Wired `BoardNameGenerator.generate()` into default board creation, new board actions, and blank-title dialog fallbacks.
4. **Desktop Typography Wiring**:
   - Bundled `google_sans_flex.ttf` in `desktopMain/resources/font/`.
   - Registered the font with Java AWT `GraphicsEnvironment` at application startup in `Main.kt`.
   - Wired `AppFontFamily` with `androidx.compose.ui.text.platform.Font` synchronously on desktop JVM.

## Consequences
- Clean, professional vector shapes matching office software standards (PowerPoint, Word, Google Slides).
- Clutter-free toolbar with intuitive Shapes picker.
- Memorable, unique 2-word board names across all creation points.
- Crisp `Google Sans Flex` typography across Linux desktop application windows, canvas text rendering, and dialogs.
