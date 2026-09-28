package com.ahd.notebk.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ahd.notebk.data.local.dao.RecordDao
import com.ahd.notebk.data.local.entity.RecordEntity

@Database(entities = [RecordEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tailor_records ADD COLUMN pieceType TEXT NOT NULL DEFAULT 'ثابت كامل'")
            }
        }

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tailor_master_db"
            ).addMigrations(MIGRATION_1_2)
             .build().also { instance = it }
        }
    }
}
