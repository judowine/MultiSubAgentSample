package com.example.data.repository

import com.example.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile operations.
 * Provides a clean API for user data access, abstracting the underlying data sources.
 *
 * Implementation by: data-layer-architect
 * PBI-1, Task 1.5: UserRepository interface
 * Architecture fix: Returns UserEntity (data layer type) instead of domain models.
 * Domain layer (Use Cases in /shared) handles conversion to domain models.
 *
 * This follows the dependency rule: composeApp → shared → data
 * The data module has no knowledge of domain models in /shared.
 */
interface UserRepository {
    /**
     * Get a user by their ID.
     *
     * @param id The user's primary key ID
     * @return The user entity, or null if not found
     */
    suspend fun getUserById(id: Long): UserEntity?

    /**
     * Get a user by their connpass ID.
     *
     * @param connpassId The connpass ID
     * @return The user entity, or null if not found
     */
    suspend fun getUserByConnpassId(connpassId: String): UserEntity?

    /**
     * Get the primary/current user.
     * For single-user scenarios.
     *
     * @return The primary user, or null if no user exists
     */
    suspend fun getPrimaryUser(): UserEntity?

    /**
     * Get all users as a Flow.
     *
     * @return Flow of all user entities
     */
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Save a new user or update an existing one.
     *
     * @param user The user entity to save
     * @return The ID of the saved user
     */
    suspend fun saveUser(user: UserEntity): Long

    /**
     * Update an existing user.
     *
     * @param user The user entity to update
     */
    suspend fun updateUser(user: UserEntity)

    /**
     * Delete a user.
     *
     * @param user The user entity to delete
     */
    suspend fun deleteUser(user: UserEntity)
}
