package com.ahd.notebk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ahd.notebk.ui.screen.ledger.LedgerScreen
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.screen.report.ReportScreen
import com.ahd.notebk.ui.screen.settings.SettingsScreen
import com.ahd.notebk.ui.screen.settings.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val ledgerViewModel: LedgerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "ledger") {
                        composable("ledger") {
                            LedgerScreen(
                                viewModel = ledgerViewModel,
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenReport = { navController.navigate("report") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(settingsViewModel, ledgerViewModel) { navController.popBackStack() }
                        }
                        composable("report") {
                            ReportScreen(ledgerViewModel) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }
}
