package com.ahd.notebk.ui.screen.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.engine.PieceCalculator
import com.ahd.notebk.ui.screen.ledger.components.*
import com.ahd.notebk.ui.screen.individual.components.RecordEditDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ledgerDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
private val arabicDayFormat = SimpleDateFormat("EEEE", Locale("ar"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel,
    shopName: String,
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
    var expenseType by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf(true) }
    var pieceType by remember { mutableStateOf("ثابت كامل") }
    var unitPrice by remember(settings.defaultPiecePrice) { mutableStateOf(settings.defaultPiecePrice.toString()) }
    var yearText by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var monthText by remember { mutableStateOf((Calendar.getInstance().get(Calendar.MONTH) + 1).toString()) }
    var editingRecord by remember { mutableStateOf<com.ahd.notebk.domain.model.TailorRecord?>(null) }
    var deletingRecord by remember { mutableStateOf<com.ahd.notebk.domain.model.TailorRecord?>(null) }

    val pieceTypes = remember { listOf("ثابت كامل", "ثابت نص", "زوج كامل", "زوج نص", "فرده كامل", "فرده نص", "فرشه") }
    val selectedDayName = arabicDayFormat.format(Date(selectedDateMillis))
    val selectedDateText = ledgerDateFormat.format(Date(selectedDateMillis))
    val daysRegistered = records.map { viewModelDateKey(it.timestamp) }.distinct().size

    val numericRecords = remember(records) { records.filter { it.recordType == "numeric" } }
    val visibleRecords = remember(numericRecords, query) {
        if (query.isBlank()) numericRecords else numericRecords.filter {
            it.dayName.contains(query, true) ||
                it.note.contains(query, true) ||
                it.expenseType.contains(query, true) ||
                it.itemQuantity.toString().contains(query) ||
                it.pieceType.contains(query, true) ||
                ledgerDateFormat.format(Date(it.timestamp)).contains(query)
        }
    }

    fun moveDay(delta: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            add(Calendar.DAY_OF_MONTH, delta)
        }
        selectedDateMillis = calendar.timeInMillis
        yearText = calendar.get(Calendar.YEAR).toString()
        monthText = (calendar.get(Calendar.MONTH) + 1).toString()
    }

    fun updatePeriod(year: String, month: String) {
        yearText = year.filter(Char::isDigit).take(4)
        monthText = month.filter(Char::isDigit).take(2)
        val y = yearText.toIntOrNull()
        val m = monthText.toIntOrNull()
        if (y != null && y in 2000..2100 && m != null && m in 1..12) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            selectedDateMillis = calendar.timeInMillis
        }
    }

    editingRecord?.let { record ->
        RecordEditDialog(
            record = record,
            defaultPrice = settings.defaultPiecePrice,
            currency = settings.currencySymbol,
            pieceTypes = pieceTypes,
            onDismiss = { editingRecord = null },
            onSave = { day, q, a, isCredit, price, editNote, type, person, pageNumber, expenseTypeValue, recordType, timestamp ->
                viewModel.updateRecord(record, day, q, a, isCredit, price, editNote, type, person, pageNumber, expenseTypeValue, recordType, timestamp)
                editingRecord = null
            }
        )
    }

    deletingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = { Text("حذف السجل") },
            text = {
                Text(
                    "هل تريد حذف سجل " +
                        record.personName.ifBlank { "غير محدد" } +
                        " بتاريخ " + ledgerDateFormat.format(Date(record.timestamp)) + "؟"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecord(record); deletingRecord = null }) {
                    Text("حذف", color = com.ahd.notebk.ui.theme.ErrorRed)
                }
            },
            dismissButton = { TextButton(onClick = { deletingRecord = null }) { Text("إلغاء") } }
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        selectedDateMillis = it
                        val c = Calendar.getInstance().apply { timeInMillis = it }
                        yearText = c.get(Calendar.YEAR).toString()
                        monthText = (c.get(Calendar.MONTH) + 1).toString()
                    }
                    showDatePicker = false
                }) { Text("اختيار") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = state) }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LedgerTopBar(
            settings = settings,
            summary = summary,
            shopName = shopName,
            onOpenStatistics = onOpenStatistics,
            onOpenIndividualLedger = onOpenIndividualLedger,
            onOpenReport = onOpenReport,
            onOpenSettings = onOpenSettings
        )

        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = yearText,
                onValueChange = { updatePeriod(it, monthText) },
                modifier = Modifier.weight(1f),
                label = { Text("السنة") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = monthText,
                onValueChange = { updatePeriod(yearText, it) },
                modifier = Modifier.weight(1f),
                label = { Text("الشهر") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Card(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الأيام المسجلة: " + daysRegistered)
                Text("التاريخ الحالي: " + selectedDateText)
            }
        }

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
            onQueryChange = viewModel::onSearchQueryChange,
            onPreviousDay = { moveDay(-1) },
            onNextDay = { moveDay(1) },
            onManage = onOpenSettings
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            LedgerTable(
                visibleRecords,
                settings.showDebitCredit,
                onEdit = { editingRecord = it },
                onDelete = { deletingRecord = it }
            )
        }

        LedgerEntryBar(
            quantity, unitPrice, amount, expenseType, note, pieceType, credit,
            settings.currencySymbol, pieceTypes,
            onQuantityChange = {
                val clean = it.filter(Char::isDigit)
                quantity = clean
                if (credit) {
                    val q = clean.toIntOrNull() ?: 0
                    val price = unitPrice.toDoubleOrNull() ?: settings.defaultPiecePrice
                    amount = if (q > 0 && price >= 0) PieceCalculator.calculateTotal(q, price).toString() else ""
                }
            },
            onAmountChange = { if (!credit) amount = it.filter { c -> c.isDigit() || c == '.' } },
            onExpenseTypeChange = { expenseType = it },
            onNoteChange = { note = it },
            onPieceTypeClick = { pieceType = pieceTypes[(pieceTypes.indexOf(pieceType) + 1) % pieceTypes.size] },
            onSave = {
                val q = quantity.toIntOrNull() ?: 0
                val price = unitPrice.toDoubleOrNull() ?: settings.defaultPiecePrice
                val a = if (credit) PieceCalculator.calculateTotal(q, price) else (amount.toDoubleOrNull() ?: 0.0)
                if ((credit && q > 0 && price >= 0) || (!credit && a > 0)) {
                    viewModel.addNewRecord(selectedDayName, q, a, credit, price, note, pieceType, "", "", expenseType, "numeric", selectedDateMillis)
                    quantity = ""
                    amount = ""
                    expenseType = ""
                    note = ""
                }
            }
        )
    }
}

private fun viewModelDateKey(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return "%04d-%02d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
