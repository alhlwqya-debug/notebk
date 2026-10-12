package com.ahd.notebk.ui.screen.ledger.work
import com.ahd.notebk.ui.components.*
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.data.local.NotebkSettingsStore
import com.ahd.notebk.domain.model.*


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// استيراد الأيقونات
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person

// استيراد Coil لعرض الصور

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


// ================ الجدول الرئيسي ================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainLedger(
    database: LedgerDatabase,
    bundle: MonthBundle,
    shops: List<ShopRecord>,
    pieces: List<PieceRecord>,
    selectedShopId: Long?,
    selectedMonthId: Long?,
    months: List<MonthRecord>,
    userName: String,
    shopMenuOpen: Boolean,
    onMenu: () -> Unit,
    onShopMenu: () -> Unit,
    onSelectShop: (Long) -> Unit,
    onSelectMonth: (Long) -> Unit,
    onAddMonth: () -> Unit,
    onAddPiece: () -> Unit,
    onManagePieces: () -> Unit, // ✅ جديد
    onEditStartDate: () -> Unit,
    onClear: () -> Unit,
    onDeleteMonth: () -> Unit,
    onDeleteShop: () -> Unit,
    onRenameMonth: () -> Unit,
    onCopyMonth: () -> Unit,
    openDailyWork: Boolean,
    onDailyWorkOpened: () -> Unit,
    onEditQuantity: (DayRecord, PieceRecord, String) -> Unit,
    onEditExpense: (DayRecord, String) -> Unit,
    onOpenExpense: (DayRecord) -> Unit,
    onSaveDailyWork: (DayRecord, Map<Long, Int>, Int) -> Unit,
    onPrint: () -> Unit
) {
    val context = LocalContext.current
    val horizontal = rememberScrollState()
    val settingsStore = remember(context) { NotebkSettingsStore(context) }
    val settingsPrefs = remember(context) { context.applicationContext.getSharedPreferences("notebk_settings", Context.MODE_PRIVATE) }
    var hideAmounts by remember { mutableStateOf(settingsStore.getBoolean("hide_amounts", false)) }
    var showTotalRow by remember { mutableStateOf(settingsStore.getBoolean("show_total_row", true)) }
    var showFab by remember { mutableStateOf(settingsStore.getBoolean("show_fab", true)) }
    DisposableEffect(settingsStore) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "hide_amounts" -> hideAmounts = settingsStore.getBoolean("hide_amounts", false)
                "show_total_row" -> showTotalRow = settingsStore.getBoolean("show_total_row", true)
                "show_fab" -> showFab = settingsStore.getBoolean("show_fab", true)
            }
        }
        settingsPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { settingsPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val totalEarned = database.calculateMonthEarned(bundle)
    val totalExpenses = database.calculateMonthExpenses(bundle)
    val net = database.calculateMonthNet(bundle)
    val totalPieces = bundle.days.sumOf { it.quantities.values.sum() }
    val shopName = shops.firstOrNull { it.id == selectedShopId }?.name ?: ""

    var showDailyWorkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(openDailyWork) {
        if (openDailyWork) {
            showDailyWorkDialog = true
            onDailyWorkOpened()
        }
    }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp)) {
        AppTopBar(
            title = shopName.ifBlank { "الدفتر" },
            subtitle = "${NotebkBrand.APP_NAME} • السجلات",
            onMenu = onMenu,
            trailingContent = {
                Box {
                    OutlinedButton(
                        onClick = onShopMenu,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(shopName.ifBlank { "اختر محل" }, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = shopMenuOpen,
                        onDismissRequest = { selectedShopId?.let { onSelectShop(it) } }
                    ) {
                        shops.forEach { shop ->
                            DropdownMenuItem(
                                text = { Text(shop.name) },
                                onClick = { onSelectShop(shop.id) }
                            )
                        }
                    }
                }
            }
        )

        Row(
            Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            months.forEach { month ->
                Button(
                    onClick = { onSelectMonth(month.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (month.id == selectedMonthId) Purple else AppPrimarySoft,
                        contentColor = if (month.id == selectedMonthId) Color.White else AppText
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(month.name, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }


        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SummaryCard("الإنتاج", if (hideAmounts) "••••" else totalEarned.toString(), CardWorkBg, Green, Modifier.weight(1f))
            SummaryCard("المصروف", if (hideAmounts) "••••" else totalExpenses.toString(), CardExpBg, Red, Modifier.weight(1f))
            SummaryCard("الصافي", if (hideAmounts) "••••" else net.toString(), CardNetBg, Blue, Modifier.weight(1f))
            SummaryCard("القطع", totalPieces.toString(), Color(0xFFFFF3E0), Orange, Modifier.weight(1f))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onAddMonth,
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("+ شهر", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onAddPiece,
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("+ قطعة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            // ✅ زر إدارة القطع (جديد)
            Button(
                onClick = onManagePieces,
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("إدارة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onPrint,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("🖨️ PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = onClear,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("مسح", color = Red, fontSize = 9.sp)
            }
        }

        Row(
            Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = onRenameMonth,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text(bundle.month.name, fontSize = 9.sp)
            }
            OutlinedButton(
                onClick = onEditStartDate,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("بداية: ${bundle.month.startDate}", fontSize = 9.sp)
            }
            OutlinedButton(
                onClick = onCopyMonth,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("نسخ", fontSize = 9.sp)
            }
            OutlinedButton(
                onClick = onDeleteMonth,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("🗑️", color = Red, fontSize = 9.sp)
            }
        }

        Box(
            Modifier
            .fillMaxWidth()
            .weight(1f)
            .horizontalScroll(horizontal)
        ) {
            val tableWidth = wDay + wDate + (wPiece * pieces.size) + wExpense + wTotal
            Column(Modifier.width(tableWidth)) {
                Row(Modifier.background(Purple)) {
                    CellText("اليوم", wDay, 32.dp, Purple, bold = true, size = 10, color = Color.White)
                    CellText("التاريخ", wDate, 32.dp, Purple, bold = true, size = 10, color = Color.White)
                    pieces.forEach {
                        CellText(it.name, wPiece, 32.dp, Purple, bold = true, size = 9, color = Color.White)
                    }
                    CellText("المصروف", wExpense, 32.dp, Purple, bold = true, size = 10, color = Color.White)
                    CellText("المجموع", wTotal, 32.dp, Purple, bold = true, size = 10, color = Color.White)
                }

                LazyColumn(Modifier.fillMaxWidth()) {
                    items(bundle.days, key = { it.id }) { day ->
                        val earned = database.calculateDayEarned(day)
                        val dayTotal = if (bundle.month.deductExpense) earned - day.expense else earned
                        Row {
                            CellText(day.dayName, wDay, rowH, size = 10)
                            CellText(day.date, wDate, rowH, size = 10)
                            pieces.forEach { piece ->
                                val quantity = day.quantities[piece.id] ?: 0
                                CellEdit(
                                    value = if (quantity == 0) "" else quantity.toString(),
                                    onValueChange = { value -> onEditQuantity(day, piece, value) },
                                    width = wPiece,
                                    height = rowH,
                                    number = true
                                )
                            }
                            Box(
                                Modifier
                                .width(wExpense)
                                .height(rowH)
                                .border(1.dp, Color.Black)
                                .background(Color.White)
                                .clickable { onOpenExpense(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (hideAmounts) "••••" else if (day.expense == 0) "" else day.expense.toString(),
                                    fontSize = 10.sp
                                )
                            }
                            CellText(
                                if (hideAmounts) "••••" else if (earned == 0) "" else dayTotal.toString(),
                                wTotal,
                                rowH,
                                LightBlue,
                                bold = true
                            )
                        }
                    }
                }

                if (showTotalRow) Row {
                    CellText("الإجمالي", wDay + wDate, 34.dp, Color(0xFFFFE58F), bold = true, size = 10, color = AppText)

                    // ✅ عرض مجموع كل قطعة على حدة وليس الإجمالي الكلي
                    pieces.forEach { piece ->
                        val count = database.pieceTotal(bundle, piece.id)
                        CellText(
                            if (count == 0) "" else count.toString(),
                            wPiece,
                            34.dp,
                            Color(0xFFFFE58F),
                            bold = true,
                            color = AppText
                        )
                    }

                    CellText(
                        if (hideAmounts) "••••" else if (totalExpenses == 0) "" else totalExpenses.toString(),
                        wExpense,
                        34.dp,
                        Color(0xFFFFE58F),
                        bold = true,
                        color = Color(0xFFC62828)
                    )
                    CellText(if (hideAmounts) "••••" else net.toString(), wTotal, 34.dp, HeaderBlue, bold = true)
                }
            }
        }

        Column(
            Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F2F5))
            .border(1.dp, Color.LightGray)
            .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "جميع الحقوق محفوظة © 2026",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "تواصل: alhlwqya@gmail.com",
                    fontSize = 8.sp,
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        try {
                            androidx.core.content.ContextCompat.startActivity(
                                context,
                                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:alhlwqya@gmail.com")),
                                null
                            )
                        } catch (_: Exception) { }
                    }
                )
            }
        }
        }
        if (showFab) {
        FloatingActionButton(
            onClick = { showDailyWorkDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 54.dp)
                .size(58.dp),
            containerColor = Green,
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "تسجيل العمل اليومي",
                modifier = Modifier.size(30.dp)
            )
        }
        }
    }

    if (showDailyWorkDialog) {
        DailyWorkEntryDialog(
            days = bundle.days,
            pieces = pieces,
            onDismiss = { showDailyWorkDialog = false },
            onSave = { day, quantities, expense ->
                onSaveDailyWork(day, quantities, expense)
                showDailyWorkDialog = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyWorkEntryDialog(
    days: List<DayRecord>,
    pieces: List<PieceRecord>,
    onDismiss: () -> Unit,
    onSave: (DayRecord, Map<Long, Int>, Int) -> Unit
) {
    if (days.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تسجيل العمل اليومي", fontWeight = FontWeight.Bold) },
            text = { Text("لا توجد أيام متاحة في هذا السجل.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }

    val today = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date())
    var selectedDay by remember(days) {
        mutableStateOf(days.firstOrNull { it.date == today } ?: days.first())
    }
    var dayMenuOpen by remember { mutableStateOf(false) }
    var quantities by remember(selectedDay.id, pieces) {
        mutableStateOf(
            pieces.associate { piece ->
                piece.id to (selectedDay.quantities[piece.id] ?: 0)
            }
        )
    }
    var expense by remember(selectedDay.id) {
        mutableStateOf(if (selectedDay.expense == 0) "" else selectedDay.expense.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("تسجيل العمل اليومي", fontWeight = FontWeight.Bold)
                Text(
                    "أدخل العمل من هنا أو عدّل الخلايا مباشرة في الجدول.",
                    fontSize = 11.sp,
                    color = AppMuted,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { dayMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            "اليوم: ${selectedDay.dayName} — ${selectedDay.date}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                    DropdownMenu(
                        expanded = dayMenuOpen,
                        onDismissRequest = { dayMenuOpen = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = {
                                    Text("${day.dayName} — ${day.date}", fontSize = 12.sp)
                                },
                                onClick = {
                                    selectedDay = day
                                    dayMenuOpen = false
                                }
                            )
                        }
                    }
                }

                if (pieces.isEmpty()) {
                    Text(
                        "لا توجد قطع مسجلة. أضف القطع أولاً من زر «+ قطعة»."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(pieces, key = { it.id }) { piece ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    piece.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = quantities[piece.id]?.takeIf { it > 0 }?.toString() ?: "",
                                    onValueChange = { value ->
                                        quantities = quantities.toMutableMap().apply {
                                            this[piece.id] = value.filter(Char::isDigit).toIntOrNull() ?: 0
                                        }
                                    },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    label = { Text("الكمية", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = expense,
                    onValueChange = { expense = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("مصروف اليوم (اختياري)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedDay,
                        quantities,
                        expense.toIntOrNull() ?: 0
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("حفظ العمل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
