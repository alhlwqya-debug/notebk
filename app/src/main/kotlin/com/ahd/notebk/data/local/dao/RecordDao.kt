package com.ahd.notebk.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.ahd.notebk.data.local.entity.RecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SQLite-backed replacement for the former Room DAO.
 *
 * The public operations intentionally match the old DAO so the repository and UI
 * keep the same behavior while avoiding CodeAssist's incompatible embedded Room/KSP
 * processor.
 */
class RecordDao internal constructor(private val database: SQLiteDatabase) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val records = MutableStateFlow<List<RecordEntity>>(emptyList())

    init {
        scope.launch { refresh() }
    }

    fun getAllRecords(): Flow<List<RecordEntity>> = records

    suspend fun getAllRecordsOnce(): List<RecordEntity> = withContext(Dispatchers.IO) {
        queryAll()
    }

    suspend fun insertRecord(record: RecordEntity): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("dayName", record.dayName)
            put("itemQuantity", record.itemQuantity)
            put("credit", record.credit)
            put("debit", record.debit)
            put("balance", record.balance)
            put("note", record.note)
            put("pieceType", record.pieceType)
            put("timestamp", record.timestamp)
        }
        val id = database.insertWithOnConflict(
            TABLE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        refresh()
        id
    }

    suspend fun insertAll(records: List<RecordEntity>) = withContext(Dispatchers.IO) {
        database.beginTransaction()
        try {
            records.forEach { record ->
                val values = ContentValues().apply {
                    put("id", record.id)
                    put("dayName", record.dayName)
                    put("itemQuantity", record.itemQuantity)
                    put("credit", record.credit)
                    put("debit", record.debit)
                    put("balance", record.balance)
                    put("note", record.note)
                    put("pieceType", record.pieceType)
                    put("timestamp", record.timestamp)
                }
                database.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        refresh()
    }

    suspend fun updateAll(records: List<RecordEntity>) = withContext(Dispatchers.IO) {
        database.beginTransaction()
        try {
            records.forEach { record ->
                val values = ContentValues().apply {
                    put("dayName", record.dayName)
                    put("itemQuantity", record.itemQuantity)
                    put("credit", record.credit)
                    put("debit", record.debit)
                    put("balance", record.balance)
                    put("note", record.note)
                    put("pieceType", record.pieceType)
                    put("timestamp", record.timestamp)
                }
                database.update(TABLE, values, "id = ?", arrayOf(record.id.toString()))
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        refresh()
    }

    suspend fun deleteRecord(record: RecordEntity) = withContext(Dispatchers.IO) {
        database.delete(TABLE, "id = ?", arrayOf(record.id.toString()))
        refresh()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        database.delete(TABLE, null, null)
        refresh()
    }

    private fun queryAll(): List<RecordEntity> {
        val result = ArrayList<RecordEntity>()
        database.query(
            TABLE,
            COLUMNS,
            null,
            null,
            null,
            null,
            "timestamp ASC, id ASC"
        ).use { cursor ->
            val id = cursor.getColumnIndexOrThrow("id")
            val dayName = cursor.getColumnIndexOrThrow("dayName")
            val quantity = cursor.getColumnIndexOrThrow("itemQuantity")
            val credit = cursor.getColumnIndexOrThrow("credit")
            val debit = cursor.getColumnIndexOrThrow("debit")
            val balance = cursor.getColumnIndexOrThrow("balance")
            val note = cursor.getColumnIndexOrThrow("note")
            val pieceType = cursor.getColumnIndexOrThrow("pieceType")
            val timestamp = cursor.getColumnIndexOrThrow("timestamp")
            while (cursor.moveToNext()) {
                result += RecordEntity(
                    id = cursor.getInt(id),
                    dayName = cursor.getString(dayName),
                    itemQuantity = cursor.getInt(quantity),
                    credit = cursor.getDouble(credit),
                    debit = cursor.getDouble(debit),
                    balance = cursor.getDouble(balance),
                    note = cursor.getString(note),
                    pieceType = cursor.getString(pieceType),
                    timestamp = cursor.getLong(timestamp)
                )
            }
        }
        return result
    }

    private suspend fun refresh() {
        records.value = queryAll()
    }

    companion object {
        private const val TABLE = "tailor_records"
        private val COLUMNS = arrayOf(
            "id", "dayName", "itemQuantity", "credit", "debit", "balance", "note", "pieceType", "timestamp"
        )
    }
}
