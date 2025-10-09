package org.example.project.judowine.domain.usecase

import com.example.data.repository.MeetingRecordRepository

/**
 * Use case for saving a new meeting record to the database.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-4, Task 7.3: SaveMeetingRecordUseCase
 *
 * This use case orchestrates the business logic for creating a new meeting record,
 * including input validation, duplicate detection, and persistence coordination.
 *
 * Business Rules:
 * - eventId must be positive (valid event ID)
 * - userId must be positive (valid user ID)
 * - nickname must not be blank
 * - Duplicate detection: Check if record already exists for (eventId, userId) pair
 * - createdAt timestamp is set automatically to current system time
 *
 * Error Handling:
 * - Returns Result<Unit> for explicit error handling in UI layer
 * - Validation errors are returned as Result.failure with descriptive messages
 * - Duplicate records are handled via repository (database UNIQUE constraint)
 * - Database errors are propagated via Result from repository layer
 *
 * @property meetingRecordRepository The repository for meeting record data access (injected from /data module)
 */
class SaveMeetingRecordUseCase(
    private val meetingRecordRepository: MeetingRecordRepository
) {
    /**
     * Save a new meeting record after validation and duplicate checking.
     *
     * This method orchestrates the following domain logic:
     * 1. Validates input parameters (business rule enforcement)
     * 2. Checks for existing record to prevent duplicates
     * 3. Delegates to MeetingRecordRepository for persistence
     *
     * Duplicate Handling:
     * - If a record already exists for (eventId, userId), returns Result.failure
     * - Database UNIQUE constraint provides additional safety at persistence layer
     *
     * @param eventId connpass event ID where the meeting occurred (must be positive)
     * @param userId connpass user ID of the person met (must be positive)
     * @param nickname User's display name (must not be blank)
     * @return Result.success(Unit) if saved successfully, or Result.failure with error if validation/persistence fails
     */
    suspend fun execute(
        eventId: Long,
        userId: Long,
        nickname: String
    ): Result<Unit> {
        // Input validation - enforce business rules
        if (eventId <= 0) {
            return Result.failure(IllegalArgumentException("Event ID must be positive"))
        }
        if (userId <= 0) {
            return Result.failure(IllegalArgumentException("User ID must be positive"))
        }
        if (nickname.isBlank()) {
            return Result.failure(IllegalArgumentException("Nickname must not be blank"))
        }

        // Check for duplicate record
        val recordExists = meetingRecordRepository.meetingRecordExists(eventId, userId)
        if (recordExists) {
            return Result.failure(
                IllegalStateException("Meeting record already exists for this event and user")
            )
        }

        // Delegate to repository for persistence
        return meetingRecordRepository.saveMeetingRecord(
            eventId = eventId,
            userId = userId,
            nickname = nickname
        )
    }

    /**
     * Check if a meeting record already exists for the given event and user.
     *
     * Useful for UI validation (e.g., disabling "Add" button if already recorded).
     *
     * @param eventId The connpass event ID
     * @param userId The connpass user ID
     * @return true if a record exists, false otherwise
     */
    suspend fun checkDuplicate(eventId: Long, userId: Long): Boolean {
        return meetingRecordRepository.meetingRecordExists(eventId, userId)
    }
}
