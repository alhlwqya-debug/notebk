package com.ahd.notebk.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tailor_app_settings")

data class AppSettings(
    val shopName: String = "ورشة الخياطة الرقمية",
    val defaultPiecePrice: Double = 2000.0,
    val currencySymbol: String = "ر.ي",
    val showDebitCredit: Boolean = true,
    val autoFillToday: Boolean = true
)

class AppSettingsManager(private val context: Context) {
    companion object {
        private val SHOP_NAME = stringPreferencesKey("shop_name")
        private val DEFAULT_PRICE = doublePreferencesKey("default_piece_price")
        private val CURRENCY = stringPreferencesKey("currency_symbol")
        private val SHOW_DEBIT_CREDIT = booleanPreferencesKey("show_debit_credit")
        private val AUTO_FILL_TODAY = booleanPreferencesKey("auto_fill_today")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            shopName = p[SHOP_NAME] ?: "ورشة الخياطة الرقمية",
            defaultPiecePrice = p[DEFAULT_PRICE] ?: 2000.0,
            currencySymbol = p[CURRENCY] ?: "ر.ي",
            showDebitCredit = p[SHOW_DEBIT_CREDIT] ?: true,
            autoFillToday = p[AUTO_FILL_TODAY] ?: true
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { p ->
            p[SHOP_NAME] = settings.shopName
            p[DEFAULT_PRICE] = settings.defaultPiecePrice
            p[CURRENCY] = settings.currencySymbol
            p[SHOW_DEBIT_CREDIT] = settings.showDebitCredit
            p[AUTO_FILL_TODAY] = settings.autoFillToday
        }
    }
}
