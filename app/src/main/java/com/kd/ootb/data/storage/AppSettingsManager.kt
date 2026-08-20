package com.kd.ootb.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.kd.ootb.domain.model.AppThemePreset

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ootb_settings", Context.MODE_PRIVATE)

    var displayName: String
        get() = prefs.getString("display_name", "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} Whiteboard") ?: "Whiteboard"
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
}
