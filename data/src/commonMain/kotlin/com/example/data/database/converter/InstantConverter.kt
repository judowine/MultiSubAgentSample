package com.example.data.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

/**
 * Room TypeConverter for kotlinx.datetime.Instant.
 * Converts between Instant and Long (epoch milliseconds) for database storage.
 *
 * Implementation by: data-layer-architect
 * PBI-1, Task 1.2 (fix): Add TypeConverter for Instant fields
 */
class InstantConverter {
    /**
     * Convert Instant to Long for database storage.
     *
     * @param instant The Instant to convert
     * @return Epoch milliseconds, or null if instant is null
     */
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    /**
     * Convert Long back to Instant when reading from database.
     *
     * @param epochMillis Epoch milliseconds
     * @return The Instant, or null if epochMillis is null
     */
    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? {
        return epochMillis?.let { Instant.fromEpochMilliseconds(it) }
    }
}
