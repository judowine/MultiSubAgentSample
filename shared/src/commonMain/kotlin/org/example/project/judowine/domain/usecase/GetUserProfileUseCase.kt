package org.example.project.judowine.domain.usecase

import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.judowine.domain.mapper.toDomainModel
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.User

/**
 * Use case for retrieving user profile data.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-1, Task 3.3: GetUserProfileUseCase
 *
 * This use case encapsulates the business logic for fetching user profiles.
 * It provides multiple query methods to retrieve users by different criteria.
 *
 * @property userRepository The repository for user data access
 */
class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Get the primary/current user profile.
     * For single-user app scenarios.
     *
     * @return The primary user, or null if no user exists
     */
    suspend fun getPrimaryUser(): User? {
        return userRepository.getPrimaryUser()?.toDomainModel()
    }

    /**
     * Get a user by their ID.
     *
     * @param userId The user's primary key ID
     * @return The user, or null if not found
     */
    suspend fun getUserById(userId: Long): User? {
        return userRepository.getUserById(userId)?.toDomainModel()
    }

    /**
     * Get a user by their connpass ID.
     *
     * @param connpassId The connpass ID
     * @return The user, or null if not found
     */
    suspend fun getUserByConnpassId(connpassId: String): User? {
        if (connpassId.isBlank()) {
            return null
        }
        return userRepository.getUserByConnpassId(connpassId)?.toDomainModel()
    }

    /**
     * Get all users as a Flow for reactive updates.
     *
     * @return Flow of all users
     */
    fun getAllUsers(): Flow<List<User>> {
        return userRepository.getAllUsers().map { it.toDomainModels() }
    }

    /**
     * Check if any user profile exists in the system.
     *
     * @return true if at least one user exists, false otherwise
     */
    suspend fun hasAnyUser(): Boolean {
        return userRepository.getPrimaryUser() != null
    }
}
