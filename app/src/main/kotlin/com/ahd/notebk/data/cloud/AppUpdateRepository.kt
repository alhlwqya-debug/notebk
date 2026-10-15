package com.ahd.notebk.data.cloud

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Checks the public AHD Store listing for this exact Android package.
 * Only the public publishable key is embedded in the app.
 */
object AppUpdateRepository {
    private const val STORE_URL = "https://ahdstoer.vercel.app"
    private const val SUPABASE_URL = "https://lwwrckjzjwpfsunvpzgq.supabase.co"
    private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_97RzgO4FM1KQllDaM3ikPw_RKtrMJZP"
    private const val CHANNEL_ID = "app_updates"
    private const val NOTIFICATION_ID = 4801
    private const val PREFS = "ahd_store_updates"
    private const val PREF_NOTIFIED_VERSION = "last_notified_version_code"

    suspend fun checkAndNotify(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val packageName = appContext.packageName
        val installedVersion = installedVersionCode(appContext)
        val encodedPackage = URLEncoder.encode(packageName, Charsets.UTF_8.name())
        val endpoint = "$SUPABASE_URL/rest/v1/apps?select=id,title,version_code,version_name&package_name=eq.$encodedPackage&is_published=eq.true&limit=1"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12_000
            connection.readTimeout = 12_000
            connection.setRequestProperty("apikey", SUPABASE_PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer $SUPABASE_PUBLISHABLE_KEY")
            connection.setRequestProperty("Accept", "application/json")
            if (connection.responseCode !in 200..299) return@withContext

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val apps = JSONArray(response)
            if (apps.length() == 0) return@withContext

            val listing = apps.getJSONObject(0)
            val availableVersion = listing.optLong("version_code", 0L)
            val versionName = listing.optString("version_name", "").ifBlank { "جديد" }
            val appId = listing.optLong("id", 0L)
            if (availableVersion <= installedVersion || availableVersion <= 0L) return@withContext

            val preferences = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (preferences.getLong(PREF_NOTIFIED_VERSION, -1L) == availableVersion) return@withContext

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "تحديثات التطبيقات",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "تنبيهات توفر إصدارات جديدة من تطبيقات متجر AHD" }
                )
            }

            val detailsUrl = "$STORE_URL/app-details.html?id=$appId"
            val openStore = PendingIntent.getActivity(
                appContext,
                NOTIFICATION_ID,
                Intent(Intent.ACTION_VIEW, Uri.parse(detailsUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(appContext.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.stat_sys_download_done)
                .setContentTitle("يتوفر تحديث لتطبيق " + listing.optString("title", "NoteBK"))
                .setContentText("الإصدار الجديد $versionName متاح في متجر AHD. اضغط لعرض التحديث.")
                .setStyle(NotificationCompat.BigTextStyle().bigText("الإصدار $versionName متاح الآن في متجر AHD. افتح صفحة التطبيق لمعرفة التفاصيل وتنزيل التحديث."))
                .setContentIntent(openStore)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            if (Build.VERSION.SDK_INT < 33 ||
                appContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
                preferences.edit().putLong(PREF_NOTIFIED_VERSION, availableVersion).apply()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun installedVersionCode(context: Context): Long {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }
}
