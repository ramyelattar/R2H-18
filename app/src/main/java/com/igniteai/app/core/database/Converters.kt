package com.igniteai.app.core.database

import androidx.room.TypeConverter

/**
 * Room type converters for non-primitive types.
 *
 * Room can only store primitives (Int, Long, String, ByteArray, etc.)
 * in SQLite. These converters handle any custom types used in entities.
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString(separator = "|")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|")
    }
}
