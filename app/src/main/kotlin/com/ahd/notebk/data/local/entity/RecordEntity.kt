package com.ahd.notebk.data.local.entity

import com.ahd.notebk.domain.model.TailorRecord

data class RecordEntity(
    val id: Int = 0,
    val dayName: String,
    val itemQuantity: Int,
    val credit: Double,
    val debit: Double,
    val balance: Double,
    val note: String,
    val pieceType: String = "ثابت كامل",
    val unitPrice: Double = 0.0,
    val personName: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain() = TailorRecord(
        id, dayName, itemQuantity, credit, debit, balance, note, pieceType, unitPrice, personName, timestamp
    )

    companion object {
        fun fromDomain(record: TailorRecord) = RecordEntity(
            id = record.id,
            dayName = record.dayName,
            itemQuantity = record.itemQuantity,
            credit = record.credit,
            debit = record.debit,
            balance = record.balance,
            note = record.note,
            pieceType = record.pieceType,
            unitPrice = record.unitPrice,
            personName = record.personName,
            timestamp = record.timestamp
        )
    }
}
