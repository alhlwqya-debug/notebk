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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.ahd.notebk.ui.components.ZoomableContainer
import com.ahd.notebk.ui.theme.BorderDivider
import com.ahd.notebk.ui.theme.ErrorRed
import com.ahd.notebk.ui.theme.PrimaryPurple
import com.ahd.notebk.ui.theme.SecondaryGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ledgerDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
private val arabicDayFormat = SimpleDateFormat("EEEE", Locale("ar", "YE"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: LedgerViewModel, onOpenSettings: () -> Unit, onOpenReport: () -> Unit) {
    val settings by viewModel.settingsState.collectAsState()
    val records by viewModel.recordsState.collectAsState()
    val summary by viewModel.summaryState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf(true) }
    var pieceType by remember { mutableStateOf("ثابت كامل") }
    val pieceTypes = listOf("ثابت كامل", "ثابت نص", "زوج كامل", "زوج نص", "فرده كامل", "فرده نص", "فرشه")

    LaunchedEffect(settings.autoFillToday) { if (settings.autoFillToday) selectedDateMillis = System.currentTimeMillis() }
    val selectedDate = Date(selectedDateMillis)
    val selectedDayName = arabicDayFormat.format(selectedDate)
    val selectedDateText = ledgerDateFormat.format(selectedDate)
    val visibleRecords = if (query.isBlank()) records else records.filter {
        it.dayName.contains(query, true) || it.note.contains(query, true) || it.itemQuantity.toString().contains(query) || it.pieceType.contains(query, true) || ledgerDateFormat.format(Date(it.timestamp)).contains(query)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDateMillis = it }; showDatePicker = false }) { Text("اختيار") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") } }) {
            DatePicker(state = datePickerState, title = { Text("اختيار تاريخ القيد") })
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Surface(color = PrimaryPurple, contentColor = Color.White) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(settings.shopName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("دفتر الحسابات • ${summary.totalPieces} قطعة", fontSize = 11.sp, color = Color.White.copy(alpha = .8f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenReport) { Icon(Icons.Default.PictureAsPdf, contentDescription = "التقرير") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("القطع", summary.totalPieces.toString(), Modifier.weight(1f))
            StatCard("له", "%.0f %s".format(summary.totalCredit, settings.currencySymbol), Modifier.weight(1f), SecondaryGreen)
            StatCard("عليه", "%.0f %s".format(summary.totalDebit, settings.currencySymbol), Modifier.weight(1f), ErrorRed)
            StatCard("الصافي", "%.0f %s".format(summary.netBalance, settings.currencySymbol), Modifier.weight(1f), PrimaryPurple)
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "اختيار التاريخ")
                Spacer(Modifier.padding(2.dp))
                Text(selectedDateText)
            }
            Button(onClick = { credit = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)) { Text("+ إنتاج") }
            Button(onClick = { credit = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("+ مصروف") }
        }

        OutlinedTextField(value = query, onValueChange = viewModel::onSearchQueryChange, label = { Text("بحث في السجلات") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(8.dp))

        ZoomableContainer(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF1F3F5)).border(.5.dp, BorderDivider)) {
                        GridCell("التاريخ", 1.2f, true); GridCell("اليوم", 1f, true); GridCell("القطع", .8f, true, PrimaryPurple)
                        if (settings.showDebitCredit) { GridCell("له (+)", 1f, true, SecondaryGreen); GridCell("عليه (-)", 1f, true, ErrorRed); GridCell("الرصيد", 1f, true) }
                        GridCell("الملاحظات", 1.3f, true)
                    }
                    LazyColumn(Modifier.fillMaxWidth()) {
                        items(visibleRecords, key = { it.id }) { item ->
                            Row(Modifier.fillMaxWidth().background(if (item.id % 2 == 0) Color.White else Color(0xFFF8F9FA))) {
                                GridCell(ledgerDateFormat.format(Date(item.timestamp)), 1.2f, fontWeight = FontWeight.SemiBold)
                                GridCell(item.dayName, 1f, fontWeight = FontWeight.SemiBold)
                                GridCell(item.itemQuantity.toString(), .8f, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                                if (settings.showDebitCredit) {
                                    GridCell(if (item.credit > 0) "%.0f".format(item.credit) else "-", 1f, color = SecondaryGreen)
                                    GridCell(if (item.debit > 0) "%.0f".format(item.debit) else "-", 1f, color = ErrorRed)
                                    GridCell("%.0f".format(item.balance), 1f, fontWeight = FontWeight.Bold)
                                }
                                GridCell(item.note.ifBlank { item.pieceType }, 1.3f)
                            }
                        }
                    }
                }
            }
        }

        Surface(shadowElevation = 6.dp, color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                OutlinedTextField(quantity, { value -> quantity = value; val q = value.toIntOrNull() ?: 0; if (q > 0 && amount.isBlank()) amount = (q * settings.defaultPiecePrice).toString() }, Modifier.weight(.8f), singleLine = true, label = { Text("القطع") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(amount, { amount = it }, Modifier.weight(1f), singleLine = true, label = { Text("المبلغ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                FilterChip(selected = true, onClick = { pieceType = pieceTypes[(pieceTypes.indexOf(pieceType) + 1) % pieceTypes.size] }, label = { Text(pieceType, fontSize = 11.sp) })
                OutlinedTextField(note, { note = it }, Modifier.weight(1.2f), singleLine = true, label = { Text("ملاحظة") })
                Button(onClick = { val q = quantity.toIntOrNull() ?: 0; val a = amount.toDoubleOrNull() ?: 0.0; if (q > 0 || a > 0.0) { viewModel.addNewRecord(selectedDayName, q, a, credit, note, pieceType, selectedDateMillis); quantity = ""; amount = ""; note = "" } }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) { Text("حفظ") }
            }
        }
    }
}
