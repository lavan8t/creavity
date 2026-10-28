package com.kd.creavity.presentation.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppLogoResourceTest {
    @Test
    fun testAdaptiveIconFilesExist() {
        // In Gradle unit test runner, working directory is app/
        val resDir = File("src/main/res")
        val bgFile = File(resDir, "drawable/ic_launcher_background.xml")
        val fgFile = File(resDir, "drawable/ic_launcher_foreground.xml")
        val monoFile = File(resDir, "drawable/ic_launcher_monochrome.xml")
        val launcherXml = File(resDir, "mipmap-anydpi/ic_launcher.xml")

        assertTrue("Background drawable must exist: ${bgFile.absolutePath}", bgFile.exists())
        assertTrue("Foreground drawable must exist: ${fgFile.absolutePath}", fgFile.exists())
        assertTrue("Monochrome drawable must exist: ${monoFile.absolutePath}", monoFile.exists())
        assertTrue("Launcher XML must exist: ${launcherXml.absolutePath}", launcherXml.exists())

        val fgContent = fgFile.readText()
        assertTrue("Foreground must contain 'cr' pathData", fgContent.contains("pathData"))

        val bgContent = bgFile.readText()
        assertTrue("Background must contain vector drawable", bgContent.contains("<vector"))

        val launcherContent = launcherXml.readText()
        assertTrue("Launcher XML must declare monochrome icon", launcherContent.contains("monochrome"))
    }
}
