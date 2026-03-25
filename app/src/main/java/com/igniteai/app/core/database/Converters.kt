package com.igniteai.app.core.database

import androidx.room.TypeConverter

/**
 * Room type converters for non-primitive types.
 *
 * Room can only store primitives (Int, Long, String, ByteArray, etc.)
 * in SQLite. These converters handle any custom types used in entities.
 */
class Converters {

    /**
     * ByteArray is stored directly by Room (as BLOB), but nullable
     * ByteArray needs explicit handling in some cases.
     * Room handles ByteArray natively, so no converter needed for it.
     *
     * Currently all entity fields use primitive types or String/ByteArray,
     * so no custom converters are needed. This class exists as the
     * designated place to add converters as the schema evolves.
     *
     * Common converters to add later:
     * - List<String> ↔ JSON String (for tags, choices)
     * - Enum ↔ String (if we switch from String fields to enums)
     * - LocalDateTime ↔ Long (if we use java.time instead of raw longs)
     */
}
