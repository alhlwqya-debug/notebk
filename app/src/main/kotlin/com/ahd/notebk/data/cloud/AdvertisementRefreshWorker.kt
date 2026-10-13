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
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}
