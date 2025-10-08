package org.example.project.judowine.domain.mapper

import com.example.data.database.entity.UserEntity
import org.example.project.judowine.domain.model.User

/**
 * Mapper functions to convert between UserEntity (data layer) and User (domain model).
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-1, Task 3.2: UserEntity ↔ User mapper
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns UserEntity (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 */

/**
 * Converts UserEntity (data layer) to User domain model.
 *
 * @receiver UserEntity from the data layer
 * @return User domain model
 */
fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        connpassId = connpassId,
        nickname = nickname,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Converts a list of UserEntity to a list of User domain models.
 *
 * @receiver List of UserEntity from the data layer
 * @return List of User domain models
 */
fun List<UserEntity>.toDomainModels(): List<User> {
    return map { it.toDomainModel() }
}

/**
 * Converts User domain model to UserEntity (data layer).
 *
 * @receiver User domain model
 * @return UserEntity for persistence
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        connpassId = connpassId,
        nickname = nickname,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
