package com.ahd.notebk.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ahd.notebk.data.local.dao.RecordDao

/**
 * Local database facade backed directly by SQLite.
 *
 * This replaces RoomDatabase because CodeAssist's embedded KSP processor crashes
 * while resolving Room's ByteArrayWrapper type. The schema and database filename
 * remain compatible with the previous Room database.
 */
class AppDatabase private constructor(context: Context) {
    private val helper = TailorDatabaseHelper(context.applicationContext)
    private val writableDatabase: SQLiteDatabase = helper.writableDatabase
    private val dao = RecordDao(writableDatabase)

    fun recordDao(): RecordDao = dao

    fun close() {
        helper.close()
    }

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: AppDatabase(context).also { instance = it }
        }
    }

    private class TailorDatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS tailor_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    dayName TEXT NOT NULL,
                    itemQuantity INTEGER NOT NULL,
                    credit REAL NOT NULL,
                    debit REAL NOT NULL,
                    balance REAL NOT NULL,
                    note TEXT NOT NULL,
                    pieceType TEXT NOT NULL DEFAULT 'ثابت كامل',
                    timestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                db.execSQL(
                    "ALTER TABLE tailor_records ADD COLUMN pieceType TEXT NOT NULL DEFAULT 'ثابت كامل'"
                )
            }
        }

        companion object {
            private const val DATABASE_NAME = "tailor_master_db"
            private const val DATABASE_VERSION = 2
        }
    }
}
