package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.ui.theme.PrimaryPurple

@Composable
fun LedgerEntryBar(
    quantity: String, personName: String, unitPrice: String, amount: String, note: String, pieceType: String,
    credit: Boolean, currency: String, pieceTypes: List<String>,
    onQuantityChange: (String) -> Unit, onPersonChange: (String) -> Unit,
    onAmountChange: (String) -> Unit, onNoteChange: (String) -> Unit,
    onPieceTypeClick: () -> Unit, onSave: () -> Unit
) {
    Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            OutlinedTextField(quantity, onQuantityChange, Modifier.weight(.75f), singleLine = true, label = { Text("القطع") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(personName, onPersonChange, Modifier.weight(1.1f), singleLine = true, label = { Text("الشخص") })
            OutlinedTextField(unitPrice, {}, Modifier.weight(.9f), singleLine = true, readOnly = true, label = { Text("سعر القطعة") }, suffix = { Text(currency) })
            OutlinedTextField(amount, onAmountChange, Modifier.weight(1f), singleLine = true, readOnly = credit, label = { Text(if (credit) "المبلغ تلقائي" else "مبلغ المصروف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            FilterChip(selected = true, onClick = onPieceTypeClick, label = { Text(pieceType, fontSize = 11.sp) })
            OutlinedTextField(note, onNoteChange, Modifier.weight(1.2f), singleLine = true, label = { Text("ملاحظة") })
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) { Text("حفظ") }
        }
    }
}
