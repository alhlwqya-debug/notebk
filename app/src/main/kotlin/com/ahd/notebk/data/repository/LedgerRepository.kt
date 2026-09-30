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
        pageNumber: String = "",
        expenseType: String = "",
        recordType: String = "numeric",
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
                pageNumber = pageNumber.trim(),
                expenseType = expenseType.trim(),
                recordType = recordType.trim().ifBlank { "numeric" },
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
        pageNumber: String = record.pageNumber,
        expenseType: String = record.expenseType,
        recordType: String = record.recordType,
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
                    pageNumber = pageNumber.trim(),
                    expenseType = expenseType.trim(),
                    recordType = recordType.trim().ifBlank { record.recordType },
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
        val existing = dao.getAllRecordsOnce().map { it.toDomain() }
        val byId = existing.associateBy { it.id }.toMutableMap()
        records.forEach { imported ->
            val normalized = imported.copy(
                pageNumber = imported.pageNumber.trim(),
                expenseType = imported.expenseType.trim(),
                recordType = imported.recordType.ifBlank { "numeric" }
            )
            if (normalized.id > 0 && byId.containsKey(normalized.id)) {
                byId[normalized.id] = normalized
            } else {
                byId[-(byId.size + 1)] = normalized.copy(id = 0)
            }
        }
        val merged = LedgerEngine.recalculateBalances(byId.values.filter { it.id != 0 })
        dao.updateAll(merged.map(RecordEntity::fromDomain))
        val newRecords = byId.values.filter { it.id == 0 }
        if (newRecords.isNotEmpty()) dao.insertAll(newRecords.map(RecordEntity::fromDomain))
        recalculateBalances()
    }

    suspend fun clearDatabase() = dao.clearAll()

    private suspend fun recalculateBalances() {
        val current = dao.getAllRecordsOnce().map { it.toDomain() }
        val recalculated = LedgerEngine.recalculateBalances(current)
        if (recalculated.isNotEmpty()) dao.updateAll(recalculated.map(RecordEntity::fromDomain))
    }
}
