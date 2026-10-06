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


// ================ الشاشة الرئيسية ================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLedgerScreen(onOpenAuth: () -> Unit = {}) {
    val context = LocalContext.current
    val database = remember { LedgerDatabase(context.applicationContext) }
    DisposableEffect(database) {
        onDispose { database.close() }
    }
    val prefs = remember {
        context.getSharedPreferences("add_paper_user", Context.MODE_PRIVATE)
    }

    // ===== حالة البيانات =====
    var shops by remember { mutableStateOf(database.getShops()) }
    var selectedShopId by remember { mutableStateOf(shops.firstOrNull()?.id) }
    var months by remember { mutableStateOf(selectedShopId?.let(database::getMonths) ?: emptyList()) }
    var selectedMonthId by remember { mutableStateOf(months.firstOrNull()?.id) }
    var bundle by remember { mutableStateOf(selectedMonthId?.let(database::loadMonthBundle)) }
    var pieces by remember { mutableStateOf(selectedShopId?.let(database::getPieces) ?: emptyList()) }

    // ===== حالة الملف الشخصي =====
    var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var userPhone by remember { mutableStateOf(prefs.getString("user_phone", "") ?: "") }
    var userEmail by remember { mutableStateOf(prefs.getString("user_email", "") ?: "") }
    var userShop by remember { mutableStateOf(prefs.getString("user_shop", "") ?: "") }
    var userImagePath by remember { mutableStateOf(prefs.getString("user_image", "") ?: "") }

    // ===== حالة واجهة المستخدم =====
    val centralSettings = remember { com.ahd.notebk.data.local.NotebkSettingsStore(context) }
    val initialPage = remember { centralSettings.getString("default_page", "home") }
    var drawerOpen by remember { mutableStateOf(false) }
    var showDashboard by remember { mutableStateOf(initialPage == "home") }
    var dataRevision by remember { mutableStateOf(0) }
    var shopMenuOpen by remember { mutableStateOf(false) }
    var showShopDialog by remember { mutableStateOf(false) }
    var showMonthDialog by remember { mutableStateOf(false) }
    var showPieceDialog by remember { mutableStateOf(false) }
    var showPieceManager by remember { mutableStateOf(false) } // ✅ جديد
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showUserProfile by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    var showMonthsPage by remember { mutableStateOf(false) }
    var showPiecesPage by remember { mutableStateOf(false) }
    var showReportsPage by remember { mutableStateOf(false) }
    var showStatisticsPage by remember { mutableStateOf(false) }
    var editPieceTarget by remember { mutableStateOf<PieceRecord?>(null) }
    var editPieceName by remember { mutableStateOf("") }
    var editPiecePrice by remember { mutableStateOf("") }
    var showAbout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showRegistrationSettings by remember { mutableStateOf(false) }
    var showShopSettings by remember { mutableStateOf(false) }
    var showAppSettings by remember { mutableStateOf(false) }
    var showSystemSettings by remember { mutableStateOf(false) }
    var showControlCenter by remember { mutableStateOf(false) }
    var showFeatureHub by remember { mutableStateOf(false) }
    var shopSettingsTargetId by remember { mutableStateOf<Long?>(null) }
    var openDailyWorkRequested by remember { mutableStateOf(initialPage == "daily") }
    var reminderEnabled by remember { mutableStateOf(prefs.getBoolean("daily_reminder_enabled", false)) }
    var darkTheme by remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteMonthDialog by remember { mutableStateOf(false) }
    var showDeleteShopDialog by remember { mutableStateOf(false) }
    var showRenameMonthDialog by remember { mutableStateOf(false) }
    var showCopyMonthDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showPdfRangeDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var appPinEnabled by remember { mutableStateOf(hasAppPin(context)) }
    var registrationMode by remember(selectedShopId, shops) {
        mutableStateOf(
            shops.firstOrNull { it.id == selectedShopId }?.registrationMode
                ?: RegistrationMode.NUMERIC
        )
    }

    // ===== حالة الإدخال =====
    var newShopName by remember { mutableStateOf("") }
    var newShopRegistrationNumber by remember { mutableStateOf("") }
    var newShopMode by remember { mutableStateOf(RegistrationMode.NUMERIC) }
    var newYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var newMonthNumber by remember { mutableStateOf((Calendar.getInstance().get(Calendar.MONTH) + 1).toString()) }
    var newStartDate by remember { mutableStateOf(todayString()) }
    var newPieceName by remember { mutableStateOf("") }
    var newPiecePrice by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseNote by remember { mutableStateOf("") }
    var activeExpenseDay by remember { mutableStateOf<DayRecord?>(null) }
    var renamedMonth by remember { mutableStateOf("") }
    var copyYear by remember { mutableStateOf("") }
    var copyMonth by remember { mutableStateOf("") }

    // ===== اختيار الصورة من المعرض =====
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val imageFile = saveImageToInternalStorage(context, uri)
                userImagePath = imageFile.absolutePath
                prefs.edit().putString("user_image", imageFile.absolutePath).apply()
                Toast.makeText(context, "تم حفظ الصورة", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر حفظ الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===== دوال التحديث =====
    fun reloadShops() {
        shops = database.getShops()
        if (shops.none { it.id == selectedShopId }) selectedShopId = shops.firstOrNull()?.id
        registrationMode = shops.firstOrNull { it.id == selectedShopId }?.registrationMode
            ?: RegistrationMode.NUMERIC
    }

    fun reloadMonths() {
        months = selectedShopId?.let(database::getMonths) ?: emptyList()
        if (months.none { it.id == selectedMonthId }) selectedMonthId = months.firstOrNull()?.id
    }

    fun reloadBundle() {
        bundle = selectedMonthId?.let(database::loadMonthBundle)
        pieces = selectedShopId?.let(database::getPieces) ?: emptyList()
    }

    fun refreshAll() {
        reloadShops()
        reloadMonths()
        reloadBundle()
        dataRevision++
    }

    val backupCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, if (exportBackup(context, uri)) "تم إنشاء النسخة الاحتياطية بنجاح" else "تعذر إنشاء النسخة الاحتياطية", Toast.LENGTH_LONG).show()
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            database.close()
            val ok = restoreBackup(context, uri)
            Toast.makeText(context, if (ok) "تمت الاستعادة. يفضل إعادة فتح التطبيق." else "تعذر استعادة النسخة الاحتياطية", Toast.LENGTH_LONG).show()
            if (ok) refreshAll()
        }
    }

    // ===== تأثيرات =====
    LaunchedEffect(selectedShopId) {
        registrationMode = shops.firstOrNull { it.id == selectedShopId }?.registrationMode
            ?: RegistrationMode.NUMERIC
        months = selectedShopId?.let(database::getMonths) ?: emptyList()
        selectedMonthId = months.firstOrNull()?.id
        bundle = selectedMonthId?.let(database::loadMonthBundle)
        pieces = selectedShopId?.let(database::getPieces) ?: emptyList()
    }

    LaunchedEffect(selectedMonthId) {
        bundle = selectedMonthId?.let(database::loadMonthBundle)
    }

    // ✅ تأثير لتحديث تاريخ البداية تلقائياً عند تغيير السنة أو الشهر
    LaunchedEffect(newYear, newMonthNumber) {
        val year = newYear.toIntOrNull()
        val month = newMonthNumber.toIntOrNull()
        if (year != null && month != null && month in 1 .. 12) {
            newStartDate = String.format(Locale.ENGLISH, "%04d/%02d/01", year, month)
        }
    }

    // ===== فتح الملف الشخصي إذا كان الاسم فارغاً =====
    LaunchedEffect(Unit) {
        // ترحيل إعداد النوع القديم مرة واحدة إلى كل محل موجود، ثم يصبح النوع مستقلاً لكل محل.
        if (!prefs.getBoolean("shop_modes_migrated_v4", false)) {
            val oldMode = runCatching {
                RegistrationMode.valueOf(
                    prefs.getString("registration_mode", RegistrationMode.NUMERIC.name)
                        ?: RegistrationMode.NUMERIC.name
                )
            }.getOrDefault(RegistrationMode.NUMERIC)
            database.getShops().forEach { shop ->
                database.updateShopRegistrationMode(shop.id, oldMode)
            }
            prefs.edit().putBoolean("shop_modes_migrated_v4", true).remove("registration_mode").apply()
            refreshAll()
        }
        // عند تشغيل التطبيق لا نفتح أي نافذة منبثقة تلقائياً.
        // الشاشة الرئيسية هي نقطة الدخول دائماً، ويمكن للمستخدم فتح
        // الملف الشخصي أو المساعدة أو إضافة محل من القائمة/الأزرار عند الحاجة.
        database.optimizeDatabase()
    }

    val currentBundle = bundle
    val currentShop = shops.firstOrNull { it.id == selectedShopId }

    // ===== الواجهة =====
    Box(
        modifier = Modifier
        .fillMaxSize()
        .background(PageBg)
    ) {
        if (showDashboard) {
            HomeDashboard(
                database = database,
                shops = shops,
                selectedShopId = selectedShopId,
                userName = userName,
                refreshKey = dataRevision,
                onMenu = { drawerOpen = true },
                onOpenLedger = {
                    showDashboard = false
                    openDailyWorkRequested = false
                    dataRevision++
                },
                onOpenDailyWork = {
                    showDashboard = false
                    openDailyWorkRequested = true
                    dataRevision++
                },
                onAddShop = {
                    newShopName = ""
                    newShopRegistrationNumber = ""
                    newShopMode = RegistrationMode.NUMERIC
                    showShopDialog = true
                },
                onShopSettings = {
                    shopSettingsTargetId = selectedShopId
                    if (selectedShopId != null) showShopSettings = true
                },
                onUserProfile = { showUserProfile = true },
                onAppSettings = { showFeatureHub = true },
                onHelp = { showHelp = true },
                onStatistics = { showStatistics = true }
            )
        } else if (shops.isEmpty()) {
            StartScreen(
                onAddShop = {
                    newShopName = ""
                    newShopRegistrationNumber = ""
                    newShopMode = RegistrationMode.NUMERIC
                    showShopDialog = true
                },
                onMenu = { drawerOpen = true }
            )
        } else if (currentBundle == null) {
            EmptyMonthState(
                shopName = currentShop?.name ?: "",
                userName = userName,
                onMenu = { drawerOpen = true },
                onAddMonth = {
                    newYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                    newMonthNumber = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
                    newStartDate = todayString()
                    showMonthDialog = true
                }
            )
        } else if (registrationMode == RegistrationMode.NUMERIC) {
            MainLedger(
                database = database,
                bundle = currentBundle,
                shops = shops,
                pieces = pieces,
                selectedShopId = selectedShopId,
                selectedMonthId = selectedMonthId,
                months = months,
                userName = userName,
                shopMenuOpen = shopMenuOpen,
                onMenu = { drawerOpen = true },
                onShopMenu = { shopMenuOpen = true },
                onSelectShop = { id ->
                    selectedShopId = id
                    registrationMode = database.getShops().firstOrNull { it.id == id }?.registrationMode ?: RegistrationMode.NUMERIC
                    shopMenuOpen = false
                    drawerOpen = false
                },
                onSelectMonth = { selectedMonthId = it },
                onAddMonth = {
                    newYear = currentBundle.month.year.toString()
                    newMonthNumber = currentBundle.month.month.toString()
                    newStartDate = currentBundle.month.startDate.ifBlank { todayString() }
                    showMonthDialog = true
                },
                onAddPiece = { showPieceDialog = true },
                onManagePieces = { showPieceManager = true }, // ✅ جديد
                onEditStartDate = { showStartDatePicker = true },
                onClear = { showClearDialog = true },
                onDeleteMonth = { showDeleteMonthDialog = true },
                onDeleteShop = { showDeleteShopDialog = true },
                onRenameMonth = {
                    renamedMonth = currentBundle.month.name
                    showRenameMonthDialog = true
                },
                onCopyMonth = {
                    copyYear = currentBundle.month.year.toString()
                    copyMonth = currentBundle.month.month.toString()
                    showCopyMonthDialog = true
                },
                onEditQuantity = { day, piece, value ->
                    database.setQuantity(
                        currentBundle.month.id,
                        day.date,
                        piece.id,
                        value.filter(Char::isDigit).toIntOrNull() ?: 0
                    )
                    reloadBundle()
                },
                onEditExpense = { day, value ->
                    database.setExpense(
                        currentBundle.month.id,
                        day.date,
                        value.filter(Char::isDigit).toIntOrNull() ?: 0,
                        day.expenseNote
                    )
                    reloadBundle()
                },
                onOpenExpense = { day ->
                    activeExpenseDay = day
                    expenseAmount = day.expense.toString().takeIf { it != "0" } ?: ""
                    expenseNote = day.expenseNote
                    showExpenseDialog = true
                },
                openDailyWork = openDailyWorkRequested,
                onDailyWorkOpened = { openDailyWorkRequested = false },
                onSaveDailyWork = { day, quantities, expense ->
                    quantities.forEach { (pieceId, quantity) ->
                        database.setQuantity(
                            currentBundle.month.id,
                            day.date,
                            pieceId,
                            quantity
                        )
                    }
                    database.setExpense(
                        currentBundle.month.id,
                        day.date,
                        expense,
                        day.expenseNote
                    )
                    reloadBundle()
                    Toast.makeText(context, "تم حفظ العمل اليومي", Toast.LENGTH_SHORT).show()
                },
                onPrint = {
                    showPdfRangeDialog = true
                }
            )
        } else {
            IndividualLedger(
                database = database,
                bundle = currentBundle,
                pieces = pieces,
                months = months,
                selectedMonthId = selectedMonthId,
                selectedShopId = selectedShopId,
                shops = shops,
                userName = userName,
                onMenu = { drawerOpen = true },
                onSelectMonth = { selectedMonthId = it },
                onSelectShop = { id ->
                    selectedShopId = id
                    registrationMode = database.getShops().firstOrNull { it.id == id }?.registrationMode ?: RegistrationMode.NUMERIC
                    shopMenuOpen = false
                },
                onAddMonth = {
                    newYear = currentBundle.month.year.toString()
                    newMonthNumber = currentBundle.month.month.toString()
                    newStartDate = currentBundle.month.startDate.ifBlank { todayString() }
                    showMonthDialog = true
                },
                onAddPiece = { showPieceDialog = true },
                onManagePieces = { showPieceManager = true },
                onDeleteMonth = { showDeleteMonthDialog = true },
                onPrintPdf = {
                    showPdfRangeDialog = true
                },
                onDataChanged = {
                    reloadBundle()
                },
                onSelectSearchResult = { monthId, date ->
                    selectedMonthId = monthId
                    // يتم تحديد اليوم داخل شاشة التسجيل الفردي بعد تحميل الشهر.
                },
            )
        }

        // ===== القائمة الجانبية =====
        if (drawerOpen) {
            Box(
                modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { drawerOpen = false }
            )
            DrawerContent(
                modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.82f)
                .fillMaxSize(),
                shops = shops,
                selectedShopId = selectedShopId,
                userName = userName,
                userImagePath = userImagePath,
                onClose = { drawerOpen = false },
                onSelectShop = { id ->
                    selectedShopId = id
                    registrationMode = database.getShops().firstOrNull { it.id == id }?.registrationMode ?: RegistrationMode.NUMERIC
                    drawerOpen = false
                },
                onHome = {
                    drawerOpen = false
                    showDashboard = true
                    dataRevision++
                },
                onLedger = {
                    drawerOpen = false
                    showDashboard = false
                    openDailyWorkRequested = false
                    dataRevision++
                },
                onDailyWork = {
                    drawerOpen = false
                    showDashboard = false
                    openDailyWorkRequested = true
                },
                onMonths = {
                    drawerOpen = false
                    showMonthsPage = true
                },
                onPieces = {
                    drawerOpen = false
                    showPiecesPage = true
                },
                onReports = {
                    drawerOpen = false
                    showReportsPage = true
                },
                onAddShop = {
                    drawerOpen = false
                    newShopName = ""
                    newShopRegistrationNumber = ""
                    newShopMode = RegistrationMode.NUMERIC
                    showShopDialog = true
                },
                onUserProfile = {
                    drawerOpen = false
                    showUserProfile = true
                },
                onStatistics = {
                    drawerOpen = false
                    showStatisticsPage = true
                },
                onAbout = {
                    drawerOpen = false
                    showAbout = true
                },
                onHelp = {
                    drawerOpen = false
                    showHelp = true
                },
                onShopSettings = { shopId ->
                    drawerOpen = false
                    shopSettingsTargetId = shopId
                    showShopSettings = true
                },
                onAppSettings = {
                    drawerOpen = false
                    showAppSettings = true
                },
                onDeleteShop = {
                    drawerOpen = false
                    if (selectedShopId != null) showDeleteShopDialog = true
                }
            )
        }

        if (showFeatureHub) {
            NotebkFeatureHub(
                onBack = { showFeatureHub = false },
                onDailyWork = { showFeatureHub = false; showDashboard = false; openDailyWorkRequested = true },
                onMonths = { showFeatureHub = false; showMonthsPage = true },
                onPieces = { showFeatureHub = false; showPiecesPage = true },
                onReports = { showFeatureHub = false; showReportsPage = true },
                onStats = { showFeatureHub = false; showStatisticsPage = true },
                onProfile = { showFeatureHub = false; showUserProfile = true },
                onShopSettings = {
                    showFeatureHub = false
                    shopSettingsTargetId = selectedShopId
                    showShopSettings = selectedShopId != null
                }
            )
        }

        if (showControlCenter) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F4FB))
            ) {
                ControlCenterScreen(
                    shopName = shops.firstOrNull { it.id == selectedShopId }?.name.orEmpty(),
                    reminderEnabled = reminderEnabled,
                    onBack = { showControlCenter = false },
                    onProfile = { showControlCenter = false; showUserProfile = true },
                    onShopSettings = {
                        showControlCenter = false
                        shopSettingsTargetId = selectedShopId
                        showShopSettings = selectedShopId != null
                    },
                    onRegistrationSettings = {
                        showControlCenter = false
                        showRegistrationSettings = selectedShopId != null
                    },
                    onDailyWork = { showControlCenter = false; showDashboard = false; openDailyWorkRequested = true },
                    onLedger = { showControlCenter = false; showDashboard = false; openDailyWorkRequested = false; dataRevision++ },
                    onMonths = { showControlCenter = false; showMonthsPage = true },
                    onPieces = { showControlCenter = false; showPiecesPage = true },
                    onReports = { showControlCenter = false; showReportsPage = true },
                    onStatistics = { showControlCenter = false; showStatisticsPage = true },
                    onSecurity = { showControlCenter = false; showSecurityDialog = true },
                    onAccountAuth = { showControlCenter = false; onOpenAuth() },
                    onAppSettings = { showControlCenter = false; showAppSettings = true }
                )
            }
        }
    }

    // ===== صفحات إدارة العمل الكاملة =====
    if (showMonthsPage) {
        MonthsPage(
            months = months,
            selectedMonthId = selectedMonthId,
            onBack = { showMonthsPage = false },
            onSelect = { id ->
                selectedMonthId = id
                bundle = database.loadMonthBundle(id)
                showMonthsPage = false
                showDashboard = false
            },
            onAdd = {
                newYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                newMonthNumber = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
                newStartDate = todayString()
                showMonthDialog = true
            },
            onRename = { month ->
                renamedMonth = month.name
                selectedMonthId = month.id
                showRenameMonthDialog = true
            },
            onCopy = { month ->
                selectedMonthId = month.id
                copyYear = month.year.toString()
                copyMonth = month.month.toString()
                showCopyMonthDialog = true
            },
            onDelete = { month ->
                selectedMonthId = month.id
                bundle = database.loadMonthBundle(month.id)
                showDeleteMonthDialog = true
            },
            onRefresh = { refreshAll() }
        )
    }

    if (showPiecesPage) {
        PiecesPage(
            pieces = pieces,
            onBack = { showPiecesPage = false },
            onAdd = {
                newPieceName = ""
                newPiecePrice = ""
                showPieceDialog = true
            },
            onEdit = { piece ->
                editPieceTarget = piece
                editPieceName = piece.name
                editPiecePrice = piece.price.toString()
            },
            onDelete = { piece ->
                if (database.deletePiece(piece.id)) {
                    reloadBundle()
                    Toast.makeText(context, "تم حذف القطعة", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "لا يمكن حذف قطعة عليها كميات مسجلة", Toast.LENGTH_LONG).show()
                }
            },
            onRefresh = { refreshAll() }
        )
    }

    if (showReportsPage) {
        ReportsPage(
            hasMonth = currentBundle != null,
            onBack = { showReportsPage = false },
            onOpenReport = { if (currentBundle != null) showPdfRangeDialog = true },
            onRefresh = { refreshAll() }
        )
    }

    if (showStatisticsPage) {
        StatisticsPage(
            bundle = currentBundle,
            database = database,
            onBack = { showStatisticsPage = false },
            onRefresh = { reloadBundle() }
        )
    }

    if (editPieceTarget != null) {
        AlertDialog(
            onDismissRequest = { editPieceTarget = null },
            title = { Text("تعديل القطعة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editPieceName, onValueChange = { editPieceName = it }, singleLine = true, label = { Text("اسم القطعة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPiecePrice, onValueChange = { editPiecePrice = it.filter(Char::isDigit) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("السعر") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val target = editPieceTarget
                    val name = editPieceName.trim()
                    val price = editPiecePrice.toIntOrNull()
                    if (target != null && name.isNotEmpty() && price != null && price >= 0) {
                        database.updatePiece(target.id, name, price)
                        editPieceTarget = null
                        reloadBundle()
                        Toast.makeText(context, "تم تحديث القطعة", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "تحقق من الاسم والسعر", Toast.LENGTH_SHORT).show()
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { editPieceTarget = null }) { Text("إلغاء") } }
        )
    }

    // ===== الحوارات =====

    // 1. صفحة الملف الشخصي
    if (showUserProfile) {
        ProfilePage(
            userName = userName,
            userPhone = userPhone,
            userEmail = userEmail,
            userShop = userShop,
            userImagePath = userImagePath,
            onUserNameChange = { userName = it },
            onUserPhoneChange = { userPhone = it },
            onUserEmailChange = { userEmail = it },
            onUserShopChange = { userShop = it },
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
                        prefs.edit()
                            .putString("user_name", userName.trim())
                            .putString("user_phone", userPhone.trim())
                            .putString("user_email", userEmail.trim())
                            .putString("user_shop", userShop.trim())
                            .putString("user_image", userImagePath)
                            .apply()
                        showUserProfile = false
                        Toast.makeText(context, "تم حفظ البيانات الشخصية", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onAccountAuth = { onOpenAuth() },
            onBack = { showUserProfile = false }
        )
    }

    // 2. حوار إضافة محل
    if (showShopDialog) {
        AlertDialog(
            onDismissRequest = { showShopDialog = false },
            title = { Text("إضافة محل جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل اسم المحل الذي تعمل فيه", fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newShopName,
                        onValueChange = { newShopName = it },
                        singleLine = true,
                        label = { Text("اسم المحل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newShopRegistrationNumber,
                        onValueChange = { newShopRegistrationNumber = it },
                        singleLine = true,
                        label = { Text("رقم المحل / التسجيل *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("نوع الحساب لهذا المحل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { newShopMode = RegistrationMode.NUMERIC },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (newShopMode == RegistrationMode.NUMERIC) Blue.copy(alpha = 0.12f) else Color.Transparent,
                                contentColor = Blue
                            )
                        ) { Text("تسجيل عددي", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { newShopMode = RegistrationMode.INDIVIDUAL },
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
                                selectedShopId = id
                                selectedMonthId = null
                                bundle = null
                                refreshAll()
                                showShopDialog = false
                                Toast.makeText(context, "تم إضافة المحل", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showShopDialog = false }) { Text("إلغاء") } }
        )
    }

    // 3. حوار إضافة شهر (مع التوليد التلقائي للتاريخ)
    if (showMonthDialog && selectedShopId != null) {
        AlertDialog(
            onDismissRequest = { showMonthDialog = false },
            title = { Text("إضافة سجل شهر جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("سيتم إنشاء جميع أيام الشهر تلقائياً", fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newYear,
                        onValueChange = { newYear = it.filter(Char::isDigit) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("السنة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newMonthNumber,
                        onValueChange = { newMonthNumber = it.filter(Char::isDigit) },
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
                            selectedMonthId = id
                            refreshAll()
                            showMonthDialog = false
                            Toast.makeText(context, "تم إنشاء الشهر وتجهيز أيامه من 01/${monthName(month)}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) { Text("إنشاء") }
            },
            dismissButton = { TextButton(onClick = { showMonthDialog = false }) { Text("إلغاء") } }
        )
    }

    // 4. حوار إضافة قطعة
    if (showPieceDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { showPieceDialog = false },
            title = { Text("إضافة قطعة جديدة", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPieceName,
                        onValueChange = { newPieceName = it },
                        singleLine = true,
                        label = { Text("اسم القطعة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPiecePrice,
                        onValueChange = { newPiecePrice = it.filter(Char::isDigit) },
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
                            newPieceName = ""
                            newPiecePrice = ""
                            showPieceDialog = false
                            reloadBundle()
                        }
                }) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showPieceDialog = false }) { Text("إلغاء") } }
        )
    }

    // 5. حوار إدارة القطع (✅ جديد)
    if (showPieceManager && currentBundle != null) {
        PieceManagerDialog(
            pieces = pieces,
            onDismiss = { showPieceManager = false },
            onAdd = {
                showPieceManager = false
                newPieceName = ""
                newPiecePrice = ""
                showPieceDialog = true
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
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("تعديل مصروف اليوم", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("التاريخ: ${activeExpenseDay!!.date}", fontSize = 12.sp, color = Purple)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it.filter(Char::isDigit) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("المبلغ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expenseNote,
                        onValueChange = { expenseNote = it },
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
                        showExpenseDialog = false
                        reloadBundle()
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showExpenseDialog = false }) { Text("إلغاء") } }
        )
    }

    // 7. حوار مسح البيانات
    if (showClearDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("مسح بيانات الشهر", color = Red, fontWeight = FontWeight.Bold) },
            text = { Text("سيتم مسح الكميات والمصروفات فقط") },
            confirmButton = {
                Button(
                    onClick = {
                        createInternalAutoBackup(context)
                        database.clearMonthData(currentBundle.month.id)
                        showClearDialog = false
                        reloadBundle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("مسح البيانات") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("إلغاء") } }
        )
    }

    // 8. حوار حذف الشهر
    if (showDeleteMonthDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { showDeleteMonthDialog = false },
            title = { Text("حذف الشهر", color = Red, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف ${currentBundle.month.name}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        createInternalAutoBackup(context)
                        database.deleteMonth(currentBundle.month.id)
                        showDeleteMonthDialog = false
                        selectedMonthId = null
                        reloadMonths()
                        reloadBundle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { showDeleteMonthDialog = false }) { Text("إلغاء") } }
        )
    }

    // 9. حوار حذف المحل
    if (showDeleteShopDialog && selectedShopId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteShopDialog = false },
            title = { Text("حذف المحل", color = Red, fontWeight = FontWeight.Bold) },
            text = { Text("سيتم حذف المحل وكل شهوره وعملياته") },
            confirmButton = {
                Button(
                    onClick = {
                        createInternalAutoBackup(context)
                        database.deleteShop(selectedShopId!!)
                        showDeleteShopDialog = false
                        selectedShopId = null
                        selectedMonthId = null
                        bundle = null
                        refreshAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("حذف المحل") }
            },
            dismissButton = { TextButton(onClick = { showDeleteShopDialog = false }) { Text("إلغاء") } }
        )
    }

    // 10. حوار تعديل اسم الشهر
    if (showRenameMonthDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { showRenameMonthDialog = false },
            title = { Text("تعديل اسم الشهر") },
            text = {
                OutlinedTextField(
                    value = renamedMonth,
                    onValueChange = { renamedMonth = it },
                    singleLine = true,
                    label = { Text("اسم السجل") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                        if (renamedMonth.trim().isNotEmpty()) {
                            database.renameMonth(currentBundle.month.id, renamedMonth.trim())
                            showRenameMonthDialog = false
                            reloadMonths()
                            reloadBundle()
                        }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showRenameMonthDialog = false }) { Text("إلغاء") } }
        )
    }

    // 11. حوار نسخ الشهر
    if (showCopyMonthDialog && currentBundle != null) {
        AlertDialog(
            onDismissRequest = { showCopyMonthDialog = false },
            title = { Text("نسخ الشهر") },
            text = {
                Column {
                    Text("سيتم إنشاء شهر جديد بنفس الإعدادات بدون الكميات", fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        copyYear,
                        { copyYear = it.filter(Char::isDigit) },
                        singleLine = true,
                        label = { Text("السنة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(7.dp))
                    OutlinedTextField(
                        copyMonth,
                        { copyMonth = it.filter(Char::isDigit) },
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
                                selectedMonthId = id
                                showCopyMonthDialog = false
                                reloadMonths()
                                reloadBundle()
                            }
                        }
                }) { Text("نسخ") }
            },
            dismissButton = { TextButton(onClick = { showCopyMonthDialog = false }) { Text("إلغاء") } }
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
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            val calendar = Calendar.getInstance().apply { timeInMillis = it }
                            newStartDate = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(calendar.time)
                        }
                        showStartDatePicker = false
                }) { Text("موافق") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state) }
    }

    // ===== اختيار نطاق تقرير PDF =====
    if (showPdfRangeDialog && currentBundle != null) {
        PdfReportRangeDialog(
            mode = registrationMode,
            bundle = currentBundle,
            database = database,
            onDismiss = { showPdfRangeDialog = false },
            onPrint = { range, selectedDate, selectedMonthId, sharePdf ->
                showPdfRangeDialog = false
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
                onBack = { showShopSettings = false },
                onSave = { name, registrationNumber, mode ->
                    database.updateShop(targetShop.id, name)
                    database.updateShopRegistrationNumber(targetShop.id, registrationNumber)
                    database.updateShopRegistrationMode(targetShop.id, mode)
                    reloadShops()
                    if (selectedShopId == targetShop.id) registrationMode = mode
                    showShopSettings = false
                    dataRevision++
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
                reminderEnabled = enabled
                prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
                if (enabled) WorkReminderScheduler.schedule(context) else WorkReminderScheduler.cancel(context)
            },
            onDarkThemeChange = { enabled ->
                darkTheme = enabled
                prefs.edit().putBoolean("dark_theme", enabled).apply()
            },
            onSecurity = { showAppSettings = false; showSecurityDialog = true },
            onSystemSettings = { showAppSettings = false; showSystemSettings = true },
            onAccountAuth = { showAppSettings = false; onOpenAuth() },
            onHelp = { showAppSettings = false; showHelp = true },
            onBack = { showAppSettings = false }
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
            onBack = { showSystemSettings = false }
        )
    }

    // ===== إعدادات نوع الحساب =====
    if (showRegistrationSettings) {
        RegistrationSettingsPage(
            currentMode = registrationMode,
            onBack = { showRegistrationSettings = false },
            onSecurity = { showRegistrationSettings = false; showSecurityDialog = true },
            onSave = { mode ->
                registrationMode = mode
                selectedShopId?.let { database.updateShopRegistrationMode(it, mode) }
                reloadShops()
                showRegistrationSettings = false
                Toast.makeText(context, "تم حفظ طريقة تسجيل العمل", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSecurityDialog) {
        SecuritySettingsDialog(
            context = context,
            hasPin = appPinEnabled,
            onDismiss = { showSecurityDialog = false },
            onSetPin = { showSetPinDialog = true },
            onRemovePin = {
                clearAppPin(context)
                appPinEnabled = false
                Toast.makeText(context, "تم إلغاء قفل التطبيق", Toast.LENGTH_SHORT).show()
            },
            onBackup = {
                backupCreateLauncher.launch("pepers_backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.zip")
            },
            onRestore = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }
        )
    }

    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onSaved = { pin ->
                setAppPin(context, pin)
                appPinEnabled = true
                showSetPinDialog = false
                Toast.makeText(context, "تم تفعيل قفل التطبيق", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 13. دليل الاستخدام
    if (showHelp) {
        HelpDialog(
            onDismiss = {
                showHelp = false
                prefs.edit().putBoolean("onboarding_completed_v1", true).apply()
            }
        )
    }

    // 14. حوار من نحن
    if (showAbout) {
        AboutDialog(
            onDismiss = { showAbout = false }
        )
    }

    // 15. حوار الإحصائيات
    if (showStatistics && currentBundle != null) {
        StatisticsDialog(
            bundle = currentBundle,
            database = database,
            userName = userName,
            onDismiss = { showStatistics = false }
        )
    }
}

