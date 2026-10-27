# Ticket 02: Apply Verified Safe Ponytail Cleanups

Status: resolved
Blocked by: none

## What it delivers
- Delete duplicate 4.15MB `app/src/main/res/font/google_sans_flex.ttf` (Compose resources in `commonMain` is the active source).
- Delete duplicate `app/src/main/AndroidManifest.xml` (the Gradle build explicitly uses `src/androidMain/AndroidManifest.xml`).
- Remove `implementation("androidx.fragment:fragment-ktx:1.8.5")` from `app/build.gradle.kts`.
- Delete unused transitions in `BrevityMotion.kt` (`enterSlideLeft`, `exitSlideRight`, `enterSlideRight`, `exitSlideLeft`, `springSnappy`, `springGentle`).
- In `MutationHistory.kt`, replace `ArrayList.removeAt(0)` with stdlib `ArrayDeque`.

## Verification
- `./gradlew :app:testDebugUnitTest` passes.
- Font is correctly loaded by Compose UI theme.
- All MutationHistory unit tests pass.
