package com.ahd.notebk.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ahd.notebk.ui.screen.individual.IndividualLedger
import com.ahd.notebk.ui.screen.home.HomeScreen
import com.ahd.notebk.ui.screen.ledger.LedgerScreen
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.screen.report.ReportScreen
import com.ahd.notebk.ui.screen.settings.SettingsScreen
import com.ahd.notebk.ui.screen.settings.SettingsViewModel
import com.ahd.notebk.ui.screen.shops.ShopViewModel
import com.ahd.notebk.ui.screen.shops.ShopsScreen
import com.ahd.notebk.ui.screen.statistics.StatisticsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavigation(
    navController: NavHostController,
    ledgerViewModel: LedgerViewModel,
    settingsViewModel: SettingsViewModel,
    shopViewModel: ShopViewModel
) {
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val activeShop by shopViewModel.selectedShop.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NoteBkDrawer(
                currentRoute = currentRoute,
                settings = ledgerViewModel.settingsState.collectAsState().value,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (route != currentRoute) navController.navigate(route) { launchSingleTop = true }
                }
            )
        }
    ) {
    val showBottomBar = currentRoute == AppRoutes.NUMERIC ||
        currentRoute == AppRoutes.INDIVIDUAL ||
        currentRoute == AppRoutes.SHOPS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == AppRoutes.NUMERIC,
                        onClick = {
                            navController.navigate(AppRoutes.NUMERIC) {
                                popUpTo(AppRoutes.NUMERIC) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.GridView, null) },
                        label = { Text("التسجيل العددي") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppRoutes.INDIVIDUAL,
                        onClick = {
                            navController.navigate(AppRoutes.INDIVIDUAL) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("التسجيل الفردي") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppRoutes.SHOPS,
                        onClick = {
                            navController.navigate(AppRoutes.SHOPS) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Default.Store, null) },
                        label = { Text("المحلات") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoutes.HOME) {
                val homeSettings by ledgerViewModel.settingsState.collectAsState()
                val homeRecords by ledgerViewModel.recordsState.collectAsState()
                val homeSummary by ledgerViewModel.summaryState.collectAsState()
                HomeScreen(
                    settings = homeSettings,
                    records = homeRecords,
                    summary = homeSummary,
                    onOpenNumeric = { navController.navigate(AppRoutes.NUMERIC) },
                    onOpenIndividual = { navController.navigate(AppRoutes.INDIVIDUAL) },
                    onOpenStatistics = { navController.navigate(AppRoutes.STATISTICS) },
                    onOpenReports = { navController.navigate(AppRoutes.REPORT) }
                )
            }
            composable(AppRoutes.NUMERIC) {
                LedgerScreen(
                    ledgerViewModel,
                    activeShop?.name.orEmpty(),
                    onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                    onOpenReport = { navController.navigate(AppRoutes.REPORT) },
                    onOpenStatistics = { navController.navigate(AppRoutes.STATISTICS) },
                    onOpenIndividualLedger = { navController.navigate(AppRoutes.INDIVIDUAL) }
                )
            }
            composable(AppRoutes.INDIVIDUAL) {
                val records by ledgerViewModel.recordsState.collectAsState()
                val settings by ledgerViewModel.settingsState.collectAsState()
                IndividualLedger(
                    records = records,
                    viewModel = ledgerViewModel,
                    settings = settings,
                    shopName = activeShop?.name.orEmpty(),
                    onOpenReport = { navController.navigate(AppRoutes.REPORT) }
                )
            }
            composable(AppRoutes.SHOPS) { ShopsScreen(shopViewModel) }
            composable(AppRoutes.STATISTICS) {
                val summary by ledgerViewModel.summaryState.collectAsState()
                val settings by ledgerViewModel.settingsState.collectAsState()
                val records by ledgerViewModel.recordsState.collectAsState()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val workingDays = records.map { dateFormat.format(Date(it.timestamp)) }.distinct().size
                StatisticsScreen(
                    summary = summary,
                    workingDays = workingDays,
                    currency = settings.currencySymbol,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.SETTINGS) {
                SettingsScreen(settingsViewModel, ledgerViewModel) { navController.popBackStack() }
            }
            composable(AppRoutes.REPORT) {
                ReportScreen(ledgerViewModel) { navController.popBackStack() }
            }
        }
    }
}
