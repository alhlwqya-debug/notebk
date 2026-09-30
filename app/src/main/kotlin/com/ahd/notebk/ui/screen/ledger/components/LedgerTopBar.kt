package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.R
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.ui.theme.PrimaryPurple

@Composable
fun LedgerTopBar(
    settings: AppSettings,
    summary: LedgerSummary,
    onOpenStatistics: () -> Unit,
    onOpenIndividualLedger: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(color = PrimaryPurple, contentColor = Color.White) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = "شعار التطبيق",
                modifier = Modifier.size(52.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(settings.shopName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("دفتر الحسابات • ${summary.totalPieces} قطعة", fontSize = 11.sp)
            }
            Row {
                IconButton(onClick = onOpenStatistics) { Icon(Icons.Default.Analytics, "الإحصائيات") }
                IconButton(onClick = onOpenIndividualLedger) { Icon(Icons.Default.Person, "السجل الفردي") }
                IconButton(onClick = onOpenReport) { Icon(Icons.Default.PictureAsPdf, "التقرير") }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, "الإعدادات") }
            }
        }
    }
}
