package com.example.homework3.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters{

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringSet(value: String?): Set<String>? {
        return value?.split(",")?.toSet()
    }
}