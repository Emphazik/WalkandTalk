package ru.walkAndTalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.walkAndTalk.ui.screens.auth.WelcomeScreen
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
import ru.walkAndTalk.ui.screens.main.MainScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
import ru.walkAndTalk.ui.screens.splash.SplashLogoScreen

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            SetupNavGraph(navController)
            handleRedirect(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.toString().startsWith("vk53306543://vk.com")) {
                println("Redirect detected: $uri")
                navController.navigate("main") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                println("Navigating to main from redirect")
            }
        }
    }

    @Composable
    fun SetupNavGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashLogoScreen(navController, this@MainActivity)
            }
            composable("onboarding") {
                OnboardingScreen(navController, this@MainActivity)
            }
            composable("welcome") {
                WelcomeScreen(navController)
            }
            composable("login") {
                LoginScreen(navController)
            }
            composable("register") {
                RegisterScreen(navController)
            }
            composable("main") {
                MainScreen(navController)
            }
        }
    }
}

//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()
//        super.onCreate(savedInstanceState)
//
//        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
//        val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_complete", false)
//
//        setContent {
//            WalkTalkTheme {
//                val navController = rememberNavController()
//                val scope = rememberCoroutineScope()
//
//                val startDestination = if (isOnboardingCompleted) "splash" else "onboarding"
//
//                NavHost(navController = navController, startDestination = startDestination) {
//                    composable("splash") {
//                        SplashLogoScreen( navController)
//                    }
//                    composable("onboarding") {
//                        OnboardingScreen(navController, this@MainActivity)
//                    }
//                    composable("auth") {
//                        AuthScreen()
//                    }
//                }
//
//                LaunchedEffect(Unit) {
//                    scope.launch {
//                        delay(3000)
//                        navController.navigate("auth") {
//                            popUpTo("splash") { inclusive = true }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
