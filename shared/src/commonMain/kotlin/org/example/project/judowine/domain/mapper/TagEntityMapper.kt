package org.example.project.judowine.domain.mapper

import com.example.data.database.entity.TagEntity
import org.example.project.judowine.domain.model.Tag

/**
 * Mapper functions to convert between TagEntity (data layer) and Tag (domain model).
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-5, Task 8.6: TagEntity ↔ Tag mapper for GetAllTagsUseCase
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns TagEntity (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 *
 * Key transformations:
 * - Both TagEntity and Tag have the same field types
 * - Direct field-to-field mapping with no data type transformations needed
 * - Tag is a Value Object with equality based on name
 */

/**
 * Converts TagEntity (data layer) to Tag domain model.
 *
 * Performs direct field-to-field mapping as both entities share the same structure.
 *
 * @receiver TagEntity from the data layer
 * @return Tag domain model (Value Object)
 */
fun TagEntity.toDomainModel(): Tag {
    return Tag(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

/**
 * Converts a list of TagEntity to a list of Tag domain models.
 *
 * @receiver List of TagEntity from the data layer
 * @return List of Tag domain models
 */
fun List<TagEntity>.toDomainModels(): List<Tag> {
    return map { it.toDomainModel() }
}
