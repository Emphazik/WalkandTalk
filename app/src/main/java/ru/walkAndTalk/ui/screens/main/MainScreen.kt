package ru.walkAndTalk.ui.screens.main

import SearchScreen
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
import ru.walkAndTalk.R
import ru.walkAndTalk.data.store.UserPreferences
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.screens.Admin
import ru.walkAndTalk.ui.screens.AnnouncementDetails
import ru.walkAndTalk.ui.screens.Chats
import ru.walkAndTalk.ui.screens.EditAnnouncement
import ru.walkAndTalk.ui.screens.EditEvent
import ru.walkAndTalk.ui.screens.EditProfile
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.screens.EventStatistics
import ru.walkAndTalk.ui.screens.Feed
import ru.walkAndTalk.ui.screens.Notifications
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.Search
import ru.walkAndTalk.ui.screens.admin.AdminScreen
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.screens.main.chats.ChatsScreen
import ru.walkAndTalk.ui.screens.main.chats.detailchat.ChatScreen
import ru.walkAndTalk.ui.screens.main.feed.FeedScreen
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.screens.main.feed.announcements.AnnouncementDetailsScreen
import ru.walkAndTalk.ui.screens.main.feed.announcements.AnnouncementDetailsViewModel
import ru.walkAndTalk.ui.screens.main.feed.announcements.editannouncements.EditAnnouncementScreen
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsScreen
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsSideEffect
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsViewModel
import ru.walkAndTalk.ui.screens.main.feed.events.editevents.EditEventScreen
import ru.walkAndTalk.ui.screens.main.feed.notifications.NotificationsScreen
import ru.walkAndTalk.ui.screens.main.feed.notifications.NotificationsViewModel
import ru.walkAndTalk.ui.screens.main.profile.ProfileScreen
import ru.walkAndTalk.ui.screens.main.profile.ProfileViewModel
import ru.walkAndTalk.ui.screens.main.profile.edit.EditProfileScreen
import ru.walkAndTalk.ui.screens.main.profile.statistics.EventStatisticsScreen

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

sealed class BottomNavBarItem(
    val route: Any,
    val label: String,
    @DrawableRes val iconId: Int,
    val tabId: String
) {
    data object FeedItem : BottomNavBarItem(Feed, "Главная", R.drawable.ic_home1, "feed")
    data object SearchItem : BottomNavBarItem(Search, "Поиск", R.drawable.ic_people64, "search")
    data object ChatsItem : BottomNavBarItem(Chats, "Чаты", R.drawable.ic_chat64, "chats")
    data class ProfileItem(val id: String) :
        BottomNavBarItem(Profile(id), "Профиль", R.drawable.ic_profile1, "profile")
}

