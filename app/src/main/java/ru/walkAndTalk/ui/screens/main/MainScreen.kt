package ru.walkAndTalk.ui.screens.main

import SearchScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.Chats
import ru.walkAndTalk.ui.screens.Feed
import ru.walkAndTalk.ui.screens.Search
import ru.walkAndTalk.ui.screens.chat.ChatsScreen
import ru.walkAndTalk.ui.screens.feed.FeedScreen
import ru.walkAndTalk.ui.screens.profile.ProfileScreen
import ru.walkAndTalk.ui.screens.profile.ProfileViewModel

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

@Composable
fun MainScreen(userId: String, viewModel: MainViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = listOf(Feed, Search, Chats, Profile(userId))
    val tabLabels = listOf("Главная", "Поиск", "Чаты", "Профиль")
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, route ->
                    val selected = currentRoute == route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                    saveState = true
                                }
                                restoreState = true
                                launchSingleTop = true
                            }
                        },
                        label = { Text(tabLabels[index]) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> R.drawable.ic_home1
                                        1 -> R.drawable.ic_people64
                                        2 -> R.drawable.ic_chat64
                                        else -> R.drawable.ic_profile1
                                    }
                                ),
                                contentDescription = tabLabels[index],
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
        )
        {
            composable<Feed> { FeedScreen() }
            composable<Search> { SearchScreen() }
            composable<Chats> { ChatsScreen() }
            composable<Profile> {
                ProfileScreen(userId = it.toRoute<Profile>().userId)
            }
        }
    }
}
//@Composable
//fun MainScreen(userId: String, viewModel: MainViewModel = koinViewModel()) {
//    val state by viewModel.collectAsState()
//    val tabs = listOf("Главная", "Поиск", "Чаты", "Профиль")
//    val colorScheme = MaterialTheme.colorScheme
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar(
//                containerColor = colorScheme.background,
//                modifier = Modifier.height(128.dp)
//            ) {
//                tabs.forEachIndexed { index, title ->
//                    val selected = state.selectedTab == index
//                    NavigationBarItem(
//                        icon = {
//                            Icon(
//                                painter = painterResource(
//                                    id = when (index) {
//                                        0 -> R.drawable.ic_home1
//                                        1 -> R.drawable.ic_people64
//                                        2 -> R.drawable.ic_chat64
//                                        else -> R.drawable.ic_profile1
//                                    }
//                                ),
//                                contentDescription = title,
//                                tint = if (selected) colorScheme.primary else colorScheme.onBackground.copy(
//                                    alpha = 0.5f
//                                )
//                            )
//                        },
//                        label = {
//                            Text(
//                                text = title,
//                                fontFamily = montserratFont,
//                                fontSize = 12.sp,
//                                color = if (selected) colorScheme.primary else colorScheme.onBackground.copy(
//                                    alpha = 0.5f
//                                )
//                            )
//                        },
//                        selected = selected,
//                        onClick = { viewModel.onSelectedTabChange(index) }
//                    )
//                }
//            }
//        }
//    ) { padding ->
//        // Здесь поменять табы на NavHost(RootScreen). и связать с bottomBar. Вынести Screens.
//        when (state.selectedTab) {
//            0 -> FeedScreen()
//            1 -> SearchScreen()
//            2 -> ChatsScreen()
//            3 -> ProfileScreen()
//        }
//    }
//}


//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//
//@Composable
//fun MainScreen(navController: NavHostController) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = "Welcome to Main Screen!",
//            fontSize = 24.sp
//        )
//    }
//}