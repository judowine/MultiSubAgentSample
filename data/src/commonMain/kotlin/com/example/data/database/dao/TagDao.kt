package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO (Data Access Object) for tag operations.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-5, Task 1.16: TagDao for Meeting Notes & Tagging
 *
 * Provides database operations for managing tags, including:
 * - Creating new tags with duplicate prevention
 * - Fetching all tags for autocomplete suggestions
 * - Fetching tags associated with specific meeting records
 * - Looking up tags by name
 *
 * The IGNORE conflict strategy on insert prevents errors when trying to
 * insert duplicate tag names (due to UNIQUE constraint on TagEntity.name).
 */
@Dao
interface TagDao {
    /**
     * Insert a new tag into the database.
     * Uses IGNORE strategy to prevent errors when tag name already exists.
     *
     * @param tag The tag entity to insert
     * @return The row ID of the inserted tag, or -1 if the tag name already exists
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    /**
     * Fetch all tags from the database, sorted alphabetically by name.
     * Used for autocomplete suggestions in tag input fields.
     *
     * @return Flow of all tags
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    /**
     * Fetch all tags associated with a specific meeting record.
     * Uses a JOIN through the cross-reference table.
     *
     * @param meetingRecordId The ID of the meeting record
     * @return Flow of tags associated with the meeting record
     */
    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN meeting_record_tag_cross_ref
        ON tags.id = meeting_record_tag_cross_ref.tagId
        WHERE meeting_record_tag_cross_ref.meetingRecordId = :meetingRecordId
        ORDER BY tags.name ASC
    """)
    fun getTagsByMeetingRecordId(meetingRecordId: Long): Flow<List<TagEntity>>

    /**
     * Find a tag by its exact name (case-sensitive).
     * Used to check if a tag already exists before creating a new one.
     *
     * @param name The tag name to search for
     * @return The tag entity if found, null otherwise
     */
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    /**
     * Delete tags that are not associated with any meeting records.
     * This is an optional cleanup operation to remove unused tags.
     *
     * @return The number of tags deleted
     */
    @Query("""
        DELETE FROM tags
        WHERE id NOT IN (
            SELECT DISTINCT tagId FROM meeting_record_tag_cross_ref
        )
    """)
    suspend fun deleteUnusedTags(): Int
}
