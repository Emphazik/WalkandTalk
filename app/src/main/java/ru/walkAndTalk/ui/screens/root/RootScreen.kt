package ru.walkAndTalk.ui.screens.root

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.screens.Admin
import ru.walkAndTalk.ui.screens.Auth
import ru.walkAndTalk.ui.screens.EditProfile
import ru.walkAndTalk.ui.screens.Login
import ru.walkAndTalk.ui.screens.Main
import ru.walkAndTalk.ui.screens.Onboarding
import ru.walkAndTalk.ui.screens.Registration
import ru.walkAndTalk.ui.screens.Splash
import ru.walkAndTalk.ui.screens.Welcome
import ru.walkAndTalk.ui.screens.admin.AdminScreen
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
import ru.walkAndTalk.ui.screens.main.MainScreen
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsViewModel
import ru.walkAndTalk.ui.screens.main.profile.edit.EditProfileScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
import ru.walkAndTalk.ui.screens.splash.SplashScreen
import ru.walkAndTalk.ui.screens.welcome.WelcomeScreen

@Composable
fun RootScreen(intent: Intent) {
    val navController = rememberNavController()
    val supabaseWrapper: SupabaseWrapper = koinInject()
    val localDataStoreRepository: LocalDataStoreRepository = koinInject()

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
        composable<Main> {
            MainScreen(
                userId = it.toRoute<Main>().userId,
                onNavigateAuth = {
                    navController.navigate(Auth) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        composable<Admin> { backStackEntry ->
            val admin = backStackEntry.toRoute<Admin>()
            val adminViewModel: AdminViewModel = koinViewModel()
            AdminScreen(
                navController = navController,
                userId = admin.userId,
                viewModel = adminViewModel
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
                        }.decodeSingle<UserDto>()
                        val userMode = localDataStoreRepository.userMode.first()
                        Log.d("RootScreen", "VK auth: isAdmin=${userData.isAdmin}, userMode=$userMode, userId=${user.id}")
                        if (userData.isAdmin) {
                            if (userMode != "user") {
                                localDataStoreRepository.saveUserMode("admin")
                                Log.d("RootScreen", "VK auth: userMode reset to admin for userId: ${user.id}")
                            }
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
                        } else {
                            localDataStoreRepository.saveUserMode("user")
                            navController.navigate(Main(user.id)) {
                                popUpTo(Splash) { inclusive = true }
                                launchSingleTop = true
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