package ru.walkAndTalk.ui.screens.auth

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.walkAndTalk.ui.screens.Screens
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen

@Composable
fun AuthScreen() {
    val navController = rememberNavController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9556E0),
                        Color(0xFF000000)
                    )
                )
            )
            .padding(16.dp).animateContentSize(),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(navController, startDestination = Screens.LOGIN, modifier = Modifier.animateContentSize()){
            composable(Screens.LOGIN) {
                LoginScreen(
                    onRegisterClick = { navController.navigate(Screens.REGISTER) }
                )
            }
            composable(Screens.REGISTER) {
                RegisterScreen(
                    onLoginClick = { navController.navigate(Screens.LOGIN) }
                )
            }
        }
    }
}