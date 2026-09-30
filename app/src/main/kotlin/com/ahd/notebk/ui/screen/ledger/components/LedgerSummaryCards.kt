package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.ui.components.StatCard
import com.ahd.notebk.ui.theme.ErrorRed
import com.ahd.notebk.ui.theme.PrimaryPurple
import com.ahd.notebk.ui.theme.SecondaryGreen

@Composable
fun LedgerSummaryCards(summary: LedgerSummary, settings: AppSettings) {
    Row(Modifier.fillMaxWidth().padding(8.dp), Arrangement.spacedBy(8.dp)) {
        StatCard("القطع", summary.totalPieces.toString(), Modifier.weight(1f))
        StatCard("له", "%.0f %s".format(summary.totalCredit, settings.currencySymbol), Modifier.weight(1f), SecondaryGreen)
        StatCard("عليه", "%.0f %s".format(summary.totalDebit, settings.currencySymbol), Modifier.weight(1f), ErrorRed)
        StatCard("الصافي", "%.0f %s".format(summary.netBalance, settings.currencySymbol), Modifier.weight(1f), PrimaryPurple)
    }
}
