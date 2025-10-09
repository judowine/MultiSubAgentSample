package com.example.data.di

import com.example.data.database.AppDatabase
import com.example.data.database.dao.MeetingRecordDao
import com.example.data.database.dao.UserDao
import com.example.data.database.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module for data layer.
 *
 * This module provides:
 * - Room database instance (single)
 * - DAOs from the database
 *
 * Architecture Note:
 * - Uses androidContext() to get Android Context for database initialization
 * - Database is initialized with fallbackToDestructiveMigration for development
 * - Production: Replace with proper migration strategy before release
 */
val androidDataModule = module {
    // Room Database: Single instance
    single<AppDatabase> {
        val builder = getDatabaseBuilder(androidContext())
        builder.fallbackToDestructiveMigration(true)
        builder.build()
    }

    // UserDao: Provided from database
    single<UserDao> {
        get<AppDatabase>().userDao()
    }

    // PBI-4: MeetingRecordDao
    single<MeetingRecordDao> {
        get<AppDatabase>().meetingRecordDao()
    }
}
