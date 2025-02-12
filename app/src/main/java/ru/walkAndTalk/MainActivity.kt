package ru.walkAndTalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.KoinAndroidContext
import ru.walkAndTalk.ui.screens.Screens
import ru.walkAndTalk.ui.screens.auth.AuthScreen
import ru.walkAndTalk.ui.screens.splash.SplashLogoScreen
import ru.walkAndTalk.ui.theme.WalkTalkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            KoinAndroidContext {
                WalkTalkTheme {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screens.SPLASH) {
                        composable(Screens.SPLASH) {
                            SplashLogoScreen(navController = navController)
                        }
                        composable(Screens.AUTH) {
                            AuthScreen()
                        }
                    }
                }
            }
        }
    }
}