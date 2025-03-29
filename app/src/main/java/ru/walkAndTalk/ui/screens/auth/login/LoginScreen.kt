package ru.walkAndTalk.ui.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.vk.id.AccessToken
import com.vk.id.VKID
import com.vk.id.VKIDAuthFail
import com.vk.id.auth.VKIDAuthCallback
import kotlinx.coroutines.launch
import ru.walkAndTalk.R

@Composable
fun LoginScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.blue_abstractback),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вход",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = "",
                onValueChange = {},
                label = { Text("E-mail или телефон") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = "",
                onValueChange = {},
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* Логика входа */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("ИЛИ", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        println("Starting VK ID authorization")
                        VKID.instance.authorize(object : VKIDAuthCallback {
                            override fun onAuth(accessToken: AccessToken) {
                                println("VK ID Auth Success: ${accessToken.token}")
                                navController.navigate("main") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                                println("Navigation to main triggered")
                            }

                            override fun onFail(fail: VKIDAuthFail) {
                                when (fail) {
                                    is VKIDAuthFail.Canceled -> println("VK ID Auth Canceled")
                                    else -> println("VK ID Auth Failed: ${fail.description}")
                                }
                            }
                        })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C75A3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти через VK", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Создать аккаунт", fontSize = 16.sp, color = Color(0xFF00796B))
            }
        }
    }
}


//fun LoginScreen(navController: NavHostController) { // Теперь принимаем navController
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Страница входа")
//        Button(onClick = { /* Логика авторизации */ }) {
//            Text("Авторизоваться")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = { navController.navigate("register") }) {
//            Text("Еще нет аккаунта? Зарегистрируйтесь")
//        }
//    }
//}

//@Composable
//fun LoginScreen(
//    viewModel: LoginViewModel = koinViewModel(),
//    onRegisterClick: () -> Unit,
//) {
//    val state by viewModel.collectAsState()
//
//    viewModel.collectSideEffect {
//        when (it) {
//            is LoginSideEffect.OnRegisterClick -> onRegisterClick()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Text(
//            text = "Вход",
//            style = MaterialTheme.typography.headlineLarge,
//            color = OnBackground,
//            textAlign = TextAlign.Center
//        )
//        Spacer(modifier = Modifier.height(1.dp))
//        OutlinedTextField(
//            value = state.username,
//            onValueChange = { viewModel.onUsernameChanged(it) },
//            label = { Text("Имя пользователя", color = OnBackground) },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            textStyle = TextStyle(color = OnBackground)
//        )
//        OutlinedTextField(
//            value = state.password,
//            onValueChange = { viewModel.onPasswordChanged(it) },
//            label = { Text("Пароль", color = OnBackground) },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            visualTransformation = PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            textStyle = TextStyle(color = OnBackground)
//        )
//        Button(
//            onClick = { viewModel.onLoginClick() },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Secondary
//            )
//        ) {
//            Text(
//                text = "Войти",
//                fontSize = 16.sp,
//                color = OnBackground
//            )
//        }
//        Button(
//            onClick = { viewModel.onRegisterClick() },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Transparent
//            ),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Text(
//                text = "Зарегистрироваться",
//                fontSize = 16.sp,
//                color = Primary
//            )
//        }
//    }
//}