package com.ahd.notebk.ui.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, ledgerViewModel: LedgerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState()
    var shopName by remember(settings) { mutableStateOf(settings.shopName) }
    var price by remember(settings) { mutableStateOf(settings.defaultPiecePrice.toString()) }
    var currency by remember(settings) { mutableStateOf(settings.currencySymbol) }
    var showMoney by remember(settings) { mutableStateOf(settings.showDebitCredit) }
    var autoToday by remember(settings) { mutableStateOf(settings.autoFillToday) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(ledgerViewModel.exportBackup().toByteArray(Charsets.UTF_8)) }
            Toast.makeText(context, "تم حفظ النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!json.isNullOrBlank()) ledgerViewModel.importBackup(json)
            Toast.makeText(context, "تم استيراد النسخة", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("إعدادات دفتر الخياط", fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A), titleContentColor = Color.White, navigationIconContentColor = Color.White))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("بيانات الورشة", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            OutlinedTextField(shopName, { shopName = it }, Modifier.fillMaxWidth(), label = { Text("اسم الورشة") }, singleLine = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(price, { price = it }, Modifier.weight(1f), label = { Text("سعر القطعة الافتراضي") }, singleLine = true)
                OutlinedTextField(currency, { currency = it }, Modifier.weight(.7f), label = { Text("العملة") }, singleLine = true)
            }
            Divider()
            Text("التحكم في العرض", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("إظهار له / عليه / الرصيد"); Switch(showMoney, { showMoney = it }) }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("تعبئة اليوم تلقائياً"); Switch(autoToday, { autoToday = it }) }
            Button(onClick = {
                settingsViewModel.save(AppSettings(shopName.trim().ifBlank { "ورشة الخياطة الرقمية" }, price.toDoubleOrNull() ?: 2000.0, currency.trim().ifBlank { "ر.ي" }, showMoney, autoToday))
                Toast.makeText(context, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
            }, Modifier.fillMaxWidth()) { Text("حفظ التغييرات") }
            Divider()
            Text("النسخ الاحتياطي والاسترجاع", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { exportLauncher.launch("notebk-backup.json") }, Modifier.fillMaxWidth()) { Text("تصدير JSON") }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }, Modifier.fillMaxWidth()) { Text("استرجاع JSON") }
            Spacer(Modifier.padding(8.dp))
        }
    }
}
