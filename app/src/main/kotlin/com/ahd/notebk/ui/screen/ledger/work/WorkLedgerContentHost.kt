package com.ahd.notebk.ui.screen.ledger.work

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.*
import com.ahd.notebk.ui.screen.individual.work.IndividualLedger

@Composable
fun WorkLedgerContentHost(
    context: Context,
    database: LedgerDatabase,
    currentBundle: MonthBundle,
    shops: List<ShopRecord>,
    pieces: List<PieceRecord>,
    months: List<MonthRecord>,
    selectedShopId: Long?,
    selectedMonthId: Long?,
    registrationMode: RegistrationMode,
    userName: String,
    shopMenuOpen: Boolean,
    openDailyWorkRequested: Boolean,
    onMenu: () -> Unit,
    onShopMenu: () -> Unit,
    onSelectShop: (Long) -> Unit,
    onSelectMonth: (Long) -> Unit,
    onAddMonth: () -> Unit,
    onAddPiece: () -> Unit,
    onManagePieces: () -> Unit,
    onEditStartDate: () -> Unit,
    onClear: () -> Unit,
    onDeleteMonth: () -> Unit,
    onDeleteShop: () -> Unit,
    onRenameMonth: () -> Unit,
    onCopyMonth: () -> Unit,
    onEditQuantity: (DayRecord, PieceRecord, String) -> Unit,
    onEditExpense: (DayRecord, String) -> Unit,
    onOpenExpense: (DayRecord) -> Unit,
    onDailyWorkOpened: () -> Unit,
    onSaveDailyWork: (DayRecord, Map<Long, Int>, Int) -> Unit,
    onPrint: () -> Unit,
    onDataChanged: () -> Unit,
    onSelectSearchResult: (Long, String) -> Unit
) {
    if (registrationMode == RegistrationMode.NUMERIC) {
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
            onMenu = onMenu,
            onShopMenu = onShopMenu,
            onSelectShop = onSelectShop,
            onSelectMonth = onSelectMonth,
            onAddMonth = onAddMonth,
            onAddPiece = onAddPiece,
            onManagePieces = onManagePieces,
            onEditStartDate = onEditStartDate,
            onClear = onClear,
            onDeleteMonth = onDeleteMonth,
            onDeleteShop = onDeleteShop,
            onRenameMonth = onRenameMonth,
            onCopyMonth = onCopyMonth,
            onEditQuantity = onEditQuantity,
            onEditExpense = onEditExpense,
            onOpenExpense = onOpenExpense,
            openDailyWork = openDailyWorkRequested,
            onDailyWorkOpened = onDailyWorkOpened,
            onSaveDailyWork = onSaveDailyWork,
            onPrint = onPrint
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
            onMenu = onMenu,
            onSelectMonth = onSelectMonth,
            onSelectShop = onSelectShop,
            onAddMonth = onAddMonth,
            onAddPiece = onAddPiece,
            onManagePieces = onManagePieces,
            onDeleteMonth = onDeleteMonth,
            onPrintPdf = onPrint,
            onDataChanged = onDataChanged,
            onSelectSearchResult = onSelectSearchResult
        )
    }
}
