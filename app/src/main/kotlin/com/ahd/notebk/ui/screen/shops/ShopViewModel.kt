package com.ahd.notebk.ui.screen.shops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahd.notebk.data.local.Shop
import com.ahd.notebk.data.local.ShopManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val manager = ShopManager(application)

    val shops: StateFlow<List<Shop>> = manager.shopsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val selectedShopId: StateFlow<Long?> = manager.selectedShopIdFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    val selectedShop: StateFlow<Shop?> = combine(shops, selectedShopId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { manager.ensureDefaultShop("ورشة الخياطة الرقمية") }
    }

    fun createShop(name: String, ownerName: String, phone: String, address: String, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            runCatching { manager.createShop(name, ownerName, phone, address) }
                .onFailure { onError(it.message ?: "تعذر إنشاء المحل") }
        }
    }

    fun selectShop(id: Long) {
        viewModelScope.launch { manager.selectShop(id) }
    }

    fun deleteShop(id: Long) {
        viewModelScope.launch { manager.deleteShop(id) }
    }
}
