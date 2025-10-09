package org.example.project.judowine.di

import org.example.project.judowine.ui.screen.event.EventViewModel
import org.example.project.judowine.ui.screen.meetingrecord.MeetingRecordViewModel
import org.example.project.judowine.ui.screen.people.PeopleListViewModel
import org.example.project.judowine.ui.screen.people.PersonDetailViewModel
import org.example.project.judowine.ui.screen.profile.ProfileViewModel
import org.example.project.judowine.ui.screen.user.UserDetailViewModel
import org.example.project.judowine.ui.screen.user.UserSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for presentation layer dependencies.
 *
 * Implementation: Navigation system implementation (DI fixes)
 *
 * This module provides:
 * - ViewModels for all screens
 *
 * Architecture Note:
 * - Presentation layer depends on domain layer (Use Cases)
 * - ViewModels are created as viewModel (scoped to Compose lifecycle)
 * - Use Cases are injected via get() and resolved from domainModule
 * - Maintains layer isolation: composeApp → shared (NO direct data access)
 */
val presentationModule = module {
    // PBI-1: Profile Management ViewModels
    viewModel {
        ProfileViewModel(
            getUserProfileUseCase = get(),
            saveUserProfileUseCase = get()
        )
    }

    // PBI-2: Event Discovery & Viewing ViewModels
    // PBI-7: Event-Centric Meeting Review (enhanced)
    viewModel {
        EventViewModel(
            getEventsUseCase = get(),
            getEventDetailUseCase = get(),
            getMeetingRecordsByEventUseCase = get()
        )
    }

    // PBI-3: User Search & Profile ViewModels
    viewModel {
        UserSearchViewModel(
            searchUsersUseCase = get()
        )
    }

    viewModel {
        UserDetailViewModel(
            searchUsersUseCase = get(),
            getUserEventsUseCase = get(),
            findCommonEventsUseCase = get()
        )
    }

    // PBI-4: Meeting Record Creation ViewModels
    // PBI-5: Meeting Notes & Tagging (enhanced)
    viewModel {
        MeetingRecordViewModel(
            saveMeetingRecordUseCase = get(),
            getMeetingRecordsUseCase = get(),
            updateMeetingRecordUseCase = get(),
            deleteMeetingRecordUseCase = get(),
            getAllTagsUseCase = get()
        )
    }

    // PBI-6: People-Centric Meeting History ViewModels
    viewModel {
        PeopleListViewModel(
            getMeetingRecordsUseCase = get()
        )
    }

    viewModel {
        PersonDetailViewModel(
            getMeetingRecordsUseCase = get()
        )
    }
}
