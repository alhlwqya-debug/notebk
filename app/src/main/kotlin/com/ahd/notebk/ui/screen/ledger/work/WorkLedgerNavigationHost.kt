package com.ahd.notebk.ui.screen.ledger.work

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.RegistrationMode
import com.ahd.notebk.ui.navigation.DrawerContent
import com.ahd.notebk.ui.screen.control.NotebkFeatureHub
import com.ahd.notebk.ui.screen.settings.ControlCenterScreen

@Composable
fun WorkLedgerDrawerHost(
    database: LedgerDatabase,
    shops: List<com.ahd.notebk.domain.model.Shop>,
    selectedShopId: Long?,
    userName: String,
    userImagePath: String,
    drawerOpen: Boolean,
    showDashboard: Boolean,
    openDailyWorkRequested: Boolean,
    showMonthsPage: Boolean,
    showPiecesPage: Boolean,
    showReportsPage: Boolean,
    showStatisticsPage: Boolean,
    showAbout: Boolean,
    showHelp: Boolean,
    showShopSettings: Boolean,
    showAppSettings: Boolean,
    showDeleteShopDialog: Boolean,
    showFeatureHub: Boolean,
    showControlCenter: Boolean,
    reminderEnabled: Boolean,
    onDrawerOpenChange: (Boolean) -> Unit,
    onSelectedShopChange: (Long?) -> Unit,
    onRegistrationModeChange: (RegistrationMode) -> Unit,
    onDashboardChange: (Boolean) -> Unit,
    onDailyWorkRequestChange: (Boolean) -> Unit,
    onMonthsPageChange: (Boolean) -> Unit,
    onPiecesPageChange: (Boolean) -> Unit,
    onReportsPageChange: (Boolean) -> Unit,
    onStatisticsPageChange: (Boolean) -> Unit,
    onAboutChange: (Boolean) -> Unit,
    onHelpChange: (Boolean) -> Unit,
    onShopSettingsChange: (Boolean) -> Unit,
    onAppSettingsChange: (Boolean) -> Unit,
    onDeleteShopDialogChange: (Boolean) -> Unit,
    onFeatureHubChange: (Boolean) -> Unit,
    onControlCenterChange: (Boolean) -> Unit,
    onShopSettingsTargetChange: (Long?) -> Unit,
    onNewShop: () -> Unit,
    onProfile: () -> Unit,
    onRefreshRevision: () -> Unit,
    onOpenAuth: () -> Unit,
    onSecurity: () -> Unit,
    onRegistrationSettings: () -> Unit
) {
    if (drawerOpen) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).clickable {
                    onDrawerOpenChange(false)
                }
            )
            DrawerContent(
                modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.82f).fillMaxSize(),
                shops = shops,
                selectedShopId = selectedShopId,
                userName = userName,
                userImagePath = userImagePath,
                onClose = { onDrawerOpenChange(false) },
                onSelectShop = { id ->
                    onSelectedShopChange(id)
                    onRegistrationModeChange(database.getShops().firstOrNull { it.id == id }?.registrationMode ?: RegistrationMode.NUMERIC)
                    onDrawerOpenChange(false)
                },
                onHome = {
                    onDrawerOpenChange(false); onDashboardChange(true); onRefreshRevision()
                },
                onLedger = {
                    onDrawerOpenChange(false); onDashboardChange(false); onDailyWorkRequestChange(false); onRefreshRevision()
                },
                onDailyWork = {
                    onDrawerOpenChange(false); onDashboardChange(false); onDailyWorkRequestChange(true)
                },
                onMonths = { onDrawerOpenChange(false); onMonthsPageChange(true) },
                onPieces = { onDrawerOpenChange(false); onPiecesPageChange(true) },
                onReports = { onDrawerOpenChange(false); onReportsPageChange(true) },
                onAddShop = { onDrawerOpenChange(false); onNewShop() },
                onUserProfile = { onDrawerOpenChange(false); onProfile() },
                onStatistics = { onDrawerOpenChange(false); onStatisticsPageChange(true) },
                onAbout = { onDrawerOpenChange(false); onAboutChange(true) },
                onHelp = { onDrawerOpenChange(false); onHelpChange(true) },
                onShopSettings = { shopId ->
                    onDrawerOpenChange(false); onShopSettingsTargetChange(shopId); onShopSettingsChange(true)
                },
                onAppSettings = { onDrawerOpenChange(false); onAppSettingsChange(true) },
                onDeleteShop = {
                    onDrawerOpenChange(false)
                    if (selectedShopId != null) onDeleteShopDialogChange(true)
                }
            )
        }
    }

    if (showFeatureHub) {
        NotebkFeatureHub(
            onBack = { onFeatureHubChange(false) },
            onDailyWork = { onFeatureHubChange(false); onDashboardChange(false); onDailyWorkRequestChange(true) },
            onMonths = { onFeatureHubChange(false); onMonthsPageChange(true) },
            onPieces = { onFeatureHubChange(false); onPiecesPageChange(true) },
            onReports = { onFeatureHubChange(false); onReportsPageChange(true) },
            onStats = { onFeatureHubChange(false); onStatisticsPageChange(true) },
            onProfile = { onFeatureHubChange(false); onProfile() },
            onShopSettings = {
                onFeatureHubChange(false)
                onShopSettingsTargetChange(selectedShopId)
                onShopSettingsChange(selectedShopId != null)
            }
        )
    }

    if (showControlCenter) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF6F4FB))) {
            ControlCenterScreen(
                shopName = shops.firstOrNull { it.id == selectedShopId }?.name.orEmpty(),
                reminderEnabled = reminderEnabled,
                onBack = { onControlCenterChange(false) },
                onProfile = { onControlCenterChange(false); onProfile() },
                onShopSettings = {
                    onControlCenterChange(false); onShopSettingsTargetChange(selectedShopId); onShopSettingsChange(selectedShopId != null)
                },
                onRegistrationSettings = { onControlCenterChange(false); onRegistrationSettings() },
                onDailyWork = { onControlCenterChange(false); onDashboardChange(false); onDailyWorkRequestChange(true) },
                onLedger = { onControlCenterChange(false); onDashboardChange(false); onDailyWorkRequestChange(false); onRefreshRevision() },
                onMonths = { onControlCenterChange(false); onMonthsPageChange(true) },
                onPieces = { onControlCenterChange(false); onPiecesPageChange(true) },
                onReports = { onControlCenterChange(false); onReportsPageChange(true) },
                onStatistics = { onControlCenterChange(false); onStatisticsPageChange(true) },
                onSecurity = { onControlCenterChange(false); onSecurity() },
                onAccountAuth = { onControlCenterChange(false); onOpenAuth() },
                onAppSettings = { onControlCenterChange(false); onAppSettingsChange(true) }
            )
        }
    }
}
