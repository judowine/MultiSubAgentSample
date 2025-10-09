package org.example.project.judowine.domain.usecase

import com.example.data.repository.MeetingRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.Tag

/**
 * Use Case for retrieving all tags from the database.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-5, Task 8.6: GetAllTagsUseCase
 *
 * This Application Service orchestrates the tag retrieval operation,
 * following the DDD pattern of separating domain logic from infrastructure concerns.
 *
 * Responsibilities:
 * - Fetch all tags from repository
 * - Convert TagEntity to Tag domain models
 * - Sort tags alphabetically by name for UI display
 * - Return reactive Flow for UI updates
 *
 * Business Rules:
 * - Tags are sorted alphabetically by name (case-sensitive)
 * - Returns empty list if no tags exist
 * - Updates automatically when tags are added/removed
 *
 * @property meetingRecordRepository Repository for meeting record and tag persistence
 */
class GetAllTagsUseCase(
    private val meetingRecordRepository: MeetingRecordRepository
) {
    /**
     * Retrieves all tags from the database as a reactive Flow.
     *
     * This use case fetches tags from the repository, converts them to domain models,
     * and sorts them alphabetically by name for consistent UI presentation.
     *
     * @return Flow of all tags sorted alphabetically by name
     */
    operator fun invoke(): Flow<List<Tag>> {
        return meetingRecordRepository.getAllTags()
            .map { tagEntities ->
                tagEntities
                    .toDomainModels()
                    .sortedBy { it.name }
            }
    }
}
