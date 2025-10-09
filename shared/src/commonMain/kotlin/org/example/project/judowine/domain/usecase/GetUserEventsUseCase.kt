package org.example.project.judowine.domain.usecase

import com.example.data.repository.UserSearchRepository
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.Event

/**
 * Use Case for retrieving events that a specific user has participated in.
 *
 * This is an Application Service in DDD terminology, orchestrating
 * domain logic and coordinating with the repository layer.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-3, Task 6.1: GetUserEventsUseCase for viewing another user's event participation
 *
 * Responsibilities:
 * - Validate input (nickname)
 * - Call UserSearchRepository to fetch EventDto list from API
 * - Convert DTOs to domain models (Event) via EventDtoMapper (reused from PBI-2)
 * - Return domain-typed Result
 *
 * Architecture Flow: UI → Use Case → Repository → API
 * - UI calls this use case with target user's nickname
 * - Use case validates and delegates to repository
 * - Repository returns EventDto list (data layer type)
 * - Use case maps to Event list (domain layer type)
 * - UI receives Result<List<Event>>
 *
 * Note: Reuses Event domain model and EventDtoMapper from PBI-2.
 *
 * @property userSearchRepository Repository for user search and event retrieval operations
 */
class GetUserEventsUseCase(
    private val userSearchRepository: UserSearchRepository
) {
    /**
     * Retrieves events that a specific user has participated in.
     *
     * Business rules:
     * - Nickname must be non-blank
     * - Nickname is trimmed before validation
     * - Returns up to 50 events by default (repository default)
     * - Events are returned in reverse chronological order (API behavior)
     *
     * @param nickname User's connpass nickname
     * @return Result containing list of Event domain models, or error if API fails
     * @throws IllegalArgumentException if nickname is blank
     */
    suspend fun execute(nickname: String): Result<List<Event>> {
        // Validate and normalize input
        val trimmedNickname = nickname.trim()
        require(trimmedNickname.isNotBlank()) { "Nickname must not be blank" }

        // Delegate to repository and transform result
        return userSearchRepository.getUserEvents(nickname = trimmedNickname)
            .map { eventDtoList ->
                // Convert DTOs to domain models using EventDtoMapper (from PBI-2)
                eventDtoList.toDomainModels()
            }
    }

    /**
     * Retrieves events that a specific user has participated in with custom count.
     *
     * Business rules:
     * - Same validation as execute(nickname)
     * - Supports custom count parameter for result limit
     * - count: Maximum number of events to retrieve
     *
     * @param nickname User's connpass nickname
     * @param count Maximum number of events to retrieve (default: 50)
     * @return Result containing list of Event domain models, or error if API fails
     * @throws IllegalArgumentException if nickname is blank or count is invalid
     */
    suspend fun execute(
        nickname: String,
        count: Int = 50
    ): Result<List<Event>> {
        // Validate and normalize input
        val trimmedNickname = nickname.trim()
        require(trimmedNickname.isNotBlank()) { "Nickname must not be blank" }
        require(count > 0) { "Count must be positive" }

        // Delegate to repository with custom count and transform result
        return userSearchRepository.getUserEvents(
            nickname = trimmedNickname,
            count = count
        ).map { eventDtoList ->
            // Convert DTOs to domain models using EventDtoMapper (from PBI-2)
            eventDtoList.toDomainModels()
        }
    }
}
