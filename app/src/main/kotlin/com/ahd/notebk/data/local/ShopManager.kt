package com.ahd.notebk.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

data class Shop(
    val id: Long,
    val name: String,
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

private val Context.shopDataStore by preferencesDataStore(name = "notebk_shops")

class ShopManager(private val context: Context) {
    private val shopsKey = stringPreferencesKey("shops_json")
    private val selectedKey = longPreferencesKey("selected_shop_id")

    val shopsFlow: Flow<List<Shop>> = context.shopDataStore.data.map { preferences ->
        decode(preferences[shopsKey].orEmpty())
    }

    val selectedShopIdFlow: Flow<Long?> = context.shopDataStore.data.map { preferences ->
        preferences[selectedKey]
    }

    suspend fun ensureDefaultShop(defaultName: String) {
        context.shopDataStore.edit { preferences ->
            val current = decode(preferences[shopsKey].orEmpty())
            if (current.isEmpty()) {
                val shop = Shop(id = 1L, name = defaultName.ifBlank { "ورشة الخياطة الرقمية" })
                preferences[shopsKey] = encode(listOf(shop))
                preferences[selectedKey] = shop.id
            } else if (preferences[selectedKey] == null) {
                preferences[selectedKey] = current.first().id
            }
        }
    }

    suspend fun createShop(name: String, ownerName: String, phone: String, address: String) {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "اسم المحل مطلوب" }
        context.shopDataStore.edit { preferences ->
            val current = decode(preferences[shopsKey].orEmpty())
            val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
            val shop = Shop(nextId, cleanName, ownerName.trim(), phone.trim(), address.trim())
            preferences[shopsKey] = encode(current + shop)
            preferences[selectedKey] = shop.id
        }
    }

    suspend fun selectShop(id: Long) {
        context.shopDataStore.edit { preferences -> preferences[selectedKey] = id }
    }

    suspend fun deleteShop(id: Long) {
        context.shopDataStore.edit { preferences ->
            val current = decode(preferences[shopsKey].orEmpty())
            if (current.size <= 1) return@edit
            val remaining = current.filterNot { it.id == id }
            preferences[shopsKey] = encode(remaining)
            if (preferences[selectedKey] == id) {\n                remaining.firstOrNull()?.id?.let { preferences[selectedKey] = it }\n            }
        }
    }

    private fun encode(shops: List<Shop>): String = JSONArray().apply {
        shops.forEach { shop ->
            put(JSONObject().apply {
                put("id", shop.id)
                put("name", shop.name)
                put("ownerName", shop.ownerName)
                put("phone", shop.phone)
                put("address", shop.address)
                put("createdAt", shop.createdAt)
            })
        }
    }.toString()

    private fun decode(json: String): List<Shop> {
        if (json.isBlank()) return emptyList()
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                add(
                    Shop(
                        id = obj.optLong("id", i.toLong() + 1L),
                        name = obj.optString("name", ""),
                        ownerName = obj.optString("ownerName", ""),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }.filter { it.name.isNotBlank() }
    }
}
