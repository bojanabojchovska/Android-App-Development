package com.example.homework3.adapter


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.homework3.R
import com.example.homework3.data.NewsItem
import com.example.homework3.databinding.FirstMaterialNewsItemViewBinding
import com.example.homework3.databinding.MaterialNewsItemViewBinding

import kotlin.random.Random

class NewsItemListAdapter(items: List<NewsItem>, val context: Context) : RecyclerView.Adapter<NewsItemListAdapter.ItemViewHolder>() {

    private val logTag = "NewsItemListAdapter"
    var items = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var itemClickListener : ((NewsItem)->Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.e(logTag, "ON CREATE VIEWHOLDER")
        val binding = when(viewType){
            R.layout.material_news_item_view ->{
                MaterialNewsItemViewBinding.inflate(
                    LayoutInflater.from(context), parent, false)
            }
            R.layout.first_material_news_item_view -> {
                FirstMaterialNewsItemViewBinding.inflate(LayoutInflater.from(context), parent, false)
            }
            else ->throw IllegalArgumentException()
        }


        return ItemViewHolder(binding)
    }
    override fun getItemViewType(position: Int): Int {
        if (position == 0){
            return R.layout.first_material_news_item_view
        }
        return R.layout.material_news_item_view
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.e(logTag, "ON BIND VIEWHOLDER $position")
        val magicCard = items[position]
        holder.bind(magicCard)
        holder.itemView.setOnClickListener { itemClickListener?.invoke(magicCard) }
    }

    override fun getItemCount(): Int {
        return items.size
    }
    private fun isShowImagesEnabled(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean("showImages", false)
    }
    inner class ItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(newsItem: NewsItem) {
            when (binding){
                is com.example.homework3.databinding.MaterialNewsItemViewBinding -> {
                    binding.tvCardTitle.text = newsItem.title
                    binding.tvCardAuthor.text = newsItem.author
                    binding.tvCardDate.text = newsItem.publicationDate.toString()

                    val imageUrl = newsItem.imageUrl

                    if (isShowImagesEnabled()) {
                        Glide.with(binding.root)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivCard)
                    } else {
                        // Hide the ImageView if showImages is false
                        binding.ivCard.visibility = View.GONE
                    }

                }
                is FirstMaterialNewsItemViewBinding ->{
                    binding.tvCardTitle.text = newsItem.title
                    binding.tvAuthor.text = newsItem.author
                    binding.tvPublicationDate.text = newsItem.publicationDate.toString()

                    val imageUrl = newsItem.imageUrl

                    if (isShowImagesEnabled()) {
                        Glide.with(binding.root)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivCard)
                    } else {
                        // Hide the ImageView if showImages is false
                        binding.ivCard.visibility = View.GONE
                    }
                }
            }

        }

    }
}

