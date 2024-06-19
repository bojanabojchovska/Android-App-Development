package com.example.homework3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homework3.adapter.NewsItemListAdapter
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.data.download.NewsDownloader
import com.example.homework3.data.repository.NewsRepository
import com.example.homework3.databinding.ActivityMainBinding
import com.example.homework3.notification.NOTIFICATION_CHANNEL_ID
import com.example.homework3.viewmodel.NewsItemViewModel
import com.example.homework3.viewmodel.NewsItemViewModelProviderFactory
import com.example.homework3.worker.WorkerUtils

class NewsItemListActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityMainBinding
    companion object {
        const val LOG_TAG = "MainActivity"
    }

    private val newsItemViewModel: NewsItemViewModel by viewModels {
        NewsItemViewModelProviderFactory(application, createRepository())
    }

    private fun createRepository(): NewsRepository {
        return NewsRepository(
            NewsDownloader(),
            AppDatabase.getDatabase(this).newsItemDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.rvNewsItem.layoutManager = layoutManager

        val adapter = NewsItemListAdapter(emptyList(), this)
        binding.rvNewsItem.adapter = adapter

        adapter.itemClickListener = {
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.ITEM_KEY, it)
            startActivity(intent)
        }

        newsItemViewModel.newsItems.observe(this) { items ->
            adapter.items = items
            adapter.notifyDataSetChanged()
            Log.e("NewsItemListActivity", "Observing the data change")
        }

        newsItemViewModel.hasError.observe(this) { hasError ->
            if (hasError) {
                Toast.makeText(this, "Error loading news items", Toast.LENGTH_SHORT).show()
            }
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(this)

        // Enqueue the initial load if the database is empty
        WorkerUtils.enqueueInitialLoadTask(this)

        // Schedule periodic updates
        WorkerUtils.enqueuePeriodicUpdateTask(this)

        createNotificationChannel()

        val permissions =
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
        requestPermissions(permissions, 123)

        // to read the deep-link data (intent from notification)
        Log.e(LOG_TAG, intent.data.toString())
    }

    // this method is not costly and idempotent, no problem to call it multiple times
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "my notifications", // for real apps use a string resource
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = "notification about news item" // for real apps use a string resource
        NotificationManagerCompat.from(this)
            .createNotificationChannel(notificationChannel)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                val settingsActivityIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsActivityIntent)
                true
            }
            R.id.menu_reload -> {
                WorkerUtils.enqueueReloadTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.settings_news_url_key)) {
            WorkerUtils.enqueueUrlChangeTask(this)
            Log.i("URL LINK CHANGED", sharedPreferences?.getString(key, "") ?: "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}

