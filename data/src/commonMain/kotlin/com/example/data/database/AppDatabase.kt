package com.example.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.example.data.database.converter.InstantConverter
import com.example.data.database.dao.TodoDao
import com.example.data.database.dao.UserDao
import com.example.data.database.entity.TodoEntity
import com.example.data.database.entity.UserEntity

/**
 * Main Room database for the application.
 *
 * Implementation by: data-layer-architect
 * PBI-1, Task 1.4: Add User entity to AppDatabase
 *
 * Version History:
 * - Version 1: TodoEntity only
 * - Version 2: Added UserEntity and UserDao with Instant TypeConverter
 *
 * Migration Strategy:
 * - For development: Using destructive migration (database recreated on schema changes)
 * - For production: Proper migrations should be added before first release
 * - Platform-specific builders should configure RoomDatabase.Builder with appropriate migration strategy
 */
@Database(entities = [TodoEntity::class, UserEntity::class], version = 2)
@TypeConverters(InstantConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun userDao(): UserDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
