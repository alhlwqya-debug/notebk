package com.ahd.notebk.ui.screen.individual.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.ui.components.StatCard
import com.ahd.notebk.ui.theme.*

@Composable
fun PersonSummary(peopleCount: Int, summary: LedgerSummary) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), Arrangement.spacedBy(8.dp)) {
        StatCard("الأشخاص", peopleCount.toString(), Modifier.weight(1f))
        StatCard("القطع", summary.totalPieces.toString(), Modifier.weight(1f))
        StatCard("له", "%.0f".format(summary.totalCredit), Modifier.weight(1f), SecondaryGreen)
        StatCard("عليه", "%.0f".format(summary.totalDebit), Modifier.weight(1f), ErrorRed)
        StatCard("الرصيد", "%.0f".format(summary.netBalance), Modifier.weight(1f), PrimaryPurple)
    }
}
