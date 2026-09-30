package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.TailorRecord
import com.ahd.notebk.ui.components.GridCell
import com.ahd.notebk.ui.components.ZoomableContainer
import com.ahd.notebk.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)

@Composable
fun LedgerTable(records: List<TailorRecord>, showDebitCredit: Boolean, onEdit: (TailorRecord) -> Unit, onDelete: (TailorRecord) -> Unit) {
    ZoomableContainer(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Card(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
            Column {
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).border(.5.dp, MaterialTheme.colorScheme.outline)) {
                    GridCell("التاريخ", 1.15f, true); GridCell("الشخص", 1.15f, true); GridCell("اليوم", .95f, true)
                    GridCell("القطع", .75f, true, PrimaryPurple); GridCell("السعر", .95f, true)
                    if (showDebitCredit) { GridCell("له (+)", .95f, true, SecondaryGreen); GridCell("عليه (-)", .95f, true, ErrorRed); GridCell("الرصيد", .95f, true) }
                    GridCell("الملاحظات", 1.2f, true); GridCell("إجراءات", 1.05f, true)
                }
                LazyColumn(Modifier.fillMaxWidth()) {
                    items(records, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth().background(if (item.id % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background)) {
                            GridCell(dateFormat.format(Date(item.timestamp)), 1.15f, fontWeight = FontWeight.SemiBold)
                            GridCell(item.personName.ifBlank { "غير محدد" }, 1.15f, fontWeight = FontWeight.SemiBold)
                            GridCell(item.dayName, .95f, fontWeight = FontWeight.SemiBold)
                            GridCell(item.itemQuantity.toString(), .75f, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            GridCell(if (item.unitPrice > 0) "%.0f".format(item.unitPrice) else "-", .95f)
                            if (showDebitCredit) {
                                GridCell(if (item.credit > 0) "%.0f".format(item.credit) else "-", .95f, color = SecondaryGreen)
                                GridCell(if (item.debit > 0) "%.0f".format(item.debit) else "-", .95f, color = ErrorRed)
                                GridCell("%.0f".format(item.balance), .95f, fontWeight = FontWeight.Bold)
                            }
                            GridCell(item.note.ifBlank { item.pieceType }, 1.2f)
                            RecordActions(item, onEdit, onDelete)
                        }
                    }
                }
            }
        }
    }
}
