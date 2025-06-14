package ru.walkAndTalk.ui.screens.admin.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import android.util.Log
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.ui.screens.admin.AdminSideEffect
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import ru.walkAndTalk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = koinViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // Обработка AdminSideEffect для уведомлений
    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
            when (sideEffect) {
                is AdminSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
                is AdminSideEffect.UserSaved -> {
                    snackbarHostState.showSnackbar("Пользователь успешно добавлен")
                }
                else -> {}
            }
        }
    }

    // Лаунчер для выбора изображения
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
        profileImageUri = uri
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (data.visuals.message.contains("Ошибка", ignoreCase = true))
                        colorScheme.errorContainer else colorScheme.primaryContainer,
                    contentColor = if (data.visuals.message.contains("Ошибка", ignoreCase = true))
                        colorScheme.onErrorContainer else colorScheme.onPrimaryContainer,
                    actionColor = colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Добавить пользователя",
                        fontFamily = montserratFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .background(colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val painter = profileImageUri?.let {
                        rememberAsyncImagePainter(
                            model = it,
                            contentScale = ContentScale.Crop
                        )
                    } ?: painterResource(id = R.drawable.preview_profile)
                    Image(
                        painter = painter,
                        contentDescription = "Фото профиля",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = when {
                        it.isEmpty() -> "Имя обязательно"
                        !it.matches(Regex("^[А-Яа-яA-Za-z\\s]+$")) -> "Имя не должно содержать цифры или специальные символы"
                        else -> null
                    }
                },
                label = { Text("Имя", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = colorScheme.error,
                            fontFamily = montserratFont,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = montserratFont, fontSize = 14.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.3f),
                    errorIndicatorColor = colorScheme.error,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = !isValidEmail(it)
                },
                label = { Text("Email", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = emailError,
                supportingText = {
                    if (emailError) {
                        Text(
                            text = "Некорректный email",
                            color = colorScheme.error,
                            fontFamily = montserratFont,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = montserratFont, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.background,
                    unfocusedContainerColor = colorScheme.background,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha= 0.3f),
                    errorIndicatorColor = colorScheme.error,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                label = { Text("Пароль", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = passwordError,
                supportingText = {
                    if (passwordError) {
                        Text(
                            text = "Пароль должен быть не менее 6 символов",
                            color = colorScheme.error,
                            fontFamily = montserratFont,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = montserratFont, fontSize = 14.sp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.background,
                    unfocusedContainerColor = colorScheme.background,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha= 0.3f),
                    errorIndicatorColor = colorScheme.error,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = it.isNotEmpty() && !isValidPhone(it)
                },
                label = { Text("Телефон", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = phoneError,
                supportingText = {
                    if (phoneError) {
                        Text(
                            text = "Некорректный номер телефона",
                            color = colorScheme.error,
                            fontFamily = montserratFont,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = montserratFont, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.background,
                    unfocusedContainerColor = colorScheme.background,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha= 0.3f),
                    errorIndicatorColor = colorScheme.error,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            // Gender Field
            var genderExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = gender?.let {
                        when (it) {
                            "male" -> "Мужской"
                            "female" -> "Женский"
                            else -> ""
                        }
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Пол", fontFamily = montserratFont, fontSize = 14.sp) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    textStyle = TextStyle(fontFamily = montserratFont, fontSize = 14.sp),
                    trailingIcon = {
                        IconButton(onClick = { genderExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Gender",
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.background,
                        unfocusedContainerColor = colorScheme.background,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha= 0.3f),
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )
                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false },
                    modifier = Modifier.background(colorScheme.background)
                ) {
                    listOf("male", "female").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (option) {
                                        "male" -> "Мужской"
                                        "female" -> "Женский"
                                        else -> ""
                                    },
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            // Is Admin Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isAdmin,
                    onCheckedChange = { isAdmin = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorScheme.primary,
                        uncheckedColor = colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
                Text(
                    text = "Администратор",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Отмена",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary
                    )
                }
                Button(
                    onClick = {
                        nameError = when {
                            name.isEmpty() -> "Имя обязательно"
                            !name.matches(Regex("^[А-Яа-яA-Za-z\\s]+$")) -> "Имя не должно содержать цифры или специальные символы"
                            else -> null
                        }
                        emailError = !isValidEmail(email)
                        passwordError = password.length < 6
                        phoneError = phone.isNotEmpty() && !isValidPhone(phone)

                        if (!nameError.isNullOrEmpty() || emailError || passwordError || phoneError) {
                            Log.d("AddUserScreen", "Validation failed: nameError=$nameError, emailError=$emailError, passwordError=$passwordError, phoneError=$phoneError")
                        } else {
                            viewModel.addUser(
                                name = name,
                                email = email,
                                password = password,
                                phone = phone,
                                isAdmin = isAdmin,
                                gender = gender,
                                profileImageUri = profileImageUri
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Сохранить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun isValidPhone(phone: String): Boolean {
    return phone.matches(Regex("^\\+?[1-9]\\d{1,14}\$"))
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}