package com.ahd.notebk.ui.screen.individual.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.TailorRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordEditDialog(
    record: TailorRecord,
    defaultPrice: Double,
    currency: String,
    pieceTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        dayName: String,
        quantity: Int,
        amount: Double,
        isCredit: Boolean,
        unitPrice: Double,
        note: String,
        pieceType: String,
        personName: String,
        timestamp: Long
    ) -> Unit
) {
    var day by remember(record) { mutableStateOf(record.dayName) }
    var quantity by remember(record) { mutableStateOf(record.itemQuantity.toString()) }
    var amount by remember(record) {
        mutableStateOf(if (record.credit > 0) record.credit.toString() else record.debit.toString())
    }
    var isCredit by remember(record) { mutableStateOf(record.credit > 0) }
    var price by remember(record) { mutableStateOf(record.unitPrice.takeIf { it > 0 }?.toString() ?: defaultPrice.toString()) }
    var note by remember(record) { mutableStateOf(record.note) }
    var pieceType by remember(record) { mutableStateOf(record.pieceType) }
    var person by remember(record) { mutableStateOf(record.personName) }

    val dateText = remember(record) {
        SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(record.timestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل السجل") },
        text = {
            Column(Modifier.heightIn(max = 520.dp)) {
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("اسم الزبون") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("اليوم") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عدد القطع") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("سعر القطعة ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isCredit) "الإنتاج ($currency)" else "المصروف ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = pieceType,
                    onValueChange = { pieceType = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("نوع القطعة") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ملاحظة") },
                    singleLine = true
                )
                Text("التاريخ: $dateText")
                RowToggle(
                    credit = isCredit,
                    onChange = { isCredit = it },
                    pieceTypes = pieceTypes,
                    currentType = pieceType,
                    onTypeChange = { pieceType = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toIntOrNull() ?: 0
                    val p = price.toDoubleOrNull() ?: 0.0
                    val a = amount.toDoubleOrNull() ?: 0.0
                    if (q >= 0 && p >= 0 && a >= 0) {
                        onSave(day, q, a, isCredit, p, note, pieceType, person, record.timestamp)
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun RowToggle(
    credit: Boolean,
    onChange: (Boolean) -> Unit,
    pieceTypes: List<String>,
    currentType: String,
    onTypeChange: (String) -> Unit
) {
    Column {
        RowToggleItem("إنتاج / له", credit, { onChange(true) })
        RowToggleItem("مصروف / عليه", !credit, { onChange(false) })
        if (pieceTypes.isNotEmpty()) {
            TextButton(onClick = {
                val index = pieceTypes.indexOf(currentType)
                onTypeChange(pieceTypes[(index + 1).mod(pieceTypes.size)])
            }) { Text("نوع القطعة: $currentType") }
        }
    }
}

@Composable
private fun RowToggleItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Row {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.heightIn(min = 48.dp))
    }
}
