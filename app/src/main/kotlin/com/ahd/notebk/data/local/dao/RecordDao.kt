package com.ahd.notebk.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.ahd.notebk.data.local.entity.RecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordDao internal constructor(private val database: SQLiteDatabase) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val records = MutableStateFlow<List<RecordEntity>>(emptyList())

    init { scope.launch { refresh() } }

    fun getAllRecords(): Flow<List<RecordEntity>> = records

    fun close() { scope.cancel() }

    suspend fun getAllRecordsOnce(): List<RecordEntity> = withContext(Dispatchers.IO) { queryAll() }

    suspend fun insertRecord(record: RecordEntity): Long = withContext(Dispatchers.IO) {
        val id = database.insertOrThrow(TABLE, null, valuesFor(record, includeId = false))
        refresh()
        id
    }

    suspend fun insertAll(records: List<RecordEntity>) = withContext(Dispatchers.IO) {
        database.beginTransaction()
        try {
            records.forEach { record ->
                database.insertWithOnConflict(
                    TABLE, null, valuesFor(record, includeId = true), SQLiteDatabase.CONFLICT_REPLACE
                )
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
                database.update(TABLE, valuesFor(record, includeId = false), "id = ?", arrayOf(record.id.toString()))
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        refresh()
    }

    suspend fun updateRecord(record: RecordEntity) = withContext(Dispatchers.IO) {
        database.update(TABLE, valuesFor(record, includeId = false), "id = ?", arrayOf(record.id.toString()))
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

    private fun valuesFor(record: RecordEntity, includeId: Boolean): ContentValues = ContentValues().apply {
        if (includeId && record.id > 0) put("id", record.id)
        put("dayName", record.dayName)
        put("itemQuantity", record.itemQuantity)
        put("credit", record.credit)
        put("debit", record.debit)
        put("balance", record.balance)
        put("note", record.note)
        put("pieceType", record.pieceType)
        put("unitPrice", record.unitPrice)
        put("personName", record.personName)
        put("timestamp", record.timestamp)
    }

    private fun queryAll(): List<RecordEntity> {
        val result = ArrayList<RecordEntity>()
        database.query(
            TABLE, COLUMNS, null, null, null, null, "timestamp ASC, id ASC"
        ).use { cursor ->
            val id = cursor.getColumnIndexOrThrow("id")
            val dayName = cursor.getColumnIndexOrThrow("dayName")
            val quantity = cursor.getColumnIndexOrThrow("itemQuantity")
            val credit = cursor.getColumnIndexOrThrow("credit")
            val debit = cursor.getColumnIndexOrThrow("debit")
            val balance = cursor.getColumnIndexOrThrow("balance")
            val note = cursor.getColumnIndexOrThrow("note")
            val pieceType = cursor.getColumnIndexOrThrow("pieceType")
            val unitPrice = cursor.getColumnIndexOrThrow("unitPrice")
            val personName = cursor.getColumnIndexOrThrow("personName")
            val timestamp = cursor.getColumnIndexOrThrow("timestamp")
            while (cursor.moveToNext()) {
                result += RecordEntity(
                    id = cursor.getInt(id), dayName = cursor.getString(dayName),
                    itemQuantity = cursor.getInt(quantity), credit = cursor.getDouble(credit),
                    debit = cursor.getDouble(debit), balance = cursor.getDouble(balance),
                    note = cursor.getString(note), pieceType = cursor.getString(pieceType),
                    unitPrice = cursor.getDouble(unitPrice), personName = cursor.getString(personName),
                    timestamp = cursor.getLong(timestamp)
                )
            }
        }
        return result
    }

    private suspend fun refresh() { records.value = queryAll() }

    companion object {
        private const val TABLE = "tailor_records"
        private val COLUMNS = arrayOf(
            "id", "dayName", "itemQuantity", "credit", "debit", "balance",
            "note", "pieceType", "unitPrice", "personName", "timestamp"
        )
    }
}
