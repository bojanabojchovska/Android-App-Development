package com.example.homework3.worker

import android.content.Context
import androidx.work.*
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.data.download.NewsDownloader
import com.example.homework3.data.repository.NewsRepository
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

object WorkerUtils {

    fun enqueueInitialLoadTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val repository = NewsRepository(NewsDownloader(), AppDatabase.getDatabase(context).newsItemDao())
        val isDatabaseEmpty = runBlocking { repository.isDatabaseEmpty() }

        if (isDatabaseEmpty) {
            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf("TASK_TYPE" to "INITIAL_LOAD"))
                .setConstraints(getNetworkConstraints())
                .build()
            workManager.enqueue(request)
        }
    }


    fun enqueueReloadTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("TASK_TYPE" to "RELOAD"))
            .setConstraints(getNetworkConstraints())
            .build()
        workManager.enqueue(request)
    }

    fun enqueueUrlChangeTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("TASK_TYPE" to "URL_CHANGE"))
            .setConstraints(getNetworkConstraints())
            .build()
        workManager.enqueue(request)
    }

    fun enqueuePeriodicUpdateTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val periodicRequest = PeriodicWorkRequestBuilder<DownloadWorker>(30, TimeUnit.MINUTES)
            .setInputData(workDataOf("TASK_TYPE" to "PERIODIC_UPDATE"))
            .setConstraints(getNetworkConstraints())
            .build()
        workManager.enqueueUniquePeriodicWork(
            "PERIODIC_UPDATE_WORKER",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    private fun getNetworkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    }
}
