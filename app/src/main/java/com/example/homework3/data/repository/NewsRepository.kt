package com.example.homework3.data.repository

import android.util.Log
import com.example.homework3.data.NewsItem
import com.example.homework3.data.db.NewsItemDao
import com.example.homework3.data.download.NewsDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/*
This class is responsible for all news data access,
 be it remote via API or local via the database
 */
class NewsRepository(
    private val newsDownloader: NewsDownloader,
    private val newsItemDao: NewsItemDao
) {

    val allNews = newsItemDao.getAllNews()

    suspend fun fetchNews(url: String): List<NewsItem> {
        val fetchedNews = newsDownloader.load(url) ?: emptyList()
        val existingNews = newsItemDao.getAllNewsSync()

        val newNews = fetchedNews.filter { fetchedItem ->
            existingNews.none { existingItem -> existingItem.uniqueIdentifier == fetchedItem.uniqueIdentifier }
        }

        if (newNews.isNotEmpty()) {
            newsItemDao.insertCards(newNews)
        }

        newNews.forEach { newsItem ->
            Log.d("FetchNews", "New news item ID being returned: ${newsItem.uniqueIdentifier}")
        }

        return newNews
    }


    suspend fun clearNews() {
        newsItemDao.deleteAllNews()
        Log.i("NewsRepository", "Previous data deleted from the database")
    }

    suspend fun findNewsByTitle(title: String): NewsItem? {
        return newsItemDao.findNewsByTitle(title)
    }

    suspend fun clearOldNews(days: Int) {
        newsItemDao.deleteOldNewsItems(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong()))
    }

    suspend fun isDatabaseEmpty(): Boolean {
        return newsItemDao.getNewsCount() == 0
    }
}


