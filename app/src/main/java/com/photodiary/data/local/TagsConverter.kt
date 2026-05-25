package com.photodiary.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class TagsConverter {
    @TypeConverter
    fun fromTags(value: List<String>): String {
        return JSONArray(value).toString()
    }

    @TypeConverter
    fun toTags(value: String): List<String> {
        return try {
            val arr = JSONArray(value)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
