package com.example.homework3

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.example.homework3.data.NewsItem
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.databinding.ActivityDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {

    companion object {
        const val ITEM_KEY = "item"
        const val LOG_TAG = "DetailsActivity"
    }

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent?.extras?.getSerializable(ITEM_KEY) as? NewsItem
        val extras = intent?.extras
        Log.d(LOG_TAG, "Intent extras: $extras")


        if (item != null) {
            Log.i(LOG_TAG, "Received item ID from intent: ${item.uniqueIdentifier}")
            displayNewsItem(item)
        } else {
            // Handle the case where no news item is present
            Log.e(LOG_TAG, "No news item found in intent")
        }
    }


    private fun fetchNewsItemById(itemId: String) {
        val database = AppDatabase.getDatabase(this)
        val newsItemDao = database.newsItemDao()

        // Using Coroutine to fetch data in background
        CoroutineScope(Dispatchers.IO).launch {
            val newsItem = newsItemDao.findNewsById(itemId)
            withContext(Dispatchers.Main) {
                newsItem?.let { displayNewsItem(it) }
            }
        }
    }

    private fun displayNewsItem(item: NewsItem) {
        binding.tvCardTitle.text = item.title
        binding.tvAuthor.text = item.author

        // Check the preference value
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val showImages = preferences.getBoolean("showImages", false)

        // Load the image if showImages is true
        if (showImages) {
            val imageUrl = item.imageUrl
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivCard)
        } else {
            // Hide the ImageView if showImages is false
            binding.ivCard.visibility = View.GONE
        }

        val htmlDescription = item.description.toString() // Assuming item.description contains the HTML content
        val webView: WebView = findViewById(R.id.webView)
        webView.loadDataWithBaseURL(null, htmlDescription, "text/html", "utf-8", null)

        binding.tvPublicationDate.text = item.publicationDate.toString()
        binding.tvKeywords.text = item.keywords.joinToString("\n")

        Log.i(LOG_TAG, item.description.toString())

        binding.btnfullstory.setOnClickListener {
            // Open the link in the default browser of the device
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.fullArticleLink))
            startActivity(intent)
        }
    }
}
