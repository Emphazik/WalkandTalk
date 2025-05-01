@file:Suppress("UNREACHABLE_CODE")

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
import androidx.compose.foundation.layout.size
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
import com.vk.id.VKIDAuthFail
import com.vk.id.onetap.compose.onetap.OneTap
import com.vk.id.onetap.common.OneTapStyle
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

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (!state.value.isLoading) {
                    OneTap(
                        modifier = Modifier.fillMaxWidth(),
                        style = OneTapStyle.Dark(),
                        onAuth = { _, accessToken ->
                            println("VK ID Auth Success: ${accessToken.token}")
                            viewModel.onVKLoginClick(accessToken)
                        },
                        onFail = { _, fail ->
                            val errorMessage = when (fail) {
                                is VKIDAuthFail.Canceled -> "Авторизация через VK отменена"
                                else -> "Ошибка VK: ${fail.description}"
                            }
                            println("VK ID Auth Failed: $errorMessage")
                            viewModel.onVKLoginClickFailed(errorMessage)
                        },
                        signInAnotherAccountButtonEnabled = true,
                        fastAuthEnabled = true
                    )
                } else {
                    CircularProgressIndicator(color = Color(0xFF4C75A3), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Создать аккаунт", fontSize = 16.sp, color = Color(0xFF00796B))
            }

//            Button(
//                onClick = {
//                    scope.launch {
//                        println("Starting VK ID authorization")
//                        VKID.instance.authorize(object : VKIDAuthCallback {
//                            override fun onAuth(accessToken: AccessToken) {
//                                println("VK ID Auth Success: ${accessToken.token}")
//                                viewModel.onVKLoginClick(accessToken)
//                            }
//
//                            override fun onFail(fail: VKIDAuthFail) {
//                                val errorMessage = when (fail) {
//                                    is VKIDAuthFail.Canceled -> "Авторизация через VK отменена"
//                                    else -> "Ошибка VK: ${fail.description}"
//                                }
//                                println("VK ID Auth Failed: $errorMessage")
//                                viewModel.onVKLoginClickFailed(errorMessage)
//                            }
//                        })
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C75A3)),
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !state.value.isLoading
//            ) {
//                if (state.value.isLoading) {
//                    CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
//                } else {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.vk_icon),
//                            contentDescription = "VK Icon",
//                            tint = Color.White,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Spacer(modifier = Modifier.padding(8.dp))
//                        Text("Войти через VK", fontSize = 18.sp, color = Color.White)
//                    }
//                }
//            }

//            Spacer(modifier = Modifier.height(16.dp))

//            TextButton(onClick = { navController.navigate("register") }) {
//                Text("Создать аккаунт", fontSize = 16.sp, color = Color(0xFF00796B))
//            }
        }
    }
}

fun OneTap(modifier: Unit, style: Nothing, onAuth: Nothing, onAuthCode: Nothing, onFail: Nothing, oAuths: Nothing, fastAuthEnabled: Nothing, signInAnotherAccountButtonEnabled: Nothing, authParams: Nothing) {

}
