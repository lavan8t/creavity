package com.kd.brevity.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.kd.brevity.domain.model.AppThemePreset

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("brevity_settings", Context.MODE_PRIVATE)

    companion object {
        fun getDeviceName(context: Context): String {
            var name: String? = null
            try {
                name = android.provider.Settings.Global.getString(
                    context.contentResolver,
                    android.provider.Settings.Global.DEVICE_NAME
                )
            } catch (_: Exception) {}

            if (name.isNullOrBlank()) {
                try {
                    name = android.provider.Settings.Secure.getString(
                        context.contentResolver,
                        "bluetooth_name"
                    )
                } catch (_: Exception) {}
            }

            if (name.isNullOrBlank()) {
                val manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() }
                val model = Build.MODEL.orEmpty()
                name = if (model.startsWith(manufacturer, ignoreCase = true)) {
                    model
                } else if (manufacturer.isNotBlank() && model.isNotBlank()) {
                    "$manufacturer $model"
                } else {
                    model.ifBlank { manufacturer.ifBlank { "Android Device" } }
                }
            }
            return name.trim()
        }
    }

    val defaultDeviceName: String = getDeviceName(context)

    var displayName: String
        get() {
            val saved = prefs.getString("display_name", null)
            return if (saved.isNullOrBlank() || saved.endsWith(" Whiteboard") || saved == "Whiteboard User" || saved == "Whiteboard") {
                defaultDeviceName
            } else {
                saved
            }
        }
        set(value) = prefs.edit().putString("display_name", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var defaultStrokeWidth: Float
        get() = prefs.getFloat("default_stroke_width", 6.0f)
        set(value) = prefs.edit().putFloat("default_stroke_width", value).apply()

    var themePreset: AppThemePreset
        get() {
            val name = prefs.getString("theme_preset", AppThemePreset.STUDIO_AMBER.name) ?: AppThemePreset.STUDIO_AMBER.name
            return try {
                AppThemePreset.valueOf(name)
            } catch (_: Exception) {
                AppThemePreset.STUDIO_AMBER
            }
        }
        set(value) = prefs.edit().putString("theme_preset", value.name).apply()

    var themeMode: com.kd.brevity.presentation.viewmodel.ThemeMode
        get() {
            val name = prefs.getString("theme_mode", com.kd.brevity.presentation.viewmodel.ThemeMode.SYSTEM.name) ?: com.kd.brevity.presentation.viewmodel.ThemeMode.SYSTEM.name
            return try {
                com.kd.brevity.presentation.viewmodel.ThemeMode.valueOf(name)
            } catch (_: Exception) {
                com.kd.brevity.presentation.viewmodel.ThemeMode.SYSTEM
            }
        }
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()
}

