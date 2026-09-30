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
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.ui.theme.PrimaryPurple

@Composable
fun LedgerTopBar(
    settings: AppSettings,
    summary: LedgerSummary,
    shopName: String,
    onOpenStatistics: () -> Unit,
    onOpenIndividualLedger: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(color = PrimaryPurple, contentColor = Color.White) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = "شعار التطبيق",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("التسجيل العددي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                val subtitle = if (settings.workerName.isBlank()) {
                    shopName.ifBlank { settings.shopName }
                } else {
                    shopName.ifBlank { settings.shopName } + " • " + settings.workerName
                }
                Text(subtitle, fontSize = 11.sp)
                Text("الإنتاج: " + summary.totalPieces + " قطعة", fontSize = 10.sp)
            }
            Row {
                IconButton(onClick = onOpenStatistics) { Icon(Icons.Default.Analytics, "الإحصائيات") }
                IconButton(onClick = onOpenIndividualLedger) { Icon(Icons.Default.Person, "التسجيل الفردي") }
                IconButton(onClick = onOpenReport) { Icon(Icons.Default.PictureAsPdf, "PDF") }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, "الإعدادات") }
            }
        }
    }
}
