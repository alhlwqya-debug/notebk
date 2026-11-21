package com.ahd.notebk.ui.screen.ledger.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ArrowDropDown
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
fun MonthsPage(months: List<MonthRecord>, selectedMonthId: Long?, onBack: () -> Unit, onSelect: (Long) -> Unit, onAdd: (Int) -> Unit, onRename: (MonthRecord) -> Unit, onCopy: (MonthRecord) -> Unit, onDelete: (MonthRecord) -> Unit, onRefresh: () -> Unit, onCreateYear: (Int) -> Unit, onCopyYear: (Int, Int) -> Unit, onDeleteYear: (Int) -> Unit) {
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { onAdd(yearOpen!!) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(5.dp)); Text("إضافة شهر") }; OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(5.dp)); Text("تحديث") } }
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
    months: List<MonthRecord>,
    selectedMonthId: Long?,
    onSelectMonth: (Long) -> Unit,
    database: LedgerDatabase,
    registrationMode: RegistrationMode,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val years = months.map { it.year }.distinct().sortedDescending()
    var selectedYear by remember { mutableStateOf<Int?>(bundle?.month?.year ?: years.firstOrNull()) }
    var annualMode by remember { mutableStateOf(false) }
    val periodMonths = months.filter { it.year == selectedYear }.sortedBy { it.month }
    val bundles = periodMonths.mapNotNull { database.loadMonthBundle(it.id) }
    val monthStats = bundles.map { b ->
        val entries = if (registrationMode == RegistrationMode.INDIVIDUAL)
            b.days.flatMap { database.getIndividualEntries(b.month.id, it.date) } else emptyList()
        val earned = if (registrationMode == RegistrationMode.INDIVIDUAL)
            entries.sumOf(database::calculateIndividualEntryEarned) else database.calculateMonthEarned(b)
        val expenses = database.calculateMonthExpenses(b)
        val net = if (b.month.deductExpense) earned - expenses else earned
        Triple(b, earned, Pair(expenses, net))
    }
    val totalEarned = monthStats.sumOf { it.second }
    val totalExpenses = monthStats.sumOf { it.third.first }
    // Expense deduction is a month-level setting. Sum each month's correctly defined net,
    // rather than subtracting all expenses globally when some months do not deduct them.
    val totalNet = monthStats.sumOf { it.third.second }
    val activeDays = bundles.sumOf { b ->
        b.days.count { day ->
            val earned = if (registrationMode == RegistrationMode.INDIVIDUAL)
                database.calculateIndividualDayEarned(database.getIndividualEntries(b.month.id, day.date))
            else database.calculateDayEarned(day)
            earned > 0 || day.expense > 0
        }
    }
    val monthLabels = listOf("يناير","فبراير","مارس","أبريل","مايو","يونيو","يوليو","أغسطس","سبتمبر","أكتوبر","نوفمبر","ديسمبر")
    val bestMonth = monthStats.maxByOrNull { it.second }
    val bestDayPair = bundles.flatMap { b -> b.days.map { day ->
        val earned = if (registrationMode == RegistrationMode.INDIVIDUAL)
            database.calculateIndividualDayEarned(database.getIndividualEntries(b.month.id, day.date))
        else database.calculateDayEarned(day)
        Triple(b.month.name, day.date, earned)
    } }.maxByOrNull { it.third }

    NotebkPage("التحليلات والإحصائيات", "تحليل شهري وسنوي من السجلات الفعلية للمحل", onBack) {
        if (months.isEmpty()) {
            NotebkSectionCard("لا توجد بيانات للتحليل") { Text("أضف شهرًا وسجّل العمل اليومي أولًا.") }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !annualMode, onClick = { annualMode = false }, label = { Text("تحليل شهري") })
                FilterChip(selected = annualMode, onClick = { annualMode = true }, label = { Text("تحليل سنوي") })
            }
            var yearMenu by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { yearMenu = true }) { Text("السنة: ${selectedYear ?: "—"}"); Icon(Icons.Default.ArrowDropDown, null) }
                DropdownMenu(expanded = yearMenu, onDismissRequest = { yearMenu = false }) {
                    years.forEach { y -> DropdownMenuItem(text = { Text(y.toString()) }, onClick = { selectedYear = y; periodMonths.firstOrNull()?.let { onSelectMonth(it.id) }; yearMenu = false }) }
                }
            }
            if (!annualMode) {
                var monthMenu by remember { mutableStateOf(false) }
                val chosen = selectedMonthId?.let(database::loadMonthBundle)?.takeIf { it.month.year == selectedYear }
                Box {
                    OutlinedButton(onClick = { monthMenu = true }) { Text("الشهر: ${chosen?.month?.name ?: "اختر شهرًا"}"); Icon(Icons.Default.ArrowDropDown, null) }
                    DropdownMenu(expanded = monthMenu, onDismissRequest = { monthMenu = false }) {
                        periodMonths.forEach { m -> DropdownMenuItem(text = { Text(m.name) }, onClick = { onSelectMonth(m.id); monthMenu = false }) }
                    }
                }
                val one = chosen?.let { b ->
                    val entries = if (registrationMode == RegistrationMode.INDIVIDUAL) b.days.flatMap { database.getIndividualEntries(b.month.id, it.date) } else emptyList()
                    val earned = if (registrationMode == RegistrationMode.INDIVIDUAL) entries.sumOf(database::calculateIndividualEntryEarned) else database.calculateMonthEarned(b)
                    val expenses = database.calculateMonthExpenses(b)
                    Triple(earned, expenses, if (b.month.deductExpense) earned-expenses else earned)
                }
                NotebkSectionCard(chosen?.month?.name ?: "الشهر المحدد", "نتائج الشهر المختار فقط") {
                    if (one == null) Text("اختر شهرًا من إدارة الأشهر لعرض تحليله التفصيلي.")
                    else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { NotebkStatCard("الإيرادات", one.first.toString(), Modifier.weight(1f)); NotebkStatCard("المصروفات", one.second.toString(), Modifier.weight(1f)) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { NotebkStatCard("الصافي المحاسبي", one.third.toString(), Modifier.weight(1f)); NotebkStatCard("أيام نشطة", chosen!!.days.count { it.expense>0 || (if(registrationMode==RegistrationMode.INDIVIDUAL) database.calculateIndividualDayEarned(database.getIndividualEntries(chosen.month.id,it.date)) else database.calculateDayEarned(it))>0 }.toString(), Modifier.weight(1f)) }
                    }
                }
            } else {
                NotebkSectionCard("ملخص السنة ${selectedYear ?: ""}", "مجموع الأشهر المسجلة فقط؛ لا تُحتسب أشهر غير موجودة كبيانات") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { NotebkStatCard("إجمالي الإيرادات", totalEarned.toString(), Modifier.weight(1f)); NotebkStatCard("إجمالي المصروفات", totalExpenses.toString(), Modifier.weight(1f)) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { NotebkStatCard("الصافي المحاسبي", totalNet.toString(), Modifier.weight(1f)); NotebkStatCard("الأشهر المسجلة", "${bundles.size}/12", Modifier.weight(1f)) }
                    NotebkStatCard("أيام النشاط خلال السنة", activeDays.toString(), Modifier.fillMaxWidth())
                    Text("أعلى شهر إيرادًا: ${bestMonth?.first?.month?.name ?: "—"} • ${bestMonth?.second ?: 0}", fontWeight = FontWeight.SemiBold)
                    Text("أفضل يوم: ${bestDayPair?.second ?: "—"} • ${bestDayPair?.third ?: 0}", fontWeight = FontWeight.SemiBold)
                }
                NotebkSectionCard("مقارنة الأشهر") {
                    monthStats.forEach { stat ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.first.month.name, Modifier.weight(1f))
                            Text("دخل ${stat.second} • مصروف ${stat.third.first} • صافي ${stat.third.second}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            NotebkSectionCard("تحليل القطع للسنة ${selectedYear ?: ""}") {
                val yearPieces = bundles.flatMap { it.pieces }.distinctBy { it.id }
                yearPieces.forEach { piece ->
                    val quantity = if (registrationMode == RegistrationMode.INDIVIDUAL) {
                        bundles.sumOf { b -> b.days.sumOf { day -> database.getIndividualEntries(b.month.id,day.date).sumOf { it.quantities[piece.id] ?: 0 } } }
                    } else bundles.sumOf { database.pieceTotal(it,piece.id) }
                    val value = if (registrationMode == RegistrationMode.INDIVIDUAL) {
                        bundles.sumOf { b -> b.days.sumOf { day -> database.getIndividualEntries(b.month.id,day.date).sumOf { (it.quantities[piece.id] ?: 0)*(it.unitPrices[piece.id] ?: 0) } } }
                    } else bundles.sumOf { database.pieceEarned(it,piece.id) }
                    Text("${piece.name}: الكمية ${quantity} • الإيراد ${value}", fontWeight = FontWeight.Medium)
                }
                if (yearPieces.isEmpty()) Text("لا توجد قطع مسجلة في الأشهر المختارة.")
            }
            NotebkSecondaryAction("تحديث التحليلات", onRefresh)
        }
    }
}

@Composable
private fun NotebkPrimaryAction(text: String, onClick: () -> Unit, enabled: Boolean) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Assessment, null); Spacer(Modifier.width(6.dp)); Text(text) }
}
