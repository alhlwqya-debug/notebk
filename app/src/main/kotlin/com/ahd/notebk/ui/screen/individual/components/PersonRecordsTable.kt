package com.ahd.notebk.ui.screen.individual.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.TailorRecord
import com.ahd.notebk.ui.components.GridCell
import com.ahd.notebk.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)

@Composable
fun PersonRecordsTable(records: List<TailorRecord>) {
    Surface(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp), shape = MaterialTheme.shapes.large, tonalElevation = 1.dp) {
        if (records.isEmpty()) {
            Text("لا توجد سجلات لهذا الشخص", Modifier.padding(24.dp), color = Color.Gray)
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { Row(Modifier.fillMaxWidth().background(Color(0xFFF1F3F5))) {
                    GridCell("التاريخ", 1.1f, true); GridCell("القطع", .8f, true, PrimaryPurple); GridCell("النوع", 1.1f, true); GridCell("السعر", .9f, true); GridCell("له", 1f, true, SecondaryGreen); GridCell("عليه", 1f, true, ErrorRed); GridCell("الرصيد", 1f, true)
                } }
                items(records, key = { it.id }) { record ->
                    Card(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth()) {
                        GridCell(dateFormat.format(Date(record.timestamp)), 1.1f)
                        GridCell(record.itemQuantity.toString(), .8f, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                        GridCell(record.pieceType, 1.1f)
                        GridCell(if (record.unitPrice > 0) "%.0f".format(record.unitPrice) else "-", .9f)
                        GridCell(if (record.credit > 0) "%.0f".format(record.credit) else "-", 1f, color = SecondaryGreen)
                        GridCell(if (record.debit > 0) "%.0f".format(record.debit) else "-", 1f, color = ErrorRed)
                        GridCell("%.0f".format(record.balance), 1f, fontWeight = FontWeight.Bold)
                    } }
                }
            }
        }
    }
}
