package ru.walkAndTalk.ui.screens.auth.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import ru.walkAndTalk.R

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RegisterViewModel(context) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
    val state by viewModel.state.collectAsState()

    if (state.isSuccess) {
        navController.navigate("main") {
            popUpTo("register") { inclusive = true }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = colorScheme.onBackground,
        unfocusedTextColor = colorScheme.onBackground,
        focusedContainerColor = colorScheme.background,
        unfocusedContainerColor = colorScheme.background,
        cursorColor = colorScheme.primary,
        focusedIndicatorColor = colorScheme.primary,
        unfocusedIndicatorColor = colorScheme.secondary
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
                        painter = painterResource(id = R.drawable.ic_profile),
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
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = Color.Red,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.onRegisterClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    Text("Загрузка...", fontSize = 18.sp, color = Color.White)
                } else {
                    Text("Зарегистрироваться", fontSize = 18.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("login") }) {
                Text("Уже есть аккаунт? Войти", fontSize = 16.sp, color = Color(0xFF00796B))
            }
        }
    }
}