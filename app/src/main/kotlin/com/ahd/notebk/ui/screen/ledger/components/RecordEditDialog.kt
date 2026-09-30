package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.engine.PieceCalculator
import com.ahd.notebk.domain.model.TailorRecord

@Composable
fun RecordEditDialog(
    record: TailorRecord,
    defaultPrice: Double,
    currency: String,
    pieceTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (dayName: String, quantity: Int, amount: Double, isCredit: Boolean, unitPrice: Double, note: String, pieceType: String, personName: String, timestamp: Long) -> Unit
) {
    var day by remember(record.id) { mutableStateOf(record.dayName) }
    var quantity by remember(record.id) { mutableStateOf(record.itemQuantity.toString()) }
    var person by remember(record.id) { mutableStateOf(record.personName) }
    var price by remember(record.id) { mutableStateOf(record.unitPrice.toString()) }
    var amount by remember(record.id) { mutableStateOf(if (record.credit > 0) record.credit.toString() else record.debit.toString()) }
    var note by remember(record.id) { mutableStateOf(record.note) }
    var type by remember(record.id) { mutableStateOf(record.pieceType) }
    var isCredit by remember(record.id) { mutableStateOf(record.credit > 0) }
    val q = quantity.toIntOrNull() ?: 0
    val p = price.toDoubleOrNull() ?: defaultPrice
    val calculatedAmount = if (isCredit) PieceCalculator.calculateTotal(q, p) else amount.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل السجل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(day, { day = it }, Modifier.weight(1f), label = { Text("اليوم") }, singleLine = true)
                    OutlinedTextField(person, { person = it }, Modifier.weight(1f), label = { Text("الشخص") }, singleLine = true)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, Modifier.weight(1f), label = { Text("القطع") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    OutlinedTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.weight(1f), label = { Text("سعر القطعة") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isCredit, onClick = { isCredit = true }, label = { Text("إنتاج") })
                    FilterChip(selected = !isCredit, onClick = { isCredit = false }, label = { Text("مصروف") })
                    Text("${"%.0f".format(calculatedAmount)} $currency", modifier = Modifier.weight(1f))
                }
                if (!isCredit) {
                    OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.fillMaxWidth(), label = { Text("مبلغ المصروف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
                FilterChip(selected = true, onClick = { type = pieceTypes[(pieceTypes.indexOf(type).coerceAtLeast(0) + 1) % pieceTypes.size] }, label = { Text(type) })
                OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("ملاحظة") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val finalAmount = if (isCredit) PieceCalculator.calculateTotal(q, p) else amount.toDoubleOrNull() ?: 0.0
                if (day.isNotBlank() && q >= 0 && p >= 0 && finalAmount >= 0) {
                    onSave(day.trim(), q, finalAmount, isCredit, p, note.trim(), type, person.trim(), record.timestamp)
                }
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
