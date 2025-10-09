package com.example.data.repository

import com.example.data.database.entity.MeetingRecordEntity
import com.example.data.database.entity.MeetingRecordWithTags
import com.example.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for meeting record operations.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-4, Task 1.12: MeetingRecordRepository interface
 * PBI-5, Task 1.20: Added tag and notes operations
 *
 * This repository provides access to meeting record data stored in the local
 * Room database. Meeting records track who the user met at specific events,
 * including notes and tags (PBI-5).
 */
interface MeetingRecordRepository {
    /**
     * Save a new meeting record.
     * Prevents duplicates using database UNIQUE constraint on (eventId, userId).
     *
     * @param eventId The connpass event ID where the meeting occurred
     * @param userId The connpass user ID of the person met
     * @param nickname The user's nickname (cached for display)
     * @return Result.success(Unit) if saved, Result.failure(DuplicateRecordException) if duplicate
     */
    suspend fun saveMeetingRecord(eventId: Long, userId: Long, nickname: String): Result<Unit>

    /**
     * Get all meeting records as a reactive Flow.
     * Ordered by creation date (newest first).
     *
     * @return Flow of all meeting records
     */
    fun getAllMeetingRecords(): Flow<List<MeetingRecordEntity>>

    /**
     * Get all meeting records for a specific event.
     * Useful for displaying "People I met at this event".
     *
     * @param eventId The connpass event ID
     * @return Flow of meeting records for the event
     */
    fun getMeetingRecordsByEvent(eventId: Long): Flow<List<MeetingRecordEntity>>

    /**
     * Get all meeting records for a specific user.
     * Useful for displaying "All times I met this person".
     *
     * @param userId The connpass user ID
     * @return Flow of meeting records for the user
     */
    fun getMeetingRecordsByUser(userId: Long): Flow<List<MeetingRecordEntity>>

    /**
     * Get a specific meeting record by its database ID.
     *
     * @param id The meeting record database ID
     * @return The meeting record, or null if not found
     */
    suspend fun getMeetingRecordById(id: Long): MeetingRecordEntity?

    /**
     * Delete a meeting record.
     *
     * @param meetingRecord The meeting record to delete
     * @return Result.success(Unit) if deleted, Result.failure if error occurred
     */
    suspend fun deleteMeetingRecord(meetingRecord: MeetingRecordEntity): Result<Unit>

    /**
     * Check if a meeting record already exists for a specific event and user.
     *
     * @param eventId The connpass event ID
     * @param userId The connpass user ID
     * @return True if a record exists, false otherwise
     */
    suspend fun meetingRecordExists(eventId: Long, userId: Long): Boolean

    // ==================== PBI-5: Meeting Notes & Tagging Operations ====================

    /**
     * Update an existing meeting record with notes and tags.
     * Replaces all existing tags with the provided tag names.
     *
     * @param id The meeting record database ID
     * @param notes Optional text memo about the meeting
     * @param tagNames List of tag names to associate with the meeting record
     * @return Result.success(Unit) if updated, Result.failure if error occurred
     */
    suspend fun updateMeetingRecord(id: Long, notes: String?, tagNames: List<String>): Result<Unit>

    /**
     * Get a meeting record with its associated tags.
     *
     * @param id The meeting record database ID
     * @return Flow of meeting record with tags, or null if not found
     */
    fun getMeetingRecordWithTags(id: Long): Flow<MeetingRecordWithTags?>

    /**
     * Get all meeting records with their associated tags.
     * Ordered by creation date (newest first).
     *
     * @return Flow of all meeting records with tags
     */
    fun getAllMeetingRecordsWithTags(): Flow<List<MeetingRecordWithTags>>

    /**
     * Get all tags from the database.
     * Used for autocomplete suggestions.
     *
     * @return Flow of all tags sorted alphabetically
     */
    fun getAllTags(): Flow<List<TagEntity>>

    /**
     * Delete a meeting record by its ID.
     * Foreign key CASCADE will automatically delete associated tag cross-refs.
     *
     * @param id The meeting record database ID
     * @return Result.success(Unit) if deleted, Result.failure if error occurred
     */
    suspend fun deleteMeetingRecordById(id: Long): Result<Unit>
}
