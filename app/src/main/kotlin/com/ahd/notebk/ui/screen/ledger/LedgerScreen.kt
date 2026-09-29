package com.ahd.notebk.ui.screen.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.ui.components.GridCell
import com.ahd.notebk.ui.components.StatCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ledgerDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
private val arabicDayFormat = SimpleDateFormat("EEEE", Locale("ar", "YE"))

@Composable
fun LedgerScreen(viewModel: LedgerViewModel, onOpenSettings: () -> Unit, onOpenReport: () -> Unit) {
    val settings by viewModel.settingsState.collectAsState()
    val records by viewModel.recordsState.collectAsState()
    val summary by viewModel.summaryState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    var day by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf(true) }
    var pieceType by remember { mutableStateOf("ثابت كامل") }
    val pieceTypes = listOf("ثابت كامل", "ثابت نص", "زوج كامل", "زوج نص", "فرده كامل", "فرده نص", "فرشه")

    LaunchedEffect(settings.autoFillToday) {
        if (settings.autoFillToday && day.isBlank()) day = arabicDayFormat.format(Date())
    }

    val visibleRecords = if (query.isBlank()) records else records.filter {
        it.dayName.contains(query, true) ||
            it.note.contains(query, true) ||
            it.itemQuantity.toString().contains(query) ||
            it.pieceType.contains(query, true) ||
            ledgerDateFormat.format(Date(it.timestamp)).contains(query)
    }

    Column(Modifier.fillMaxSize()) {
        Surface(color = Color(0xFF0F172A), contentColor = Color.White) {
            Row(Modifier.fillMaxWidth().padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(settings.shopName, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("دفتر الخياط الرقمي • ${summary.totalPieces} قطعة", fontSize = 11.sp, color = Color.LightGray)
                }
                Row {
                    Button(onClick = onOpenReport, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) { Text("التقرير") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, "الإعدادات") }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatCard("إجمالي القطع", summary.totalPieces.toString(), Modifier.weight(1f))
            StatCard("له", "%.0f %s".format(summary.totalCredit, settings.currencySymbol), Modifier.weight(1f), Color(0xFF15803D))
            StatCard("عليه", "%.0f %s".format(summary.totalDebit, settings.currencySymbol), Modifier.weight(1f), Color(0xFFB91C1C))
            StatCard("الصافي", "%.0f %s".format(summary.netBalance, settings.currencySymbol), Modifier.weight(1f))
        }

        OutlinedTextField(query, viewModel::onSearchQueryChange, label = { Text("بحث في الدفتر أو التاريخ") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth().background(Color(0xFFE2E8F0)).border(0.5.dp, Color(0xFF94A3B8))) {
            GridCell("التاريخ", 1.2f, true)
            GridCell("اليوم", 1f, true)
            GridCell("القطع", .8f, true, Color(0xFF0284C7))
            if (settings.showDebitCredit) {
                GridCell("له (+)", 1f, true, Color(0xFF15803D))
                GridCell("عليه (-)", 1f, true, Color(0xFFB91C1C))
                GridCell("الرصيد", 1f, true)
            }
            GridCell("الملاحظات", 1.3f, true)
        }

        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(visibleRecords, key = { it.id }) { item ->
                Row(Modifier.fillMaxWidth().background(if (item.id % 2 == 0) Color.White else Color(0xFFF8FAFC))) {
                    GridCell(ledgerDateFormat.format(Date(item.timestamp)), 1.2f, fontWeight = FontWeight.SemiBold)
                    GridCell(item.dayName, 1f, fontWeight = FontWeight.SemiBold)
                    GridCell(item.itemQuantity.toString(), .8f, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    if (settings.showDebitCredit) {
                        GridCell(if (item.credit > 0) "%.0f".format(item.credit) else "-", 1f, color = Color(0xFF15803D))
                        GridCell(if (item.debit > 0) "%.0f".format(item.debit) else "-", 1f, color = Color(0xFFB91C1C))
                        GridCell("%.0f".format(item.balance), 1f, fontWeight = FontWeight.Bold)
                    }
                    GridCell(item.note.ifBlank { item.pieceType }, 1.3f)
                }
            }
        }

        Surface(shadowElevation = 10.dp, color = Color(0xFFF1F5F9), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(day, { day = it }, Modifier.weight(1f), singleLine = true, label = { Text("اليوم", fontSize = 10.sp) })
                    OutlinedTextField(quantity, { value ->
                        quantity = value
                        val q = value.toIntOrNull() ?: 0
                        if (q > 0 && amount.isBlank()) amount = (q * settings.defaultPiecePrice).toString()
                    }, Modifier.weight(.8f), singleLine = true, label = { Text("القطع", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(amount, { amount = it }, Modifier.weight(1f), singleLine = true, label = { Text("المبلغ", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    FilterChip(selected = true, onClick = { pieceType = pieceTypes[(pieceTypes.indexOf(pieceType) + 1) % pieceTypes.size] }, label = { Text(pieceType, fontSize = 10.sp) })
                    OutlinedTextField(note, { note = it }, Modifier.weight(1f), singleLine = true, label = { Text("ملاحظة", fontSize = 10.sp) })
                    FilterChip(selected = credit, onClick = { credit = !credit }, label = { Text(if (credit) "له (+)" else "عليه (-)") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if (credit) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)))
                    Button(onClick = {
                        viewModel.addNewRecord(day, quantity.toIntOrNull() ?: 0, amount.toDoubleOrNull() ?: 0.0, credit, note, pieceType)
                        quantity = ""; amount = ""; note = ""
                    }, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) { Text("حفظ") }
                }
            }
        }
    }
}
