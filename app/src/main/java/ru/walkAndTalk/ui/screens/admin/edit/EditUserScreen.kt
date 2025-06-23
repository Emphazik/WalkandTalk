package ru.walkAndTalk.ui.screens.admin.edit


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.ui.screens.admin.AdminSideEffect
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.main.profile.edit.InterestItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vkId by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf<String?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedInterests by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCityKnowledgeLevel by remember { mutableStateOf<String?>(null) }
    var showInterestSelection by remember { mutableStateOf(false) }
    var showCityKnowledgeMenu by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var vkIdError by remember { mutableStateOf(false) }
    var birthdateError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(yearRange = IntRange(1900, 2025))

    // Load user data
    LaunchedEffect(userId) {
        val user = state.users.find { it.id == userId }
        user?.let {
            name = it.name
            email = it.email
            password = it.password
            phone = it.phone
            vkId = (it.vkId ?: "").toString()
            bio = it.bio ?: ""
            goals = it.goals ?: ""
            birthdate = it.birthdate?.let { convertToDisplayFormat(it) } ?: ""
            city = it.city ?: ""
            isAdmin = it.isAdmin
            gender = it.gender
            selectedInterests = it.interestIds.mapNotNull { id ->
                state.availableInterests.find { it.second == id }?.first
            }
            selectedCityKnowledgeLevel = it.cityKnowledgeLevelId?.let { id ->
                state.cityKnowledgeLevels.find { it.second == id }?.first
            }
        } ?: run {
            snackbarHostState.showSnackbar("Пользователь не найден")
            onBackClick()
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
            when (sideEffect) {
                is AdminSideEffect.ShowError -> snackbarHostState.showSnackbar(sideEffect.message)
                is AdminSideEffect.UserSaved -> {
                    snackbarHostState.showSnackbar("Пользователь успешно обновлен")
                    onBackClick()
                }
                else -> {}
            }
        }
    }

    // Лаунчер для обработки результата обрезки
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        Log.d("CropImage", "Crop result: isSuccessful=${result.isSuccessful}, uri=${result.uriContent}, error=${result.error?.message}")
        when {
            result.isSuccessful && result.uriContent != null -> {
                result.uriContent?.let { croppedUri ->
                    Log.d("CropImage", "Cropped URI: $croppedUri")
                    profileImageUri = croppedUri
                    viewModel.onProfileImageSelected(croppedUri)
                }
            }
            !result.isSuccessful && result.uriContent == null -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Обрезка отменена")
                }
            }
            else -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Ошибка обрезки: ${result.error?.message ?: "Неизвестная ошибка"}")
                }
            }
        }
    }

    // Лаунчер для выбора изображения
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("CropImage", "Launching CropImage with URI: $uri")
                cropImageLauncher.launch(
                    CropImageContractOptions(
                        uri = uri,
                        cropImageOptions = CropImageOptions().apply {
                            guidelines = CropImageView.Guidelines.ON
                            cropShape = CropImageView.CropShape.RECTANGLE
                            aspectRatioX = 1
                            aspectRatioY = 1
                            fixAspectRatio = true
                            minCropResultWidth = 120
                            minCropResultHeight = 120
                            maxCropResultWidth = 1000
                            maxCropResultHeight = 1000
                            showCropLabel = true
                        }
                    )
                )
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Выбор изображения отменен")
            }
        }
    }

    // Лаунчер для запроса разрешения на доступ к галерее
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Требуется разрешение для доступа к галерее")
            }
        }
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
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val painter = profileImageUri?.let {
                        rememberAsyncImagePainter(model = it, contentScale = ContentScale.Crop)
                    } ?: state.users.find { it.id == userId }?.profileImageUrl?.let {
                        rememberAsyncImagePainter(model = it, contentScale = ContentScale.Crop)
                    } ?: painterResource(id = R.drawable.preview_profile)
                    Image(
                        painter = painter,
                        contentDescription = "Фото профиля",
                        modifier = Modifier.size(120.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = validateName(it)
                },
                label = { Text("Имя", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = nameError != null,
                supportingText = {
                    nameError?.let {
                        Text(
                            text = it,
                            color = colorScheme.error,
                            fontFamily = montserratFont,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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
                label = { Text("Пароль (оставьте пустым, чтобы не менять)", fontFamily = montserratFont, fontSize = 12.sp) },
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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

            // Birthdate Field
            OutlinedTextField(
                value = birthdate,
                onValueChange = { /* Блокируем прямой ввод */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true },
                label = { Text("День рождения (ДД.ММ.ГГГГ)", fontFamily = montserratFont, fontSize = 14.sp) },
                isError = birthdateError != null,
                supportingText = {
                    birthdateError?.let {
                        Text(
                            text = it,
                            color = colorScheme.error,
                            fontFamily = montserratFont,
                            fontSize = 12.sp
                        )
                    }
                },
                textStyle = TextStyle(fontFamily = montserratFont, fontSize = 14.sp),
                enabled = false,
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

            // City Field
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Город", fontFamily = montserratFont, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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

            // Interests Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colorScheme.outline, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Интересы",
                            fontFamily = montserratFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Изменить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.primary,
                            modifier = Modifier
                                .clickable { showInterestSelection = true }
                                .padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedInterests.isNotEmpty()) {
                        val rows = selectedInterests.chunked(4)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rows.forEach { rowInterests ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowInterests.forEach { interest ->
                                        InterestItem(
                                            interest = interest,
                                            colorScheme = colorScheme
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Не указано",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Interest Selection Dialog
            if (showInterestSelection) {
                AlertDialog(
                    onDismissRequest = { showInterestSelection = false },
                    title = {
                        Text(
                            text = "Выберите интересы",
                            fontFamily = montserratFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp)
                        ) {
                            state.availableInterests.forEach { (interest, _) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedInterests = if (selectedInterests.contains(interest)) {
                                                selectedInterests - interest
                                            } else {
                                                selectedInterests + interest
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedInterests.contains(interest),
                                        onCheckedChange = {
                                            selectedInterests = if (it) {
                                                selectedInterests + interest
                                            } else {
                                                selectedInterests - interest
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colorScheme.primary,
                                            uncheckedColor = colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = interest,
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showInterestSelection = false }
                        ) {
                            Text(
                                text = "Готово",
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                color = colorScheme.primary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showInterestSelection = false }
                        ) {
                            Text(
                                text = "Отмена",
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                color = colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    containerColor = colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // City Knowledge Level Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уровень знаний о городе:",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box {
                    Text(
                        text = selectedCityKnowledgeLevel?.ifEmpty { "Не указано" } ?: "Не указано",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { showCityKnowledgeMenu = true }
                    )
                    DropdownMenu(
                        expanded = showCityKnowledgeMenu,
                        onDismissRequest = { showCityKnowledgeMenu = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        state.cityKnowledgeLevels.forEach { (name, _) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = name,
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedCityKnowledgeLevel = name
                                    showCityKnowledgeMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Bio Field
            OutlinedTextField(
                value = bio,
                onValueChange = {
                    bio = it
                    if (it.length > 500) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Биография не может быть длиннее 500 символов")
                        }
                    }
                },
                label = { Text("Биография", fontFamily = montserratFont, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp)),
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
                onValueChange = {
                    goals = it
                    if (it.length > 300) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Цели не могут быть длиннее 300 символов")
                        }
                    }
                },
                label = { Text("Цели", fontFamily = montserratFont, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp)),
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
                            "Мужской" -> "Мужской"
                            "Женский" -> "Женский"
                            else -> ""
                        }
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Пол", fontFamily = montserratFont, fontSize = 14.sp) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
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
                    listOf("Мужской", "Женский").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (option) {
                                        "Мужской" -> "Мужской"
                                        else -> "Женский"
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
                            nameError = validateName(name)
                            emailError = !isValidEmail(email)
                            passwordError = password.isNotEmpty() && password.length < 6
                            phoneError = phone.isNotEmpty() && !isValidPhone(phone)
                            vkIdError = vkId.isNotEmpty() && !vkId.matches(Regex("^[0-9]+$"))
                            birthdateError = validateBirthDate(birthdate)

                            if (nameError == null && !emailError && !passwordError && !phoneError && !vkIdError && birthdateError == null) {
                                val interestIds = selectedInterests.mapNotNull { name ->
                                    state.availableInterests.find { it.first == name }?.second
                                }
                                val cityKnowledgeLevelId = selectedCityKnowledgeLevel?.let { name ->
                                    state.cityKnowledgeLevels.find { it.first == name }?.second
                                }
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
                                    interestIds = interestIds,
                                    cityKnowledgeLevelId = cityKnowledgeLevelId,
                                    bio = bio.takeIf { it.isNotEmpty() },
                                    goals = goals.takeIf { it.isNotEmpty() },
                                    birthdate = birthdate.takeIf { it.isNotEmpty() }
                                        ?.let { convertToDbFormat(it) },
                                    city = city.takeIf { it.isNotEmpty() }
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
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val currentDate = Date()
                            val minAgeDate = Calendar.getInstance().apply {
                                add(Calendar.YEAR, -13)
                            }.time
                            if (date.before(currentDate) && date.before(minAgeDate)) {
                                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                birthdate = sdf.format(date)
                                birthdateError = validateBirthDate(birthdate)
                            } else {
                                birthdateError = "Дата рождения недопустима"
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        "OK",
                        fontFamily = montserratFont,
                        color = colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text(
                        "Отмена",
                        fontFamily = montserratFont,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Выберите дату рождения",
                        fontFamily = montserratFont,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}

private fun validateName(name: String): String? {
    return when {
        name.isBlank() -> "Имя не может быть пустым"
        name.length > 50 -> "Имя не может быть длиннее 50 символов"
        !name.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s'-]+$")) -> "Имя может содержать только буквы, пробелы, дефисы или апострофы"
        else -> null
    }
}

private fun validateBirthDate(birthDate: String): String? {
    if (birthDate.isEmpty()) return null
    return try {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdf.isLenient = false
        val date = sdf.parse(birthDate) ?: return "Неверный формат даты"
        val currentDate = Date()
        val minAgeDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, -13)
        }.time
        when {
            date.after(currentDate) -> "Дата рождения не может быть в будущем"
            date.after(minAgeDate) -> "Вы должны быть старше 13 лет"
            else -> null
        }
    } catch (e: Exception) {
        "Неверный формат даты"
    }
}

private fun convertToDisplayFormat(dbDate: String): String {
    return try {
        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfDb.parse(dbDate)
        val sdfDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdfDisplay.format(date)
    } catch (e: Exception) {
        dbDate
    }
}

private fun convertToDbFormat(displayDate: String): String? {
    return try {
        val sdfDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = sdfDisplay.parse(displayDate)
        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdfDb.format(date)
    } catch (e: Exception) {
        null
    }
}

private fun isValidPhone(phone: String): Boolean {
    return phone.matches(Regex("^\\+?[1-9]\\d{1,14}\$"))
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}