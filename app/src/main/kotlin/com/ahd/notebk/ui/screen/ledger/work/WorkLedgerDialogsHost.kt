package com.ahd.notebk.ui.screen.ledger.work

import com.ahd.notebk.ui.components.*
import com.ahd.notebk.ui.navigation.DrawerContent
import com.ahd.notebk.ui.screen.individual.work.IndividualLedger
import com.ahd.notebk.ui.screen.home.HomeDashboard
import com.ahd.notebk.ui.screen.settings.ControlCenterScreen
import com.ahd.notebk.ui.screen.settings.ProfilePage
import com.ahd.notebk.ui.screen.settings.ShopSettingsPage
import com.ahd.notebk.ui.screen.settings.RegistrationSettingsPage
import com.ahd.notebk.ui.screen.settings.AppSettingsPage
import com.ahd.notebk.ui.screen.settings.SystemSettingsPage
import com.ahd.notebk.ui.screen.control.NotebkFeatureHub
import com.ahd.notebk.ui.screen.ledger.work.MonthsPage
import com.ahd.notebk.ui.screen.ledger.work.PiecesPage
import com.ahd.notebk.ui.screen.ledger.work.ReportsPage
import com.ahd.notebk.ui.screen.ledger.work.StatisticsPage
import com.ahd.notebk.notification.WorkReminderScheduler
import com.ahd.notebk.security.*
import com.ahd.notebk.security.createInternalAutoBackup
import com.ahd.notebk.data.report.*
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.*
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLedgerDialogsHost(
    context: Context,
    database: LedgerDatabase,
    prefs: android.content.SharedPreferences,
    currentBundle: MonthBundle?,
    currentShop: ShopRecord?,
    shops: List<ShopRecord>,
    selectedShopId: Long?,
    months: List<MonthRecord>,
    pieces: List<PieceRecord>,
    userName: String,
    userPhone: String,
    userEmail: String,
    userShop: String,
    userImagePath: String,
    registrationMode: RegistrationMode,
    reminderEnabled: Boolean,
    darkTheme: Boolean,
    showUserProfile: Boolean,
    showShopDialog: Boolean,
    showMonthDialog: Boolean,
    showPieceDialog: Boolean,
    showPieceManager: Boolean,
    showExpenseDialog: Boolean,
    showClearDialog: Boolean,
    showDeleteMonthDialog: Boolean,
    showDeleteShopDialog: Boolean,
    showRenameMonthDialog: Boolean,
    showCopyMonthDialog: Boolean,
    showStartDatePicker: Boolean,
    showPdfRangeDialog: Boolean,
    showShopSettings: Boolean,
    showAppSettings: Boolean,
    showSystemSettings: Boolean,
    shopSettingsTargetId: Long?,
    activeExpenseDay: DayRecord?,
    newShopName: String,
    newShopRegistrationNumber: String,
    newShopMode: RegistrationMode,
    newYear: String,
    newMonthNumber: String,
    newStartDate: String,
    newPieceName: String,
    newPiecePrice: String,
    expenseAmount: String,
    expenseNote: String,
    renamedMonth: String,
    copyYear: String,
    copyMonth: String,
    selectedMonthId: Long?,
    onShowUserProfile: (Boolean) -> Unit,
    onShowShopDialog: (Boolean) -> Unit,
    onShowMonthDialog: (Boolean) -> Unit,
    onShowPieceDialog: (Boolean) -> Unit,
    onShowPieceManager: (Boolean) -> Unit,
    onShowExpenseDialog: (Boolean) -> Unit,
    onShowClearDialog: (Boolean) -> Unit,
    onShowDeleteMonthDialog: (Boolean) -> Unit,
    onShowDeleteShopDialog: (Boolean) -> Unit,
    onShowRenameMonthDialog: (Boolean) -> Unit,
    onShowCopyMonthDialog: (Boolean) -> Unit,
    onShowStartDatePicker: (Boolean) -> Unit,
    onShowPdfRangeDialog: (Boolean) -> Unit,
    onShowShopSettings: (Boolean) -> Unit,
    onShowAppSettings: (Boolean) -> Unit,
    onShowSystemSettings: (Boolean) -> Unit,
    onActiveExpenseDay: (DayRecord?) -> Unit,
    onNewShopName: (String) -> Unit,
    onNewShopRegistrationNumber: (String) -> Unit,
    onNewShopMode: (RegistrationMode) -> Unit,
    onNewYear: (String) -> Unit,
    onNewMonthNumber: (String) -> Unit,
    onNewStartDate: (String) -> Unit,
    onNewPieceName: (String) -> Unit,
    onNewPiecePrice: (String) -> Unit,
    onExpenseAmount: (String) -> Unit,
    onExpenseNote: (String) -> Unit,
    onRenamedMonth: (String) -> Unit,
    onCopyYear: (String) -> Unit,
    onCopyMonth: (String) -> Unit,
    onSelectedMonthId: (Long?) -> Unit,
    onSelectedShopId: (Long?) -> Unit,
    onBundleChange: (MonthBundle?) -> Unit,
    onRegistrationModeChange: (RegistrationMode) -> Unit,
    onUserNameChange: (String) -> Unit,
    onUserPhoneChange: (String) -> Unit,
    onUserEmailChange: (String) -> Unit,
    onUserShopChange: (String) -> Unit,
    onUserImagePathChange: (String) -> Unit,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    onReminderChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onSecurity: () -> Unit,
    onSystemSettings: () -> Unit,
    onAccountAuth: () -> Unit,
    onHelp: () -> Unit,
    onBackAppSettings: () -> Unit,
    onBackShopSettings: () -> Unit,
    refreshAll: () -> Unit,
    reloadBundle: () -> Unit,
    reloadMonths: () -> Unit,
    reloadShops: () -> Unit,
    dataRevision: Int,
    onDataRevisionChange: () -> Unit,
    onRegistrationSettings: () -> Unit = {},
    onOpenAuth: () -> Unit = {}
) {
// ===== الحوارات =====

    // 1. صفحة الملف الشخصي
    if (showUserProfile) {
        ProfilePage(
            userName = userName,
            userPhone = userPhone,
            userEmail = userEmail,
            userShop = userShop,
            userImagePath = userImagePath,
            onUserNameChange = onUserNameChange,
            onUserPhoneChange = onUserPhoneChange,
            onUserEmailChange = onUserEmailChange,
            onUserShopChange = onUserShopChange,
            onSelectImage = { imagePickerLauncher.launch("image/*") },
            onSave = {
                when {
                    userName.trim().isBlank() -> {
                        Toast.makeText(context, "يرجى إدخال اسمك", Toast.LENGTH_SHORT).show()
                    }
                    userPhone.trim().isBlank() && userEmail.trim().isBlank() -> {
                        Toast.makeText(context, "أدخل رقم الهاتف أو البريد الإلكتروني", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        com.ahd.notebk.data.local.AccountDataScope.profilePreferences(context).edit()
                            .putString("user_name", userName.trim())
                            .putString("user_phone", userPhone.trim())
                            .putString("user_email", userEmail.trim())
                            .putString("user_shop", userShop.trim())
                            .putString("user_image", userImagePath)
                            .apply()
                        onShowUserProfile(false)
                        Toast.makeText(context, "تم حفظ البيانات الشخصية", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onAccountAuth = { onAccountAuth() },
            onBack = { onShowUserProfile(false) }
        )
    }

    // 2. حوار إضافة محل
    if (showShopDialog) {
        AlertDialog(
            onDismissRequest = { onShowShopDialog(false) },
            title = { Text("إضافة محل جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل اسم المحل الذي تعمل فيه", fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newShopName,
                        onValueChange = { onNewShopName(it) },
                        singleLine = true,
                        label = { Text("اسم المحل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newShopRegistrationNumber,
                        onValueChange = { onNewShopRegistrationNumber(it) },
                        singleLine = true,
                        label = { Text("رقم المحل / التسجيل *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("نوع الحساب لهذا المحل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { onNewShopMode(RegistrationMode.NUMERIC) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (newShopMode == RegistrationMode.NUMERIC) Blue.copy(alpha = 0.12f) else Color.Transparent,
                                contentColor = Blue
                            )
                        ) { Text("تسجيل عددي", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { onNewShopMode(RegistrationMode.INDIVIDUAL) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (newShopMode == RegistrationMode.INDIVIDUAL) Purple.copy(alpha = 0.12f) else Color.Transparent,
                                contentColor = Purple
                            )
                        ) { Text("تسجيل فردي", fontSize = 11.sp) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newShopName.trim()
                        val registrationNumber = newShopRegistrationNumber.trim()
                        if (name.isEmpty()) {
                            Toast.makeText(context, "اكتب اسم المحل أولاً", Toast.LENGTH_SHORT).show()
                        } else if (registrationNumber.isEmpty()) {
                            Toast.makeText(context, "أدخل رقم المحل أو رقم التسجيل", Toast.LENGTH_SHORT).show()
                        } else {
                            val id = database.addShop(name, newShopMode, registrationNumber)
                            if (id > 0L) {
                                onSelectedShopId(id)
                                onSelectedMonthId(null)
                                onBundleChange(null)
                                refreshAll()
                                onShowShopDialog(false)
                                Toast.makeText(context, "تم إضافة المحل", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { onShowShopDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 3. حوار إضافة شهر (مع التوليد التلقائي للتاريخ)
    if (showMonthDialog && selectedShopId != null) {
        AlertDialog(
            onDismissRequest = { onShowMonthDialog(false) },
            title = { Text("إضافة سجل شهر جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("سيتم إنشاء جميع أيام الشهر تلقائياً", fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newYear,
                        onValueChange = { onNewYear(it.filter(Char::isDigit)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("السنة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newMonthNumber,
                        onValueChange = { onNewMonthNumber(it.filter(Char::isDigit)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("رقم الشهر (1-12)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newStartDate,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("تاريخ البداية (تلقائي)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val year = newYear.toIntOrNull()
                        val month = newMonthNumber.toIntOrNull()
                        if (year == null || year !in 1900 .. 2500 || month == null || month !in 1 .. 12) {
                            Toast.makeText(context, "تحقق من السنة ورقم الشهر", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // ✅ توليد تاريخ البداية تلقائياً بناءً على الشهر
                        val startDate = String.format(Locale.ENGLISH, "%04d/%02d/01", year, month)

                        val id = database.addMonth(
                            shopId = selectedShopId!!,
                            year = year,
                            month = month,
                            name = "$year - ${monthName(month)}",
                            workerName = userName.ifBlank { "عامل" },
                            startDate = startDate, // ✅ استخدام التاريخ المولد تلقائياً
                            deductExpense = true,
                            workerId = null
                        )
                        if (id > 0L) {
                            onSelectedMonthId(id)
                            refreshAll()
                            onShowMonthDialog(false)
                            Toast.makeText(context, "تم إنشاء الشهر وتجهيز أيامه من 01/${monthName(month)}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) { Text("إنشاء") }
            },
            dismissButton = { TextButton(onClick = { onShowMonthDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 4. حوار إضافة قطعة
    if (showPieceDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { onShowPieceDialog(false) },
            title = { Text("إضافة قطعة جديدة", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPieceName,
                        onValueChange = { onNewPieceName(it) },
                        singleLine = true,
                        label = { Text("اسم القطعة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPiecePrice,
                        onValueChange = { onNewPiecePrice(it.filter(Char::isDigit)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("السعر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                        val name = newPieceName.trim()
                        val price = newPiecePrice.toIntOrNull() ?: 0
                        if (name.isEmpty()) {
                            Toast.makeText(context, "اكتب اسم القطعة", Toast.LENGTH_SHORT).show()
                        } else {
                            database.addPiece(currentBundle.month.shopId, name, price)
                            onNewPieceName("")
                            onNewPiecePrice("")
                            onShowPieceDialog(false)
                            reloadBundle()
                        }
                }) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { onShowPieceDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 5. حوار إدارة القطع (✅ جديد)
    if (showPieceManager && currentBundle != null) {
        PieceManagerDialog(
            pieces = pieces,
            onDismiss = { onShowPieceManager(false) },
            onAdd = {
                onShowPieceManager(false)
                onNewPieceName("")
                onNewPiecePrice("")
                onShowPieceDialog(true)
            },
            onUpdate = { piece, newName, newPrice ->
                database.updatePiece(piece.id, newName, newPrice)
                reloadBundle()
            },
            onDelete = { piece ->
                if (database.deletePiece(piece.id)) {
                    reloadBundle()
                    Toast.makeText(context, "تم حذف القطعة", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "لا يمكن حذف قطعة عليها كميات مسجلة", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // 6. حوار المصروف (مع عرض البيان)
    if (showExpenseDialog && activeExpenseDay != null && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { onShowExpenseDialog(false) },
            title = { Text("تعديل مصروف اليوم", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("التاريخ: ${activeExpenseDay!!.date}", fontSize = 12.sp, color = Purple)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { onExpenseAmount(it.filter(Char::isDigit)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("المبلغ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expenseNote,
                        onValueChange = { onExpenseNote(it) },
                        singleLine = true,
                        label = { Text("بيان المصروف") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ✅ عرض البيان الحالي إن وجد
                    if (activeExpenseDay!!.expenseNote.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "البيان الحالي: ${activeExpenseDay!!.expenseNote}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                        val day = activeExpenseDay!!
                        database.setExpense(
                            currentBundle.month.id,
                            day.date,
                            expenseAmount.toIntOrNull() ?: 0,
                            expenseNote
                        )
                        onShowExpenseDialog(false)
                        reloadBundle()
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { onShowExpenseDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 7. حوار مسح البيانات
    if (showClearDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { onShowClearDialog(false) },
            title = { Text("مسح بيانات الشهر", color = Red, fontWeight = FontWeight.Bold) },
            text = { Text("سيتم مسح الكميات والمصروفات فقط") },
            confirmButton = {
                Button(
                    onClick = {
                        createInternalAutoBackup(context)
                        database.clearMonthData(currentBundle.month.id)
                        onShowClearDialog(false)
                        reloadBundle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("مسح البيانات") }
            },
            dismissButton = { TextButton(onClick = { onShowClearDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 8. حوار حذف الشهر
    if (showDeleteMonthDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { onShowDeleteMonthDialog(false) },
            title = { Text("حذف الشهر", color = Red, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف ${currentBundle.month.name}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        createInternalAutoBackup(context)
                        database.deleteMonth(currentBundle.month.id)
                        onShowDeleteMonthDialog(false)
                        onSelectedMonthId(null)
                        reloadMonths()
                        reloadBundle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { onShowDeleteMonthDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 9. حوار حذف المحل
    if (showDeleteShopDialog && selectedShopId != null) {
        AlertDialog(
            onDismissRequest = { onShowDeleteShopDialog(false) },
            title = { Text("حذف المحل", color = Red, fontWeight = FontWeight.Bold) },
            text = { Text("سيتم حذف المحل وكل شهوره وعملياته") },
            confirmButton = {
                Button(
                    onClick = {
                        createInternalAutoBackup(context)
                        database.deleteShop(selectedShopId!!)
                        onShowDeleteShopDialog(false)
                        onSelectedShopId(null)
                        onSelectedMonthId(null)
                        onBundleChange(null)
                        refreshAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("حذف المحل") }
            },
            dismissButton = { TextButton(onClick = { onShowDeleteShopDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 10. حوار تعديل اسم الشهر
    if (showRenameMonthDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { onShowRenameMonthDialog(false) },
            title = { Text("تعديل اسم الشهر") },
            text = {
                OutlinedTextField(
                    value = renamedMonth,
                    onValueChange = { onRenamedMonth(it) },
                    singleLine = true,
                    label = { Text("اسم السجل") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                        if (renamedMonth.trim().isNotEmpty()) {
                            database.renameMonth(currentBundle.month.id, renamedMonth.trim())
                            onShowRenameMonthDialog(false)
                            reloadMonths()
                            reloadBundle()
                        }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { onShowRenameMonthDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 11. حوار نسخ الشهر
    if (showCopyMonthDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { onShowCopyMonthDialog(false) },
            title = { Text("نسخ الشهر") },
            text = {
                Column {
                    Text("سيتم إنشاء شهر جديد بنفس الإعدادات بدون الكميات", fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        copyYear,
                        { onCopyYear(it.filter(Char::isDigit)) },
                        singleLine = true,
                        label = { Text("السنة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(7.dp))
                    OutlinedTextField(
                        copyMonth,
                        { onCopyMonth(it.filter(Char::isDigit)) },
                        singleLine = true,
                        label = { Text("رقم الشهر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                        val year = copyYear.toIntOrNull()
                        val month = copyMonth.toIntOrNull()
                        if (year == null || month == null || month !in 1 .. 12) {
                            Toast.makeText(context, "تحقق من السنة ورقم الشهر", Toast.LENGTH_SHORT).show()
                        } else {
                            val id = database.copyMonth(
                                currentBundle.month.id,
                                "$year - ${monthName(month)}",
                                year,
                                month
                            )
                            if (id > 0L) {
                                onSelectedMonthId(id)
                                onShowCopyMonthDialog(false)
                                reloadMonths()
                                reloadBundle()
                            }
                        }
                }) { Text("نسخ") }
            },
            dismissButton = { TextButton(onClick = { onShowCopyMonthDialog(false) }) { Text("إلغاء") } }
        )
    }

    // 12. منتقي التاريخ
    if (showStartDatePicker) {
        val millis = remember(newStartDate) {
            try {
                SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(newStartDate)?.time
            } catch (_: Exception) {
                null
            }
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { onShowStartDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            val calendar = Calendar.getInstance().apply { timeInMillis = it }
                            onNewStartDate(SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(calendar.time))
                        }
                        onShowStartDatePicker(false)
                }) { Text("موافق") }
            },
            dismissButton = { TextButton(onClick = { onShowStartDatePicker(false) }) { Text("إلغاء") } }
        ) { DatePicker(state) }
    }

    // ===== اختيار نطاق تقرير PDF =====
    if (showPdfRangeDialog && currentBundle != null) {
        PdfReportRangeDialog(
            mode = registrationMode,
            bundle = currentBundle,
            database = database,
            onDismiss = { onShowPdfRangeDialog(false) },
            onPrint = { range, selectedDate, selectedMonthId, sharePdf ->
                onShowPdfRangeDialog(false)
                val allBundles = months.mapNotNull { database.loadMonthBundle(it.id) }
                if (registrationMode == RegistrationMode.NUMERIC) {
                    printNumericReportRange(
                        context = context,
                        database = database,
                        bundles = allBundles,
                        range = range,
                        selectedDate = selectedDate,
                        selectedMonthId = selectedMonthId,
                        userName = userName,
                        userPhone = userPhone,
                        userEmail = userEmail,
                        userShop = userShop,
                        userImagePath = userImagePath,
                        sharePdf = sharePdf
                    )
                } else {
                    printIndividualReportRange(
                        context = context,
                        database = database,
                        bundles = allBundles,
                        range = range,
                        selectedDate = selectedDate,
                        selectedMonthId = selectedMonthId,
                        pieces = pieces,
                        userName = userName,
                        userPhone = userPhone,
                        userEmail = userEmail,
                        userShop = userShop,
                        userImagePath = userImagePath,
                        sharePdf = sharePdf
                    )
                }
            }
        )
    }

    if (showShopSettings && shopSettingsTargetId != null) {
        val targetShop = shops.firstOrNull { it.id == shopSettingsTargetId }
        if (targetShop != null) {
            ShopSettingsPage(
                shop = targetShop,
                onBack = { onShowShopSettings(false) },
                onSave = { name, registrationNumber, mode ->
                    database.updateShop(targetShop.id, name)
                    database.updateShopRegistrationNumber(targetShop.id, registrationNumber)
                    database.updateShopRegistrationMode(targetShop.id, mode)
                    reloadShops()
                    if (selectedShopId == targetShop.id) onRegistrationModeChange(mode)
                    onShowShopSettings(false)
                    onDataRevisionChange()
                    Toast.makeText(context, "تم حفظ إعدادات المحل", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showAppSettings) {
        AppSettingsPage(
            reminderEnabled = reminderEnabled,
            darkTheme = darkTheme,
            onReminderChange = { enabled ->
                prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
                if (enabled) WorkReminderScheduler.schedule(context) else WorkReminderScheduler.cancel(context)
                onReminderChange(enabled)
            },
            onDarkThemeChange = { enabled ->
                onDarkThemeChange(enabled)
                prefs.edit().putBoolean("dark_theme", enabled).putBoolean("theme_mode_dark", enabled).apply()
            },
            onSecurity = { onShowAppSettings(false); onSecurity() },
            onSystemSettings = { onShowAppSettings(false); onShowSystemSettings(true) },
            onAccountAuth = { onShowAppSettings(false); onAccountAuth() },
            onHelp = { onShowAppSettings(false); onHelp() },
            onBack = { onShowAppSettings(false) }
        )
    }

    if (showSystemSettings) {
        val openAppSettings = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            })
        }
        SystemSettingsPage(
            onOpenAppSettings = openAppSettings,
            onOpenNotifications = {
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                })
            },
            onOpenDisplay = { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) },
            onOpenLanguage = {
                context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            },
            onOpenBattery = {
                context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
            },
            onBack = { onShowSystemSettings(false) }
        )
    }

    
}
