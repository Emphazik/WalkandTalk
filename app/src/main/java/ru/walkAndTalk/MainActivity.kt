package ru.walkAndTalk

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
import ru.walkAndTalk.ui.screens.splash.SplashLogoScreen
import ru.walkAndTalk.ui.theme.WalkTalkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            WalkTalkTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashLogoScreen(navController)
                    }
                    composable("auth") {
                        AuthScreen()
                    }
                }

                // Запускаем задержку перед переходом на экран авторизации
                LaunchedEffect(Unit) {
                    scope.launch {
                        delay(3000)
                        navController.navigate("auth") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}