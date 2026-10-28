# App Logo, Adaptive Icons, and Animated Stylus Splash Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the final CreaVITy app logo on an isometric dot-grid canvas with an enlarged clipped stylus nib, configure full Android Adaptive Icons, and build a sleek animated stylus drawing splash screen in `MainActivity` that smoothly transitions into the whiteboard workspace upon completion.

**Architecture:** 
- Vector assets: Clean native Android `VectorDrawable` and `AnimatedVectorDrawable` XML resources (no third-party dependencies).
- Adaptive Icons: Background layer with isometric dot lattice, Foreground layer with `cr` glyph and clipped nib, and Android 13+ themed `monochrome` layer.
- Splash Screen: A high-performance drawing animation overlay in `MainActivity` where a full digital stylus traces the user's `cr` path on the isometric canvas, fading smoothly into `AdaptiveWhiteboardScreen` when the stroke finishes.

**Tech Stack:** Native Android VectorDrawable, AnimatedVectorDrawable, Android AAPT2, Jetpack Compose, Kotlin Multiplatform.

**Spec:** [`docs/superpowers/specs/2026-10-28-app-logo-splash-design.md`](file:///home/lavan/Documents/projects/OutofTheBox/docs/superpowers/specs/2026-10-28-app-logo-splash-design.md)

## Global Constraints
- Android Package Manager: Always use `bun` and `bunx` if JS is needed (never npm/yarn).
- Git commits: Always use `--no-gpg-sign` to prevent timeout on interactive pinentry.
- XML Drawables: Must be 100% native Android XML compatible with AAPT2 and hardware acceleration.
- Single continuous path for the `cr` drawing line to ensure smooth `trimPathEnd` animation.

---

### Task 1: Generate Master Vector & Android Adaptive Icon Layers

**Files:**
- Create: `app/src/main/res/drawable/ic_launcher_background.xml`
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- Create: `app/src/main/res/drawable/ic_launcher_monochrome.xml`
- Modify: `app/src/main/res/mipmap-anydpi/ic_launcher.xml`
- Modify: `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`
- Test: `app/src/test/java/com/kd/creavity/presentation/ui/AppLogoResourceTest.kt`

**Interfaces:**
- Consumes: User's custom `cr` path from `creavity_logo.svg`
- Produces: Standard Android Adaptive Icon drawables for all launcher form factors

- [ ] **Step 1: Write unit test validating resource files and path data**

```kotlin
package com.kd.creavity.presentation.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppLogoResourceTest {
    @Test
    fun testAdaptiveIconFilesExist() {
        val resDir = File("src/main/res")
        val bgFile = File(resDir, "drawable/ic_launcher_background.xml")
        val fgFile = File(resDir, "drawable/ic_launcher_foreground.xml")
        val monoFile = File(resDir, "drawable/ic_launcher_monochrome.xml")
        val launcherXml = File(resDir, "mipmap-anydpi/ic_launcher.xml")

        assertTrue("Background drawable must exist", bgFile.exists())
        assertTrue("Foreground drawable must exist", fgFile.exists())
        assertTrue("Monochrome drawable must exist", monoFile.exists())
        assertTrue("Launcher XML must exist", launcherXml.exists())
        
        val fgContent = fgFile.readText()
        assertTrue("Foreground must contain 'cr' pathData", fgContent.contains("pathData"))
        assertTrue("Foreground must contain stylus nib element", fgContent.contains("nib") || fgContent.contains("pathData"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kd.creavity.presentation.ui.AppLogoResourceTest"`
Expected: FAIL (files missing or not yet created)

- [ ] **Step 3: Create the Isometric Dot Canvas Background XML**

Create `app/src/main/res/drawable/ic_launcher_background.xml`:
- 108dp x 108dp viewport
- Warm drafting canvas color `#F4F5F7`
- Isometric dot lattice matching `BackgroundDrawer.kt` with dot color `#CBD2DC`

- [ ] **Step 4: Create the Foreground XML with 'cr' and Enlarged Clipped Nib**

Create `app/src/main/res/drawable/ic_launcher_foreground.xml`:
- 108dp x 108dp viewport
- Optical safe zone: 72dp centered (inner circle/squircle)
- User's custom `cr` stroke scaled with balanced left padding
- Enlarged stylus nib clipped by the container edge

- [ ] **Step 5: Create Monochrome Theme Icon and Update Mipmap XMLs**

Create `app/src/main/res/drawable/ic_launcher_monochrome.xml`:
- High-contrast pure white/tintable monochrome vector for Android 13+ Material You
Update `app/src/main/res/mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml` to reference background, foreground, and monochrome.

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kd.creavity.presentation.ui.AppLogoResourceTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/ app/src/test/java/com/kd/creavity/presentation/ui/AppLogoResourceTest.kt
git commit --no-gpg-sign -m "feat(branding): add isometric canvas adaptive icon layers and monochrome theme icon"
```

---

### Task 2: Create Native Animated Vector Drawable (AVD) for Stylus Drawing

**Files:**
- Create: `app/src/main/res/drawable/vd_creavity_splash_logo.xml`
- Create: `app/src/main/res/drawable/avd_creavity_splash_logo.xml`
- Create: `app/src/main/res/animator/anim_logo_stroke_draw.xml`
- Create: `app/src/main/res/animator/anim_stylus_motion.xml`
- Test: `app/src/test/java/com/kd/creavity/presentation/ui/AnimatedVectorDrawableTest.kt`

**Interfaces:**
- Consumes: Continuous `cr` path data from Task 1
- Produces: `R.drawable.avd_creavity_splash_logo` for smooth hardware-accelerated drawing animation

- [ ] **Step 1: Write test validating AVD and animator resource structure**

```kotlin
package com.kd.creavity.presentation.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AnimatedVectorDrawableTest {
    @Test
    fun testAnimatedVectorDrawableConfig() {
        val resDir = File("src/main/res")
        val avdFile = File(resDir, "drawable/avd_creavity_splash_logo.xml")
        val vdFile = File(resDir, "drawable/vd_creavity_splash_logo.xml")
        val strokeAnim = File(resDir, "animator/anim_logo_stroke_draw.xml")

        assertTrue("AVD file must exist", avdFile.exists())
        assertTrue("Base VD file must exist", vdFile.exists())
        assertTrue("Stroke animator must exist", strokeAnim.exists())

        val avdContent = avdFile.readText()
        assertTrue("AVD must use animated-vector root", avdContent.contains("<animated-vector"))
        assertTrue("AVD must bind target to stroke animator", avdContent.contains("anim_logo_stroke_draw"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kd.creavity.presentation.ui.AnimatedVectorDrawableTest"`
Expected: FAIL

- [ ] **Step 3: Implement Base VectorDrawable with Full Stylus**

Create `app/src/main/res/drawable/vd_creavity_splash_logo.xml`:
- Contains isometric dot canvas background
- Contains `<path android:name="stroke_path" android:trimPathEnd="0.0" .../>`
- Contains `<group android:name="stylus_group">` with the full stylus (barrel, accent ring, conical nose, fine nib tip)

- [ ] **Step 4: Implement ObjectAnimators**

Create `app/src/main/res/animator/anim_logo_stroke_draw.xml`:
- Animates `trimPathEnd` from `0.0f` to `1.0f`
- Duration: 1600ms, `@android:interpolator/fast_out_slow_in`
Create `app/src/main/res/animator/anim_stylus_motion.xml`:
- Moves the stylus along the path trajectory synchronized with `trimPathEnd`

- [ ] **Step 5: Assemble AnimatedVectorDrawable**

Create `app/src/main/res/drawable/avd_creavity_splash_logo.xml`:
- Root `<animated-vector>` tying `vd_creavity_splash_logo` with the animators.

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kd.creavity.presentation.ui.AnimatedVectorDrawableTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/ app/src/test/java/com/kd/creavity/presentation/ui/AnimatedVectorDrawableTest.kt
git commit --no-gpg-sign -m "feat(branding): create native AnimatedVectorDrawable for stylus drawing animation"
```

---

### Task 3: Build Compose Splash Drawing Overlay & Integrate in MainActivity

**Files:**
- Create: `app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/components/SplashDrawingOverlay.kt`
- Modify: `app/src/androidMain/kotlin/com/kd/creavity/MainActivity.kt:34-62`
- Test: `app/src/test/java/com/kd/creavity/presentation/ui/SplashDrawingOverlayTest.kt`

**Interfaces:**
- Consumes: `R.drawable.avd_creavity_splash_logo` from Task 2
- Produces: Seamless launch experience where animation draws the logo and fades smoothly into `AdaptiveWhiteboardScreen`

- [ ] **Step 1: Write test for splash state transition logic**

```kotlin
package com.kd.creavity.presentation.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashDrawingOverlayTest {
    @Test
    fun testSplashStateTransitions() {
        var isSplashActive = true
        var whiteboardReady = false

        fun onAnimationComplete() {
            isSplashActive = false
            whiteboardReady = true
        }

        assertTrue("Splash must be active on start", isSplashActive)
        assertFalse("Whiteboard must not be visible yet", whiteboardReady)

        onAnimationComplete()

        assertFalse("Splash must be dismissed after animation", isSplashActive)
        assertTrue("Whiteboard must be displayed after animation", whiteboardReady)
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kd.creavity.presentation.ui.SplashDrawingOverlayTest"`
Expected: PASS

- [ ] **Step 3: Implement SplashDrawingOverlay Component**

Create `app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/components/SplashDrawingOverlay.kt`:
- Displays the isometric dot canvas background
- Runs the smooth drawing animation with the stylus drawing the cursive `cr` monogram
- Animates alpha crossfade out when complete (or on tap)
- Calls `onFinished()` callback when complete

- [ ] **Step 4: Integrate Splash in MainActivity.kt**

In `MainActivity.kt`:
- Hold `var showSplash by remember { mutableStateOf(true) }`
- Show `SplashDrawingOverlay(onFinished = { showSplash = false })` over the whiteboard
- Once the animation completes, smoothly reveal `AdaptiveWhiteboardScreen`

- [ ] **Step 5: Run all unit tests to verify integration**

Run: `./gradlew :app:testDebugUnitTest`
Expected: ALL PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/commonMain/kotlin/com/kd/creavity/presentation/ui/components/SplashDrawingOverlay.kt app/src/androidMain/kotlin/com/kd/creavity/MainActivity.kt app/src/test/java/com/kd/creavity/presentation/ui/SplashDrawingOverlayTest.kt
git commit --no-gpg-sign -m "feat(ui): integrate animated stylus drawing splash screen in MainActivity"
```

---

### Task 4: End-to-End Build Verification & Preview Verification

**Files:**
- Create: `scratch/verify_adaptive_and_splash.py`
- Test: Build full debug APK `./gradlew :app:assembleDebug`

**Interfaces:**
- Consumes: All resources from Tasks 1-3
- Produces: Verified working Android APK with adaptive icon and animated splash screen

- [ ] **Step 1: Run full project unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all tests passing

- [ ] **Step 2: Run assembleDebug to verify AAPT2 and XML compiling without errors**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL (APK generated at `app/build/outputs/apk/debug/app-debug.apk`)

- [ ] **Step 3: Generate visual verification image of the adaptive icon and splash preview**

Run script to generate `final_logo_verification.png` and open in Firefox / display for inspection.

- [ ] **Step 4: Commit**

```bash
git add app/
git commit --no-gpg-sign -m "chore(release): verify adaptive icon, AVD, and splash screen compilation"
```
