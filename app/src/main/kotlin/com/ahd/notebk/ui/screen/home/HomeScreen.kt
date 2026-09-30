package com.ahd.notebk.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.domain.model.TailorRecord
import com.ahd.notebk.ui.components.BrandTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    settings: AppSettings,
    records: List<TailorRecord>,
    summary: LedgerSummary,
    onOpenNumeric: () -> Unit,
    onOpenIndividual: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenReports: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar", "YE"))
    val recent = records.sortedByDescending { it.timestamp }.take(5)
    val days = records.map { LedgerEngine.localDateKey(it.timestamp) }.distinct().size

    Scaffold(topBar = { BrandTopBar(title = "الرئيسية") }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "مرحباً " + settings.ownerName.ifBlank { settings.workerName }.ifBlank { "بك" } + " 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(dateFormat.format(Date()), style = MaterialTheme.typography.bodyMedium)
                        Text(settings.shopName, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeStat("الإنتاج", "%.0f %s".format(summary.totalCredit, settings.currencySymbol), Modifier.weight(1f))
                    HomeStat("المصروف", "%.0f %s".format(summary.totalDebit, settings.currencySymbol), Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeStat("الصافي", "%.0f %s".format(summary.netBalance, settings.currencySymbol), Modifier.weight(1f))
                    HomeStat("القطع", summary.totalPieces.toString(), Modifier.weight(1f))
                }
            }
            item { Text("إجراءات سريعة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeAction("التسجيل العددي", Icons.Default.MenuBook, onOpenNumeric, Modifier.weight(1f))
                    HomeAction("السجل الفردي", Icons.Default.People, onOpenIndividual, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeAction("الإحصائيات", Icons.Default.Analytics, onOpenStatistics, Modifier.weight(1f))
                    HomeAction("التقارير", Icons.Default.Description, onOpenReports, Modifier.weight(1f))
                }
            }
            item { Text("آخر النشاطات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (recent.isEmpty()) {
                item { Text("لا توجد سجلات حتى الآن.") }
            } else {
                items(recent, key = { it.id }) { record ->
                    ListItem(
                        leadingContent = { Icon(Icons.Default.CalendarMonth, null) },
                        headlineContent = { Text(record.personName.ifBlank { "سجل عام" }, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(record.itemQuantity.toString() + " قطعة • " + dateFormat.format(Date(record.timestamp))) },
                        trailingContent = {
                            Text("%.0f %s".format(if (record.credit > 0) record.credit else record.debit, settings.currencySymbol))
                        }
                    )
                    HorizontalDivider()
                }
            }
            item { Text("الأيام المسجلة: " + days, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun HomeStat(title: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier
) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(58.dp)) {
        Icon(icon, null)
        Spacer(Modifier.width(6.dp))
        Text(title)
    }
}
