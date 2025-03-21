package ru.walkAndTalk.ui.screens.auth.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ru.walkAndTalk.R
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter


@Composable
fun RegisterScreen(navController: NavHostController, viewModel: RegisterViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    val colorScheme = MaterialTheme.colorScheme // Получаем текущие цвета темы

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = colorScheme.onBackground, // Цвет текста в активном поле
        unfocusedTextColor = colorScheme.onBackground, // Цвет текста в неактивном поле
        focusedContainerColor = colorScheme.background, // Цвет фона текстового поля
        unfocusedContainerColor = colorScheme.background,
        cursorColor = colorScheme.primary, // Цвет курсора
        focusedIndicatorColor = colorScheme.primary, // Цвет линии под текстовым полем
        unfocusedIndicatorColor = colorScheme.secondary
    )

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.onProfileImageSelected(it) }
        }

    Box(modifier = Modifier.fillMaxSize()) {
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
                text = "Создать аккаунт",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(state.profileImageUri),
                        contentDescription = "Фото профиля",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.preview_profile),
                        contentDescription = "Фото профиля",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Имя") },
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = state.phone,
                onValueChange = { viewModel.onPhoneChange(it) },
                label = { Text("Телефон") },
                colors = textFieldColors

            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("E-mail") },
                colors = textFieldColors

            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text(text = "Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors

            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onRegisterClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрироваться", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("login") }) {
                Text("Уже есть аккаунт? Войти", fontSize = 16.sp, color = Color(0xFF00796B))
            }
        }
    }
}


//fun RegisterScreen(navController: NavHostController) { // Аналогично, принимаем navController
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Страница регистрации")
//        Button(onClick = { /* Логика регистрации */ }) {
//            Text("Зарегистрироваться")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = { navController.navigate("login") }) {
//            Text("Уже есть аккаунт? Войти")
//        }
//    }
//}

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