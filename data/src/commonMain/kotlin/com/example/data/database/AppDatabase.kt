package com.example.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.example.data.database.converter.InstantConverter
import com.example.data.database.dao.EventDao
import com.example.data.database.dao.MeetingRecordDao
import com.example.data.database.dao.TagDao
import com.example.data.database.dao.TodoDao
import com.example.data.database.dao.UserDao
import com.example.data.database.entity.EventEntity
import com.example.data.database.entity.MeetingRecordEntity
import com.example.data.database.entity.MeetingRecordTagCrossRef
import com.example.data.database.entity.TagEntity
import com.example.data.database.entity.TodoEntity
import com.example.data.database.entity.UserEntity

/**
 * Main Room database for the application.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-1, Task 1.4: Add User entity to AppDatabase
 * PBI-2, Task 1.8: Add Event entity to AppDatabase
 * PBI-4, Task 1.11: Add MeetingRecord entity to AppDatabase
 * PBI-5, Task 1.19: Add Tag entity and MeetingRecordTagCrossRef to AppDatabase
 *
 * Version History:
 * - Version 1: TodoEntity only
 * - Version 2: Added UserEntity and UserDao with Instant TypeConverter
 * - Version 3: Added EventEntity and EventDao for Event Discovery & Viewing
 * - Version 4: Added MeetingRecordEntity and MeetingRecordDao for Meeting Record Creation
 * - Version 5: Added TagEntity, MeetingRecordTagCrossRef, and notes field to MeetingRecordEntity (PBI-5)
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
 *
 * Version 4 → Version 5 Migration (PBI-5):
 * - Adds 'tags' table with columns: id, name (UNIQUE), createdAt
 * - Adds 'meeting_record_tag_cross_ref' table (many-to-many junction table)
 * - Adds 'notes' column to 'meeting_records' table (nullable TEXT)
 * - Foreign key constraints with CASCADE delete on cross-reference table
 * - No data loss (existing tables remain unchanged)
 * - Migration SQL:
 *   CREATE TABLE tags (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL UNIQUE, createdAt INTEGER NOT NULL);
 *   CREATE TABLE meeting_record_tag_cross_ref (meetingRecordId INTEGER NOT NULL, tagId INTEGER NOT NULL, PRIMARY KEY(meetingRecordId, tagId), FOREIGN KEY(meetingRecordId) REFERENCES meeting_records(id) ON DELETE CASCADE, FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE);
 *   ALTER TABLE meeting_records ADD COLUMN notes TEXT;
 */
@Database(
    entities = [
        TodoEntity::class,
        UserEntity::class,
        EventEntity::class,
        MeetingRecordEntity::class,
        TagEntity::class,
        MeetingRecordTagCrossRef::class
    ],
    version = 5
)
@TypeConverters(InstantConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun meetingRecordDao(): MeetingRecordDao
    abstract fun tagDao(): TagDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
