package com.ahd.notebk.data.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class HomeAdvertisement(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String,
    val images: List<String>,
    val buttonText: String,
    val targetUrl: String
)

/**
 * Reads only advertisements visible under the public Supabase RLS policy.
 * Admin-only create/update/delete operations are never exposed by this repository.
 */
object AdvertisementRepository {
    private const val CACHE_PREFS = "notebk_advertisement_cache"
    private const val CACHE_KEY = "active_ads_json"

    fun loadCached(context: Context): List<HomeAdvertisement> {
        val raw = context.applicationContext.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            .getString(CACHE_KEY, null) ?: return emptyList()
        return parseAds(raw)
    }

    private fun saveCache(context: Context, raw: String) {
        context.applicationContext.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            .edit().putString(CACHE_KEY, raw).putLong("last_successful_refresh", System.currentTimeMillis()).apply()
    }
    suspend fun loadActive(context: Context? = null): List<HomeAdvertisement> = withContext(Dispatchers.IO) {
        val connection = (URL(
            "${SupabaseConfig.URL}/rest/v1/notebk_ads?select=id,title,body,image_url,image_urls,button_text,target_url&is_active=eq.true&order=sort_order.asc,created_at.desc"
        ).openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12_000
            connection.readTimeout = 12_000
            connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (code !in 200..299) return@withContext context?.let { loadCached(it) } ?: emptyList()
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            val parsed = parseAds(raw)
            if (context != null) saveCache(context, raw)
            parsed
        } catch (_: Exception) {
            context?.let { loadCached(it) } ?: emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseImageUrls(array: JSONArray?, legacyImageUrl: String): List<String> {
        val urls = if (array == null) emptyList() else
            (0 until array.length()).mapNotNull { index ->
                array.optString(index).takeIf { it.startsWith("https://") || it.startsWith("http://") }
            }
        return (urls.ifEmpty {
            listOf(legacyImageUrl).filter { it.startsWith("https://") || it.startsWith("http://") }
        }).distinct().take(10)
    }

    private fun parseAds(raw: String): List<HomeAdvertisement> {
        val array = JSONArray(raw)
        return (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val title = item.optString("title").trim()
            if (title.isBlank()) return@mapNotNull null
            HomeAdvertisement(
                id = item.optString("id"),
                title = title,
                body = item.optString("body"),
                imageUrl = item.optString("image_url"),
                images = parseImageUrls(item.optJSONArray("image_urls"), item.optString("image_url")),
                buttonText = item.optString("button_text", "معرفة المزيد").ifBlank { "معرفة المزيد" },
                targetUrl = item.optString("target_url")
            )
        }
    }
}
