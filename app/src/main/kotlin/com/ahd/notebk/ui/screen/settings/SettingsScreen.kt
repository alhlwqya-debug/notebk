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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ExperimentalMaterial3Api
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, ledgerViewModel: LedgerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState()
    var shopName by remember(settings) { mutableStateOf(settings.shopName) }
    var ownerName by remember(settings) { mutableStateOf(settings.ownerName) }
    var shopNumber by remember(settings) { mutableStateOf(settings.shopNumber) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var workerName by remember(settings) { mutableStateOf(settings.workerName) }
    var price by remember(settings) { mutableStateOf(settings.defaultPiecePrice.toString()) }
    var currency by remember(settings) { mutableStateOf(settings.currencySymbol) }
    var showMoney by remember(settings) { mutableStateOf(settings.showDebitCredit) }
    var autoToday by remember(settings) { mutableStateOf(settings.autoFillToday) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(ledgerViewModel.exportBackup().toByteArray(Charsets.UTF_8)) }
        }.also { Toast.makeText(context, "تم حفظ النسخة الاحتياطية", Toast.LENGTH_SHORT).show() }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }?.let(ledgerViewModel::importBackup)
        }.also { Toast.makeText(context, "تم استيراد النسخة", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("الملف الشخصي والإعدادات", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0F172A),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("الملف الشخصي", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            OutlinedTextField(ownerName, { ownerName = it }, Modifier.fillMaxWidth(), label = { Text("اسم صاحب الحساب") }, singleLine = true)
            OutlinedTextField(shopName, { shopName = it }, Modifier.fillMaxWidth(), label = { Text("اسم الورشة / المحل") }, singleLine = true)
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(shopNumber, { shopNumber = it }, Modifier.weight(1f), label = { Text("رقم المحل") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, Modifier.weight(1f), label = { Text("الهاتف") }, singleLine = true)
            }
            OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), label = { Text("العنوان") }, singleLine = true)
            OutlinedTextField(workerName, { workerName = it }, Modifier.fillMaxWidth(), label = { Text("اسم العامل") }, singleLine = true)
            Divider()
            Text("إعدادات الحساب", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(price, { price = it }, Modifier.weight(1f), label = { Text("سعر القطعة") }, singleLine = true)
                OutlinedTextField(currency, { currency = it }, Modifier.weight(.7f), label = { Text("العملة") }, singleLine = true)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("إظهار له / عليه / الرصيد"); Switch(showMoney, { showMoney = it }) }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("تعبئة اليوم تلقائياً"); Switch(autoToday, { autoToday = it }) }
            Button(onClick = {
                settingsViewModel.save(AppSettings(shopName.trim().ifBlank { "ورشة الخياطة الرقمية" }, ownerName.trim(), shopNumber.trim(), phone.trim(), address.trim(), workerName.trim(), price.toDoubleOrNull() ?: 2000.0, currency.trim().ifBlank { "ر.ي" }, showMoney, autoToday))
                Toast.makeText(context, "تم حفظ الملف الشخصي والإعدادات", Toast.LENGTH_SHORT).show()
            }, Modifier.fillMaxWidth()) { Text("حفظ التغييرات") }
            Divider()
            Text("النسخ الاحتياطي", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { exportLauncher.launch("notebk-backup.json") }, Modifier.fillMaxWidth()) { Text("تصدير JSON") }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }, Modifier.fillMaxWidth()) { Text("استرجاع JSON") }
            Spacer(Modifier.padding(8.dp))
        }
    }
}
