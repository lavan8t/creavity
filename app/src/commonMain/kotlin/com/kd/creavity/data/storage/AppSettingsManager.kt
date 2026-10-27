package com.kd.creavity.data.storage

import com.kd.creavity.domain.model.AppThemePreset
import com.kd.creavity.presentation.viewmodel.ThemeMode
import com.kd.creavity.util.getAppStorageDir
import com.kd.creavity.util.getDeviceName
import org.json.JSONObject
import java.io.File

class AppSettingsManager(
    private val settingsFile: File = File(getAppStorageDir(), "settings.json")
) {
    private var json: JSONObject = loadJson()

    private fun loadJson(): JSONObject {
        return try {
            if (settingsFile.exists()) JSONObject(settingsFile.readText()) else JSONObject()
        } catch (_: Exception) {
            JSONObject()
        }
    }

    private fun save() {
        try {
            settingsFile.writeText(json.toString())
        } catch (_: Exception) {}
    }

    val defaultDeviceName: String = getDeviceName()

    var displayName: String
        get() {
            val saved = json.optString("display_name", "")
            return if (saved.isBlank() || saved.endsWith(" Whiteboard") || saved == "Whiteboard User" || saved == "Whiteboard") {
                defaultDeviceName
            } else {
                saved
            }
        }
        set(value) {
            json.put("display_name", value)
            save()
        }

    var hapticsEnabled: Boolean
        get() = json.optBoolean("haptics_enabled", true)
        set(value) {
            json.put("haptics_enabled", value)
            save()
        }

    var defaultStrokeWidth: Float
        get() = json.optDouble("default_stroke_width", 6.0).toFloat()
        set(value) {
            json.put("default_stroke_width", value.toDouble())
            save()
        }

    var themePreset: AppThemePreset
        get() {
            val name = json.optString("theme_preset", AppThemePreset.DYNAMIC.name)
            return runCatching { AppThemePreset.valueOf(name) }.getOrDefault(AppThemePreset.DYNAMIC)
        }
        set(value) {
            json.put("theme_preset", value.name)
            save()
        }

    var themeMode: ThemeMode
        get() {
            val name = json.optString("theme_mode", ThemeMode.SYSTEM.name)
            return runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
        }
        set(value) {
            json.put("theme_mode", value.name)
            save()
        }

    fun addKnownPeer(peerName: String) {
        if (peerName.isBlank()) return
        val list = getKnownPeers().toMutableSet()
        list.add(peerName.trim())
        val arr = org.json.JSONArray()
        list.forEach { arr.put(it) }
        json.put("known_peers", arr)
        save()
    }

    fun isKnownPeer(peerName: String): Boolean {
        if (peerName.isBlank()) return false
        val list = getKnownPeers()
        val trimmed = peerName.trim()
        return list.any { it.equals(trimmed, ignoreCase = true) || trimmed.contains(it, ignoreCase = true) }
    }

    fun getKnownPeers(): Set<String> {
        val arr = json.optJSONArray("known_peers") ?: return emptySet()
        val set = mutableSetOf<String>()
        for (i in 0 until arr.length()) {
            val s = arr.optString(i)
            if (s.isNotBlank()) set.add(s)
        }
        return set
    }
}
