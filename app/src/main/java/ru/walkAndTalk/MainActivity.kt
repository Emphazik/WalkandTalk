package ru.walkAndTalk

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.KoinAndroidContext
import ru.walkAndTalk.ui.screens.Screens
import ru.walkAndTalk.ui.screens.auth.AuthScreen
import ru.walkAndTalk.ui.screens.onboarding.OnboardingScreen
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
                    composable("auth") {
                        AuthScreen()
                    }
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
