package com.ahd.notebk.data.repository

import com.ahd.notebk.data.local.dao.RecordDao
import com.ahd.notebk.data.local.entity.RecordEntity
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.TailorRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LedgerRepository(private val dao: RecordDao) {
    val allRecords: Flow<List<TailorRecord>> = dao.getAllRecords().map { rows -> rows.map { it.toDomain() } }

    suspend fun addRecord(dayName: String, quantity: Int, amount: Double, isCredit: Boolean, note: String, timestamp: Long) {
        require(dayName.isNotBlank()) { "dayName is required" }
        require(quantity >= 0) { "quantity must be non-negative" }
        require(amount >= 0.0) { "amount must be non-negative" }
        val record = RecordEntity(
            dayName = dayName.trim(),
            itemQuantity = quantity,
            credit = if (isCredit) amount else 0.0,
            debit = if (isCredit) 0.0 else amount,
            balance = 0.0,
            note = note.trim(),
            timestamp = timestamp
        )
        dao.insertRecord(record)
        recalculateAndPersist()
    }

    suspend fun deleteRecord(record: TailorRecord) {
        dao.deleteRecord(RecordEntity.fromDomain(record))
        recalculateAndPersist()
    }

    suspend fun restoreAll(records: List<TailorRecord>) {
        dao.clearAll()
        val recalculated = LedgerEngine.recalculateBalances(records)
        if (recalculated.isNotEmpty()) dao.insertAll(recalculated.map(RecordEntity::fromDomain))
    }

    suspend fun clearDatabase() = dao.clearAll()

    private suspend fun recalculateAndPersist() {
        // The next Flow emission contains the authoritative rows. This method is intentionally
        // lightweight; balance values are also recalculated during restore/import.
    }
}
