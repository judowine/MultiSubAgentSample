package org.example.project.judowine.domain.usecase

import com.example.data.repository.UserSearchRepository
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.ConnpassUser

/**
 * Use Case for searching connpass users by nickname.
 *
 * This is an Application Service in DDD terminology, orchestrating
 * domain logic and coordinating with the repository layer.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-3, Task 5.3: SearchUsersUseCase for user search feature
 *
 * Responsibilities:
 * - Validate input (query string)
 * - Call UserSearchRepository to fetch UserDto list from API
 * - Convert DTOs to domain models (ConnpassUser) via mapper
 * - Return domain-typed Result
 *
 * Architecture Flow: UI → Use Case → Repository → API
 * - UI calls this use case with search query
 * - Use case validates and delegates to repository
 * - Repository returns UserDto (data layer type)
 * - Use case maps to ConnpassUser (domain layer type)
 * - UI receives Result<List<ConnpassUser>>
 *
 * @property userSearchRepository Repository for user search operations
 */
class SearchUsersUseCase(
    private val userSearchRepository: UserSearchRepository
) {
    /**
     * Searches for connpass users by nickname (partial match supported).
     *
     * Business rules:
     * - Empty/blank query returns empty list (valid but no-op search)
     * - Query is trimmed before validation
     * - API returns up to 100 results per request (repository default)
     *
     * @param query User's nickname to search for (partial match)
     * @return Result containing list of ConnpassUser domain models, or error if API fails
     */
    suspend fun execute(query: String): Result<List<ConnpassUser>> {
        // Validate and normalize input
        val trimmedQuery = query.trim()

        // Empty query is valid but returns empty list (no API call needed)
        if (trimmedQuery.isBlank()) {
            return Result.success(emptyList())
        }

        // Delegate to repository and transform result
        return userSearchRepository.searchUsers(nickname = trimmedQuery)
            .map { userDtoList ->
                // Convert DTOs to domain models using mapper
                userDtoList.toDomainModels()
            }
    }

    /**
     * Searches for connpass users with pagination support.
     *
     * Business rules:
     * - Same validation as execute(query)
     * - Supports custom pagination parameters
     * - start: 1-based index (connpass API convention)
     * - count: Maximum results per page (max: 100)
     *
     * @param query User's nickname to search for (partial match)
     * @param start Starting index for pagination (1-based, default: 1)
     * @param count Maximum number of results (default: 100, max: 100)
     * @return Result containing list of ConnpassUser domain models, or error if API fails
     */
    suspend fun execute(
        query: String,
        start: Int = 1,
        count: Int = 100
    ): Result<List<ConnpassUser>> {
        // Validate and normalize input
        val trimmedQuery = query.trim()

        // Empty query is valid but returns empty list (no API call needed)
        if (trimmedQuery.isBlank()) {
            return Result.success(emptyList())
        }

        // Validate pagination parameters
        require(start >= 1) { "Start index must be >= 1 (1-based pagination)" }
        require(count in 1..100) { "Count must be between 1 and 100" }

        // Delegate to repository with pagination and transform result
        return userSearchRepository.searchUsers(
            nickname = trimmedQuery,
            start = start,
            count = count
        ).map { userDtoList ->
            // Convert DTOs to domain models using mapper
            userDtoList.toDomainModels()
        }
    }
}
