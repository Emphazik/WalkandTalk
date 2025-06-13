package ru.walkAndTalk.ui.screens.admin.edit


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.ui.screens.admin.AdminSideEffect
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import ru.walkAndTalk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    userId: String,
    viewModel: AdminViewModel = koinViewModel(),
    onBackClick: () -> Unit = {}

) {
    val state by viewModel.container.stateFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(userId) {
        val user = state.users.find { it.id == userId }
        user?.let {
            name = user.name
            email = user.email
            phone = user.phone
            isAdmin = user.isAdmin
            gender = user.gender
        } ?: run {
            snackbarHostState.showSnackbar("Пользователь не найден")
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is AdminSideEffect.ShowError -> snackbarHostState.showSnackbar(sideEffect.message)
                is AdminSideEffect.UserSaved -> onBackClick()
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Редактировать пользователя",
                        fontFamily = montserratFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
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

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isEmpty()
                },
                label = { Text("Имя", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text(
                            text = "Имя обязательно",
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
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.3f),
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
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.3f),
                    errorIndicatorColor = colorScheme.error,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            // Gender Field
            var genderExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = gender ?: "",
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
//                                painter = painterResource(id = R.drawable.ic_dropdown),
                                contentDescription = "Select Gender",
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.3f),
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )
                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false },
                    modifier = Modifier.background(colorScheme.surface)
                ) {
                    listOf("male", "female", "other").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (option) {
                                        "male" -> "Мужской"
                                        "female" -> "Женский"
                                        else -> "Другой"
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
                    onClick = { onBackClick() },
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
                        nameError = name.isEmpty()
                        emailError = !isValidEmail(email)
                        phoneError = phone.isNotEmpty() && !isValidPhone(phone)

                        if (!nameError && !emailError && !phoneError) {
                            viewModel.updateUser(
                                userId = userId,
                                name = name,
                                email = email,
                                phone = phone,
                                isAdmin = isAdmin,
                                gender = gender
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