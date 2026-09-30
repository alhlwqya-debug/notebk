package com.ahd.notebk.ui.screen.ledger

import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.domain.model.TailorRecord
import java.util.Date

/** UI-only state for the ledger screen. Business calculations stay outside Compose. */
data class LedgerUiState(
    val settings: com.ahd.notebk.data.local.AppSettings,
    val records: List<TailorRecord>,
    val summary: LedgerSummary,
    val query: String,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val showDatePicker: Boolean = false,
    val quantity: String = "",
    val amount: String = "",
    val note: String = "",
    val personName: String = "",
    val credit: Boolean = true,
    val pieceType: String = "ثابت كامل",
    val unitPrice: String = settings.defaultPiecePrice.toString()
) {
    val selectedDate: Date get() = Date(selectedDateMillis)
}
