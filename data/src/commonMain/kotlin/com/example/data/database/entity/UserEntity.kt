package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * Room entity representing a user profile in the local database.
 * Maps to the "users" table.
 *
 * Implementation by: data-layer-architect
 * PBI-1, Task 1.2: User entity with fields for profile management
 *
 * @property id Primary key (auto-generated)
 * @property connpassId User's connpass ID (unique identifier from connpass platform)
 * @property nickname User's display nickname
 * @property createdAt Timestamp when the profile was created
 * @property updatedAt Timestamp when the profile was last updated
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val connpassId: String,
    val nickname: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
