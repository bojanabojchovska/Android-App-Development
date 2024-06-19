package com.example.homework3.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.homework3.data.NewsItem


@Database(entities = [NewsItem::class], version = 11)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // create abstract methods for all Dao classes
    abstract fun newsItemDao(): NewsItemDao
    // similar to static in Java
    companion object {

        /*
        non thread-safe variant
        private var INSTANCE: AppDatabase? = null

        fun getInstance() : AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = createInstance()
            }
            return INSTANCE
        } */

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // double checking
                val tempInstance = INSTANCE
                if (tempInstance != null) {
                    return tempInstance
                }

                // createInstance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration() // DO NOT use in production
                    // all data is lost for version upgrades
                    .build()
                INSTANCE = instance
                return instance
            }


        }


    }
}