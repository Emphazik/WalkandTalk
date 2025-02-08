package ru.walkAndTalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.KoinAndroidContext
import ru.walkAndTalk.ui.screens.Screens
import ru.walkAndTalk.ui.screens.auth.AuthScreen
import ru.walkAndTalk.ui.screens.splash.SplashScreen
import ru.walkAndTalk.ui.theme.WalkTalkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KoinAndroidContext {
                WalkTalkTheme {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screens.SPLASH) {
                        composable(Screens.SPLASH) {
                            SplashScreen(onSplashEnded = {
                                navController.navigate(Screens.AUTH) {
                                    popUpTo(Screens.SPLASH) {
                                        inclusive = true
                                    }
                                }
                            })
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