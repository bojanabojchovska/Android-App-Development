package com.example.homework3.worker

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homework3.R
import com.example.homework3.data.NewsItem
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.data.download.NewsDownloader
import com.example.homework3.data.repository.NewsRepository
import com.example.homework3.notification.NotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val repository: NewsRepository

    init {
        val database = AppDatabase.getDatabase(appContext)
        repository = NewsRepository(NewsDownloader(), database.newsItemDao())
    }

    override suspend fun doWork(): Result {
        val taskType = inputData.getString("TASK_TYPE")
        return when (taskType) {
            "INITIAL_LOAD" -> performInitialLoad()
            "RELOAD" -> performReload()
            "URL_CHANGE" -> performUrlChange()
            "PERIODIC_UPDATE" -> performPeriodicUpdate()
            else -> Result.failure()
        }
    }

    private suspend fun performInitialLoad(): Result {
        val newItems = repository.fetchNews(getUrl())
        if (newItems.isNotEmpty()) {
            notifyNewItems(newItems)
            return Result.success()
        }
        return Result.failure()
    }

    private suspend fun performReload(): Result {
        repository.clearNews()
        val newItems = repository.fetchNews(getUrl())
        if (newItems.isNotEmpty()) {
            notifyNewItems(newItems)
            return Result.success()
        }
        return Result.failure()
    }

    private suspend fun performUrlChange(): Result {
        repository.clearNews()
        val newItems = repository.fetchNews(getUrl())
        if (newItems.isNotEmpty()) {
            notifyNewItems(newItems)
            return Result.success()
        }
        return Result.failure()
    }

    private suspend fun performPeriodicUpdate(): Result {
        val newItems = repository.fetchNews(getUrl())
        repository.clearOldNews(5)
        if (newItems.isNotEmpty()) {
            notifyNewItems(newItems)
            return Result.success()
        }
        return Result.failure()
    }


    private fun getUrl(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        return sharedPreferences.getString(
            applicationContext.getString(R.string.settings_news_url_key),
            applicationContext.getString(R.string.settings_news_url_default)
        ) ?: applicationContext.getString(R.string.settings_news_url_default)
    }

    private suspend fun notifyNewItems(newItems: List<NewsItem>) {
        newItems.forEach { newsItem ->
            NotificationUtil.postNotification(applicationContext, newsItem)
        }
    }

}
