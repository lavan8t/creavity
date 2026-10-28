# Specification: Office-Grade Shapes, Diamond Removal, 2-Word Board Naming & Desktop Typography

## 1. Context & Motivation
- **Diamond Tool**: The standalone Diamond tool is awkward and clutters the primary drawing toolkit. Users requested its complete removal in favor of a cohesive, first-class **Shapes** tool option.
- **Office-Grade Shapes**: Existing shape drawing defaulted to sketchy, wobbly multi-line hand-drawn primitives (`RoughnessStyle.SKETCHY`), looking like cartoon scribbles rather than professional office tools. Users expect shapes to behave with the fidelity and precision of Microsoft Word, PowerPoint, Google Slides, and Figma: clean, crisp vector paths, live drag bounding box, and interactive resize handles.
- **Board Naming**: Boards defaulted to "Untitled Board". Users requested that board names be generated automatically as an evocative fusion of two words (e.g., "Cosmic Canvas", "Velvet Echo", "Solar Drift").
- **Desktop Typography**: On Compose Desktop / Linux, `Google Sans Flex` was not registered with the Java AWT `GraphicsEnvironment` or wired statically for canvas text measuring and Material 3 typography, resulting in system font fallbacks.

## 2. Technical Architecture & Decisions

### 2.1. Shapes Tooling & Diamond Removal
- Completely remove `DrawingTool.DIAMOND` from all UI toolbars, FAB menus, and keyboard shortcuts.
- Add a dedicated **Shapes** tool button in `CanvasBottomToolbar`:
  - Active whenever the selected tool is any shape (`RECTANGLE`, `CIRCLE`, `TRIANGLE`, `STAR`, `CYLINDER`, `ARROW`, `LINE`).
  - Displays the active shape icon (or Material `Category`/`Shapes` symbol).
  - Clicking/tapping toggles the `ShapeCalloutCard` and selects the active shape.
- Remove loose shape items from the expandable FAB menu, replacing them with a unified "Shapes" menu item.
- Create `ShapeCalloutCard` in `CanvasCalloutCards.kt`:
  - M3 Expressive card offering one-tap switching between:
    - Rectangle
    - Circle / Oval
    - Triangle
    - Star
    - Cylinder
    - Line
    - Arrow
  - Styling toggles for Fill (Transparent, Solid, Tint) and Geometry style (Clean / Architectural / Sketchy), defaulting to **Clean**.

### 2.2. Office-Grade Shape Geometry & Resizing
- Set default `roughness` in `Stroke` and shape creation to `RoughnessStyle.CLEAN`.
- In `StrokeDrawer.kt`, render clean shapes directly using Compose canvas vector primitives (`drawRect`, `drawOval`, `drawPath`) with smooth anti-aliased geometry and crisp corner joints.
- In `CanvasGestureController.kt`, implement 8-point interactive resize handles on selected shapes:
  - North-West, North, North-East, East, South-East, South, South-West, West.
  - Dragging any handle recalculates stroke/shape bounds dynamically, exactly like PowerPoint and Word.

### 2.3. Two-Word Fusion Board Name Generator
- Introduce `BoardNameGenerator` domain utility:
  - Curated dictionary of 60+ evocative adjectives + 60+ inspiring nouns.
  - Combinations produce names like "Cosmic Canvas", "Solar Drift", "Amber Horizon", "Velvet Peak", "Emerald Glade", "Cobalt Mirage".
- Wire generator across:
  - Default `Board` creation title (`Board.title = BoardNameGenerator.generate()`).
  - `WhiteboardViewModel.createNewBoard(title: String = "")`.
  - `BoardsLibraryScreen.kt` "New Board" button.
  - `PeerConnectScreen.kt` "Create Board" button.
  - Blank-title fallback in board renaming sheets.

### 2.4. Google Sans Flex Desktop Typography Wiring
- Copy `google_sans_flex.ttf` to `app/src/desktopMain/resources/font/google_sans_flex.ttf`.
- In `Main.kt`, initialize and register the font in Java AWT `GraphicsEnvironment` at application startup.
- In `Type.kt`, provide platform-aware `FontFamily` resolution so desktop uses the direct bundled font without relying solely on asynchronous compose-resources lookup, ensuring canvas `textMeasurer` and MaterialTheme immediately have access to `Google Sans Flex`.

## 3. Plan & Ticket Breakdown
- **Ticket 01**: Remove Diamond tool and introduce dedicated Shapes option in toolbar, FAB, and Callout Card.
- **Ticket 02**: Office-grade shape drawing (Clean default vector rendering) & interactive 8-point handle resizing in gesture controller.
- **Ticket 03**: Implement `BoardNameGenerator` and wire 2-word fusion titles across all board creation workflows.
- **Ticket 04**: Wire `Google Sans Flex` font for Desktop JVM, register in AWT `GraphicsEnvironment`, and verify typography.
