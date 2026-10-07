package com.ahd.notebk

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.ahd.notebk.ui.screen.ledger.work.WorkLedgerScreen
import com.ahd.notebk.ui.screen.auth.AuthScreen
import com.ahd.notebk.data.cloud.SupabaseAuthRepository
import android.content.Context
import android.view.WindowManager
import com.ahd.notebk.ui.screen.splash.SplashScreen
import com.ahd.notebk.ui.theme.NotebkTheme
import com.ahd.notebk.data.local.NotebkSettingsStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setContent {
            val settingsPrefs = getSharedPreferences("add_paper_user", Context.MODE_PRIVATE)
            val centralSettings = remember { NotebkSettingsStore(applicationContext) }
            var darkTheme by remember { mutableStateOf(settingsPrefs.getBoolean("dark_theme", centralSettings.getString("theme_mode", "light") == "dark")) }
            var secureScreen by remember { mutableStateOf(centralSettings.getBoolean("prevent_screenshot", false)) }
            DisposableEffect(settingsPrefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_theme") darkTheme = settingsPrefs.getBoolean("dark_theme", false)
                    if (key == "prevent_screenshot") secureScreen = centralSettings.getBoolean("prevent_screenshot", false)
                }
                settingsPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { settingsPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                    if (secureScreen) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
            }
            NotebkTheme(darkTheme = darkTheme) {
                Surface(Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        var showSplash by remember { mutableStateOf(true) }
                        var showAuth by remember { mutableStateOf(false) }
                        var authFromSettings by remember { mutableStateOf(false) }
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val authRepository = remember { SupabaseAuthRepository(context.applicationContext) }
                        val accountPrefs = remember { context.getSharedPreferences("add_paper_user", Context.MODE_PRIVATE) }
                        if (showSplash) {
                            SplashScreen {
                                showAuth = authRepository.savedSession() == null && !accountPrefs.getBoolean("guest_mode", false)
                                authFromSettings = false
                                showSplash = false
                            }
                        } else if (showAuth) {
                            AuthScreen(
                                allowBack = authFromSettings,
                                onBack = { showAuth = false; authFromSettings = false },
                                onContinue = { showAuth = false; authFromSettings = false }
                            )
                        } else {
                            WorkLedgerScreen(onOpenAuth = { authFromSettings = true; showAuth = true })
                        }
                    }
                }
            }
        }
    }
}
