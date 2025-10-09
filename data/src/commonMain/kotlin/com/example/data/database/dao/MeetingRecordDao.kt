package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.MeetingRecordEntity
import com.example.data.database.entity.MeetingRecordTagCrossRef
import com.example.data.database.entity.MeetingRecordWithTags
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for meeting record operations.
 * Provides CRUD and query operations for the meeting_records table.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-4, Task 1.10: MeetingRecordDao with CRUD + duplicate check operations
 * PBI-5, Task 1.18: Added tag operations and MeetingRecordWithTags queries
 */
@Dao
interface MeetingRecordDao {
    /**
     * Insert a new meeting record.
     * Uses IGNORE strategy to handle duplicates (same eventId + userId).
     * Due to UNIQUE constraint on (eventId, userId), duplicates will be silently ignored.
     *
     * @param meetingRecord The meeting record entity to insert
     * @return The row ID of the inserted record, or -1 if duplicate was ignored
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(meetingRecord: MeetingRecordEntity): Long

    /**
     * Delete a meeting record.
     *
     * @param meetingRecord The meeting record entity to delete
     */
    @Delete
    suspend fun delete(meetingRecord: MeetingRecordEntity)

    /**
     * Get a meeting record by its primary key ID.
     * Returns a Flow for reactive updates.
     *
     * @param id The primary key ID (local database ID)
     * @return Flow of meeting record entity, or null if not found
     */
    @Query("SELECT * FROM meeting_records WHERE id = :id")
    fun getMeetingRecordById(id: Long): Flow<MeetingRecordEntity?>

    /**
     * Get all meeting records as a Flow for reactive updates.
     * Ordered by creation date (newest first) for chronological display.
     *
     * @return Flow of list of all meeting records sorted by createdAt DESC
     */
    @Query("SELECT * FROM meeting_records ORDER BY createdAt DESC")
    fun getAllMeetingRecords(): Flow<List<MeetingRecordEntity>>

    /**
     * Get all meeting records for a specific event.
     * Useful for displaying "People I met at this event".
     *
     * @param eventId The connpass event ID
     * @return Flow of meeting records for the event, sorted by createdAt DESC
     */
    @Query("SELECT * FROM meeting_records WHERE eventId = :eventId ORDER BY createdAt DESC")
    fun getMeetingRecordsByEvent(eventId: Long): Flow<List<MeetingRecordEntity>>

    /**
     * Get all meeting records for a specific user.
     * Useful for displaying "All times I met this person".
     *
     * @param userId The connpass user ID
     * @return Flow of meeting records for the user, sorted by createdAt DESC
     */
    @Query("SELECT * FROM meeting_records WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMeetingRecordsByUser(userId: Long): Flow<List<MeetingRecordEntity>>

    /**
     * Check if a meeting record already exists for a specific event and user.
     * Used for duplicate detection before insertion.
     *
     * @param eventId The connpass event ID
     * @param userId The connpass user ID
     * @return The count of matching records (0 or 1 due to UNIQUE constraint)
     */
    @Query("SELECT COUNT(*) FROM meeting_records WHERE eventId = :eventId AND userId = :userId")
    suspend fun getMeetingRecordCount(eventId: Long, userId: Long): Int

    /**
     * Delete all meeting records.
     * Useful for testing or data reset.
     */
    @Query("DELETE FROM meeting_records")
    suspend fun deleteAll()

    /**
     * Get count of all meeting records in the database.
     *
     * @return Total number of meeting records
     */
    @Query("SELECT COUNT(*) FROM meeting_records")
    suspend fun getTotalMeetingRecordCount(): Int

    // ==================== PBI-5: Meeting Notes & Tagging Operations ====================

    /**
     * Update an existing meeting record.
     * Used to modify notes field and other mutable properties.
     *
     * @param meetingRecord The meeting record entity with updated values
     */
    @Update
    suspend fun updateMeetingRecord(meetingRecord: MeetingRecordEntity)

    /**
     * Get a meeting record with its associated tags.
     * Uses @Transaction to ensure atomic query execution.
     *
     * @param id The primary key ID (local database ID)
     * @return Flow of meeting record with tags, or null if not found
     */
    @Transaction
    @Query("SELECT * FROM meeting_records WHERE id = :id")
    fun getMeetingRecordWithTags(id: Long): Flow<MeetingRecordWithTags?>

    /**
     * Get all meeting records with their associated tags.
     * Uses @Transaction to ensure atomic query execution.
     * Ordered by creation date (newest first).
     *
     * @return Flow of list of all meeting records with tags
     */
    @Transaction
    @Query("SELECT * FROM meeting_records ORDER BY createdAt DESC")
    fun getAllMeetingRecordsWithTags(): Flow<List<MeetingRecordWithTags>>

    /**
     * Insert a meeting record-tag association into the cross-reference table.
     * Creates a many-to-many relationship between a meeting record and a tag.
     *
     * @param crossRef The cross-reference entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeetingRecordTagCrossRef(crossRef: MeetingRecordTagCrossRef)

    /**
     * Delete a specific meeting record-tag association.
     * Removes the relationship between a meeting record and a tag.
     *
     * @param meetingRecordId The meeting record ID
     * @param tagId The tag ID
     */
    @Query("DELETE FROM meeting_record_tag_cross_ref WHERE meetingRecordId = :meetingRecordId AND tagId = :tagId")
    suspend fun deleteMeetingRecordTagCrossRef(meetingRecordId: Long, tagId: Long)

    /**
     * Delete all tag associations for a specific meeting record.
     * Useful when updating tags (clear all, then re-associate).
     *
     * @param meetingRecordId The meeting record ID
     */
    @Query("DELETE FROM meeting_record_tag_cross_ref WHERE meetingRecordId = :meetingRecordId")
    suspend fun deleteAllTagsForMeetingRecord(meetingRecordId: Long)

    /**
     * Delete a meeting record by its ID.
     * Foreign key CASCADE will automatically delete associated tag cross-refs.
     *
     * @param id The meeting record ID to delete
     */
    @Query("DELETE FROM meeting_records WHERE id = :id")
    suspend fun deleteMeetingRecordById(id: Long)
}
