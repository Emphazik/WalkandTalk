package ru.walkAndTalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import ru.walkAndTalk.ui.screens.auth.AuthScreen
import ru.walkAndTalk.ui.screens.auth.WelcomeScreen
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
import ru.walkAndTalk.ui.screens.main.MainScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
//import ru.walkAndTalk.ui.screens.root.RootScreen
import ru.walkAndTalk.ui.screens.splash.SplashLogoScreen
import ru.walkAndTalk.ui.theme.WalkTalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            WalkTalkTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashLogoScreen(navController, this@MainActivity)
                    }
                    composable("onboarding") {
                        OnboardingScreen(navController, this@MainActivity)
                    }
                    composable("welcome") { WelcomeScreen(navController) }  // Теперь тут передаем navController
                    composable("login") { LoginScreen(navController) }  // Передаем navController в login
                    composable("register") { RegisterScreen(navController) }
                    composable("main") { MainScreen(navController) }
                }
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
