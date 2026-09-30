package com.ahd.notebk.ui.screen.individual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.TailorRecord
import com.ahd.notebk.ui.screen.individual.components.*
import com.ahd.notebk.ui.theme.LightBackground
import com.ahd.notebk.ui.theme.PrimaryPurple
import com.ahd.notebk.ui.components.BrandTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualLedger(records: List<TailorRecord>, onBack: () -> Unit) {
    val people = remember(records) { LedgerEngine.personNames(records) }
    var selectedPerson by remember { mutableStateOf<String?>(null) }
    val availableSelection = selectedPerson?.takeIf { it == "__UNASSIGNED__" || it in people }
    val filtered = remember(records, availableSelection) { LedgerEngine.recordsForPerson(records, availableSelection) }
    val calculated = remember(filtered) { LedgerEngine.recalculateBalances(filtered) }
    val summary = remember(calculated) { LedgerEngine.calculateSummary(calculated) }
    val hasUnassigned = remember(records) { records.any { LedgerEngine.normalizePersonName(it.personName).isBlank() } }
    val peopleWithRecords = people.size + if (hasUnassigned) 1 else 0

    Scaffold(topBar = {
        BrandTopBar(
            title = if (availableSelection == null) "السجل الفردي" else if (availableSelection == "__UNASSIGNED__") "غير محدد" else availableSelection,
            onBack = onBack
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().background(LightBackground).padding(padding)) {
            if (peopleWithRecords == 0) EmptyIndividualState()
            else {
                PersonSelector(people, availableSelection, hasUnassigned) { selectedPerson = it }
                PersonSummary(peopleWithRecords, summary)
                Spacer(Modifier.height(8.dp))
                PersonRecordsTable(calculated)
            }
        }
    }
}

@Composable
private fun EmptyIndividualState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Person, null, tint = PrimaryPurple)
        Spacer(Modifier.height(12.dp))
        Text("لا توجد سجلات بعد", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("اكتب اسم الشخص عند إضافة السجل، وسيظهر تلقائيًا هنا كسجل مستقل.", color = Color.Gray)
    }
}
