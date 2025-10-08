package org.example.project.judowine.domain.usecase

import com.example.data.repository.EventRepository
import org.example.project.judowine.domain.mapper.toDomainModel
import org.example.project.judowine.domain.model.Event

/**
 * Use case for retrieving a single event by its ID.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-2, Task 4.5: GetEventDetailUseCase
 *
 * This use case orchestrates the business logic for fetching event details.
 * It provides a clean domain-level interface for retrieving individual events
 * and handles conversion from data layer types (EventEntity) to domain models (Event).
 *
 * Fetch Strategy:
 * - Cache-first with API fallback: Checks cache first, fetches from API if not found
 * - This provides optimal performance while ensuring data availability
 *
 * Error Handling:
 * - Returns Result<Event?> for explicit error handling
 * - Returns null if event not found in cache or API
 * - Network errors and database errors are propagated via Result
 *
 * @property eventRepository The repository for event data access (injected from /data module)
 */
class GetEventDetailUseCase(
    private val eventRepository: EventRepository
) {
    /**
     * Fetch a single event by its connpass event ID.
     *
     * This method orchestrates the following domain logic:
     * 1. Delegates to EventRepository to fetch EventEntity by ID
     * 2. Converts data layer type (EventEntity) to domain model (Event) using mapper
     * 3. Returns Result for explicit error handling
     *
     * Repository behavior:
     * - Checks local cache first for optimal performance
     * - Falls back to API if not found in cache
     * - Returns error Result if both cache and API fail
     *
     * @param eventId connpass event ID (must be positive)
     * @return Result containing Event domain model, null if not found, or error if fetch fails
     * @throws IllegalArgumentException if eventId is not positive
     */
    suspend fun execute(eventId: Long): Result<Event?> {
        require(eventId > 0) { "Event ID must be positive" }

        return eventRepository.getEventById(eventId)
            .map { eventEntity ->
                // Convert data layer type to domain model
                eventEntity.toDomainModel()
            }
            .recoverCatching { throwable ->
                // If API/cache fetch fails, return null instead of propagating error
                // This allows UI to show "Event not found" instead of generic error
                null
            }
    }

    /**
     * Get event from cache only, without API fallback.
     *
     * This is a complementary method for scenarios where:
     * - Network calls should be avoided (offline mode, battery saving)
     * - Instant retrieval is needed without waiting for network
     * - You want to check if event exists in cache before triggering API call
     *
     * @param eventId connpass event ID (must be positive)
     * @return Event domain model if found in cache, null otherwise
     * @throws IllegalArgumentException if eventId is not positive
     */
    suspend fun getCachedEventById(eventId: Long): Event? {
        require(eventId > 0) { "Event ID must be positive" }

        return eventRepository.getEventByIdFromCache(eventId)?.toDomainModel()
    }

    /**
     * Check if event exists in cache.
     *
     * Useful for determining whether to show cached data immediately
     * or trigger a network fetch.
     *
     * @param eventId connpass event ID (must be positive)
     * @return true if event exists in cache, false otherwise
     * @throws IllegalArgumentException if eventId is not positive
     */
    suspend fun isEventCached(eventId: Long): Boolean {
        require(eventId > 0) { "Event ID must be positive" }

        return eventRepository.getEventByIdFromCache(eventId) != null
    }
}
