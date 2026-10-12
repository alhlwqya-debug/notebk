package com.ahd.notebk.data.local

import android.content.Context

/** Stores editable profile/business identity fields separately for each account. */
class AccountProfileStore(context: Context) {
    private val prefs = AccountDataScope.profilePreferences(context)

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
