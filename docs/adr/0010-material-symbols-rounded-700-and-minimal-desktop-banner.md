# 0010. Material Symbols Rounded (700 Bold, Filled), Minimal Desktop Banner, and Top MenuBar

Date: 2026-10-28

## Status
Accepted

## Context
1. **Icon Consistency & Styling**: The application previously used mixed Material icons with regular 400 stroke weights and sharp geometric corners. The user requested standardizing all icons exclusively on Google Fonts official **Material Symbols Rounded** (`opsz=24, wght=700, FILL=1, GRAD=0, ROND=50`), which delivers high-contrast bold filled geometry with soft rounded terminals.
2. **Launch Experience**: The previous full-screen splash animation was overly intrusive with heavy background gradients and loaders. The user specified a minimal, non-blocking floating startup banner displaying only the brand mark, "CreaVITy", and "Initialising" over the visible workspace.
3. **Desktop Workstation Control**: Desktop workflows require top-level application navigation (`MenuBar`) for instant keyboard-accessible system operations (File, Edit, View, Tools) and cursor ergonomics.

## Decision

1. **Material Symbols Rounded (24dp, 700 Bold, Filled) Vector Engine**:
   - Ingested 68 official Google Material Symbols Rounded vector definitions from Google Fonts gstatic repository (`opsz=24, wght=700, FILL=1, GRAD=0, ROND=50`).
   - Placed generated Kotlin Compose vector objects in `com.kd.creavity.presentation.ui.symbols.generated.*`.
   - Wired `MaterialSymbol.kt` to map all application icon requests directly to these bold rounded filled vectors across both Desktop and Android builds.

2. **Minimal Desktop Launch Banner**:
   - Created `DesktopLaunchBanner.kt` in `desktopMain/kotlin/com/kd/creavity/ui/components/`.
   - Built a non-fullscreen floating pill banner (`RoundedCornerShape(20.dp)`, `surfaceContainerHighest`, tonal elevation 6.dp, shadow elevation 8.dp) containing:
     - 24dp circular CreaVITy logo (`icon.png`).
     - "CreaVITy" (FontWeight.Bold).
     - Divider dot.
     - "Initialising" status label.
   - Floats cleanly at top center (`Modifier.align(Alignment.TopCenter).padding(top = 16.dp)`) for ~950ms before smoothly sliding and fading out. The main workspace (`AdaptiveWhiteboardScreen`) renders immediately underneath with zero blocking.

3. **Desktop Top MenuBar & Canvas Ergonomics**:
   - Added Compose Desktop native `MenuBar` in `Main.kt`:
     - **File** (`Alt+F`): New Board (`Ctrl+N`), Exit (`Ctrl+Q`).
     - **Edit** (`Alt+E`): Undo (`Ctrl+Z`), Redo (`Ctrl+Y`).
     - **View** (`Alt+V`): Zoom In (`Ctrl++`), Zoom Out (`Ctrl+-`), Reset Zoom (`Ctrl+0`).
     - **Tools** (`Alt+T`): Pen (`P`), Highlighter (`H`), Eraser (`E`), Shapes (`R`), Select (`V`), Sticky Note (`S`), Text (`T`).
   - Added global canvas keyboard zoom accelerators (`Ctrl++`, `Ctrl+-`, `Ctrl+0`) and Spacebar pan activation in `AdaptiveWhiteboardScreen.kt`.
   - Integrated contextual dynamic cursor (`pointerHoverIcon`) in `WhiteboardCanvas.kt`:
     - Crosshair for Pen / Highlighter / Eraser.
     - Text I-beam for Text tool.
     - Hand for Select / Pan tool.

## Consequences
- Every icon in the application displays high-impact, uniform Google Material Symbols Rounded 700 Bold Filled graphics.
- App startup feels instantaneous and modern: no heavy fullscreen splashes, only a light, floating initialization pill over the workspace.
- Pro desktop control via native application menu bar and instant keyboard accelerators.
- Zero test regressions on Desktop and Android unit test suites.
