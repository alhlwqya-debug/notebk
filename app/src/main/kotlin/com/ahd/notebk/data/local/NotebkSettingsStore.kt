package com.ahd.notebk.data.local

import android.content.Context

/**
 * Central preferences store.
 *
 * Settings are persisted under the existing preference file/key names for
 * backward compatibility. Resetting general settings deliberately does not
 * touch cloud-sync, backup, security, notification, report, or ledger options.
 */
class NotebkSettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("notebk_settings", Context.MODE_PRIVATE)

    fun getString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun getInt(key: String, default: Int = 0): Int =
        prefs.getInt(key, default)

    fun getFloat(key: String, default: Float = 1f): Float =
        prefs.getFloat(key, default)

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun putFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    /**
     * Restores only general appearance, locale, formatting and navigation preferences.
     *
     * @return true when Android successfully persisted the default values.
     */
    fun resetGeneral(): Boolean {
        val editor = prefs.edit()
            .putString("language", "ar")
            .putString("direction", "rtl")
            .putString("report_language", "ar")
            .putString("currency_code", "YER")
            .putString("currency_symbol", "ريال")
            .putString("currency_position", "after")
            .putString("thousands_separator", ",")
            .putString("decimal_separator", ".")
            .putInt("decimal_places", 0)
            .putString("date_format", "yyyy/MM/dd")
            .putString("time_format", "24h")
            .putString("calendar_type", "gregorian")
            .putString("timezone", "auto")
            .putBoolean("show_date_tables", true)
            .putString("number_format", "western")
            .putString("theme_mode", "light")
            .putFloat("font_scale", 1f)
            .putString("font_family", "system")
            .putString("density", "normal")
            .putBoolean("animations_enabled", true)
            .putBoolean("show_search", true)
            .putBoolean("show_fab", true)
            .putBoolean("show_bottom_nav", true)
            .putBoolean("show_drawer", true)
            .putBoolean("remember_page", true)
            .putString("default_page", "home")

        return editor.commit()
    }

    /**
     * Clears application preferences only. This does not delete the ledger database.
     * Call only from an explicitly confirmed full-settings reset flow.
     */
    fun resetAllNonAccount(): Boolean = prefs.edit().clear().commit()
}
