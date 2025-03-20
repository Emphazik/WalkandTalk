package ru.walkAndTalk.ui.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.ui.theme.OnBackground
import ru.walkAndTalk.ui.theme.Primary

@Composable
fun RegisterScreen(navController: NavHostController) { // Аналогично, принимаем navController
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Страница регистрации")
        Button(onClick = { /* Логика регистрации */ }) {
            Text("Зарегистрироваться")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("login") }) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}

//@Composable
//fun RegisterScreen(
//    viewModel: RegisterViewModel = koinViewModel(),
//    onLoginClick: () -> Unit,
//) {
//    val state by viewModel.collectAsState()
//
//    viewModel.collectSideEffect {
//        when (it) {
//            is RegisterSideEffect.OnLoginClick -> onLoginClick()
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
//            text = "Регистрация",
//            style = MaterialTheme.typography.headlineLarge,
//            color = OnBackground,
//            textAlign = TextAlign.Center
//        )
//        Spacer(modifier = Modifier.height(1.dp))
//        OutlinedTextField(
//            value = state.login,
//            onValueChange = { viewModel.onLoginChanged(it) },
//            label = { Text("Имя пользователя", color = OnBackground) },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            textStyle = TextStyle(color = OnBackground)
//        )
//
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
//        OutlinedTextField(
//            value = state.confirmPassword,
//            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
//            label = { Text("Подтвердите пароль", color = OnBackground) },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            visualTransformation = PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            textStyle = TextStyle(color = OnBackground)
//        )
//        Button(
//            onClick = { viewModel.onRegisterClick() },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Text(
//                text = "Зарегистрироваться",
//                fontSize = 16.sp,
//                color = OnBackground
//            )
//        }
//        Button(
//            onClick = { viewModel.onLoginClick() },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Transparent
//            ),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Text(
//                text = "Войти",
//                fontSize = 16.sp,
//                color = Primary
//            )
//        }
//    }
//}