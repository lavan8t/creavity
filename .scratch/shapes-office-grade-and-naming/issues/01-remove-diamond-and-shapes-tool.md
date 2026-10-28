# Issue 01: Remove Diamond Tool & Introduce Dedicated Shapes Tool Option

## Status
resolved

## Description
Remove `DrawingTool.DIAMOND` from all UI toolbars, FAB menus, and keyboard shortcuts. Instead of cluttering the tools menu with loose shapes and diamond, introduce a dedicated "Shapes" tool button on the main floating toolbar and FAB menu that opens a Material 3 Expressive `ShapeCalloutCard` with Rectangle, Circle/Oval, Triangle, Arrow, Line, Star, and Cylinder.

## Tasks
- [x] Remove `Diamond` from `CanvasBottomToolbar.kt` FAB items.
- [x] Remove `Key.D -> SelectTool(DrawingTool.DIAMOND)` from `AdaptiveWhiteboardScreen.kt`.
- [x] Add Shapes tool button (`ToolbarIconButton`) in `CanvasBottomToolbar.kt` floating toolbar between Eraser and Select.
- [x] Add `ShapeCalloutCard` in `CanvasCalloutCards.kt` allowing one-tap selection among Rectangle, Circle, Triangle, Star, Cylinder, Arrow, and Line.
- [x] Anchor `ShapeCalloutCard` in `CanvasViewportScreen.kt` when `activeCalloutTool == "SHAPES"`.
- [x] Consolidate individual shapes in FAB menu into a single "Shapes" option.
- [x] Update unit tests to reflect shapes tooling without Diamond.
