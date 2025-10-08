package com.example.data.di

import com.example.data.repository.UserRepository
import com.example.data.repository.UserRepositoryImpl
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 *
 * This module provides:
 * - Repository implementations (single instances)
 * - Database access objects (DAOs) are provided through platform-specific modules
 *
 * Architecture Note:
 * - Data layer is the leaf node (no dependencies on other app modules)
 * - Repository implementations are singletons to ensure single source of truth
 * - Platform-specific database initialization is handled in androidDataModule, iosDataModule, etc.
 */
val dataModule = module {
    // Repository: Single instance for single source of truth
    single<UserRepository> {
        UserRepositoryImpl(get()) // get() resolves UserDao from platform-specific module
    }
}
