package com.ahd.notebk.ui.screen.ledger.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.*
import com.ahd.notebk.ui.components.*

@Composable
fun MonthsPage(
    months: List<MonthRecord>,
    selectedMonthId: Long?,
    onBack: () -> Unit,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    onRename: (MonthRecord) -> Unit,
    onCopy: (MonthRecord) -> Unit,
    onDelete: (MonthRecord) -> Unit,
    onRefresh: () -> Unit
) {
    NotebkPage("الأشهر", "إدارة الأشهر والسجلات المرتبطة بالمحل", onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("إضافة شهر") }
            OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("تحديث") }
        }
        NotebkSectionCard("الأشهر المسجلة", "اضغط على الشهر لفتحه والعمل عليه") {
            if (months.isEmpty()) {
                Text("لا توجد أشهر لهذا المحل حتى الآن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                months.forEach { month ->
                    Card(
                        onClick = { onSelect(month.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (month.id == selectedMonthId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(month.name, fontWeight = FontWeight.Bold)
                                    Text("${month.year}/${month.month} • بداية ${month.startDate}", style = MaterialTheme.typography.bodySmall)
                                    Text("العامل: ${month.workerName}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { onRename(month) }) { Icon(Icons.Default.Edit, "إعادة تسمية") }
                                    IconButton(onClick = { onCopy(month) }) { Icon(Icons.Default.ContentCopy, "نسخ") }
                                    IconButton(onClick = { onDelete(month) }) { Icon(Icons.Default.Delete, "حذف") }
                                }
                            }
                            if (month.id == selectedMonthId) Text("✓ الشهر المحدد", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PiecesPage(
    pieces: List<PieceRecord>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (PieceRecord) -> Unit,
    onDelete: (PieceRecord) -> Unit,
    onRefresh: () -> Unit
) {
    NotebkPage("إدارة القطع", "القطع والأسعار المستخدمة في تسجيل العمل", onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("إضافة قطعة") }
            OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("تحديث") }
        }
        NotebkSectionCard("قائمة القطع", "تعديل السعر ينعكس على التسجيلات الجديدة دون حذف السجلات القديمة") {
            if (pieces.isEmpty()) Text("لا توجد قطع مسجلة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            pieces.forEach { piece ->
                ListItem(
                    headlineContent = { Text(piece.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("السعر: ${piece.price}") },
                    leadingContent = { Icon(Icons.Default.Category, null) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onEdit(piece) }) { Icon(Icons.Default.Edit, "تعديل") }
                            IconButton(onClick = { onDelete(piece) }) { Icon(Icons.Default.Delete, "حذف") }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ReportsPage(
    hasMonth: Boolean,
    onBack: () -> Unit,
    onOpenReport: () -> Unit,
    onRefresh: () -> Unit
) {
    NotebkPage("التقارير والطباعة", "إنشاء التقارير ومشاركتها بصيغة PDF", onBack) {
        NotebkSectionCard("التقرير الشهري", "اختر النطاق بعد الضغط على إنشاء التقرير") {
            Text(if (hasMonth) "يوجد شهر محدد وجاهز للتقرير." else "اختر شهرًا أولًا قبل إنشاء التقرير.")
            NotebkPrimaryAction("إنشاء تقرير PDF", onOpenReport, enabled = hasMonth)
        }
        NotebkSectionCard("الطباعة والمشاركة") {
            Text("بعد إنشاء التقرير يمكنك معاينته وطباعته أو مشاركته من شاشة التقرير.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            NotebkSecondaryAction("تحديث البيانات", onRefresh)
        }
    }
}

@Composable
fun StatisticsPage(
    bundle: MonthBundle?,
    database: LedgerDatabase,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    NotebkPage("الإحصائيات", "تحليل فعلي لبيانات الشهر المحدد", onBack) {
        val current = bundle
        if (current == null) {
            NotebkSectionCard("لا يوجد شهر محدد") { Text("اختر شهرًا من صفحة الأشهر ثم افتح الإحصائيات.") }
        } else {
            val earned = database.calculateMonthEarned(current)
            val expenses = database.calculateMonthExpenses(current)
            val net = database.calculateMonthNet(current)
            val activeDays = current.days.count { database.calculateDayEarned(it) > 0 || it.expense > 0 }
            val bestDay = current.days.maxByOrNull { database.calculateDayEarned(it) }
            val bestDayValue = bestDay?.let(database::calculateDayEarned) ?: 0
            NotebkSectionCard(current.month.name, "ملخص الشهر") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotebkStatCard("الإيرادات", earned.toString(), Modifier.weight(1f))
                    NotebkStatCard("المصروفات", expenses.toString(), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotebkStatCard("الصافي", net.toString(), Modifier.weight(1f))
                    NotebkStatCard("أيام النشاط", activeDays.toString(), Modifier.weight(1f))
                }
                Text("أفضل يوم: ${bestDay?.date ?: "—"} • ${bestDayValue}", fontWeight = FontWeight.SemiBold)
            }
            NotebkSectionCard("تحليل القطع") {
                current.pieces.forEach { piece ->
                    val total = database.pieceTotal(current, piece.id)
                    val value = database.pieceEarned(current, piece.id)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(piece.name, Modifier.weight(1f))
                        Text("الكمية: $total • الدخل: $value", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            NotebkSecondaryAction("تحديث الإحصائيات", onRefresh)
        }
    }
}

@Composable
private fun NotebkPrimaryAction(text: String, onClick: () -> Unit, enabled: Boolean) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Assessment, null); Spacer(Modifier.width(6.dp)); Text(text) }
}
