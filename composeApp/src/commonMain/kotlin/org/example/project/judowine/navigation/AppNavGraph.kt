package org.example.project.judowine.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.example.project.judowine.domain.usecase.GetUserProfileUseCase
import org.example.project.judowine.domain.usecase.SaveUserProfileUseCase
import org.example.project.judowine.ui.screen.event.EventDetailScreen
import org.example.project.judowine.ui.screen.event.EventDetailUiState
import org.example.project.judowine.ui.screen.event.EventIntent
import org.example.project.judowine.ui.screen.event.EventViewModel
import org.example.project.judowine.ui.screen.main.MainScreen
import org.example.project.judowine.ui.screen.meetingrecord.AddMeetingRecordScreen
import org.example.project.judowine.ui.screen.meetingrecord.EditMeetingRecordScreen
import org.example.project.judowine.ui.screen.meetingrecord.MeetingRecordDetailScreen
import org.example.project.judowine.ui.screen.meetingrecord.MeetingRecordListScreen
import org.example.project.judowine.ui.screen.meetingrecord.MeetingRecordViewModel
import org.example.project.judowine.ui.screen.people.PersonDetailScreen
import org.example.project.judowine.ui.screen.people.PersonDetailViewModel
import org.example.project.judowine.ui.screen.profile.ProfileEditScreen
import org.example.project.judowine.ui.screen.profile.ProfileRegistrationScreen
import org.example.project.judowine.ui.screen.user.UserDetailScreen
import org.example.project.judowine.ui.screen.user.UserDetailViewModel
import org.example.project.judowine.ui.screen.user.UserSearchScreen
import org.example.project.judowine.ui.screen.user.UserSearchViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Navigation Graph for EventMeet Application
 *
 * Implementation: Navigation system implementation (PBI-8)
 * Architecture: Centralized navigation graph with type-safe routing
 *
 * Navigation Structure:
 * - Start destination: ProfileRegistration (for new users)
 * - After registration: MainScreen with Bottom Navigation (Events, People, Profile tabs)
 * - Deep navigation: Event details, Person details, Profile edit
 * - All screens are stateless and receive navigation callbacks
 *
 * MainScreen Integration:
 * - Bottom Navigation with 3 tabs (Events, People, Profile)
 * - Each tab has independent navigation state
 * - Deep navigation from tabs uses root NavController
 *
 * ViewModels:
 * - Injected using Koin (koinViewModel())
 * - Scoped appropriately for each screen
 *
 * @param navController Navigation controller managing the back stack
 * @param startDestination Initial destination route (default: ProfileRegistration)
 * @param modifier Optional modifier for NavHost
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.ProfileRegistration.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ========== Main Navigation (PBI-8) ==========

        composable(Routes.Main.route) {
            MainScreen(
                rootNavController = navController
            )
        }

        // ========== Profile Management (PBI-1) ==========

        composable(Routes.ProfileRegistration.route) {
            val saveUserProfileUseCase = koinInject<SaveUserProfileUseCase>()
            ProfileRegistrationScreen(
                saveUserProfileUseCase = saveUserProfileUseCase,
                onRegistrationSuccess = {
                    // Navigate to Main Screen with Bottom Navigation after successful registration
                    navController.navigate(Routes.Main.route) {
                        // Remove ProfileRegistration from back stack
                        popUpTo(Routes.ProfileRegistration.route) { inclusive = true }
                    }
                }
            )
        }

        // Note: ProfileDisplay is now part of MainScreen's Profile tab

        composable(Routes.ProfileEdit.route) {
            val getUserProfileUseCase = koinInject<GetUserProfileUseCase>()
            val saveUserProfileUseCase = koinInject<SaveUserProfileUseCase>()
            ProfileEditScreen(
                getUserProfileUseCase = getUserProfileUseCase,
                saveUserProfileUseCase = saveUserProfileUseCase,
                onEditSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== Event Discovery & Viewing (PBI-2, PBI-7) ==========
        // Note: EventList is now part of MainScreen's Events tab

        composable(
            route = Routes.EventDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.EventDetail.ARG_EVENT_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val eventId = checkNotNull(backStackEntry.arguments?.getLong(Routes.EventDetail.ARG_EVENT_ID)) {
                "eventId is required"
            }
            val viewModel = koinViewModel<EventViewModel>()
            EventDetailScreen(
                viewModel = viewModel,
                eventId = eventId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddPersonMet = { event ->
                    // Navigate to AddMeetingRecordScreen with pre-selected event (PBI-4)
                    navController.navigate(Routes.AddMeetingRecord(preSelectedEventId = event.eventId).route)
                },
                onMeetingRecordClick = { meetingRecordId ->
                    // Navigate to MeetingRecordDetailScreen (PBI-7)
                    navController.navigate(Routes.MeetingRecordDetail(meetingRecordId).route)
                }
            )
        }

        // ========== User Search & Profile (PBI-3) ==========

        composable(Routes.UserSearch.route) {
            val viewModel = koinViewModel<UserSearchViewModel>()
            UserSearchScreen(
                viewModel = viewModel,
                onUserClick = { connpassUser ->
                    navController.navigate(Routes.UserDetail(connpassUser.nickname).route)
                }
            )
        }

        composable(
            route = Routes.UserDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.UserDetail.ARG_NICKNAME) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val nickname = checkNotNull(backStackEntry.arguments?.getString(Routes.UserDetail.ARG_NICKNAME)) {
                "nickname is required"
            }
            val viewModel = koinViewModel<UserDetailViewModel>()
            // TODO: Replace hardcoded loggedInUserNickname with actual logged-in user
            val loggedInUserNickname: String? = null
            UserDetailScreen(
                viewModel = viewModel,
                nickname = nickname,
                loggedInUserNickname = loggedInUserNickname,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEventClick = { event ->
                    navController.navigate(Routes.EventDetail(event.eventId).route)
                }
            )
        }

        // ========== Meeting Record Creation (PBI-4, PBI-5) ==========

        composable(Routes.MeetingRecordList.route) {
            val viewModel = koinViewModel<MeetingRecordViewModel>()
            MeetingRecordListScreen(
                viewModel = viewModel,
                onNavigateToAdd = {
                    navController.navigate(Routes.AddMeetingRecord().route)
                },
                onMeetingRecordClick = { meetingRecord ->
                    navController.navigate(Routes.MeetingRecordDetail(meetingRecord.id).route)
                }
            )
        }

        composable(
            route = Routes.AddMeetingRecord.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.AddMeetingRecord.ARG_EVENT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val preSelectedEventId = backStackEntry.arguments
                ?.getString(Routes.AddMeetingRecord.ARG_EVENT_ID)
                ?.toLongOrNull()
            val meetingRecordViewModel = koinViewModel<MeetingRecordViewModel>()
            val userSearchViewModel = koinViewModel<UserSearchViewModel>()
            val eventViewModel = koinViewModel<EventViewModel>()

            // Load pre-selected event if eventId is provided
            if (preSelectedEventId != null) {
                LaunchedEffect(preSelectedEventId) {
                    eventViewModel.handleIntent(EventIntent.LoadEventDetail(preSelectedEventId))
                }
            }

            val eventDetailState by eventViewModel.eventDetailState.collectAsState()
            val preSelectedEvent = if (eventDetailState is EventDetailUiState.Success) {
                (eventDetailState as EventDetailUiState.Success).event
            } else null

            AddMeetingRecordScreen(
                meetingRecordViewModel = meetingRecordViewModel,
                userSearchViewModel = userSearchViewModel,
                preSelectedEvent = preSelectedEvent,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.MeetingRecordDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.MeetingRecordDetail.ARG_MEETING_RECORD_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val meetingRecordId = checkNotNull(backStackEntry.arguments?.getLong(Routes.MeetingRecordDetail.ARG_MEETING_RECORD_ID)) {
                "meetingRecordId is required"
            }
            val viewModel = koinViewModel<MeetingRecordViewModel>()
            MeetingRecordDetailScreen(
                meetingRecordId = meetingRecordId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.EditMeetingRecord(id).route)
                }
            )
        }

        composable(
            route = Routes.EditMeetingRecord.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.EditMeetingRecord.ARG_MEETING_RECORD_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val meetingRecordId = checkNotNull(backStackEntry.arguments?.getLong(Routes.EditMeetingRecord.ARG_MEETING_RECORD_ID)) {
                "meetingRecordId is required"
            }
            val viewModel = koinViewModel<MeetingRecordViewModel>()
            EditMeetingRecordScreen(
                meetingRecordId = meetingRecordId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== People-Centric Meeting History (PBI-6) ==========
        // Note: PeopleList is now part of MainScreen's People tab

        composable(
            route = Routes.PersonDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.PersonDetail.ARG_USER_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val userId = checkNotNull(backStackEntry.arguments?.getLong(Routes.PersonDetail.ARG_USER_ID)) {
                "userId is required"
            }
            val viewModel = koinViewModel<PersonDetailViewModel>()
            PersonDetailScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEventClick = { eventId ->
                    navController.navigate(Routes.EventDetail(eventId).route)
                },
                onMeetingClick = { meetingRecordId ->
                    // Navigate to MeetingRecordDetailScreen (PBI-7)
                    navController.navigate(Routes.MeetingRecordDetail(meetingRecordId).route)
                }
            )
        }
    }
}
