package com.ahd.notebk.data.local

import android.content.Context

/**
 * Resolves local storage to the currently authenticated account.
 * The existing database and preferences remain the guest profile, so existing
 * offline records are not silently copied into a newly registered account.
 */
object AccountDataScope {
    fun userId(context: Context): String? =
        context.applicationContext
            .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
            .getString("user_id", null)
            ?.takeIf { it.isNotBlank() }

    fun databaseName(context: Context): String {
        val id = userId(context) ?: return "add_paper.db"
        val safeId = id.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        return if (safeId.isBlank()) "add_paper.db" else "add_paper_account_$safeId.db"
    }

    /** User profile fields are private per account; guest fields keep legacy keys. */
    fun profilePreferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            userId(context)?.let { "notebk_profile_$it" } ?: "add_paper_user",
            Context.MODE_PRIVATE
        )

    fun profilePreferences(context: Context, userId: String) =
        context.applicationContext.getSharedPreferences("notebk_profile_$userId", Context.MODE_PRIVATE)

    fun accountKey(context: Context): String = userId(context) ?: "guest"
}
