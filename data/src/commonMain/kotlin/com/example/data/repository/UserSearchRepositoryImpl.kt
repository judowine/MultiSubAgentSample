package com.example.data.repository

import com.example.data.network.ApiException
import com.example.data.network.ConnpassApiClient
import com.example.data.network.dto.EventDto
import com.example.data.network.dto.UserDto

/**
 * Implementation of UserSearchRepository with connpass API integration.
 *
 * Implementation by: data-layer-architect
 * PBI-3, Task 2.9: UserSearchRepository implementation for user search
 *
 * Data Strategy:
 * - API-only operations (no local database caching as per PBI-3 scope)
 * - Network-first: Always fetch fresh data from connpass API
 * - Simple error handling: Propagate exceptions as-is (follows EventRepository pattern)
 *
 * Error Handling Pattern:
 * - API errors (ApiException) are propagated through Result.failure
 * - Network errors are propagated through Result.failure
 * - Unexpected errors are propagated through Result.failure
 * - Follows the same error handling pattern as EventRepositoryImpl
 *
 * Note: More sophisticated error handling (DataError sealed interface) will be
 * implemented in future PBI when comprehensive error handling strategy is designed.
 *
 * @property apiClient Ktor client for connpass API
 */
class UserSearchRepositoryImpl(
    private val apiClient: ConnpassApiClient
) : UserSearchRepository {

    override suspend fun searchUsers(
        nickname: String,
        start: Int,
        count: Int
    ): Result<List<UserDto>> {
        return try {
            val response = apiClient.searchUsers(
                nickname = nickname,
                start = start,
                count = count
            )

            Result.success(response.users)

        } catch (e: ApiException) {
            // API-specific error - propagate as-is
            Result.failure(e)
        } catch (e: Exception) {
            // Unexpected error - wrap in generic exception
            Result.failure(
                Exception("Failed to search users: ${e.message}", e)
            )
        }
    }

    override suspend fun getUserEvents(
        nickname: String,
        count: Int
    ): Result<List<EventDto>> {
        return try {
            val response = apiClient.getEventsByNickname(
                nickname = nickname,
                count = count,
                order = 2  // started_at (upcoming events first)
            )

            Result.success(response.events)

        } catch (e: ApiException) {
            // API-specific error - propagate as-is
            Result.failure(e)
        } catch (e: Exception) {
            // Unexpected error - wrap in generic exception
            Result.failure(
                Exception("Failed to fetch user events: ${e.message}", e)
            )
        }
    }
}

