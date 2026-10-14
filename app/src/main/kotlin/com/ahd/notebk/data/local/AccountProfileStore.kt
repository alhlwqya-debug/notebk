package com.ahd.notebk.data.local

import android.content.Context

/** Stores editable profile/business identity fields separately for each account. */
class AccountProfileStore(context: Context) {
    private val prefs = AccountDataScope.profilePreferences(context)

    init {
        // Preserve existing guest business-profile fields that older versions stored
        // in the general settings file. Never copy those values into a signed-in account.
        if (AccountDataScope.userId(context) == null) {
            val legacy = context.applicationContext.getSharedPreferences("notebk_settings", Context.MODE_PRIVATE)
            val stringKeys = listOf(
                "user_job_title", "user_bio", "user_phone_alt", "user_address",
                "user_whatsapp", "user_registration_number", "user_tax_number",
                "user_commercial_register", "business_address", "social_facebook",
                "social_instagram", "social_twitter", "social_tiktok",
                "social_telegram", "social_website"
            )
            val editor = prefs.edit()
            stringKeys.forEach { key ->
                if (!prefs.contains(key) && legacy.contains(key)) {
                    editor.putString(key, legacy.getString(key, "").orEmpty())
                }
            }
            if (!prefs.contains("whatsapp_enabled") && legacy.contains("whatsapp_enabled")) {
                editor.putBoolean("whatsapp_enabled", legacy.getBoolean("whatsapp_enabled", false))
            }
            editor.apply()
        }
    }

    fun getString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
