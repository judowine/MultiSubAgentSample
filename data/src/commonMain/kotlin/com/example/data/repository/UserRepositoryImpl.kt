package com.example.data.repository

import com.example.data.database.dao.UserDao
import com.example.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of UserRepository using Room as the data source.
 *
 * Implementation by: data-layer-architect
 * PBI-1, Task 1.5: UserRepository implementation
 * Architecture fix: Returns UserEntity (data layer type) directly.
 * Domain layer (Use Cases in /shared) handles conversion to domain models via mappers.
 *
 * This implementation provides a single source of truth for user data,
 * currently backed by local Room database. Future enhancements may include
 * remote API synchronization.
 *
 * Note: Made public for Application-level dependency injection.
 * UI components should still only access Use Cases from /shared module.
 *
 * @property userDao The Room DAO for user operations
 */
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserById(id: Long): UserEntity? {
        return userDao.getUser(id)
    }

    override suspend fun getUserByConnpassId(connpassId: String): UserEntity? {
        return userDao.getUserByConnpassId(connpassId)
    }

    override suspend fun getPrimaryUser(): UserEntity? {
        return userDao.getPrimaryUser()
    }

    override fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    override suspend fun saveUser(user: UserEntity): Long {
        return userDao.insert(user)
    }

    override suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    override suspend fun deleteUser(user: UserEntity) {
        userDao.delete(user)
    }
}
