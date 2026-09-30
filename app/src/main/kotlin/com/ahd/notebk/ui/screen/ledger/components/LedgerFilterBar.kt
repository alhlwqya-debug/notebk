package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahd.notebk.ui.theme.ErrorRed
import com.ahd.notebk.ui.theme.SecondaryGreen

@Composable
fun LedgerFilterBar(
    selectedDateText: String,
    query: String,
    onDateClick: () -> Unit,
    onProductionClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onQueryChange: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(8.dp), Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onDateClick, Modifier.weight(1f)) {
            Icon(Icons.Default.CalendarMonth, null)
            Spacer(Modifier.width(4.dp))
            Text(selectedDateText)
        }
        Button(onClick = onProductionClick, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)) { Text("+ إنتاج") }
        Button(onClick = onExpenseClick, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("+ مصروف") }
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        label = { Text("بحث في السجلات") },
        singleLine = true
    )
}
