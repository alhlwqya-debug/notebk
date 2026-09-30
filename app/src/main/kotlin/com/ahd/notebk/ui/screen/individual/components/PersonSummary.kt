package com.ahd.notebk.ui.screen.individual.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.ui.components.StatCard
import com.ahd.notebk.ui.theme.ErrorRed
import com.ahd.notebk.ui.theme.PrimaryPurple
import com.ahd.notebk.ui.theme.SecondaryGreen

@Composable
fun PersonSummary(peopleCount: Int, summary: LedgerSummary, currency: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        Arrangement.spacedBy(8.dp)
    ) {
        StatCard("الزبائن", peopleCount.toString(), Modifier.weight(1f))
        StatCard("القطع", summary.totalPieces.toString(), Modifier.weight(1f), PrimaryPurple)
        StatCard("المصروف", "%.0f %s".format(summary.totalDebit, currency), Modifier.weight(1f), ErrorRed)
        StatCard("الصافي", "%.0f %s".format(summary.netBalance, currency), Modifier.weight(1f), SecondaryGreen)
    }
}
