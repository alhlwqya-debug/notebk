package com.ahd.notebk.ui.screen.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.engine.PieceCalculator
import com.ahd.notebk.ui.screen.ledger.components.*
import com.ahd.notebk.ui.theme.LightBackground
import java.text.SimpleDateFormat
import java.util.*

private val ledgerDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
private val arabicDayFormat = SimpleDateFormat("EEEE", Locale("ar"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel,
    onOpenSettings: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenStatistics: () -> Unit = {},
    onOpenIndividualLedger: () -> Unit = {}
) {
    val settings by viewModel.settingsState.collectAsState()
    val records by viewModel.recordsState.collectAsState()
    val summary by viewModel.summaryState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf(settings.workerName) }
    var credit by remember { mutableStateOf(true) }
    var pieceType by remember { mutableStateOf("ثابت كامل") }
    var unitPrice by remember(settings.defaultPiecePrice) { mutableStateOf(settings.defaultPiecePrice.toString()) }
    val pieceTypes = remember { listOf("ثابت كامل", "ثابت نص", "زوج كامل", "زوج نص", "فرده كامل", "فرده نص", "فرشه") }
    var editingRecord by remember { mutableStateOf<com.ahd.notebk.domain.model.TailorRecord?>(null) }
    var deletingRecord by remember { mutableStateOf<com.ahd.notebk.domain.model.TailorRecord?>(null) }
    val selectedDayName = arabicDayFormat.format(Date(selectedDateMillis))
    val selectedDateText = ledgerDateFormat.format(Date(selectedDateMillis))
    val visibleRecords = remember(records, query) {
        if (query.isBlank()) records else records.filter {
            it.dayName.contains(query, true) || it.note.contains(query, true) || it.personName.contains(query, true) ||
                it.itemQuantity.toString().contains(query) || it.pieceType.contains(query, true) ||
                ledgerDateFormat.format(Date(it.timestamp)).contains(query)
        }
    }

    editingRecord?.let { record ->
        RecordEditDialog(
            record = record,
            defaultPrice = settings.defaultPiecePrice,
            currency = settings.currencySymbol,
            pieceTypes = pieceTypes,
            onDismiss = { editingRecord = null },
            onSave = { day, q, a, isCredit, price, editNote, type, person, timestamp ->
                viewModel.updateRecord(record, day, q, a, isCredit, price, editNote, type, person, timestamp)
                editingRecord = null
            }
        )
    }

    deletingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = { Text("حذف السجل") },
            text = { Text("هل تريد حذف سجل ${record.personName.ifBlank { "غير محدد" }} بتاريخ ${ledgerDateFormat.format(Date(record.timestamp))}؟") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecord(record); deletingRecord = null }) { Text("حذف", color = com.ahd.notebk.ui.theme.ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { deletingRecord = null }) { Text("إلغاء") } }
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { selectedDateMillis = it }; showDatePicker = false }) { Text("اختيار") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = state) }
    }

    Column(Modifier.fillMaxSize().background(LightBackground)) {
        LedgerTopBar(settings, summary, onOpenStatistics, onOpenIndividualLedger, onOpenReport, onOpenSettings)
        LedgerSummaryCards(summary, settings)
        LedgerFilterBar(
            selectedDateText = selectedDateText,
            query = query,
            onDateClick = { showDatePicker = true },
            onProductionClick = {
                credit = true
                val q = quantity.toIntOrNull() ?: 0
                val price = unitPrice.toDoubleOrNull() ?: settings.defaultPiecePrice
                amount = if (q > 0) PieceCalculator.calculateTotal(q, price).toString() else ""
            },
            onExpenseClick = { credit = false },
            onQueryChange = viewModel::onSearchQueryChange
        )
        LedgerTable(visibleRecords, settings.showDebitCredit, onEdit = { editingRecord = it }, onDelete = { deletingRecord = it })
        Spacer(Modifier.height(6.dp))
        LedgerEntryBar(
            quantity, personName, unitPrice, amount, note, pieceType, credit, settings.currencySymbol, pieceTypes,
            onQuantityChange = {
                val clean = it.filter(Char::isDigit)
                quantity = clean
                if (credit) {
                    val q = clean.toIntOrNull() ?: 0
                    val price = unitPrice.toDoubleOrNull() ?: settings.defaultPiecePrice
                    amount = if (q > 0 && price >= 0) PieceCalculator.calculateTotal(q, price).toString() else ""
                }
            },
            onPersonChange = { personName = it },
            onAmountChange = { if (!credit) amount = it.filter { c -> c.isDigit() || c == '.' } },
            onNoteChange = { note = it },
            onPieceTypeClick = { pieceType = pieceTypes[(pieceTypes.indexOf(pieceType) + 1) % pieceTypes.size] },
            onSave = {
                val q = quantity.toIntOrNull() ?: 0
                val price = unitPrice.toDoubleOrNull() ?: settings.defaultPiecePrice
                val a = if (credit) PieceCalculator.calculateTotal(q, price) else (amount.toDoubleOrNull() ?: 0.0)
                if ((credit && q > 0 && price >= 0) || (!credit && a > 0)) {
                    viewModel.addNewRecord(selectedDayName, q, a, credit, price, note, pieceType, personName, selectedDateMillis)
                    quantity = ""; amount = ""; note = ""
                }
            }
        )
    }
}
