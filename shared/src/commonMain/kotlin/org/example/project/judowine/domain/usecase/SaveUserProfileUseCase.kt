package org.example.project.judowine.domain.usecase

import com.example.data.repository.UserRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.example.project.judowine.domain.mapper.toDomainModel
import org.example.project.judowine.domain.mapper.toEntity
import org.example.project.judowine.domain.model.User

/**
 * Use case for saving and updating user profile data.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-1, Task 3.4: SaveUserProfileUseCase
 *
 * This use case encapsulates the business logic for creating and updating user profiles.
 * It handles validation, timestamp management, and persistence orchestration.
 *
 * @property userRepository The repository for user data access
 */
class SaveUserProfileUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Create a new user profile.
     *
     * @param connpassId The user's connpass ID (must not be blank)
     * @param nickname The user's nickname (must not be blank)
     * @return Result containing the created User with assigned ID, or failure with error message
     */
    suspend fun createUserProfile(
        connpassId: String,
        nickname: String
    ): Result<User> {
        // Validation
        if (connpassId.isBlank()) {
            return Result.failure(IllegalArgumentException("connpassId cannot be blank"))
        }
        if (nickname.isBlank()) {
            return Result.failure(IllegalArgumentException("nickname cannot be blank"))
        }

        // Check for duplicate connpass ID
        val existingUser = userRepository.getUserByConnpassId(connpassId)?.toDomainModel()
        if (existingUser != null) {
            return Result.failure(
                IllegalStateException("A user with connpassId '$connpassId' already exists")
            )
        }

        // Create new user
        val now = Clock.System.now()
        val newUser = User(
            id = 0, // Will be assigned by database
            connpassId = connpassId,
            nickname = nickname,
            createdAt = now,
            updatedAt = now
        )

        return try {
            val savedId = userRepository.saveUser(newUser.toEntity())
            val savedUser = newUser.copy(id = savedId)
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing user's profile.
     *
     * @param user The user to update (must have a valid ID > 0)
     * @param newNickname Optional new nickname (if null, nickname is not changed)
     * @return Result containing the updated User, or failure with error message
     */
    suspend fun updateUserProfile(
        user: User,
        newNickname: String? = null
    ): Result<User> {
        // Validation
        if (user.id == 0L) {
            return Result.failure(IllegalArgumentException("Cannot update a user with id = 0 (new user)"))
        }

        // Prepare updated user
        val updatedUser = if (newNickname != null && newNickname != user.nickname) {
            if (newNickname.isBlank()) {
                return Result.failure(IllegalArgumentException("nickname cannot be blank"))
            }
            user.updateNickname(newNickname, Clock.System.now())
        } else {
            user.copy(updatedAt = Clock.System.now())
        }

        return try {
            userRepository.updateUser(updatedUser.toEntity())
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a user profile.
     *
     * @param user The user to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteUserProfile(user: User): Result<Unit> {
        return try {
            userRepository.deleteUser(user.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save or update a user profile (upsert operation).
     * Creates a new user if id is 0, otherwise updates existing user.
     *
     * @param connpassId The user's connpass ID
     * @param nickname The user's nickname
     * @param existingUserId Optional existing user ID (0 or null for new user)
     * @return Result containing the saved User, or failure with error message
     */
    suspend fun saveOrUpdateUserProfile(
        connpassId: String,
        nickname: String,
        existingUserId: Long? = null
    ): Result<User> {
        return if (existingUserId == null || existingUserId == 0L) {
            // Create new user
            createUserProfile(connpassId, nickname)
        } else {
            // Update existing user
            val existingUser = userRepository.getUserById(existingUserId)?.toDomainModel()
                ?: return Result.failure(IllegalArgumentException("User with id $existingUserId not found"))

            updateUserProfile(existingUser, newNickname = nickname)
        }
    }
}
