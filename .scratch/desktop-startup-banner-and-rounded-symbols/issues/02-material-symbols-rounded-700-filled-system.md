# Issue 02: Material Symbols Rounded 700 Filled System

## Status
resolved

## Description
User requested:
"https://fonts.gstatic.com/render/v1/Material+Symbols+Rounded/24dp/search.kt?var=opsz,wght,FILL,GRAD,ROND@24,700,1,0,50
use these ciosn only. use these styels only 700, rounded, filled for all icons. add that toolbar on top. etc all stuff do."

## Plan
1. Retrieve and ingest Material Symbols Rounded (opsz=24, wght=700, FILL=1, GRAD=0, ROND=50) Kotlin vector sources from Google Fonts gstatic generator.
2. Build `Rounded700Symbols.kt` in `presentation/ui/symbols/`:
   - Search, Add, Close, Check, Delete, ContentCopy, Edit, Draw, Brush, InkHighlighter, InkEraser, Undo, Redo, PanToolAlt, Gesture, CropFree, StickyNote2, TextFields, Category, Rectangle, Circle, ChangeHistory, Star, Share, Menu, Folder, History, Settings, Person, Groups, Visibility, Lock, Info, etc.
   - All styled with weight 700, rounded geometry, and filled paths.
3. Wire into `MaterialSymbol.kt` so every icon in the app uses these official Google Material Symbols Rounded 700 filled vectors.
4. Verify tests pass.
