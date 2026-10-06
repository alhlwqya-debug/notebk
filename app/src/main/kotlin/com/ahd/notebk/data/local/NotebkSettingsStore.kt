package com.ahd.notebk.data.local

import android.content.Context

/** Central preferences store. Existing keys are preserved for backward compatibility. */
class NotebkSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("notebk_settings", Context.MODE_PRIVATE)

    fun getString(key: String, default: String = "") = prefs.getString(key, default) ?: default
    fun getBoolean(key: String, default: Boolean = false) = prefs.getBoolean(key, default)
    fun getInt(key: String, default: Int = 0) = prefs.getInt(key, default)
    fun getFloat(key: String, default: Float = 1f) = prefs.getFloat(key, default)
    fun putString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    fun putBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
    fun putInt(key: String, value: Int) { prefs.edit().putInt(key, value).apply() }
    fun putFloat(key: String, value: Float) { prefs.edit().putFloat(key, value).apply() }

    fun resetGeneral() {
        prefs.edit().apply {
            putString("language", "ar")
            putString("direction", "rtl")
            putString("report_language", "ar")
            putString("currency_code", "YER")
            putString("currency_symbol", "ريال")
            putString("currency_position", "after")
            putString("thousands_separator", ",")
            putString("decimal_separator", ".")
            putInt("decimal_places", 0)
            putString("date_format", "yyyy/MM/dd")
            putString("time_format", "24h")
            putString("calendar_type", "gregorian")
            putString("timezone", "auto")
            putBoolean("show_date_tables", true)
            putString("number_format", "western")
            putString("theme_mode", "light")
            putFloat("font_scale", 1f)
            putString("font_family", "system")
            putString("density", "normal")
            putBoolean("animations_enabled", true)
            putBoolean("show_search", true)
            putBoolean("show_fab", true)
            putBoolean("show_bottom_nav", true)
            putBoolean("show_drawer", true)
            putBoolean("remember_page", true)
            putString("default_page", "home")
            putBoolean("notification_sound", true)
            putBoolean("notification_vibrate", true)
            putBoolean("backup_notification", true)
            putBoolean("auto_backup", false)
            putBoolean("backup_wifi_only", true)
            putBoolean("cloud_sync", false)
            putString("sync_frequency", "manual")
            putInt("backup_retention", 10)
            putBoolean("report_header", true)
            putBoolean("report_footer", true)
            putBoolean("report_logo", true)
            putBoolean("report_shop_data", true)
            putBoolean("report_page_numbers", true)
            putBoolean("report_chart", true)
            putBoolean("report_empty_months", false)
            putBoolean("report_compress", true)
            putString("paper_size", "A4")
            putString("paper_orientation", "portrait")
            putBoolean("autosave", true)
            putBoolean("confirm_delete", true)
            putBoolean("next_day_after_save", false)
            putBoolean("show_total_row", true)
            putInt("table_rows", 20)
            putBoolean("highlight_incomplete", true)
            putBoolean("lock_on_background", false)
            putBoolean("hide_amounts", false)
            putBoolean("prevent_screenshot", false)
            putBoolean("show_date_tables", true)
            apply()
        }
    }

    fun resetAllNonAccount() {
        prefs.edit().clear().apply()
    }
}
