package ru.walkAndTalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.KoinAndroidContext
import ru.walkAndTalk.ui.screens.auth.WelcomeScreen
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
import ru.walkAndTalk.ui.screens.main.MainScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
import ru.walkAndTalk.ui.screens.splash.SplashLogoScreen
import ru.walkAndTalk.ui.theme.WalkTalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinAndroidContext {
                WalkTalkTheme {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = SplashLogoScreen) {
                        composable<SplashLogoScreen> {
                            SplashLogoScreen(navController, this@MainActivity)
                        }
                        composable<OnboardingScreen> {
                            OnboardingScreen(navController, this@MainActivity)
                        }
                        composable<WelcomeScreen> {
                            WelcomeScreen(navController)
                        }
                        composable<LoginScreen> {
                            LoginScreen(navController)
                        }
                        composable<RegisterScreen> {
                            RegisterScreen(navController)
                        }
                        composable<MainScreen> {
                            MainScreen(navController)
                        }
                    }
                    LaunchedEffect(intent) {
                        intent?.data?.let { uri ->
                            if (uri.toString().startsWith("vk53306543://vk.com")) {
                                println("Redirect detected: $uri")
                                navController.navigate(MainScreen) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                                println("Navigating to main from redirect")
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}







