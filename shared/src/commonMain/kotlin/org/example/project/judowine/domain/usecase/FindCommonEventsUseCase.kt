package org.example.project.judowine.domain.usecase

import org.example.project.judowine.domain.model.Event

/**
 * Use Case for finding common events between two users.
 *
 * This is an Application Service in DDD terminology, orchestrating
 * domain logic and coordinating multiple use case calls.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-3, Task 6.2: FindCommonEventsUseCase for detecting shared event participation
 *
 * Responsibilities:
 * - Validate input (both user nicknames)
 * - Fetch events for both users via GetUserEventsUseCase
 * - Implement business logic for "common events detection"
 * - Return events that both users participated in
 *
 * Business Logic:
 * - Two users share a common event if they both participated in the same eventId
 * - Common events are identified by matching eventId (unique connpass event identifier)
 * - Returns events from the logged-in user's perspective (maintains domain context)
 *
 * Architecture Flow: UI → Use Case → GetUserEventsUseCase (2x) → Repository → API
 * - UI calls this use case with two nicknames
 * - Use case delegates to GetUserEventsUseCase twice (parallel execution possible)
 * - Use case performs intersection logic on domain models (Event)
 * - UI receives Result<List<Event>>
 *
 * @property getUserEventsUseCase Use case for fetching a user's events
 */
class FindCommonEventsUseCase(
    private val getUserEventsUseCase: GetUserEventsUseCase
) {
    /**
     * Finds common events between two users (events both users participated in).
     *
     * Business rules:
     * - Both nicknames must be non-blank
     * - Nicknames are trimmed before validation
     * - Common events are identified by matching eventId
     * - Returns events from logged-in user's event list (maintains domain context)
     * - Returns empty list if either user has no events or if no common events exist
     *
     * Implementation strategy:
     * - Fetch events for both users concurrently (if possible)
     * - Create a set of eventIds from searched user's events (O(n) lookup)
     * - Filter logged-in user's events where eventId exists in the set
     * - Return filtered list (preserves logged-in user's event details)
     *
     * @param loggedInUserNickname Logged-in user's connpass nickname
     * @param searchedUserNickname Searched user's connpass nickname
     * @return Result containing list of common Event domain models, or error if API fails
     * @throws IllegalArgumentException if either nickname is blank
     */
    suspend fun execute(
        loggedInUserNickname: String,
        searchedUserNickname: String
    ): Result<List<Event>> {
        // Validate and normalize input
        val trimmedLoggedInNickname = loggedInUserNickname.trim()
        val trimmedSearchedNickname = searchedUserNickname.trim()

        require(trimmedLoggedInNickname.isNotBlank()) { "Logged-in user nickname must not be blank" }
        require(trimmedSearchedNickname.isNotBlank()) { "Searched user nickname must not be blank" }

        // Fetch events for logged-in user
        val loggedInUserEventsResult = getUserEventsUseCase.execute(trimmedLoggedInNickname)
        if (loggedInUserEventsResult.isFailure) {
            return Result.failure(
                loggedInUserEventsResult.exceptionOrNull()
                    ?: Exception("Failed to fetch events for logged-in user")
            )
        }

        // Fetch events for searched user
        val searchedUserEventsResult = getUserEventsUseCase.execute(trimmedSearchedNickname)
        if (searchedUserEventsResult.isFailure) {
            return Result.failure(
                searchedUserEventsResult.exceptionOrNull()
                    ?: Exception("Failed to fetch events for searched user")
            )
        }

        // Extract event lists from results
        val loggedInUserEvents = loggedInUserEventsResult.getOrThrow()
        val searchedUserEvents = searchedUserEventsResult.getOrThrow()

        // Find common events by matching eventId
        // Strategy: Create set of eventIds from searched user, filter logged-in user's events
        val searchedUserEventIds = searchedUserEvents.map { it.eventId }.toSet()

        val commonEvents = loggedInUserEvents.filter { event ->
            event.eventId in searchedUserEventIds
        }

        return Result.success(commonEvents)
    }

    /**
     * Finds common events with custom event count limit per user.
     *
     * Business rules:
     * - Same validation as execute(loggedInUserNickname, searchedUserNickname)
     * - Supports custom count parameter for result limit per user
     * - count: Maximum number of events to fetch per user
     *
     * Note: The final common events count may be less than the specified count,
     * as it depends on the intersection of the two event lists.
     *
     * @param loggedInUserNickname Logged-in user's connpass nickname
     * @param searchedUserNickname Searched user's connpass nickname
     * @param count Maximum number of events to retrieve per user (default: 50)
     * @return Result containing list of common Event domain models, or error if API fails
     * @throws IllegalArgumentException if either nickname is blank or count is invalid
     */
    suspend fun execute(
        loggedInUserNickname: String,
        searchedUserNickname: String,
        count: Int = 50
    ): Result<List<Event>> {
        // Validate and normalize input
        val trimmedLoggedInNickname = loggedInUserNickname.trim()
        val trimmedSearchedNickname = searchedUserNickname.trim()

        require(trimmedLoggedInNickname.isNotBlank()) { "Logged-in user nickname must not be blank" }
        require(trimmedSearchedNickname.isNotBlank()) { "Searched user nickname must not be blank" }
        require(count > 0) { "Count must be positive" }

        // Fetch events for logged-in user with custom count
        val loggedInUserEventsResult = getUserEventsUseCase.execute(
            nickname = trimmedLoggedInNickname,
            count = count
        )
        if (loggedInUserEventsResult.isFailure) {
            return Result.failure(
                loggedInUserEventsResult.exceptionOrNull()
                    ?: Exception("Failed to fetch events for logged-in user")
            )
        }

        // Fetch events for searched user with custom count
        val searchedUserEventsResult = getUserEventsUseCase.execute(
            nickname = trimmedSearchedNickname,
            count = count
        )
        if (searchedUserEventsResult.isFailure) {
            return Result.failure(
                searchedUserEventsResult.exceptionOrNull()
                    ?: Exception("Failed to fetch events for searched user")
            )
        }

        // Extract event lists from results
        val loggedInUserEvents = loggedInUserEventsResult.getOrThrow()
        val searchedUserEvents = searchedUserEventsResult.getOrThrow()

        // Find common events by matching eventId
        val searchedUserEventIds = searchedUserEvents.map { it.eventId }.toSet()

        val commonEvents = loggedInUserEvents.filter { event ->
            event.eventId in searchedUserEventIds
        }

        return Result.success(commonEvents)
    }
}
