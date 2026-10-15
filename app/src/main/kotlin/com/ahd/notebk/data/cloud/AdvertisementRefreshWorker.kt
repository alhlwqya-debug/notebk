package com.ahd.notebk.data.cloud

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AdvertisementRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        AdvertisementRepository.loadActive(applicationContext)
        // Update checking is independent: a store/API hiccup must not fail ad refresh.
        runCatching { AppUpdateRepository.checkAndNotify(applicationContext) }
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}
