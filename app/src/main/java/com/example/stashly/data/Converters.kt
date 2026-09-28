package com.example.stashly.data

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStashType(type: StashType): String {
        return type.name
    }

    @TypeConverter
    fun toStashType(value: String): StashType {
        return try {
            StashType.valueOf(value)
        } catch (e: Exception) {
            StashType.NOTE
        }
    }

    @TypeConverter
    fun fromTags(tags: List<String>): String {
        return tags.joinToString("|||")
    }

    @TypeConverter
    fun toTags(data: String): List<String> {
        if (data.isBlank()) return emptyList()
        return data.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
