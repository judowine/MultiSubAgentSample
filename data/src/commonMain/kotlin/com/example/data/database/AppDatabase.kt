package com.example.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.example.data.database.converter.InstantConverter
import com.example.data.database.dao.EventDao
import com.example.data.database.dao.MeetingRecordDao
import com.example.data.database.dao.TodoDao
import com.example.data.database.dao.UserDao
import com.example.data.database.entity.EventEntity
import com.example.data.database.entity.MeetingRecordEntity
import com.example.data.database.entity.TodoEntity
import com.example.data.database.entity.UserEntity

/**
 * Main Room database for the application.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-1, Task 1.4: Add User entity to AppDatabase
 * PBI-2, Task 1.8: Add Event entity to AppDatabase
 * PBI-4, Task 1.11: Add MeetingRecord entity to AppDatabase
 *
 * Version History:
 * - Version 1: TodoEntity only
 * - Version 2: Added UserEntity and UserDao with Instant TypeConverter
 * - Version 3: Added EventEntity and EventDao for Event Discovery & Viewing
 * - Version 4: Added MeetingRecordEntity and MeetingRecordDao for Meeting Record Creation
 *
 * Migration Strategy:
 * - For development: Using destructive migration (database recreated on schema changes)
 * - For production: Proper migrations should be added before first release
 * - Platform-specific builders should configure RoomDatabase.Builder with appropriate migration strategy
 *
 * Version 2 → Version 3 Migration (PBI-2):
 * - Adds 'events' table with columns: id, eventId, title, description, startedAt, endedAt, url, address, limit, accepted, waiting
 * - Reuses existing InstantConverter TypeConverter for startedAt/endedAt fields
 * - No data loss (users table remains unchanged)
 *
 * Version 3 → Version 4 Migration (PBI-4):
 * - Adds 'meeting_records' table with columns: id, eventId, userId, nickname, createdAt
 * - UNIQUE constraint on (eventId, userId) to prevent duplicate records
 * - Foreign key reference from eventId to events table (CASCADE on delete)
 * - Reuses existing InstantConverter TypeConverter for createdAt field
 * - No data loss (users and events tables remain unchanged)
 */
@Database(
    entities = [
        TodoEntity::class,
        UserEntity::class,
        EventEntity::class,
        MeetingRecordEntity::class
    ],
    version = 4
)
@TypeConverters(InstantConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun meetingRecordDao(): MeetingRecordDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
