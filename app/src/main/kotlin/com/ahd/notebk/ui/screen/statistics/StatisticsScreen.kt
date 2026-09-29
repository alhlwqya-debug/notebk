package com.ahd.notebk.ui.screen.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.ui.theme.*

@Composable
fun StatisticsScreen(
    summary: LedgerSummary,
    workingDays: Int,
    currency: String = "ر.ي",
    onBack: () -> Unit,
    onPerformance: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإحصائيات") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().background(LightBackground).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text("الصافي النهائي", color = Color.White.copy(alpha = .8f))
                        Text("%.0f %s".format(summary.netBalance, currency), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("إجمالي الحركة المسجلة في الدفتر", color = Color.White.copy(alpha = .8f))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("الإنتاج", "%.0f %s".format(summary.totalCredit, currency), SecondaryGreen, Modifier.weight(1f))
                    SummaryCard("المصروف", "%.0f %s".format(summary.totalDebit, currency), ErrorRed, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("القطع", summary.totalPieces.toString(), PrimaryPurple, Modifier.weight(1f))
                    SummaryCard("أيام العمل", workingDays.toString(), PrimaryPurple, Modifier.weight(1f))
                }
            }
            item {
                Button(onClick = onPerformance, Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
                    Icon(Icons.Default.Analytics, null)
                    Spacer(Modifier.width(6.dp))
                    Text("مؤشرات الأداء")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, accent: Color, modifier: Modifier) {
    Card(modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = TextSecondary, fontSize = 13.sp)
            Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}
