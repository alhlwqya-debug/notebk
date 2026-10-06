package com.ahd.notebk.domain.model

import java.io.Serializable

enum class RegistrationMode { NUMERIC, INDIVIDUAL }
data class ShopRecord(
    val id: Long,
    val name: String,
    val defaultWorkerId: Long?,
    val registrationMode: RegistrationMode,
    val registrationNumber: String
)

data class WorkerRecord(
    val id: Long,
    val shopId: Long,
    val name: String
)

data class MonthRecord(
    val id: Long,
    val shopId: Long,
    val year: Int,
    val month: Int,
    val name: String,
    val workerName: String,
    val startDate: String,
    val deductExpense: Boolean
)

data class PieceRecord(
    val id: Long,
    val shopId: Long,
    val name: String,
    val price: Int
)

data class IndividualSearchResult(
    val entryId: Long,
    val customerName: String,
    val pageNumber: String,
    val monthId: Long,
    val monthName: String,
    val date: String
)

data class IndividualEntryRecord(
    val id: Long,
    val dayId: Long,
    val customerName: String,
    val pageNumber: String,
    val quantities: Map<Long, Int>,
    val unitPrices: Map<Long, Int>
)

data class DayRecord(
    val id: Long,
    val monthId: Long,
    val date: String,
    val dayName: String,
    val expense: Int,
    val expenseNote: String,
    val quantities: Map<Long, Int>,
    val unitPrices: Map<Long, Int>
)

data class MonthBundle(
    val month: MonthRecord,
    val pieces: List<PieceRecord>,
    val days: List<DayRecord>
)

