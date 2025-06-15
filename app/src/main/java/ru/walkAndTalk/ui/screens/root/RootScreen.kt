package ru.walkAndTalk.ui.screens.root

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.screens.AddUser
import ru.walkAndTalk.ui.screens.Admin
import ru.walkAndTalk.ui.screens.AnnouncementDetails
import ru.walkAndTalk.ui.screens.Auth
import ru.walkAndTalk.ui.screens.Chats
import ru.walkAndTalk.ui.screens.EditAnnouncement
import ru.walkAndTalk.ui.screens.EditEvent
import ru.walkAndTalk.ui.screens.EditProfile
import ru.walkAndTalk.ui.screens.EditUser
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.screens.EventStatistics
import ru.walkAndTalk.ui.screens.Login
import ru.walkAndTalk.ui.screens.Main
import ru.walkAndTalk.ui.screens.Onboarding
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.Registration
import ru.walkAndTalk.ui.screens.Splash
import ru.walkAndTalk.ui.screens.Welcome
import ru.walkAndTalk.ui.screens.admin.AdminScreen
import ru.walkAndTalk.ui.screens.admin.AdminSideEffect
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.screens.admin.add.AddUserScreen
import ru.walkAndTalk.ui.screens.admin.edit.AdminEditAnnouncementScreen
import ru.walkAndTalk.ui.screens.admin.edit.EditUserScreen
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
import ru.walkAndTalk.ui.screens.main.MainScreen
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.screens.main.feed.announcements.AnnouncementDetailsScreen
import ru.walkAndTalk.ui.screens.main.feed.announcements.editannouncements.EditAnnouncementScreen
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsScreen
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsSideEffect
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsViewModel
import ru.walkAndTalk.ui.screens.main.feed.events.editevents.EditEventScreen
import ru.walkAndTalk.ui.screens.main.profile.ProfileScreen
import ru.walkAndTalk.ui.screens.main.profile.edit.EditProfileScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
import ru.walkAndTalk.ui.screens.splash.SplashScreen
import ru.walkAndTalk.ui.screens.welcome.WelcomeScreen

