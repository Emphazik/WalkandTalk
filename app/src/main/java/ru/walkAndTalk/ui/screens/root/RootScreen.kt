package ru.walkAndTalk.ui.screens.root

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ru.walkAndTalk.ui.screens.Auth
import ru.walkAndTalk.ui.screens.Login
import ru.walkAndTalk.ui.screens.Main
import ru.walkAndTalk.ui.screens.Onboarding
import ru.walkAndTalk.ui.screens.Registration
import ru.walkAndTalk.ui.screens.Splash
import ru.walkAndTalk.ui.screens.Welcome
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
import ru.walkAndTalk.ui.screens.main.MainScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
import ru.walkAndTalk.ui.screens.splash.SplashScreen
import ru.walkAndTalk.ui.screens.welcome.WelcomeScreen

@Composable
fun RootScreen(intent: Intent) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Splash,
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateOnboarding = {
                    navController.navigate(Onboarding) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                onNavigateWelcome = {
                    navController.navigate(Auth) {
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
                    }
                )
            }
            composable<Login> {
                LoginScreen(
                    onNavigateRegister = {
                        navController.navigate(Registration) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateMain = {
                        navController.navigate(Main) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
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
                    onNavigateMain = {
                        navController.navigate(Main) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable<Main> {
            MainScreen()
        }
    }

    LaunchedEffect(intent) {
        intent.data?.let { uri ->
            if (uri.toString().startsWith("vk53306543://vk.com")) {
                navController.navigate(Main) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
}

