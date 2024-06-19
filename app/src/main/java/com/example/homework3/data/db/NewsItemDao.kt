package com.example.homework3.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework3.data.NewsItem


@Dao
interface NewsItemDao {

    @Query("SELECT * from news_item")
    fun getAllNews(): LiveData<List<NewsItem>>

    @Query("SELECT * from news_item WHERE title = :xyz")
    suspend fun findNewsByTitle(xyz: String): NewsItem?

    @Query("SELECT * FROM news_item WHERE uniqueIdentifier = :uniqueIdentifier")
    suspend fun findNewsById(uniqueIdentifier: String): NewsItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)//if data comes with the same id (maybe updated data), that data in the table will be replaced with the updated one
    suspend fun insertCards(cards: List<NewsItem>): List<Long>

    @Query("DELETE FROM news_item")
    suspend fun deleteAllNews()


    @Query("DELETE FROM news_item WHERE publicationDate < :timeInMillis")
    suspend fun deleteOldNewsItems(timeInMillis: Long)

    @Query("SELECT COUNT(*) FROM news_item")
    suspend fun getNewsCount(): Int

    @Query("SELECT * from news_item")
    suspend fun getAllNewsSync(): List<NewsItem>


}