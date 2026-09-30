package com.ahd.notebk.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ahd.notebk.data.local.dao.RecordDao

/** Single application-local SQLite database owner. */
class AppDatabase private constructor(context: Context) {
    private val helper = TailorDatabaseHelper(context.applicationContext)
    private val writableDatabase: SQLiteDatabase = helper.writableDatabase
    private val dao = RecordDao(writableDatabase)

    fun recordDao(): RecordDao = dao

    fun close() {
        dao.close()
        helper.close()
    }

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: AppDatabase(context).also { instance = it }
        }

        fun closeInstance() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }

    private class TailorDatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onConfigure(db: SQLiteDatabase) {
            super.onConfigure(db)
            db.setForeignKeyConstraintsEnabled(true)
        }

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
                    unitPrice REAL NOT NULL DEFAULT 0,
                    personName TEXT NOT NULL DEFAULT '',
                    timestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )
            createIndexes(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2 && !hasColumn(db, "tailor_records", "pieceType")) {
                db.execSQL("ALTER TABLE tailor_records ADD COLUMN pieceType TEXT NOT NULL DEFAULT 'ثابت كامل'")
            }
            if (oldVersion < 3) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tailor_records_timestamp ON tailor_records(timestamp)")
            }
            if (oldVersion < 4 && !hasColumn(db, "tailor_records", "unitPrice")) {
                db.execSQL("ALTER TABLE tailor_records ADD COLUMN unitPrice REAL NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE tailor_records SET unitPrice = CASE WHEN itemQuantity > 0 THEN CASE WHEN credit > 0 THEN credit ELSE debit END / itemQuantity ELSE 0 END WHERE unitPrice = 0"
                )
            }
            if (oldVersion < 5 && !hasColumn(db, "tailor_records", "personName")) {
                db.execSQL("ALTER TABLE tailor_records ADD COLUMN personName TEXT NOT NULL DEFAULT ''")
                // Before phase 3 the note field was also used as the person's name
                // in the old individual-ledger screen. Preserve that data as a person
                // while keeping note intact.
                db.execSQL(
                    "UPDATE tailor_records SET personName = TRIM(note) WHERE TRIM(note) <> '' AND personName = ''"
                )
            }
            createIndexes(db)
        }

        private fun createIndexes(db: SQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tailor_records_timestamp ON tailor_records(timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tailor_records_person_name ON tailor_records(personName)")
        }

        private fun hasColumn(db: SQLiteDatabase, table: String, column: String): Boolean {
            db.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex >= 0 && cursor.getString(nameIndex) == column) return true
                }
            }
            return false
        }

        companion object {
            private const val DATABASE_NAME = "tailor_master_db"
            private const val DATABASE_VERSION = 5
        }
    }
}
