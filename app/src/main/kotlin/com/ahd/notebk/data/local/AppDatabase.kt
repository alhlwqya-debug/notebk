package com.ahd.notebk.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ahd.notebk.data.local.dao.RecordDao
import com.ahd.notebk.data.local.entity.RecordEntity

@Database(entities = [RecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tailor_master_db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
