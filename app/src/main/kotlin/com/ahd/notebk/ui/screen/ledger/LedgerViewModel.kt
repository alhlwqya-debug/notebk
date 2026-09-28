package com.ahd.notebk.ui.screen.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahd.notebk.data.local.AppDatabase
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.data.local.AppSettingsManager
import com.ahd.notebk.data.repository.LedgerRepository
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.domain.model.TailorRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = LedgerRepository(database, database.recordDao())
    val settingsManager = AppSettingsManager(application)

    val settingsState: StateFlow<AppSettings> = settingsManager.settingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
    val recordsState: StateFlow<List<TailorRecord>> = repository.allRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val summaryState: StateFlow<LedgerSummary> = MutableStateFlow(LedgerSummary()).also { target ->
        viewModelScope.launch { repository.allRecords.collect { target.value = LedgerEngine.calculateSummary(it) } }
    }.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun addNewRecord(dayName: String, quantity: Int, amount: Double, isCredit: Boolean, note: String, pieceType: String = "ثابت كامل") {
        if (dayName.isBlank() || quantity < 0 || amount < 0.0) return
        viewModelScope.launch {
            runCatching { repository.addRecord(dayName, quantity, amount, isCredit, note, pieceType, System.currentTimeMillis()) }
        }
    }

    fun deleteRecord(record: TailorRecord) { viewModelScope.launch { runCatching { repository.deleteRecord(record) } } }
    fun exportBackup(): String = LedgerEngine.exportToJson(recordsState.value)
    fun importBackup(jsonString: String) { viewModelScope.launch { runCatching { repository.restoreAll(LedgerEngine.importFromJson(jsonString)) } } }
}
