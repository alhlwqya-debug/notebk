package com.ahd.notebk.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Copies the local guest ledger into a newly created account's empty database.
 *
 * The guest database is deliberately retained after a successful copy. This makes
 * migration recoverable and prevents a failed account transition from deleting work.
 */
object GuestAccountMigration {
    enum class Outcome {
        MIGRATED,
        NO_GUEST_DATABASE,
        NO_GUEST_RECORDS,
        ACCOUNT_ALREADY_HAS_DATA,
        FAILED
    }

    private val ledgerTables = listOf(
        "shops",
        "workers",
        "months",
        "pieces",
        "days",
        "entries",
        "individual_entries",
        "individual_entry_items"
    )

    suspend fun migrate(context: Context, userId: String): Outcome = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val guestFile = appContext.getDatabasePath("add_paper.db")
        if (!guestFile.isFile) return@withContext Outcome.NO_GUEST_DATABASE

        val helper = try {
            LedgerDatabase(appContext)
        } catch (_: Exception) {
            return@withContext Outcome.FAILED
        }

        try {
            val db = helper.writableDatabase
            val guestPath = guestFile.absolutePath
            db.execSQL("ATTACH DATABASE ? AS guest", arrayOf(guestPath))
            try {
                val accountHasData = ledgerTables.any { countRows(db, "main", it) > 0L }
                if (accountHasData) {
                    return@withContext Outcome.ACCOUNT_ALREADY_HAS_DATA
                }

                val guestHasData = ledgerTables.any { countRows(db, "guest", it) > 0L }
                if (!guestHasData) {
                    copyGuestProfile(appContext, userId)
                    return@withContext Outcome.NO_GUEST_RECORDS
                }

                db.beginTransaction()
                try {
                    // Preserve primary keys and foreign-key relationships. Copy parent
                    // tables before child tables; the whole operation rolls back on error.
                    ledgerTables.forEach { table ->
                        db.execSQL("INSERT INTO main.$table SELECT * FROM guest.$table")
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }

                // Preferences are copied only after the database transaction succeeds.
                copyGuestProfile(appContext, userId)
                Outcome.MIGRATED
            } finally {
                db.execSQL("DETACH DATABASE guest")
            }
        } catch (_: Exception) {
            Outcome.FAILED
        } finally {
            helper.close()
        }
    }

    private fun countRows(db: SQLiteDatabase, schema: String, table: String): Long =
        db.rawQuery("SELECT COUNT(*) FROM $schema.$table", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        }

    private fun copyGuestProfile(context: Context, userId: String) {
        val guest = context.getSharedPreferences("add_paper_user", Context.MODE_PRIVATE)
        val account = AccountDataScope.profilePreferences(context, userId)
        val keys = listOf(
            "user_phone", "user_shop", "user_image",
            "user_job_title", "user_bio", "user_phone_alt", "user_address",
            "user_whatsapp", "user_registration_number", "user_tax_number",
            "user_commercial_register", "business_address",
            "social_facebook", "social_instagram", "social_twitter",
            "social_tiktok", "social_telegram", "social_website"
        )
        val editor = account.edit()
        keys.forEach { key ->
            if (!account.contains(key) && guest.contains(key)) {
                when (val value = guest.all[key]) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                }
            }
        }
        editor.apply()
    }
}
