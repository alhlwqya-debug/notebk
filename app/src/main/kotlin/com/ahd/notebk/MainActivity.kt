package com.ahd.notebk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ahd.notebk.ui.navigation.AppNavigation
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.screen.settings.SettingsViewModel
import com.ahd.notebk.ui.screen.splash.SplashScreen
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
import com.ahd.notebk.ui.screen.statistics.StatisticsScreen
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk.git
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk.git
import com.ahd.notebk.ui.theme.NotebkTheme

class MainActivity : ComponentActivity() {
    private val ledgerViewModel: LedgerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotebkTheme {
                Surface(Modifier.fillMaxSize()) {
<<<<<<< HEAD
                    var showSplash by rememberSaveable { mutableStateOf(true) }
                    if (showSplash) {
                        SplashScreen { showSplash = false }
                    } else {
                        AppNavigation(rememberNavController(), ledgerViewModel, settingsViewModel)
=======
<<<<<<< HEAD
                    var showSplash by rememberSaveable { mutableStateOf(true) }
                    if (showSplash) {
                        SplashScreen { showSplash = false }
                    } else {
                        AppNavigation(rememberNavController(), ledgerViewModel, settingsViewModel)
=======
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "ledger") {
                        composable("ledger") {
                            LedgerScreen(
                                viewModel = ledgerViewModel,
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenReport = { navController.navigate("report") },
                                onOpenStatistics = { navController.navigate("statistics") },
                                onOpenIndividualLedger = { navController.navigate("individual") }
                            )
                        }
                        composable("statistics") {
                            val summary = ledgerViewModel.summaryState.value
                            val settings = ledgerViewModel.settingsState.value
                            val records = ledgerViewModel.recordsState.value
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val workingDays = records
                                .map { dateFormat.format(Date(it.timestamp)) }
                                .distinct()
                                .size
                            StatisticsScreen(
                                summary = summary,
                                workingDays = workingDays,
                                currency = settings.currencySymbol,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("individual") {
                            IndividualLedger(
                                records = ledgerViewModel.recordsState.value,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(settingsViewModel, ledgerViewModel) { navController.popBackStack() }
                        }
                        composable("report") {
                            ReportScreen(ledgerViewModel) { navController.popBackStack() }
                        }
                        }
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk.git
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk.git
                    }
                }
            }
        }
    }
}
