package com.ahd.notebk.domain.model

data class LedgerSummary(
    val totalPieces: Int = 0,
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val netBalance: Double = 0.0,
    val averagePiecesPerDay: Double = 0.0
)
