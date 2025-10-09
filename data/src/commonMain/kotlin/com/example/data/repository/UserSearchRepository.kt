package com.example.data.repository

import com.example.data.network.dto.EventDto
import com.example.data.network.dto.UserDto

/**
 * Repository interface for user search and discovery.
 * Abstracts connpass API user search functionality from the domain layer.
 *
 * Implementation by: data-layer-architect
 * PBI-3, Task 2.9: UserSearchRepository interface for user search API
 *
 * Data Flow Strategy:
 * - API-only repository (no local database persistence as per PBI-3 scope)
 * - UserDto represents OTHER USERS being searched (distinct from UserEntity for profile)
 * - Reuses existing EventRepository logic for fetching user's participated events
 *
 * Note: Returns DTOs (data layer types). Domain layer (shared module)
 * will convert DTOs → domain models via mappers if needed.
 */
interface UserSearchRepository {
    /**
     * Search users by nickname (partial match supported).
     * Uses connpass API v2 /users/ endpoint.
     *
     * API-only operation (no caching as per PBI-3 requirements).
     *
     * @param nickname User's nickname to search for (partial match supported)
     * @param start Pagination start index (1-based, default: 1)
     * @param count Maximum number of results (default: 100, max: 100)
     * @return Result containing list of UserDto or error
     */
    suspend fun searchUsers(
        nickname: String,
        start: Int = 1,
        count: Int = 100
    ): Result<List<UserDto>>

    /**
     * Get events that a specific user has participated in.
     * Uses connpass API v2 /events/ endpoint with nickname parameter.
     *
     * Reuses event fetching logic to get a user's activity history.
     * Useful for viewing another user's event participation.
     *
     * @param nickname User's nickname to fetch events for
     * @param count Maximum number of events to retrieve (default: 50)
     * @return Result containing list of EventDto or error
     */
    suspend fun getUserEvents(
        nickname: String,
        count: Int = 50
    ): Result<List<EventDto>>
}
