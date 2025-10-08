package com.example.data.repository

import com.example.data.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for event data access.
 * Abstracts data sources (connpass API + local Room database) from the domain layer.
 *
 * Implementation by: project-orchestrator (coordinated by data-layer-architect)
 * PBI-2, Task 2.5: EventRepository interface for API + DB caching
 *
 * Caching Strategy:
 * - Network-first for fresh data (fetchEvents)
 * - Cache-first for offline support (getEventsFromCache)
 * - Automatic cache update after successful API fetch
 *
 * Note: Returns EventEntity (data layer type). Domain layer (shared module)
 * will convert EventEntity → Event domain model via mappers.
 */
interface EventRepository {
    /**
     * Fetch events from connpass API for a specific user.
     * Automatically caches results to local database.
     *
     * Network-first strategy: Always attempts API call, falls back to cache on failure.
     *
     * @param userId connpass user ID (required for fetching user's participated events)
     * @param forceRefresh If true, clears cache before fetching (pull-to-refresh scenario)
     * @return Result containing list of EventEntity or error
     */
    suspend fun fetchEvents(userId: Long, forceRefresh: Boolean = false): Result<List<EventEntity>>

    /**
     * Get cached events from local database as reactive Flow.
     * Emits updates whenever the database changes.
     *
     * Cache-first strategy: Returns cached data immediately, no network call.
     * Useful for offline mode and instant UI rendering.
     *
     * @return Flow of event list sorted by start date (newest first)
     */
    fun getEventsFromCache(): Flow<List<EventEntity>>

    /**
     * Get cached events (one-shot query).
     *
     * @return List of cached events
     */
    suspend fun getEventsFromCacheOnce(): List<EventEntity>

    /**
     * Get a single event by its connpass event ID.
     * Checks cache first, falls back to API if not found.
     *
     * @param eventId connpass event ID
     * @return Result containing EventEntity or error if not found
     */
    suspend fun getEventById(eventId: Long): Result<EventEntity>

    /**
     * Get a single event from cache only (no network fallback).
     *
     * @param eventId connpass event ID
     * @return EventEntity or null if not found in cache
     */
    suspend fun getEventByIdFromCache(eventId: Long): EventEntity?

    /**
     * Clear all cached events.
     * Useful for cache invalidation or testing.
     */
    suspend fun clearCache()

    /**
     * Get count of cached events.
     *
     * @return Number of events in local cache
     */
    suspend fun getCacheCount(): Int
}
