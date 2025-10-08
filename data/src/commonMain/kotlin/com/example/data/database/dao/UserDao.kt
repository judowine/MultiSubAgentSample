package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user profile operations.
 * Provides CRUD operations for the users table.
 *
 * Implementation by: data-layer-architect
 * PBI-1, Task 1.3: UserDao with insert, update, delete, get operations
 */
@Dao
interface UserDao {
    /**
     * Insert a new user profile.
     * If a user with the same ID already exists, replace it.
     *
     * @param user The user entity to insert
     * @return The row ID of the inserted user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    /**
     * Update an existing user profile.
     *
     * @param user The user entity with updated data
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * Delete a user profile.
     *
     * @param user The user entity to delete
     */
    @Delete
    suspend fun delete(user: UserEntity)

    /**
     * Get a user by their primary key ID.
     *
     * @param id The primary key ID
     * @return The user entity, or null if not found
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Long): UserEntity?

    /**
     * Get a user by their connpass ID.
     * Useful for lookups by connpass identity.
     *
     * @param connpassId The connpass ID
     * @return The user entity, or null if not found
     */
    @Query("SELECT * FROM users WHERE connpassId = :connpassId")
    suspend fun getUserByConnpassId(connpassId: String): UserEntity?

    /**
     * Get all users as a Flow for reactive updates.
     * Ordered by most recently updated first.
     *
     * @return Flow of list of all users
     */
    @Query("SELECT * FROM users ORDER BY updatedAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Get all users (one-shot query).
     * Ordered by most recently updated first.
     *
     * @return List of all users
     */
    @Query("SELECT * FROM users ORDER BY updatedAt DESC")
    suspend fun getAllUsersOnce(): List<UserEntity>

    /**
     * Get the first/primary user (for single-user scenarios).
     * Returns the most recently updated user.
     *
     * @return The primary user entity, or null if no users exist
     */
    @Query("SELECT * FROM users ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getPrimaryUser(): UserEntity?
}
