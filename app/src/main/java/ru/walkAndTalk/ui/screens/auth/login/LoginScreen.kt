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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vk.id.AccessToken
import com.vk.id.VKID
import com.vk.id.VKIDAuthFail
import com.vk.id.auth.VKIDAuthCallback
import kotlinx.coroutines.launch
import ru.walkAndTalk.R

@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = viewModel()) {
    val state = viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.value.isSuccess) {
        if (state.value.isSuccess) {
            navController.navigate("main") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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
                value = state.value.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("E-mail или телефон") },
                placeholder = { Text("+79991234567 или email@example.com") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = state.value.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Показываем ошибку, если она есть
            state.value.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onLoginClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.value.isLoading
            ) {
                if (state.value.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
                } else {
                    Text("Войти", fontSize = 18.sp, color = Color.White)
                }
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