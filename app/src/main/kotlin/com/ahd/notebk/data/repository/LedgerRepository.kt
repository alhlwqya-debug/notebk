package com.ahd.notebk.data.repository

import com.ahd.notebk.data.local.dao.RecordDao
import com.ahd.notebk.data.local.entity.RecordEntity
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.TailorRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LedgerRepository(private val dao: RecordDao) {
    val allRecords: Flow<List<TailorRecord>> =
        dao.getAllRecords().map { rows -> rows.map { it.toDomain() } }

    suspend fun addRecord(
        dayName: String,
        quantity: Int,
        amount: Double,
        isCredit: Boolean,
        note: String,
        pieceType: String,
        unitPrice: Double,
        personName: String,
        timestamp: Long
    ) {
        require(dayName.isNotBlank()) { "dayName is required" }
        require(quantity >= 0) { "quantity must be non-negative" }
        require(amount >= 0.0) { "amount must be non-negative" }
        require(unitPrice >= 0.0) { "unitPrice must be non-negative" }

        dao.insertRecord(
            RecordEntity(
                dayName = dayName.trim(),
                itemQuantity = quantity,
                credit = if (isCredit) amount else 0.0,
                debit = if (isCredit) 0.0 else amount,
                balance = 0.0,
                note = note.trim(),
                pieceType = pieceType.ifBlank { "ثابت كامل" }.trim(),
                unitPrice = unitPrice,
                personName = LedgerEngine.normalizePersonName(personName),
                timestamp = timestamp
            )
        )
        recalculateBalances()
    }

    suspend fun updateRecord(
        record: TailorRecord,
        dayName: String,
        quantity: Int,
        amount: Double,
        isCredit: Boolean,
        unitPrice: Double,
        note: String,
        pieceType: String,
        personName: String,
        timestamp: Long
    ) {
        require(dayName.isNotBlank()) { "dayName is required" }
        require(quantity >= 0) { "quantity must be non-negative" }
        require(amount >= 0.0) { "amount must be non-negative" }
        require(unitPrice >= 0.0) { "unitPrice must be non-negative" }
        dao.updateRecord(
            RecordEntity.fromDomain(
                record.copy(
                    dayName = dayName.trim(),
                    itemQuantity = quantity,
                    credit = if (isCredit) amount else 0.0,
                    debit = if (isCredit) 0.0 else amount,
                    note = note.trim(),
                    pieceType = pieceType.ifBlank { "ثابت كامل" }.trim(),
                    unitPrice = unitPrice,
                    personName = LedgerEngine.normalizePersonName(personName),
                    timestamp = timestamp
                )
            )
        )
        recalculateBalances()
    }

    suspend fun deleteRecord(record: TailorRecord) {
        dao.deleteRecord(RecordEntity.fromDomain(record))
        recalculateBalances()
    }

    suspend fun restoreAll(records: List<TailorRecord>) {
        dao.clearAll()
        val recalculated = LedgerEngine.recalculateBalances(records)
        if (recalculated.isNotEmpty()) dao.insertAll(recalculated.map(RecordEntity::fromDomain))
    }

    suspend fun clearDatabase() = dao.clearAll()

    private suspend fun recalculateBalances() {
        val current = dao.getAllRecordsOnce().map { it.toDomain() }
        val recalculated = LedgerEngine.recalculateBalances(current)
        if (recalculated.isNotEmpty()) dao.updateAll(recalculated.map(RecordEntity::fromDomain))
    }
}
