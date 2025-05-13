package ru.walkAndTalk.ui.screens.main

import SearchScreen
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.Chats
import ru.walkAndTalk.ui.screens.Feed
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.Search
import ru.walkAndTalk.ui.screens.chat.ChatsScreen
import ru.walkAndTalk.ui.screens.feed.FeedScreen
import ru.walkAndTalk.ui.screens.profile.ProfileScreen

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

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { item ->
                    val selected = currentRoute == item.route
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
            composable<Feed> { FeedScreen() }
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
        }
    }
}
