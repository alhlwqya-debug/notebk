package com.ahd.notebk.ui.screen.ledger.work

import androidx.compose.runtime.Composable
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.ShopRecord
import com.ahd.notebk.ui.screen.home.HomeDashboard

@Composable
fun WorkLedgerDashboardHost(
    database: LedgerDatabase,
    shops: List<ShopRecord>,
    selectedShopId: Long?,
    userName: String,
    refreshKey: Int,
    onMenu: () -> Unit,
    onOpenLedger: () -> Unit,
    onOpenDailyWork: () -> Unit,
    onAddShop: () -> Unit,
    onShopSettings: () -> Unit,
    onUserProfile: () -> Unit,
    onAppSettings: () -> Unit,
    onHelp: () -> Unit,
    onStatistics: () -> Unit
) {
    HomeDashboard(
        database = database,
        shops = shops,
        selectedShopId = selectedShopId,
        userName = userName,
        refreshKey = refreshKey,
        onMenu = onMenu,
        onOpenLedger = onOpenLedger,
        onOpenDailyWork = onOpenDailyWork,
        onAddShop = onAddShop,
        onShopSettings = onShopSettings,
        onUserProfile = onUserProfile,
        onAppSettings = onAppSettings,
        onHelp = onHelp,
        onStatistics = onStatistics
    )
}
