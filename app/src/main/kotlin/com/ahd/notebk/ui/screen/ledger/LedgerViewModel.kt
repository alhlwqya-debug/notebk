package com.ahd.notebk.ui.screen.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahd.notebk.data.local.AppDatabase
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.data.local.AppSettingsManager
import com.ahd.notebk.data.repository.BackupManager
import com.ahd.notebk.data.repository.LedgerRepository
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.engine.PieceCalculator
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
    private val repository = LedgerRepository(database.recordDao())

    val settingsManager = AppSettingsManager(application)
    val settingsState: StateFlow<AppSettings> = settingsManager.settingsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings()
    )
    val recordsState: StateFlow<List<TailorRecord>> = repository.allRecords.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    private val _summaryState = MutableStateFlow(LedgerSummary())
    val summaryState: StateFlow<LedgerSummary> = _summaryState.asStateFlow()
    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()
    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allRecords.collect { records -> _summaryState.value = LedgerEngine.calculateSummary(records) }
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun clearOperationError() { _operationError.value = null }
    fun clearBackupMessage() { _backupMessage.value = null }

    fun addNewRecord(dayName: String, quantity: Int, amount: Double, isCredit: Boolean, unitPrice: Double,
                     note: String, pieceType: String = "ثابت كامل", personName: String = "",
                     timestamp: Long = System.currentTimeMillis()) {
        if (dayName.isBlank() || quantity < 0 || amount < 0.0 || unitPrice < 0.0) {
            _operationError.value = "بيانات السجل غير صالحة"; return
        }
        val finalAmount = if (isCredit) PieceCalculator.calculateTotal(quantity, unitPrice) else amount
        viewModelScope.launch {
            runCatching { repository.addRecord(dayName, quantity, finalAmount, isCredit, note, pieceType, unitPrice, personName, timestamp) }
                .onFailure { _operationError.value = it.message ?: "تعذر حفظ السجل" }
        }
    }

    fun updateRecord(record: TailorRecord, dayName: String, quantity: Int, amount: Double, isCredit: Boolean, unitPrice: Double, note: String, pieceType: String, personName: String, timestamp: Long) {
        viewModelScope.launch {
            runCatching {
                repository.updateRecord(record, dayName, quantity, amount, isCredit, unitPrice, note, pieceType, personName, timestamp)
            }.onFailure { _operationError.value = it.message ?: "تعذر تعديل السجل" }
        }
    }

    fun deleteRecord(record: TailorRecord) {
        viewModelScope.launch { runCatching { repository.deleteRecord(record) }.onFailure { _operationError.value = it.message ?: "تعذر حذف السجل" } }
    }

    fun exportBackup(): String = BackupManager.export(recordsState.value, settingsState.value)

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            runCatching {
                val backup = BackupManager.import(jsonString)
                repository.restoreAll(backup.records)
                settingsManager.updateSettings(backup.settings)
            }.onSuccess {
                _backupMessage.value = "تم استرجاع النسخة الاحتياطية بنجاح"
            }.onFailure {
                _backupMessage.value = it.message ?: "تعذر استيراد النسخة الاحتياطية"
            }
        }
    }

    override fun onCleared() {
        AppDatabase.closeInstance()
        super.onCleared()
    }
}