@Composable
fun MainScreen(
    userId: String,
    onNavigateAuth: () -> Unit,
    onNavigateAdmin: (String) -> Unit,
    openProfile: Boolean = false, // Added parameter to control initial navigation
    viewOnly: Boolean = false,
    viewUserId: String? = null, // ID пользователя для просмотра
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = listOf(
        BottomNavBarItem.FeedItem,
        BottomNavBarItem.SearchItem,
        BottomNavBarItem.ChatsItem,
        BottomNavBarItem.ProfileItem(userId),
    )
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val notificationsViewModel: NotificationsViewModel =
        koinViewModel(parameters = { parametersOf(userId) })
    val feedViewModel: FeedViewModel = koinViewModel(parameters = { parametersOf(userId) })
    val profileViewModel: ProfileViewModel = koinViewModel(parameters = { parametersOf(userId) })
    var isSnackbarVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showProfileSetupNotification by remember { mutableStateOf(false) }

    LaunchedEffect(openProfile) {
        if (openProfile) {
            navController.navigate(Profile(userId, viewOnly = false)) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                    saveState = true
                }
                restoreState = true
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(userId) {
        val hasSeenNotification = UserPreferences.hasSeenProfileNotification(context)
        if (!hasSeenNotification) {
            val user = profileViewModel.getUserProfile()
            showProfileSetupNotification =
                user?.bio.isNullOrBlank() && user?.interestIds.isNullOrEmpty()
            println("MainScreen: User profile check - showProfileSetupNotification=$showProfileSetupNotification, user=$user")
        }
    }

    LaunchedEffect(showProfileSetupNotification) {
        if (showProfileSetupNotification) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Настройте свой профиль и интересы в разделе 'Профиль'!",
                    actionLabel = "Перейти",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val scale by animateFloatAsState(
                    targetValue = if (data.visuals.message.isNotEmpty()) 1f else 0.8f,
                    animationSpec = tween(300)
                )

                AnimatedVisibility(
                    visible = data.visuals.message.isNotEmpty(),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .scale(scale),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = CardDefaults.shape // Rounded corners
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF00796B), // Teal
                                            Color(0xFF00ACC1) // Cyan
                                            // For pink/red gradient, uncomment below and comment above:
                                            // Color(0xFFF06292), // Pink
                                            // Color(0xFFF50057) // Red
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_profile1),
                                        contentDescription = "Profile Icon",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = data.visuals.message,
                                        fontFamily = montserratFont,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (data.visuals.actionLabel != null) {
                                    TextButton(
                                        onClick = {
                                            if (data.visuals.actionLabel == "Перейти") {
                                                navController.navigate(Profile(userId)) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    restoreState = true
                                                    launchSingleTop = true
                                                }
                                                showProfileSetupNotification = false
                                                coroutineScope.launch {
                                                    UserPreferences.setHasSeenProfileNotification(
                                                        context,
                                                        true
                                                    )
                                                }
                                            }
                                            data.dismiss()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFFFFCA28)
                                        )
                                    ) {
                                        Text(
                                            text = data.visuals.actionLabel!!,
                                            fontFamily = montserratFont,
                                            fontSize = 14.sp,
                                            color = Color(0xFFFFCA28)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.surface,
                contentColor = colorScheme.onSurface
            ) {
                tabs.forEach { item ->
                    val selected = when (item.tabId) {
                        "feed" -> currentRoute?.startsWith("ru.walkAndTalk.ui.screens.Feed") == true ||
                                currentRoute?.startsWith("ru.walkAndTalk.ui.screens.EventDetails") == true

                        "search" -> currentRoute?.startsWith("ru.walkAndTalk.ui.screens.Search") == true
                        "chats" -> currentRoute?.startsWith("ru.walkAndTalk.ui.screens.Chats") == true
//                        "profile" -> currentRoute?.startsWith("ru.walkAndTalk.ui.screens.Profile") == true ||
//                                currentRoute?.startsWith("ru.walkAndTalk.ui.screens.EditProfile") == true ||
//                                currentRoute?.startsWith("profile/") == true
                        "profile" -> currentRoute?.startsWith("ru.walkAndTalk.ui.screens.Profile") == true ||
                                currentRoute?.startsWith("ru.walkAndTalk.ui.screens.EditProfile") == true ||
                                currentRoute?.startsWith("profile/") == true ||
                                currentRoute == "ru.walkAndTalk.ui.screens.EventStatistics"

                        else -> false
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                    saveState = true
                                }
                                restoreState = true
                                launchSingleTop = true
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontFamily = montserratFont,
                                color = if (selected) colorScheme.primary else colorScheme.onSurface.copy(
                                    alpha = 0.6f
                                )
                            )
                        },
                        icon = {
                            Box(
                                modifier = Modifier.padding(bottom = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(item.iconId),
                                    contentDescription = if (selected) "${item.label} (выбрано)" else item.label,
                                    tint = if (selected) colorScheme.primary else colorScheme.onSurface.copy(
                                        alpha = 0.6f
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .height(3.dp)
                                            .width(24.dp)
                                            .background(colorScheme.primary)
                                    )
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.6f),
                            selectedTextColor = colorScheme.primary,
                            unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Feed,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable<Feed> { backStackEntry ->
                FeedScreen(
                    navController = navController,
                    feedViewModel = feedViewModel
                )
                LaunchedEffect(feedViewModel) {
                    feedViewModel.container.sideEffectFlow.collect { sideEffect ->
                        when (sideEffect) {
                            is FeedSideEffect.NavigateToEventDetails -> {
                                navController.navigate(EventDetails(sideEffect.eventId))
                            }

                            is FeedSideEffect.NavigateToNotifications -> {
                                navController.navigate(Notifications)
                            }

                            is FeedSideEffect.NavigateToChat -> {
                                navController.navigate("chat/${sideEffect.chatId}")
                            }

                            is FeedSideEffect.ParticipateInEvent -> {
                                val event =
                                    feedViewModel.container.stateFlow.value.events.find { it.id == sideEffect.eventId }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Вы успешно записались на '${event?.title ?: "мероприятие"}' ${
                                            event?.let { feedViewModel.formatEventDate(it.eventDate) } ?: ""
                                        }!"
                                    )
                                }
                            }

                            is FeedSideEffect.ShowError -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message = sideEffect.message)
                                }
                            }

                            is FeedSideEffect.LeaveEventSuccess -> {
                                val event =
                                    feedViewModel.container.stateFlow.value.events.find { it.id == sideEffect.eventId }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Вы успешно отменили участие в '${event?.title ?: "мероприятии"}'!"
                                    )
                                }
                            }

                            is FeedSideEffect.ShowMessage -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message = sideEffect.message)
                                }
                            }

                            is FeedSideEffect.NavigateToAnnouncementDetails -> {
                                navController.navigate("announcement_details/${sideEffect.announcementId}")
                            }
                        }
                    }
                }
            }
            composable<Search> {
                SearchScreen(
                    userId = userId,
                    navController = navController
                )
            }
            composable<Chats> {
                ChatsScreen(
                    userId = userId,
                    navController = navController,
                    viewModel = koinViewModel(parameters = { parametersOf(userId) })
                )
            }
            composable<Profile> {
                val profile = it.toRoute<Profile>()
                ProfileScreen(
                    viewModel = koinViewModel(parameters = { parametersOf(profile.userId) }),
                    onNavigateAuth = onNavigateAuth,
                    onNavigateEditProfile = { navController.navigate(EditProfile) },
                    onNavigateEventStatistics = { navController.navigate(EventStatistics) },
                    onNavigateAdminScreen = { onNavigateAdmin(profile.userId) },
                    isViewOnly = profile.viewOnly,
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
            composable<Admin> { backStackEntry ->
                val admin = backStackEntry.toRoute<Admin>()
                val adminViewModel: AdminViewModel = koinViewModel()
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
                    onAnnouncementClick = { announcementId ->
                        navController.navigate(AnnouncementDetails(announcementId))
                    }
                )
            }
            composable<AnnouncementDetails> { backStackEntry ->
                val announcement = backStackEntry.toRoute<AnnouncementDetails>()
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
            composable<EventDetails> { navBackStackEntry ->
                val eventId = navBackStackEntry.toRoute<EventDetails>().eventId
                val eventDetailsViewModel: EventDetailsViewModel = koinViewModel()
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
//            composable(
//                route = EventDetails.ROUTE,
//                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
//            ) { navBackStackEntry ->
//                val eventId = navBackStackEntry.arguments?.getString("eventId")
//                if (eventId != null) {
//                    val eventDetailsViewModel: EventDetailsViewModel = koinViewModel()
//                    EventDetailsScreen(
//                        onNavigateBack = { navController.popBackStack() },
//                        eventId = eventId,
//                        viewModel = eventDetailsViewModel,
//                        feedViewModel = feedViewModel,
//                        onNavigateToEditEvent = { navController.navigate("edit_event/$it") },
//                        onNavigateToChat = { chatId ->
//                            println("MainScreen: Navigating to ChatsScreen and chatId=$chatId")
//                            navController.navigate(Chats) {
//                                popUpTo(navController.graph.startDestinationId) {
//                                    saveState = true
//                                }
//                                restoreState = true
//                                launchSingleTop = true
//                            }
//                            navController.navigate("chat/$chatId")
//                        }
//                    )
//                    LaunchedEffect(eventDetailsViewModel) {
//                        eventDetailsViewModel.container.sideEffectFlow.collect { sideEffect ->
//                            when (sideEffect) {
//                                is EventDetailsSideEffect.NavigateToChat -> {
//                                    // Обработка в onNavigateToChat, здесь не требуется
//                                }
//
//                                is EventDetailsSideEffect.ShowError -> {
//                                    coroutineScope.launch {
//                                        snackbarHostState.showSnackbar(sideEffect.message)
//                                    }
//                                }
//
//                                is EventDetailsSideEffect.OnNavigateBack -> {
//                                    navController.popBackStack()
//                                }
//
//                                is EventDetailsSideEffect.NavigateToEditEvent -> {
//                                    navController.navigate("edit_event/${sideEffect.eventId}")
//                                }
//
//                                is EventDetailsSideEffect.EventDeleted -> {
//                                    coroutineScope.launch {
//                                        snackbarHostState.showSnackbar("Мероприятие удалено")
//                                    }
//                                    navController.popBackStack()
//
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Text(
//                            text = "Ошибка: ID мероприятия не найден",
//                            modifier = Modifier.align(Alignment.Center)
//                        )
//                    }
//                }
//            }
            composable<EditEvent> { navBackStackEntry ->
                val eventId = navBackStackEntry.toRoute<EditEvent>().eventId
                EditEventScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() },
                    onEventUpdated = { navController.popBackStack() }
                )
            }
            composable<EditProfile> { navBackStackEntry ->
                val userId = navBackStackEntry.toRoute<EditProfile>()
                EditProfileScreen(
                    navController = navController,
                    viewModel = koinViewModel(parameters = { parametersOf(userId) })
                )
            }
            composable<EventStatistics> { navBackStackEntry ->
                val userId1 = navBackStackEntry.toRoute<EventStatistics>()
                EventStatisticsScreen(
                    userId = userId,
                    navController = navController,
                    viewModel = koinViewModel(parameters = { parametersOf(userId) })
                )
            }
//            composable<Chats> { navBackStackEntry ->
//
//            }
            composable(
                route = "chat/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                if (chatId != null) {
                    ChatScreen(
                        chatId = chatId,
                        userId = userId,
                        navController = navController,
                        viewModel = koinViewModel(parameters = { parametersOf(chatId, userId) })
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Ошибка: ID чата не найден",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            composable<Notifications> {
                NotificationsScreen(
                    navController = navController,
                    userId = userId,
                    viewModel = koinViewModel(parameters = { parametersOf(userId) })
                )
            }
        }
    }
}
