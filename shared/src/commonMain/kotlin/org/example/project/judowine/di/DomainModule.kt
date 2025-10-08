package org.example.project.judowine.di

import org.example.project.judowine.domain.usecase.SaveUserProfileUseCase
import org.koin.dsl.module

/**
 * Koin module for domain layer dependencies.
 *
 * This module provides:
 * - Use Cases (business logic orchestration)
 * - Domain services (if any)
 *
 * Architecture Note:
 * - Domain layer depends on data layer (repositories)
 * - Use Cases are created as factory (new instance each time) for stateless operation
 * - Repositories are injected via get() and resolved from dataModule
 */
val domainModule = module {
    // Use Cases: Factory (new instance per injection)
    // SaveUserProfileUseCase orchestrates user profile save/update operations
    factory {
        SaveUserProfileUseCase(
            userRepository = get() // Resolves UserRepository from dataModule
        )
    }

    // Future Use Cases can be added here:
    // factory { GetUserProfileUseCase(get()) }
    // factory { DeleteUserProfileUseCase(get()) }
}
