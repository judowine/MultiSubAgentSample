package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for event operations.
 * Provides CRUD and query operations for the events table.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-2, Task 1.7: EventDao with CRUD + query operations
 */
@Dao
interface EventDao {
    /**
     * Insert a new event.
     * If an event with the same ID already exists, replace it.
     *
     * @param event The event entity to insert
     * @return The row ID of the inserted event
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    /**
     * Insert multiple events in a single transaction.
     * Replaces events with the same ID if they already exist.
     *
     * @param events List of event entities to insert
     * @return List of row IDs for inserted events
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>): List<Long>

    /**
     * Update an existing event.
     *
     * @param event The event entity with updated data
     */
    @Update
    suspend fun update(event: EventEntity)

    /**
     * Delete an event.
     *
     * @param event The event entity to delete
     */
    @Delete
    suspend fun delete(event: EventEntity)

    /**
     * Get an event by its primary key ID.
     *
     * @param id The primary key ID (local database ID)
     * @return The event entity, or null if not found
     */
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEvent(id: Long): EventEntity?

    /**
     * Get an event by its connpass event ID.
     * Useful for lookups by connpass API identifier.
     *
     * @param eventId The connpass event ID
     * @return The event entity, or null if not found
     */
    @Query("SELECT * FROM events WHERE eventId = :eventId")
    suspend fun getEventByConnpassId(eventId: Long): EventEntity?

    /**
     * Get all events as a Flow for reactive updates.
     * Ordered by start date (newest first) for chronological display.
     *
     * @return Flow of list of all events sorted by startedAt DESC
     */
    @Query("SELECT * FROM events ORDER BY startedAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    /**
     * Get all events (one-shot query).
     * Ordered by start date (newest first).
     *
     * @return List of all events sorted by startedAt DESC
     */
    @Query("SELECT * FROM events ORDER BY startedAt DESC")
    suspend fun getAllEventsOnce(): List<EventEntity>

    /**
     * Get upcoming events (events that haven't ended yet).
     * Useful for filtering future/ongoing events.
     *
     * @param currentTime Current timestamp for comparison
     * @return Flow of upcoming events sorted by startedAt ASC
     */
    @Query("SELECT * FROM events WHERE endedAt >= :currentTime ORDER BY startedAt ASC")
    fun getUpcomingEvents(currentTime: Long): Flow<List<EventEntity>>

    /**
     * Get past events (events that have already ended).
     * Ordered by most recent first.
     *
     * @param currentTime Current timestamp for comparison
     * @return Flow of past events sorted by startedAt DESC
     */
    @Query("SELECT * FROM events WHERE endedAt < :currentTime ORDER BY startedAt DESC")
    fun getPastEvents(currentTime: Long): Flow<List<EventEntity>>

    /**
     * Delete all events.
     * Useful for cache clearing/refresh.
     */
    @Query("DELETE FROM events")
    suspend fun deleteAll()

    /**
     * Get count of all events in the database.
     *
     * @return Total number of events
     */
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int
}
