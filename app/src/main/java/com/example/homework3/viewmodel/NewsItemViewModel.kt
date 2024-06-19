package com.example.homework3.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.homework3.R
import com.example.homework3.data.NewsItem
import com.example.homework3.data.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsItemViewModel(
    application: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(application) {

    private val TAG = "NewsItemViewModel"

    val newsItems: LiveData<List<NewsItem>> = newsRepository.allNews

    private val _hasError = MutableLiveData<Boolean>()
    val hasError: LiveData<Boolean> = _hasError

    init {
        reload()
    }

    fun downloadNewsItems(newsFeedUrl: String) {
        Log.d(TAG, "Starting download for URL: $newsFeedUrl")
        _hasError.value = false

        viewModelScope.launch {
            val newItems = newsRepository.fetchNews(newsFeedUrl)
            val fetchSuccessful = newItems.isNotEmpty()
            if (fetchSuccessful) {
                Log.d(TAG, "News items fetched successfully")
            } else {
                _hasError.postValue(true)
                Log.d(TAG, "Error fetching news items")
            }
        }
    }

    fun reload() {
        val url = getUrl()
        Log.d(TAG, "Reloading news items with URL: $url")
        downloadNewsItems(url)
    }

    private fun getUrl(): String {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val url = sharedPreferences.getString(
            context.getString(R.string.settings_news_url_key),
            context.getString(R.string.settings_news_url_default)
        ) ?: context.getString(R.string.settings_news_url_default)
        Log.d(TAG, "Current URL from preferences: $url")
        return url
    }

    fun clearNews() {
        viewModelScope.launch {
            newsRepository.clearNews()
        }
    }
}

class NewsItemViewModelProviderFactory(
    private val application: Application,
    private val newsRepository: NewsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsItemViewModel::class.java)) {
            return NewsItemViewModel(application, newsRepository) as T
        }
        throw IllegalArgumentException("Only NewsItemViewModel is allowed")
    }
}
