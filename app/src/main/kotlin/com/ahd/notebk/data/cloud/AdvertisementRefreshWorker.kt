package com.ahd.notebk.data.cloud

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AdvertisementRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val adsResult = runCatching {
            AdvertisementRepository.loadActive(applicationContext)
        }
        // Keep update notifications independent from advertisement API availability.
        runCatching { AppUpdateRepository.checkAndNotify(applicationContext) }
        return if (adsResult.isSuccess) Result.success() else Result.retry()
    }
}
