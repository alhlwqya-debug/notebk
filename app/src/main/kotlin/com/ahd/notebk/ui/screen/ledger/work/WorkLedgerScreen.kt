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
            WorkLedgerDashboardHost(
                database = database, shops = shops, selectedShopId = selectedShopId,
                userName = userName, refreshKey = dataRevision,
                onMenu = { drawerOpen = true },
                onOpenLedger = { showDashboard = false; openDailyWorkRequested = false; dataRevision++ },
                onOpenDailyWork = { showDashboard = false; openDailyWorkRequested = true; dataRevision++ },
                onAddShop = {
                    newShopName = ""; newShopRegistrationNumber = ""; newShopMode = RegistrationMode.NUMERIC
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
        } else {
            WorkLedgerContentHost(
                context = context, database = database, currentBundle = currentBundle,
                shops = shops, pieces = pieces, months = months,
                selectedShopId = selectedShopId, selectedMonthId = selectedMonthId,
                registrationMode = registrationMode, userName = userName,
                shopMenuOpen = shopMenuOpen, openDailyWorkRequested = openDailyWorkRequested,
                onMenu = { drawerOpen = true }, onShopMenu = { shopMenuOpen = true },
                onSelectShop = { id ->
                    selectedShopId = id
                    registrationMode = database.getShops().firstOrNull { it.id == id }?.registrationMode ?: RegistrationMode.NUMERIC
                    shopMenuOpen = false; drawerOpen = false
                },
                onSelectMonth = { selectedMonthId = it },
                onAddMonth = {
                    newYear = currentBundle.month.year.toString()
                    newMonthNumber = currentBundle.month.month.toString()
                    newStartDate = currentBundle.month.startDate.ifBlank { todayString() }
                    showMonthDialog = true
                },
                onAddPiece = { showPieceDialog = true },
                onManagePieces = { showPieceManager = true },
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
                    newYear = currentBundle.month.year.toString()
                    copyYear = currentBundle.month.year.toString()
                    copyMonth = currentBundle.month.month.toString()
                    showCopyMonthDialog = true
                },
                onEditQuantity = { day, piece, value ->
                    database.setQuantity(currentBundle.month.id, day.date, piece.id, value.filter(Char::isDigit).toIntOrNull() ?: 0)
                    reloadBundle()
                },
                onEditExpense = { day, value ->
                    database.setExpense(currentBundle.month.id, day.date, value.filter(Char::isDigit).toIntOrNull() ?: 0, day.expenseNote)
                    reloadBundle()
                },
                onOpenExpense = { day ->
                    activeExpenseDay = day
                    expenseAmount = day.expense.toString().takeIf { it != "0" } ?: ""
                    expenseNote = day.expenseNote
                    showExpenseDialog = true
                },
                onDailyWorkOpened = { openDailyWorkRequested = false },
                onSaveDailyWork = { day, quantities, expense ->
                    quantities.forEach { (pieceId, quantity) ->
                        database.setQuantity(currentBundle.month.id, day.date, pieceId, quantity)
                    }
                    database.setExpense(currentBundle.month.id, day.date, expense, day.expenseNote)
                    reloadBundle()
                    Toast.makeText(context, "تم حفظ العمل اليومي", Toast.LENGTH_SHORT).show()
                },
                onPrint = { showPdfRangeDialog = true },
                onDataChanged = { reloadBundle() },
                onSelectSearchResult = { monthId, _ -> selectedMonthId = monthId }
            )
        }

        WorkLedgerDrawerHost(
            database = database, shops = shops, selectedShopId = selectedShopId, userName = userName, userImagePath = userImagePath,
            drawerOpen = drawerOpen, showDashboard = showDashboard, openDailyWorkRequested = openDailyWorkRequested,
            showMonthsPage = showMonthsPage, showPiecesPage = showPiecesPage, showReportsPage = showReportsPage,
            showStatisticsPage = showStatisticsPage, showAbout = showAbout, showHelp = showHelp,
            showShopSettings = showShopSettings, showAppSettings = showAppSettings, showDeleteShopDialog = showDeleteShopDialog,
            showFeatureHub = showFeatureHub, showControlCenter = showControlCenter, reminderEnabled = reminderEnabled,
            onDrawerOpenChange = { drawerOpen = it }, onSelectedShopChange = { selectedShopId = it },
            onRegistrationModeChange = { registrationMode = it }, onDashboardChange = { showDashboard = it },
            onDailyWorkRequestChange = { openDailyWorkRequested = it }, onMonthsPageChange = { showMonthsPage = it },
            onPiecesPageChange = { showPiecesPage = it }, onReportsPageChange = { showReportsPage = it },
            onStatisticsPageChange = { showStatisticsPage = it }, onAboutChange = { showAbout = it },
            onHelpChange = { showHelp = it }, onShopSettingsChange = { showShopSettings = it },
            onAppSettingsChange = { showAppSettings = it }, onDeleteShopDialogChange = { showDeleteShopDialog = it },
            onFeatureHubChange = { showFeatureHub = it }, onControlCenterChange = { showControlCenter = it },
            onShopSettingsTargetChange = { shopSettingsTargetId = it },
            onNewShop = { newShopName = ""; newShopRegistrationNumber = ""; newShopMode = RegistrationMode.NUMERIC; showShopDialog = true },
            onProfile = { showUserProfile = true }, onRefreshRevision = { dataRevision++ },
            onOpenAuth = onOpenAuth, onSecurity = { showSecurityDialog = true },
            onRegistrationSettings = { showRegistrationSettings = selectedShopId != null }
        )

    WorkLedgerManagementHost(
        context = context, database = database, months = months, pieces = pieces, currentBundle = currentBundle,
        selectedMonthId = selectedMonthId, showMonthsPage = showMonthsPage, showPiecesPage = showPiecesPage,
        showReportsPage = showReportsPage, showStatisticsPage = showStatisticsPage, editPieceTarget = editPieceTarget,
        editPieceName = editPieceName, editPiecePrice = editPiecePrice,
        onMonthsPageChange = { showMonthsPage = it }, onPiecesPageChange = { showPiecesPage = it },
        onReportsPageChange = { showReportsPage = it }, onStatisticsPageChange = { showStatisticsPage = it },
        onSelectedMonthChange = { selectedMonthId = it }, onBundleChange = { bundle = it },
        onDashboardChange = { showDashboard = it },
        onNewMonth = { newYear = Calendar.getInstance().get(Calendar.YEAR).toString(); newMonthNumber = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString(); newStartDate = todayString(); showMonthDialog = true },
        onRenameMonth = { month -> renamedMonth = month.name; selectedMonthId = month.id; showRenameMonthDialog = true },
        onCopyMonth = { month -> selectedMonthId = month.id; copyYear = month.year.toString(); copyMonth = month.month.toString(); showCopyMonthDialog = true },
        onDeleteMonth = { month -> selectedMonthId = month.id; bundle = database.loadMonthBundle(month.id); showDeleteMonthDialog = true },
        onNewPiece = { newPieceName = ""; newPiecePrice = ""; showPieceDialog = true },
        onEditPiece = { piece -> editPieceTarget = piece; editPieceName = piece.name; editPiecePrice = piece.price.toString() },
        onDeletePiece = { piece -> if (database.deletePiece(piece.id)) { reloadBundle(); Toast.makeText(context, "تم حذف القطعة", Toast.LENGTH_SHORT).show() } else Toast.makeText(context, "لا يمكن حذف قطعة عليها كميات مسجلة", Toast.LENGTH_LONG).show() },
        onRefresh = { refreshAll() }, onReloadBundle = { reloadBundle() },
        onEditPieceTargetChange = { editPieceTarget = it }, onEditPieceNameChange = { editPieceName = it },
        onEditPiecePriceChange = { editPiecePrice = it }, onOpenReport = { showPdfRangeDialog = true }
    )

        WorkLedgerDialogsHost(
        context = context, database = database, prefs = prefs, currentBundle = currentBundle, currentShop = currentShop,
        shops = shops, selectedShopId = selectedShopId, months = months, pieces = pieces,
        userName = userName, userPhone = userPhone, userEmail = userEmail, userShop = userShop, userImagePath = userImagePath,
        registrationMode = registrationMode, reminderEnabled = reminderEnabled, darkTheme = darkTheme,
        showUserProfile = showUserProfile, showShopDialog = showShopDialog, showMonthDialog = showMonthDialog,
        showPieceDialog = showPieceDialog, showPieceManager = showPieceManager, showExpenseDialog = showExpenseDialog,
        showClearDialog = showClearDialog, showDeleteMonthDialog = showDeleteMonthDialog, showDeleteShopDialog = showDeleteShopDialog,
        showRenameMonthDialog = showRenameMonthDialog, showCopyMonthDialog = showCopyMonthDialog, showStartDatePicker = showStartDatePicker,
        showPdfRangeDialog = showPdfRangeDialog, showShopSettings = showShopSettings, showAppSettings = showAppSettings,
        showSystemSettings = showSystemSettings, shopSettingsTargetId = shopSettingsTargetId, activeExpenseDay = activeExpenseDay,
        newShopName = newShopName, newShopRegistrationNumber = newShopRegistrationNumber, newShopMode = newShopMode,
        newYear = newYear, newMonthNumber = newMonthNumber, newStartDate = newStartDate, newPieceName = newPieceName,
        newPiecePrice = newPiecePrice, expenseAmount = expenseAmount, expenseNote = expenseNote, renamedMonth = renamedMonth,
        copyYear = copyYear, copyMonth = copyMonth, selectedMonthId = selectedMonthId,
        onShowUserProfile = { showUserProfile = it }, onShowShopDialog = { showShopDialog = it },
        onShowMonthDialog = { showMonthDialog = it }, onShowPieceDialog = { showPieceDialog = it },
        onShowPieceManager = { showPieceManager = it }, onShowExpenseDialog = { showExpenseDialog = it },
        onShowClearDialog = { showClearDialog = it }, onShowDeleteMonthDialog = { showDeleteMonthDialog = it },
        onShowDeleteShopDialog = { showDeleteShopDialog = it }, onShowRenameMonthDialog = { showRenameMonthDialog = it },
        onShowCopyMonthDialog = { showCopyMonthDialog = it }, onShowStartDatePicker = { showStartDatePicker = it },
        onShowPdfRangeDialog = { showPdfRangeDialog = it }, onShowShopSettings = { showShopSettings = it },
        onShowAppSettings = { showAppSettings = it }, onShowSystemSettings = { showSystemSettings = it },
        onActiveExpenseDay = { activeExpenseDay = it }, onNewShopName = { newShopName = it },
        onNewShopRegistrationNumber = { newShopRegistrationNumber = it }, onNewShopMode = { newShopMode = it },
        onNewYear = { newYear = it }, onNewMonthNumber = { newMonthNumber = it }, onNewStartDate = { newStartDate = it },
        onNewPieceName = { newPieceName = it }, onNewPiecePrice = { newPiecePrice = it },
        onExpenseAmount = { expenseAmount = it }, onExpenseNote = { expenseNote = it }, onRenamedMonth = { renamedMonth = it },
        onCopyYear = { copyYear = it }, onCopyMonth = { copyMonth = it }, onSelectedMonthId = { selectedMonthId = it },
        onSelectedShopId = { selectedShopId = it }, onBundleChange = { bundle = it }, onRegistrationModeChange = { registrationMode = it },
        onUserNameChange = { userName = it }, onUserPhoneChange = { userPhone = it }, onUserEmailChange = { userEmail = it },
        onUserShopChange = { userShop = it }, onUserImagePathChange = { userImagePath = it }, imagePickerLauncher = imagePickerLauncher,
        onReminderChange = { enabled ->
            reminderEnabled = enabled
            prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
            centralSettings.putBoolean("daily_reminder_enabled", enabled)
            if (enabled) {
                runCatching { WorkReminderScheduler.schedule(context) }
                    .onFailure { Toast.makeText(context, "تعذر جدولة التذكير: ${it.message ?: "خطأ غير معروف"}", Toast.LENGTH_LONG).show() }
            } else {
                runCatching { WorkReminderScheduler.cancel(context) }
                    .onFailure { Toast.makeText(context, "تعذر إيقاف التذكير: ${it.message ?: "خطأ غير معروف"}", Toast.LENGTH_LONG).show() }
            }
            Toast.makeText(context, if (enabled) "تم تفعيل التذكير اليومي" else "تم إيقاف التذكير اليومي", Toast.LENGTH_SHORT).show()
        },
        onDarkThemeChange = { enabled ->
            darkTheme = enabled
            prefs.edit().putBoolean("dark_theme", enabled).apply()
            centralSettings.putString("theme_mode", if (enabled) "dark" else "light")
        },
        onSecurity = { showAppSettings = false; showSecurityDialog = true },
        onSystemSettings = { showAppSettings = false; showSystemSettings = true },
        onAccountAuth = { showAppSettings = false; onOpenAuth() }, onHelp = { showAppSettings = false; showHelp = true },
        onBackAppSettings = { showAppSettings = false }, onBackShopSettings = { showShopSettings = false },
        refreshAll = { refreshAll() }, reloadBundle = { reloadBundle() }, reloadMonths = { reloadMonths() }, reloadShops = { reloadShops() },
        dataRevision = dataRevision, onDataRevisionChange = { dataRevision++ },
        onRegistrationSettings = { showRegistrationSettings = false; showSecurityDialog = true }, onOpenAuth = onOpenAuth
    )
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
}
