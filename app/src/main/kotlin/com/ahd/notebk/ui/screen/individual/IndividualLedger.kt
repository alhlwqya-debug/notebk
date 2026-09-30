package com.ahd.notebk.ui.screen.individual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.TailorRecord
import com.ahd.notebk.ui.components.BrandTopBar
import com.ahd.notebk.ui.screen.individual.components.*
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.theme.LightBackground
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val individualDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualLedger(
    records: List<TailorRecord>,
    viewModel: LedgerViewModel,
    settings: AppSettings,
    shopName: String,
    onOpenReport: () -> Unit
) {
    val people = remember(records) { LedgerEngine.personNames(records) }
    var selectedPerson by remember { mutableStateOf<String?>(null) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var query by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddPerson by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<TailorRecord?>(null) }
    var deletingRecord by remember { mutableStateOf<TailorRecord?>(null) }

    val availableSelection = selectedPerson?.takeIf { it == "__UNASSIGNED__" || it in people }
    val filteredByPerson = remember(records, availableSelection) {
        LedgerEngine.recordsForPerson(records, availableSelection)
    }
    val filtered = remember(filteredByPerson, query) {
        if (query.isBlank()) filteredByPerson else filteredByPerson.filter {
            it.personName.contains(query, true) ||
                it.note.contains(query, true) ||
                it.itemQuantity.toString().contains(query) ||
                individualDateFormat.format(Date(it.timestamp)).contains(query)
        }
    }
    val calculated = remember(filteredByPerson) { LedgerEngine.recalculateBalances(filteredByPerson) }
    val summary = remember(calculated) { LedgerEngine.calculateSummary(calculated) }
    val hasUnassigned = remember(records) { records.any { LedgerEngine.normalizePersonName(it.personName).isBlank() } }
    val peopleWithRecords = people.size + if (hasUnassigned) 1 else 0

    val calendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }
    val yearText = calendar.get(Calendar.YEAR).toString()
    val monthText = (calendar.get(Calendar.MONTH) + 1).toString()
    val daysRegistered = records.map { LedgerEngine.localDateKey(it.timestamp) }.distinct().size

    editingRecord?.let { record ->
        RecordEditDialog(
            record = record,
            defaultPrice = settings.defaultPiecePrice,
            currency = settings.currencySymbol,
            pieceTypes = listOf("ثابت كامل", "ثابت نص", "زوج كامل", "زوج نص", "فرده كامل", "فرده نص", "فرشه"),
            onDismiss = { editingRecord = null },
            onSave = { day, q, amount, isCredit, price, note, type, person, timestamp ->
                viewModel.updateRecord(record, day, q, amount, isCredit, price, note, type, person, timestamp)
                editingRecord = null
            }
        )
    }

    deletingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = { Text("حذف السجل") },
            text = { Text("سيتم حذف سجل " + record.personName.ifBlank { "غير محدد" } + ".") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecord(record); deletingRecord = null }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deletingRecord = null }) { Text("إلغاء") } }
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("اختيار") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        topBar = {
            BrandTopBar(
                title = "التسجيل الفردي",
                trailing = {
                    Text(
                        shopName.ifBlank { settings.shopName },
                        modifier = Modifier.padding(end = 10.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(LightBackground).padding(padding)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = yearText,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    label = { Text("السنة") },
                    readOnly = true,
                    singleLine = true
                )
                OutlinedTextField(
                    value = monthText,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    label = { Text("الشهر") },
                    readOnly = true,
                    singleLine = true
                )
            }

            Card(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الأيام المسجلة: " + daysRegistered)
                    Text("تاريخ البداية/العرض: " + individualDateFormat.format(Date(selectedDateMillis)))
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(onClick = { showDatePicker = true }, Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Text(" التاريخ")
                }
                Button(
                    onClick = { selectedDateMillis = System.currentTimeMillis() },
                    Modifier.weight(1f)
                ) { Text("إضافة يوم") }
                OutlinedButton(onClick = onOpenReport, Modifier.weight(1f)) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Text(" PDF")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                label = { Text("بحث باسم الزبون أو رقم/تاريخ السجل") },
                singleLine = true
            )

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { showAddPerson = true },
                    Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PersonAdd, null)
                    Text(" إضافة زبون جديد")
                }
                OutlinedButton(
                    onClick = { filtered.firstOrNull()?.let { editingRecord = it } },
                    enabled = filtered.isNotEmpty(),
                    Modifier.weight(1f)
                ) {
                    Text("تعديل القطع")
                }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    Modifier.weight(1f)
                ) { Text("إدارة اليوم") }
            }

            if (peopleWithRecords > 0) {
                PersonSelector(people, availableSelection, hasUnassigned) { selectedPerson = it }
            }

            PersonSummary(peopleWithRecords, summary, settings.currencySymbol)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                PersonRecordsTable(
                    records = filtered,
                    onEdit = { editingRecord = it },
                    onDelete = { deletingRecord = it }
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            add(Calendar.DAY_OF_MONTH, -1)
                        }
                        selectedDateMillis = c.timeInMillis
                    },
                    Modifier.weight(1f)
                ) { Text("← اليوم السابق") }
                Surface(
                    Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        individualDateFormat.format(Date(selectedDateMillis)),
                        Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                        selectedDateMillis = c.timeInMillis
                    },
                    Modifier.weight(1f)
                ) { Text("اليوم التالي →") }
            }
        }
    }

    if (showAddPerson) {
        AddIndividualRecordDialog(
            defaultPrice = settings.defaultPiecePrice,
            currency = settings.currencySymbol,
            initialPerson = selectedPerson.orEmpty(),
            selectedDate = selectedDateMillis,
            onDismiss = { showAddPerson = false },
            onSave = { person, quantity, price, note, pieceType, timestamp ->
                viewModel.addNewRecord(
                    dayName = SimpleDateFormat("EEEE", Locale("ar")).format(Date(timestamp)),
                    quantity = quantity,
                    amount = quantity * price,
                    isCredit = true,
                    unitPrice = price,
                    note = note,
                    pieceType = pieceType,
                    personName = person,
                    timestamp = timestamp
                )
                selectedPerson = person
                showAddPerson = false
            }
        )
    }
}

@Composable
private fun AddIndividualRecordDialog(
    defaultPrice: Double,
    currency: String,
    initialPerson: String,
    selectedDate: Long,
    onDismiss: () -> Unit,
    onSave: (String, Int, Double, String, String, Long) -> Unit
) {
    var person by remember { mutableStateOf(initialPerson) }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(defaultPrice.toString()) }
    var note by remember { mutableStateOf("") }
    var pieceType by remember { mutableStateOf("ثابت كامل") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة زبون / سجل فردي") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(person, { person = it }, label = { Text("اسم الزبون *") }, singleLine = true)
                OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, label = { Text("عدد القطع") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(price, { price = it }, label = { Text("سعر القطعة ($currency)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(pieceType, { pieceType = it }, label = { Text("نوع القطعة") }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("ملاحظة") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toIntOrNull() ?: 0
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (person.isNotBlank() && q > 0 && p >= 0) onSave(person.trim(), q, p, note, pieceType, selectedDate)
                },
                enabled = person.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0
            ) {
                Icon(Icons.Default.Add, null)
                Text("حفظ")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
