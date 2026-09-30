package com.ahd.notebk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ahd.notebk.ui.navigation.AppNavigation
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.screen.settings.SettingsViewModel
import com.ahd.notebk.ui.screen.splash.SplashScreen
import com.ahd.notebk.ui.theme.NotebkTheme

class MainActivity : ComponentActivity() {
    private val ledgerViewModel: LedgerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotebkTheme {
                Surface(Modifier.fillMaxSize()) {
                    var showSplash by rememberSaveable { mutableStateOf(true) }
                    if (showSplash) {
                        SplashScreen { showSplash = false }
                    } else {
                        AppNavigation(rememberNavController(), ledgerViewModel, settingsViewModel)
                    }
                }
            }
        }
    }
}
