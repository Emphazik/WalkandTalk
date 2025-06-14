package ru.walkAndTalk.ui.screens.admin.edit


import android.net.Uri
import android.util.Log
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
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.ui.screens.admin.AdminSideEffect
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import ru.walkAndTalk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    userId: String,
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = koinViewModel()
) {
    val state by viewModel.container.stateFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vkId by remember { mutableStateOf("") }
    var interestIds by remember { mutableStateOf("") }
    var cityKnowledgeLevelId by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf<String?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var vkIdError by remember { mutableStateOf(false) }
    var interestIdsError by remember { mutableStateOf(false) }
    var cityKnowledgeLevelIdError by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(userId) {
        val user = state.users.find { it.id == userId }
        user?.let {
            name = it.name
            email = it.email
            password = it.password
            phone = it.phone
            vkId = (it.vkId ?: "").toString()
            interestIds = it.interestIds.joinToString(",")
            cityKnowledgeLevelId = it.cityKnowledgeLevelId ?: ""
            bio = it.bio ?: ""
            goals = it.goals ?: ""
            isAdmin = it.isAdmin
            gender = it.gender
        } ?: run {
            snackbarHostState.showSnackbar("Пользователь не найден")
            onBackClick()
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
            Log.d("EditUserScreen", "Received sideEffect: $sideEffect")
            when (sideEffect) {
                is AdminSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
                is AdminSideEffect.UserSaved -> {
                    snackbarHostState.showSnackbar("Пользователь успешно обновлен")
                    onBackClick()
                }
                else -> {}
            }
        }
    }

    // Image picker launcher
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
                        text = "Редактировать пользователя",
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
                    } ?: state.users.find { it.id == userId }?.profileImageUrl?.let {
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
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.3f),
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
                    passwordError = it.isNotEmpty() && it.length < 6
                },
                label = { Text("Пароль (оставьте пустым, чтобы не менять)", fontFamily = montserratFont, fontSize = 14.sp) },
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

            // VK ID Field
            OutlinedTextField(
                value = vkId,
                onValueChange = {
                    vkId = it
                    vkIdError = it.isNotEmpty() && !it.matches(Regex("^[0-9]+$"))
                },
                label = { Text("VK ID", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = vkIdError,
                supportingText = {
                    if (vkIdError) {
                        Text(
                            text = "VK ID должен содержать только цифры",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Interest IDs Field
            OutlinedTextField(
                value = interestIds,
                onValueChange = {
                    interestIds = it
                    interestIdsError = it.isNotEmpty() && !it.matches(Regex("^([0-9a-fA-F-]+,)*[0-9a-fA-F-]+$"))
                },
                label = { Text("ID интересов (через запятую)", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = interestIdsError,
                supportingText = {
                    if (interestIdsError) {
                        Text(
                            text = "ID интересов должны быть UUID, разделенные запятыми",
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

            // City Knowledge Level ID Field
            OutlinedTextField(
                value = cityKnowledgeLevelId,
                onValueChange = {
                    cityKnowledgeLevelId = it
                    cityKnowledgeLevelIdError = it.isNotEmpty() && !it.matches(Regex("^[0-9a-fA-F-]+$"))
                },
                label = { Text("ID уровня знаний о городе", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = cityKnowledgeLevelIdError,
                supportingText = {
                    if (cityKnowledgeLevelIdError) {
                        Text(
                            text = "ID должен быть в формате UUID",
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

            // Bio Field
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Биография", fontFamily = montserratFont, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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

            // Goals Field
            OutlinedTextField(
                value = goals,
                onValueChange = { goals = it },
                label = { Text("Цели", fontFamily = montserratFont, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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

            // Gender Field
            var genderExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = gender?.let {
                        when (it) {
                            "male" -> "Мужской"
                            "female" -> "Женский"
                            "other" -> "Другой"
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
                    modifier = Modifier.background(colorScheme.background)
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
                        passwordError = password.isNotEmpty() && password.length < 6
                        phoneError = phone.isNotEmpty() && !isValidPhone(phone)
                        vkIdError = vkId.isNotEmpty() && !vkId.matches(Regex("^[0-9]+$"))
                        interestIdsError = interestIds.isNotEmpty() && !interestIds.matches(Regex("^([0-9a-fA-F-]+,)*[0-9a-fA-F-]+$"))
                        cityKnowledgeLevelIdError = cityKnowledgeLevelId.isNotEmpty() && !cityKnowledgeLevelId.matches(Regex("^[0-9a-fA-F-]+$"))

                        if (!nameError.isNullOrEmpty() || emailError || passwordError || phoneError || vkIdError || interestIdsError || cityKnowledgeLevelIdError) {
                            Log.d("EditUserScreen", "Validation failed: nameError=$nameError, emailError=$emailError, passwordError=$passwordError, phoneError=$phoneError, vkIdError=$vkIdError, interestIdsError=$interestIdsError, cityKnowledgeLevelIdError=$cityKnowledgeLevelIdError")
                        } else {
                            viewModel.updateUser(
                                userId = userId,
                                name = name,
                                email = email,
                                password = if (password.isNotEmpty()) password else null,
                                phone = phone,
                                isAdmin = isAdmin,
                                gender = gender,
                                profileImageUri = profileImageUri,
                                vkId = vkId.takeIf { it.isNotEmpty() },
                                interestIds = interestIds.takeIf { it.isNotEmpty() }?.split(",")?.map { it.trim() } ?: emptyList(),
                                cityKnowledgeLevelId = cityKnowledgeLevelId.takeIf { it.isNotEmpty() },
                                bio = bio.takeIf { it.isNotEmpty() },
                                goals = goals.takeIf { it.isNotEmpty() }
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