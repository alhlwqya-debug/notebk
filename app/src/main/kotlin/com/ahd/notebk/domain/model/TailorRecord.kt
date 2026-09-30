package com.ahd.notebk.domain.model

data class TailorRecord(
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
    val pageNumber: String = "",
    val expenseType: String = "",
    val recordType: String = "numeric",
    val timestamp: Long = System.currentTimeMillis()
)