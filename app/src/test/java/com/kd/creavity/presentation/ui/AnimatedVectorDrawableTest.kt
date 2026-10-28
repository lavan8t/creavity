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

        assertTrue("AVD file must exist: ${avdFile.absolutePath}", avdFile.exists())
        assertTrue("Base VD file must exist: ${vdFile.absolutePath}", vdFile.exists())
        assertTrue("Stroke animator must exist: ${strokeAnim.exists()}", strokeAnim.exists())

        val avdContent = avdFile.readText()
        assertTrue("AVD must use animated-vector root", avdContent.contains("<animated-vector"))
        assertTrue("AVD must bind target to stroke animator", avdContent.contains("anim_logo_stroke_draw"))
        assertTrue("AVD must target stroke_path", avdContent.contains("stroke_path"))

        val vdContent = vdFile.readText()
        assertTrue("Base VD must contain stroke_path", vdContent.contains("stroke_path"))
        assertTrue("Base VD must contain stylus_group", vdContent.contains("stylus_group"))
    }
}
