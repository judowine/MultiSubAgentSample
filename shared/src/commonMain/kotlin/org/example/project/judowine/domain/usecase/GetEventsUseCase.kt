package org.example.project.judowine.domain.usecase

import com.example.data.repository.EventRepository
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.Event

/**
 * Use case for retrieving events from the repository with caching strategy.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-2, Task 4.4: GetEventsUseCase
 *
 * This use case orchestrates the business logic for fetching events from connpass API
 * with intelligent caching. It provides control over cache refresh behavior and
 * handles conversion from data layer types (EventEntity) to domain models (Event).
 *
 * Caching Strategy:
 * - forceRefresh = true: Always fetch from API, clear cache first (pull-to-refresh)
 * - forceRefresh = false: Network-first with cache fallback (normal loading)
 *
 * Error Handling:
 * - Returns Result<List<Event>> for explicit error handling in UI layer
 * - Network errors, database errors, and mapping errors are propagated via Result
 *
 * @property eventRepository The repository for event data access (injected from /data module)
 */
class GetEventsUseCase(
    private val eventRepository: EventRepository
) {
    /**
     * Fetch events for a specific user with optional cache refresh.
     *
     * This method orchestrates the following domain logic:
     * 1. Delegates to EventRepository to fetch EventEntity list
     * 2. Converts data layer types (EventEntity) to domain models (Event) using mapper
     * 3. Returns Result for explicit error handling
     *
     * Caching behavior:
     * - When forceRefresh = true: Clears cache before fetching (pull-to-refresh scenario)
     * - When forceRefresh = false: Uses cached data if network fails (offline support)
     *
     * @param userId connpass user ID to fetch events for
     * @param forceRefresh If true, forces fresh API fetch and cache clear; if false, uses cache on network failure
     * @return Result containing list of Event domain models, or error if fetch fails
     */
    suspend fun execute(userId: Long, forceRefresh: Boolean = false): Result<List<Event>> {
        return eventRepository.fetchEvents(userId, forceRefresh)
            .map { eventEntities ->
                // Convert data layer types to domain models
                eventEntities.toDomainModels()
            }
    }

    /**
     * Get cached events immediately without network call.
     *
     * This is a complementary method for scenarios where:
     * - Instant UI rendering is needed (show cached data immediately)
     * - Offline mode is required
     * - Network calls should be avoided (battery/data saving)
     *
     * @return List of cached Event domain models, empty list if no cache
     */
    suspend fun getCachedEvents(): List<Event> {
        return eventRepository.getEventsFromCacheOnce().toDomainModels()
    }

    /**
     * Check if cache is empty.
     * Useful for determining whether to show empty state or loading state.
     *
     * @return true if cache has no events, false otherwise
     */
    suspend fun isCacheEmpty(): Boolean {
        return eventRepository.getCacheCount() == 0
    }
}
