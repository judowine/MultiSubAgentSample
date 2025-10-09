package com.example.data.di

import com.example.data.network.ConnpassApiClient
import com.example.data.database.AppDatabase
import com.example.data.database.dao.EventDao
import com.example.data.database.dao.MeetingRecordDao
import com.example.data.database.dao.TagDao
import com.example.data.database.dao.UserDao
import com.example.data.database.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File
import java.util.Properties

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

    // PBI-2: EventDao
    single<EventDao> {
        get<AppDatabase>().eventDao()
    }

    // PBI-3: ConnpassApiClient (Ktor HTTP client with authentication)
    single<ConnpassApiClient> {
        // API key is injected from local.properties via BuildConfig and Koin properties
        // See: composeApp/build.gradle.kts (BuildConfig generation)
        // See: EventMeetApplication.kt (Koin properties injection)
        val apiKey = getProperty<String>("connpass.api.key", "")
        ConnpassApiClient(apiKey = apiKey)
    }

    // PBI-4: MeetingRecordDao
    single<MeetingRecordDao> {
        get<AppDatabase>().meetingRecordDao()
    }

    // PBI-5: TagDao
    single<TagDao> {
        get<AppDatabase>().tagDao()
    }
}