@Composable
fun RootScreen(intent: Intent) {
    val navController = rememberNavController()
    val supabaseWrapper: SupabaseWrapper = koinInject()
    val localDataStoreRepository: LocalDataStoreRepository = koinInject()
    val adminViewModel: AdminViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        adminViewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is AdminSideEffect.NavigateToAuth -> {
                    navController.navigate(Auth) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is AdminSideEffect.NavigateToMain -> {
                    navController.navigate(Main(sideEffect.userId)) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is AdminSideEffect.NavigateToAddUser -> {
                    navController.navigate(AddUser)
                }
                is AdminSideEffect.NavigateToEditUser -> {
                    navController.navigate(EditUser(userId = sideEffect.userId))
                }
                is AdminSideEffect.NavigateToProfile -> {
                    navController.navigate(Profile(userId = sideEffect.userId, viewOnly = true))
                }
                is AdminSideEffect.NavigateToEventDetails -> {
                    navController.navigate(EventDetails(sideEffect.eventId))
                }
                is AdminSideEffect.NavigateToAnnouncementDetails -> {
                    navController.navigate(AnnouncementDetails(sideEffect.announcementId))
                }
                is AdminSideEffect.NavigateToEditAnnouncement -> {
                    navController.navigate(EditAnnouncement(sideEffect.announcementId))
                }
                is AdminSideEffect.UserSaved -> {
                    navController.navigateUp() // Возвращаемся на AdminScreen
                }
                is AdminSideEffect.ShowError -> {
                    // Уведомления обрабатываются в AdminScreen и AddUserScreen
                }
                else -> Unit
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateMain = { userId ->
                    navController.navigate(Main(userId)) {
                        popUpTo(Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateAdmin = { userId ->
                    navController.navigate(Admin(userId)) {
                        popUpTo(Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateWelcome = {
                    navController.navigate(Auth) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                onNavigateOnboarding = {
                    navController.navigate(Onboarding) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }
        composable<Onboarding> {
            OnboardingScreen(
                onNavigateWelcome = {
                    navController.navigate(Auth) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                }
            )
        }
        navigation<Auth>(startDestination = Welcome) {
            composable<Welcome> {
                WelcomeScreen(
                    onNavigateLogin = {
                        navController.navigate(Login) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    },
                    onNavigateRegister = {
                        navController.navigate(Registration) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    },
                    navController = navController
                )
            }
            composable<Login> {
                LoginScreen(
                    onNavigateRegister = {
                        navController.navigate(Registration) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateMain = { userId ->
                        navController.navigate(Main(userId)) {
                            popUpTo(Auth) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateAdmin = { userId ->
                        navController.navigate(Admin(userId)) {
                            popUpTo(Auth) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable<Registration> {
                RegisterScreen(
                    onNavigateLogin = {
                        navController.navigate(Login) {
                            popUpTo(Registration) { inclusive = true }
                        }
                    },
                    onNavigateMain = { userId ->
                        navController.navigate(Main(userId)) {
                            popUpTo(Auth) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable<Main> { backStackEntry ->
            val main = backStackEntry.toRoute<Main>()
            MainScreen(
                userId = main.userId,
                onNavigateAuth = {
                    navController.navigate(Auth) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateAdmin = { userId ->
                    navController.navigate(Admin(userId)) {
                        popUpTo(Main(main.userId)) { inclusive = true } // Очищаем MainScreen
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Admin> { backStackEntry ->
            val admin = backStackEntry.toRoute<Admin>()
            AdminScreen(
                userId = admin.userId,
                viewModel = adminViewModel,
                onEventEditClick = { eventId ->
                    navController.navigate(EditEvent(eventId))
                },
                onEventClick = { eventId ->
                    navController.navigate(EventDetails(eventId))
                },
                onAnnouncementEditClick = { announcementId ->
                    navController.navigate(EditAnnouncement(announcementId))
                },
                onAnnouncementClick = {announcementId ->
                    navController.navigate(AnnouncementDetails(announcementId))
                }
            )
        }
        composable<EventDetails> { navBackStackEntry ->
            val eventId = navBackStackEntry.toRoute<EventDetails>().eventId
            val eventDetailsViewModel: EventDetailsViewModel = koinViewModel()
            val feedViewModel: FeedViewModel = koinViewModel()
            EventDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                eventId = eventId,
                viewModel = eventDetailsViewModel,
                feedViewModel = feedViewModel,
                onNavigateToEditEvent = { navController.navigate("edit_event/$it") },
                onNavigateToChat = { chatId ->
                    println("MainScreen: Navigating to ChatsScreen and chatId=$chatId")
                    navController.navigate(Chats) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                    navController.navigate("chat/$chatId")
                }
            )
            LaunchedEffect(eventDetailsViewModel) {
                eventDetailsViewModel.container.sideEffectFlow.collect { sideEffect ->
                    when (sideEffect) {
                        is EventDetailsSideEffect.NavigateToChat -> {
                            // Обработка в onNavigateToChat, здесь не требуется
                        }

                        is EventDetailsSideEffect.ShowError -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(sideEffect.message)
                            }
                        }

                        is EventDetailsSideEffect.OnNavigateBack -> {
                            navController.popBackStack()
                        }

                        is EventDetailsSideEffect.NavigateToEditEvent -> {
                            navController.navigate("edit_event/${sideEffect.eventId}")
                        }

                        is EventDetailsSideEffect.EventDeleted -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Мероприятие удалено")
                            }
                            navController.popBackStack()

                        }
                    }
                }
            }
        }
        composable<EditEvent> { navBackStackEntry ->
            val eventId = navBackStackEntry.toRoute<EditEvent>().eventId
            EditEventScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onEventUpdated = { navController.popBackStack() }
            )
        }
        composable<AnnouncementDetails> { backStackEntry ->
            val announcement = backStackEntry.toRoute<AnnouncementDetails>()
            val feedViewModel: FeedViewModel = koinViewModel()
            AnnouncementDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                announcementId = announcement.announcementId,
                feedViewModel = feedViewModel,
                onNavigateToEditAnnouncement = { id ->
                    navController.navigate("edit_announcement/$id")
                },
                onNavigateToChat = { chatId ->
                    navController.navigate("chat/$chatId")
                }
            )
        }
        composable<EditAnnouncement> { backStackEntry ->
            val editannouncement = backStackEntry.toRoute<EditAnnouncement>()
            EditAnnouncementScreen(
                announcementId = editannouncement.announcementId,
                onNavigateBack = { navController.popBackStack() },
                onAnnouncementUpdated = { navController.popBackStack() },
                onAnnouncementDeleted = { navController.popBackStack() }
            )
        }
        composable<AddUser> {
            AddUserScreen(
                viewModel = adminViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<EditUser> { navBackStackEntry ->
            val editUser = navBackStackEntry.toRoute<EditUser>()
            EditUserScreen(
                userId = editUser.userId,
                viewModel = adminViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }
        composable<Profile> { backStackEntry ->
            val profile = backStackEntry.toRoute<Profile>()
            ProfileScreen(
                viewModel = koinViewModel(parameters = { parametersOf(profile.userId) }),
                onNavigateAuth = {
                    navController.navigate(Auth) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateEditProfile = { navController.navigate(EditProfile) },
                onNavigateEventStatistics = { navController.navigate(EventStatistics) },
                onNavigateAdminScreen = {
                    // Вызываем навигацию на AdminScreen с заменой стека
                    navController.navigate(Admin(profile.userId)) {
                        popUpTo(Main(profile.userId)) { inclusive = true } // Очищаем MainScreen
                        launchSingleTop = true
                    }
                },
                isViewOnly = profile.viewOnly,
                onBackClick = { navController.navigateUp() }
            )
        }
    }

    LaunchedEffect(intent) {
        intent.data?.let { uri ->
            if (uri.toString().startsWith("vk53306543://vk.com")) {
                try {
                    val user = supabaseWrapper.auth.currentUserOrNull()
                    if (user != null) {
                        val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
                            filter { eq("id", user.id) }
                        }.decodeSingleOrNull<UserDto>()
                        if(userData != null){
                            localDataStoreRepository.userMode.collect { userMode ->
                                Log.d(
                                    "RootScreen",
                                    "VK auth: isAdmin=${userData.isAdmin}, userMode=$userMode, userId=${user.id}"
                                )
                                if (userMode == "admin") {
                                    navController.navigate(Admin(user.id)) {
                                        popUpTo(Splash) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(Main(user.id)) {
                                        popUpTo(Splash) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    } else {
                        localDataStoreRepository.saveUserMode("admin")
                        Log.d("RootScreen", "VK auth: No user, userMode reset to admin")
                        navController.navigate(Auth) {
                            popUpTo(Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RootScreen", "VK auth error", e)
                    localDataStoreRepository.saveUserMode("admin")
                    navController.navigate(Auth) {
                        popUpTo(Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
