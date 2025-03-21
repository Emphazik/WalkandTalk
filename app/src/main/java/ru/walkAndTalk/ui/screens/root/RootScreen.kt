import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.walkAndTalk.ui.screens.auth.WelcomeScreen
import ru.walkAndTalk.ui.screens.auth.login.LoginScreen
import ru.walkAndTalk.ui.screens.auth.register.RegisterScreen

//package ru.walkAndTalk.ui.screens.root
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import ru.walkAndTalk.ui.screens.auth.AuthScreen
//
//@Composable
//fun RootScreen(navController: NavHostController) {
//    AuthScreen(navController)
//}

//@Composable
//fun RootScreen(navController: NavHostController, isUserLoggedIn: Boolean) {
//    NavHost(navController = navController, startDestination = if (isUserLoggedIn) "main" else "welcome") {
//
//        composable("welcome") {
//            WelcomeScreen(navController)
//        }
//
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = { navController.navigate("main") }
//            )
//        }
//
//        composable("register") {
//            RegisterScreen(
//                onRegisterSuccess = { navController.navigate("main") }
//            )
//        }
//
//        composable("main") {
//            MainScreen() // Главный экран приложения
//        }
//    }
//}
