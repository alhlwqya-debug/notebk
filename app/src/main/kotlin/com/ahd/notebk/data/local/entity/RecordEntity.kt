package com.ahd.notebk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ahd.notebk.domain.model.TailorRecord

@Entity(tableName = "tailor_records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayName: String,
    val itemQuantity: Int,
    val credit: Double,
    val debit: Double,
    val balance: Double,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain() = TailorRecord(id, dayName, itemQuantity, credit, debit, balance, note, timestamp)

    companion object {
        fun fromDomain(record: TailorRecord) = RecordEntity(
            id = record.id,
            dayName = record.dayName,
            itemQuantity = record.itemQuantity,
            credit = record.credit,
            debit = record.debit,
            balance = record.balance,
            note = record.note,
            timestamp = record.timestamp
        )
    }
}
