package com.ahd.notebk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ahd.notebk.ui.screen.individual.IndividualLedger
import com.ahd.notebk.ui.screen.ledger.LedgerScreen
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.screen.report.ReportScreen
import com.ahd.notebk.ui.screen.settings.SettingsScreen
import com.ahd.notebk.ui.screen.settings.SettingsViewModel
import com.ahd.notebk.ui.screen.statistics.StatisticsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavigation(navController: NavHostController, ledgerViewModel: LedgerViewModel, settingsViewModel: SettingsViewModel) {
    NavHost(navController = navController, startDestination = AppRoutes.LEDGER) {
        composable(AppRoutes.LEDGER) {
            LedgerScreen(ledgerViewModel,
                onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onOpenReport = { navController.navigate(AppRoutes.REPORT) },
                onOpenStatistics = { navController.navigate(AppRoutes.STATISTICS) },
                onOpenIndividualLedger = { navController.navigate(AppRoutes.INDIVIDUAL) })
        }
        composable(AppRoutes.STATISTICS) {
            val summary by ledgerViewModel.summaryState.collectAsState()
            val settings by ledgerViewModel.settingsState.collectAsState()
            val records by ledgerViewModel.recordsState.collectAsState()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val workingDays = records.map { dateFormat.format(Date(it.timestamp)) }.distinct().size
            StatisticsScreen(summary, workingDays, settings.currencySymbol, onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.INDIVIDUAL) {
            val records by ledgerViewModel.recordsState.collectAsState()
            IndividualLedger(records) { navController.popBackStack() }
        }
        composable(AppRoutes.SETTINGS) { SettingsScreen(settingsViewModel, ledgerViewModel) { navController.popBackStack() } }
        composable(AppRoutes.REPORT) { ReportScreen(ledgerViewModel) { navController.popBackStack() } }
    }
}
