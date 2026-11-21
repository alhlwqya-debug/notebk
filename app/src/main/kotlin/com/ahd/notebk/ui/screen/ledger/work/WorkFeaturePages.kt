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
fun MonthsPage(months: List<MonthRecord>, selectedMonthId: Long?, onBack: () -> Unit, onSelect: (Long) -> Unit, onAdd: () -> Unit, onRename: (MonthRecord) -> Unit, onCopy: (MonthRecord) -> Unit, onDelete: (MonthRecord) -> Unit, onRefresh: () -> Unit, onCreateYear: (Int) -> Unit, onCopyYear: (Int, Int) -> Unit, onDeleteYear: (Int) -> Unit) {
    var yearOpen by remember { mutableStateOf<Int?>(null) }
    var createDialog by remember { mutableStateOf(false) }; var yearInput by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()) }
    var copyFrom by remember { mutableStateOf<Int?>(null) }; var copyTo by remember { mutableStateOf("") }; var deleteYear by remember { mutableStateOf<Int?>(null) }
    val years = months.map { it.year }.distinct().sortedDescending()
    val yearMonths = months.filter { it.year == yearOpen }.sortedByDescending { it.month }
    NotebkPage(if (yearOpen == null) "إدارة السنوات" else "أشهر سنة $yearOpen", "إدارة السنوات والشهور المرتبطة بالمحل", { if (yearOpen != null) yearOpen = null else onBack() }) {
        if (yearOpen == null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { createDialog = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(5.dp)); Text("إنشاء سنة") }
                OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(5.dp)); Text("تحديث") }
            }
            NotebkSectionCard("السنوات المسجلة", "افتح السنة لإدارة أشهرها") {
                if (years.isEmpty()) Text("لا توجد سنوات لهذا المحل بعد. أنشئ سنة للبدء.")
                years.forEach { year ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f).clickable { yearOpen = year }) { Text("السنة $year", fontWeight = FontWeight.Bold); Text("الأشهر: ${months.count { it.year == year }} / 12", style = MaterialTheme.typography.bodySmall); Text("اضغط لفتح السنة", color = MaterialTheme.colorScheme.primary) }
                            Row { IconButton(onClick = { copyFrom = year; copyTo = (year + 1).toString() }) { Icon(Icons.Default.ContentCopy, "نسخ سنة") }; IconButton(onClick = { deleteYear = year }) { Icon(Icons.Default.Delete, "حذف سنة") } }
                        }
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(5.dp)); Text("إضافة شهر") }; OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(5.dp)); Text("تحديث") } }
            NotebkSectionCard("الأشهر المسجلة في $yearOpen", "السجلات الموجودة فعليًا فقط") {
                if (yearMonths.isEmpty()) Text("لا توجد أشهر مسجلة في هذه السنة.")
                yearMonths.forEach { month ->
                    Card(onClick = { onSelect(month.id) }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (month.id == selectedMonthId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text(month.name, fontWeight = FontWeight.Bold); Text("${month.year}/${month.month} • ${month.startDate}", style = MaterialTheme.typography.bodySmall); Text("العامل: ${month.workerName}", style = MaterialTheme.typography.bodySmall) }; Row { IconButton(onClick = { onRename(month) }) { Icon(Icons.Default.Edit, "تعديل") }; IconButton(onClick = { onCopy(month) }) { Icon(Icons.Default.ContentCopy, "نسخ") }; IconButton(onClick = { onDelete(month) }) { Icon(Icons.Default.Delete, "حذف") } } }; if (month.id == selectedMonthId) Text("✓ الشهر المحدد", color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        }
    }
    if (createDialog) AlertDialog(onDismissRequest = { createDialog = false }, title = { Text("إنشاء سنة") }, text = { OutlinedTextField(yearInput, { yearInput = it.filter(Char::isDigit).take(4) }, label = { Text("السنة") }, singleLine = true) }, confirmButton = { Button(onClick = { val y = yearInput.toIntOrNull(); if (y != null && y in 1900..2500) { onCreateYear(y); createDialog = false } }) { Text("إنشاء") } }, dismissButton = { TextButton(onClick = { createDialog = false }) { Text("إلغاء") } })
    if (copyFrom != null) AlertDialog(onDismissRequest = { copyFrom = null }, title = { Text("نسخ سنة") }, text = { Column { Text("إنشاء سجلات أشهر فارغة دون نسخ الحسابات القديمة."); OutlinedTextField(copyTo, { copyTo = it.filter(Char::isDigit).take(4) }, label = { Text("السنة الجديدة") }, singleLine = true) } }, confirmButton = { Button(onClick = { val y = copyTo.toIntOrNull(); if (y != null && y in 1900..2500 && y != copyFrom) { onCopyYear(copyFrom!!, y); copyFrom = null } }) { Text("نسخ") } }, dismissButton = { TextButton(onClick = { copyFrom = null }) { Text("إلغاء") } })
    if (deleteYear != null) AlertDialog(onDismissRequest = { deleteYear = null }, title = { Text("حذف السنة", color = MaterialTheme.colorScheme.error) }, text = { Text("سيتم حذف أشهر سنة $deleteYear وجميع سجلاتها لهذا المحل. يُنصح بإنشاء نسخة احتياطية.") }, confirmButton = { Button(onClick = { onDeleteYear(deleteYear!!); deleteYear = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("حذف السنة") } }, dismissButton = { TextButton(onClick = { deleteYear = null }) { Text("إلغاء") } })
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
    NotebkPage("النواتج والتقارير", "إدارة تقارير الشهر والسنة وكل السنوات ومشاركتها أو طباعتها بصيغة PDF", onBack) {
        NotebkSectionCard("التقارير والنواتج", "اختر النطاق من شاشة إعداد التقرير") {
            Text(if (hasMonth) "يمكن إنشاء تقرير للشهر المحدد أو سنة كاملة أو جميع السنوات المسجلة." else "أضف شهرًا وسجّل بيانات العمل أولًا حتى تتوفر نواتج للتقرير.")
            NotebkPrimaryAction("إدارة التقارير والطباعة والمشاركة", onOpenReport, enabled = hasMonth)
        }
        NotebkSectionCard("ما الذي يشمله التقرير؟") {
            Text("يُنشأ التقرير من تسجيلات المحل الفعلية، ويتضمن الإنتاج والمصروفات والصافي وتفاصيل الأيام؛ والتسجيل الفردي يتضمن تفاصيل الزبائن والصفحات والقطع.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            NotebkSecondaryAction("تحديث البيانات", onRefresh)
        }
    }
}

@Composable
fun StatisticsPage(
    bundle: MonthBundle?,
    database: LedgerDatabase,
    registrationMode: RegistrationMode,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    NotebkPage("الإحصائيات", "تحليل فعلي لبيانات الشهر المحدد", onBack) {
        val current = bundle
        if (current == null) {
            NotebkSectionCard("لا يوجد شهر محدد") { Text("اختر شهرًا من صفحة الأشهر ثم افتح الإحصائيات.") }
        } else {
            // Individual/customer ledger stores quantities and prices on each customer item,
            // not in the numeric day rows. Aggregate from the correct source for this shop.
            val individualEntries = if (registrationMode == RegistrationMode.INDIVIDUAL) {
                current.days.flatMap { day -> database.getIndividualEntries(current.month.id, day.date) }
            } else emptyList()

            fun earnedForDay(day: DayRecord): Int =
                if (registrationMode == RegistrationMode.INDIVIDUAL) {
                    database.calculateIndividualDayEarned(
                        database.getIndividualEntries(current.month.id, day.date)
                    )
                } else database.calculateDayEarned(day)

            val earned = if (registrationMode == RegistrationMode.INDIVIDUAL) {
                individualEntries.sumOf(database::calculateIndividualEntryEarned)
            } else database.calculateMonthEarned(current)
            val expenses = database.calculateMonthExpenses(current)
            val net = if (current.month.deductExpense) earned - expenses else earned
            val activeDays = current.days.count { earnedForDay(it) > 0 || it.expense > 0 }
            val bestDay = current.days.maxByOrNull(::earnedForDay)
            val bestDayValue = bestDay?.let(::earnedForDay) ?: 0

            NotebkSectionCard(current.month.name, "ملخص الشهر") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotebkStatCard("الإيرادات", earned.toString(), Modifier.weight(1f))
                    NotebkStatCard("المصروفات", expenses.toString(), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotebkStatCard("الصافي", net.toString(), Modifier.weight(1f))
                    NotebkStatCard("أيام النشاط", activeDays.toString(), Modifier.weight(1f))
                }
                Text("أفضل يوم: ${bestDay?.date ?: "—"} • $bestDayValue", fontWeight = FontWeight.SemiBold)
            }
            NotebkSectionCard("تحليل القطع") {
                current.pieces.forEach { piece ->
                    val total: Int
                    val value: Int
                    if (registrationMode == RegistrationMode.INDIVIDUAL) {
                        total = individualEntries.sumOf { it.quantities[piece.id] ?: 0 }
                        value = individualEntries.sumOf { entry ->
                            (entry.quantities[piece.id] ?: 0) * (entry.unitPrices[piece.id] ?: 0)
                        }
                    } else {
                        total = database.pieceTotal(current, piece.id)
                        value = database.pieceEarned(current, piece.id)
                    }
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
