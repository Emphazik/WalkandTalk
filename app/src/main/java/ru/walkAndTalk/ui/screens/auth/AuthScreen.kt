//package ru.walkAndTalk.ui.screens.auth
//
//import androidx.compose.animation.animateContentSize
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import ru.walkAndTalk.ui.screens.Screens
//import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
//import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen
//import ru.walkAndTalk.ui.theme.DarkBlue
//import ru.walkAndTalk.ui.theme.DarkGray
//
//@Composable
//fun AuthScreen(navController: NavHostController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        DarkBlue,
//                        DarkGray
//                    )
//                )
//            )
//            .padding(16.dp)
//            .animateContentSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        NavHost(
//            navController = navController, // Передаем контроллер
//            startDestination = "login",
//            modifier = Modifier.animateContentSize()
//        ) {
//            composable("login") {
//                LoginScreen(
//                    onRegisterClick = { navController.navigate("register") }
//                )
//            }
//            composable("register") {
//                RegisterScreen(
//                    onLoginClick = { navController.navigate("login") }
//                )
//            }
//        }
//    }
//}
