package com.ahd.notebk.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.data.local.AppSettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val manager = AppSettingsManager(application)
    val settings: StateFlow<AppSettings> = manager.settingsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings()
    )

    fun save(settings: AppSettings) {
        viewModelScope.launch { manager.updateSettings(settings) }
    }
}
