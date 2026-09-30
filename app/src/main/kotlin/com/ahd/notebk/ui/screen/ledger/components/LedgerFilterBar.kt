package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
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
    onQueryChange: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onManage: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("بحث في أيام الشهر أو التاريخ") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = onProductionClick, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)) { Text("+ قطعة") }
            Button(onClick = onExpenseClick, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("+ مصروف") }
            OutlinedButton(onClick = onDateClick, Modifier.weight(1f)) {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(Modifier.width(3.dp))
                Text(selectedDateText)
            }
            IconButton(onClick = onManage) { Icon(Icons.Default.Settings, contentDescription = "إدارة") }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onPreviousDay, Modifier.weight(1f)) {
                Icon(Icons.Default.ChevronRight, null)
                Text("اليوم السابق")
            }
            OutlinedButton(onClick = onNextDay, Modifier.weight(1f)) {
                Text("اليوم التالي")
                Icon(Icons.Default.ChevronLeft, null)
            }
        }
    }
}
