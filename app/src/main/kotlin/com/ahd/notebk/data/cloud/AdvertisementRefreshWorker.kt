package com.ahd.notebk.data.cloud

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Refreshes the public advertisement cache while the app is in the background.
 * WorkManager applies Android's network and battery scheduling constraints.
 */
class AdvertisementRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            AdvertisementRepository.loadActive(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
