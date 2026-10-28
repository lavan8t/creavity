# Issue 04: Testing, Verification, and Desktop Distributable Sync

## Status
resolved

## Description
Run all unit tests across Desktop JVM and Android, verify zero regressions, package the release binary, and update the installed desktop files.

## Plan
1. Add tests in `app/src/desktopTest/kotlin/com/kd/creavity/presentation/canvas/ShapeGeometryAndDockPositionTest.kt`.
2. Run `./gradlew testDebugUnitTest :app:desktopTest`.
3. Build `./gradlew :app:createReleaseDistributable`.
4. Sync release binary to `~/.local/share/creavity/` and verify `~/.local/bin/creavity`.
5. Update ADR and commit with `--no-gpg-sign`.
