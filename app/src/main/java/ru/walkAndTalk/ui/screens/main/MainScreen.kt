package ru.walkAndTalk.ui.screens.main

import SearchScreen
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.Chats
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.screens.Feed
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.Search
import ru.walkAndTalk.ui.screens.chat.ChatsScreen
import ru.walkAndTalk.ui.screens.main.feed.FeedScreen
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsScreen
import ru.walkAndTalk.ui.screens.main.profile.ProfileScreen

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

sealed class BottomNavBarItem(
    val route: Any,
    val label: String,
    @DrawableRes val iconId: Int,
) {
    data object FeedItem : BottomNavBarItem(Feed, "Главная", R.drawable.ic_home1)
    data object SearchItem : BottomNavBarItem(Search, "Поиск", R.drawable.ic_people64)
    data object ChatsItem : BottomNavBarItem(Chats, "Чаты", R.drawable.ic_chat64)
    data class ProfileItem(val id: String) : BottomNavBarItem(Profile(id), "Профиль", R.drawable.ic_profile1)
}

@Composable
fun MainScreen(
    userId: String,
    onNavigateAuth: () -> Unit,
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
    val feedViewModel: FeedViewModel = koinViewModel(
        parameters = { parametersOf(userId) } // Передаем userId в FeedViewModel
    )
    var isSnackbarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarHostState.currentSnackbarData) {
        isSnackbarVisible = snackbarHostState.currentSnackbarData != null
    }

    Scaffold(
        snackbarHost = {
            AnimatedVisibility(
                visible = isSnackbarVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { it }
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)) + slideOutVertically(
                    animationSpec = tween(durationMillis = 300),
                    targetOffsetY = { it }
                )
            ) {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        contentColor = colorScheme.onSurface,
                        containerColor = colorScheme.surfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { item ->
                    val selected = currentRoute == item.route.toString()
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
                        label = { Text(item.label) },
                        icon = {
                            Icon(
                                painter = painterResource(item.iconId),
                                contentDescription = item.label,
                                tint = if (selected) colorScheme.primary else colorScheme.onBackground.copy(
                                    alpha = 0.5f
                                )
                            )
                        }
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
                FeedScreen(navController = navController, feedViewModel = feedViewModel) // Передаем один экземпляр
                LaunchedEffect(feedViewModel) {
                    feedViewModel.container.sideEffectFlow.collect { sideEffect ->
                        when (sideEffect) {
                            is FeedSideEffect.NavigateToEventDetails -> {
                                navController.navigate(EventDetails.createRoute(sideEffect.eventId))
                            }
                            is FeedSideEffect.ParticipateInEvent -> {
                                val event = feedViewModel.container.stateFlow.value.events.find { it.id == sideEffect.eventId }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Вы успешно записались на '${event?.title ?: "мероприятие"}' ${event?.let { feedViewModel.formatEventDate(it.eventDate) } ?: ""}!"
                                    )
                                }
                            }
                            is FeedSideEffect.ShowError -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = sideEffect.message
                                    )
                                }
                            }
                            is FeedSideEffect.LeaveEventSuccess -> {
                                val event = feedViewModel.container.stateFlow.value.events.find { it.id == sideEffect.eventId }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Вы успешно отменили участие в '${event?.title ?: "мероприятии"}'!"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            composable<Search> { SearchScreen() }
            composable<Chats> { ChatsScreen() }
            composable<Profile> {
                ProfileScreen(
                    viewModel = koinViewModel(
                        parameters = {
                            parametersOf(it.toRoute<Profile>().userId)
                        }
                    ),
                    onNavigateAuth = onNavigateAuth
                )
            }
            composable(
                route = EventDetails.ROUTE,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { navBackStackEntry ->
                val eventId = navBackStackEntry.arguments?.getString("eventId")
                if (eventId != null) {
                    EventDetailsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        eventId = eventId,
                        viewModel = koinViewModel(),
                        feedViewModel = feedViewModel // Передаем один экземпляр
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Ошибка: ID мероприятия не найден",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
