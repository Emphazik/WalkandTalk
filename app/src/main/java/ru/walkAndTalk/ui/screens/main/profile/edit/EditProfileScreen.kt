package ru.walkAndTalk.ui.screens.main.profile.edit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.main.montserratFont
import ru.walkAndTalk.ui.screens.main.profile.ProfileSideEffect
import ru.walkAndTalk.ui.screens.main.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Лаунчер для запроса разрешений
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Для определения города требуется разрешение на местоположение")
            }
        }
    }

    LaunchedEffect(state.nameError, state.birthDateError, state.bioError, state.goalsError) {
        val errorMessage = when {
            state.nameError != null -> state.nameError
            state.birthDateError != null -> state.birthDateError
            state.bioError != null -> state.bioError
            state.goalsError != null -> state.goalsError
            else -> null
        }
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.onImageSelected(uri)
                }
            }
        }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.LaunchImagePicker -> {
                val intent = Intent(Intent.ACTION_PICK).apply { this.type = "image/*" }
                launcher.launch(intent)
            }
            is ProfileSideEffect.OnNavigateExit -> {}
            is ProfileSideEffect.RequestLocationPermission -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) { padding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок с кнопкой "Назад"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .background(
                                    Color.Gray.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Редактирование профиля",
                            fontFamily = montserratFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                // Аватарка
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = state.photoURL?.let { rememberAsyncImagePainter(it) }
                                        ?: painterResource(id = R.drawable.ic_profile1),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colorScheme.surface)
                                        .border(
                                            2.dp,
                                            colorScheme.onSurface.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(4.dp)
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.onChangeImageClick() },
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Изменить изображение",
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp,
                                    color = colorScheme.onPrimary
                                )
                            }
                            Button(
                                onClick = { viewModel.onDeleteImage() },
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Удалить изображение",
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp,
                                    color = colorScheme.onError
                                )
                            }
                        }
                    }
                }
                // Секция с именем и датой рождения
                item {
                    EditPersonalInfoSection(
                        name = state.name,
                        birthDate = state.birthDate ?: "",
                        gender = state.gender,
                        city = state.newCity,
                        isEditing = state.isEditingPersonalInfo,
                        newName = state.newName,
                        newBirthDate = state.newBirthDate,
                        newGender = state.newGender,
                        newCity = state.newCity,
                        onEditClick = viewModel::onEditPersonalInfo,
                        onNameChanged = viewModel::onNameChanged,
                        onBirthDateChanged = viewModel::onBirthDateChanged,
                        onGenderChanged = viewModel::onGenderChanged,
                        onCityUpdateClick = viewModel::onUpdateCity,
                        onSaveClick = viewModel::onSavePersonalInfo,
                        onCancelClick = viewModel::onCancelPersonalInfo,
                        colorScheme = colorScheme
                    )
                }

                // Остальные карточки
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Статус в городе:",
                                    fontFamily = montserratFont,
                                    fontSize = 16.sp,
                                    color = colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box {
                                    Text(
                                        text = state.selectedCityStatus?.ifEmpty { "Не указано" }
                                            ?: "Не указано",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.primary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clickable { viewModel.onCityStatusClick() }
                                    )
                                    DropdownMenu(
                                        expanded = state.showCityStatusMenu,
                                        onDismissRequest = viewModel::onDismissCityStatusMenu,
                                        modifier = Modifier.background(colorScheme.surface)
                                    ) {
                                        state.cityStatuses.forEach { status ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = status,
                                                        fontFamily = montserratFont,
                                                        fontSize = 16.sp,
                                                        color = colorScheme.onSurface
                                                    )
                                                },
                                                onClick = { viewModel.onCityStatusSelected(status) }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            EditBioSection(
                                bio = state.bio,
                                isEditing = state.isEditingBio,
                                newBio = state.newBio,
                                onEditClick = viewModel::onEditBio,
                                onBioChanged = viewModel::onBioChanged,
                                onSaveClick = viewModel::onSaveBio,
                                onCancelClick = viewModel::onCancelBio,
                                colorScheme = colorScheme
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            EditInterestSection(
                                interests = state.interests,
                                onAddInterest = { viewModel.onAddInterestClick() },
                                colorScheme = colorScheme
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            EditGoalsSection(
                                goals = state.goals,
                                isEditing = state.isEditingGoals,
                                newGoals = state.newGoals,
                                onEditClick = viewModel::onEditGoals,
                                onGoalsChanged = viewModel::onGoalsChanged,
                                onSaveClick = viewModel::onSaveGoals,
                                onCancelClick = viewModel::onCancelGoals,
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
            if (state.showInterestSelection) {
                InterestSelectionMenu(
                    availableInterests = state.availableInterests,
                    selectedInterests = state.tempSelectedInterests,
                    onInterestToggled = { interest -> viewModel.onInterestToggled(interest) },
                    onDismiss = { viewModel.onDismissInterestSelection() },
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonalInfoSection(
    name: String,
    birthDate: String,
    gender: String?,
    city: String,
    isEditing: Boolean,
    newName: String,
    newBirthDate: String,
    newGender: String?,
    newCity: String,
    onEditClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onGenderChanged: (String?) -> Unit,
    onCityUpdateClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    colorScheme: ColorScheme
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        yearRange = IntRange(1900, 2025)
    )

    var nameError by remember { mutableStateOf<String?>(null) }

    fun validateName(input: String): String? {
        return when {
            input.isBlank() -> "Имя не может быть пустым"
            input.length > 50 -> "Имя не может быть длиннее 50 символов"
            !input.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s'-]+$")) -> "Имя может содержать только буквы, пробелы, дефисы или апострофы"
            else -> null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
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
                    text = "Личная информация",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (!isEditing) {
                    Text(
                        text = "Изменить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.clickable { onEditClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { input ->
                        onNameChanged(input)
                        nameError = validateName(input)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = colorScheme.error) } },
                    textStyle = TextStyle(
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newBirthDate,
                    onValueChange = { /* Блокируем прямой ввод */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    label = { Text("День рождения (ДД.ММ.ГГГГ)") },
                    textStyle = TextStyle(
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = when (newGender) {
                            "Мужской" -> "Мужской"
                            "Женский" -> "Женский"
                            else -> "Не указано"
                        },
                        onValueChange = { /* Блокируем прямой ввод */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGenderMenu = true },
                        label = { Text("Пол") },
                        textStyle = TextStyle(
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        enabled = false
                    )
                    DropdownMenu(
                        expanded = showGenderMenu,
                        onDismissRequest = { showGenderMenu = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        listOf("Мужской" to "Мужской", "Женский" to "Женский", "Не указано" to null).forEach { (display, value) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = display,
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onGenderChanged(value)
                                    showGenderMenu = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { /* Блокируем прямой ввод */ },
                        modifier = Modifier.weight(1f),
                        label = { Text("Город") },
                        textStyle = TextStyle(
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCityUpdateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Обновить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = nameError == null && newName.isNotBlank()
                    ) {
                        Text(
                            "Сохранить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Отмена",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = "Имя: $name",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "День рождения: ${birthDate.ifEmpty { "Не указано" }}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Город: ${city.ifEmpty { "Не указано" }}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
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
                                onBirthDateChanged(sdf.format(date))
                            } else {
                                Log.e("EditProfileScreen", "Дата рождения недопустима")
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


// Остальные composables (EditBioSection, EditGoalsSection, etc.) остаются без изменений
@Composable
fun EditBioSection(
    bio: String?,
    isEditing: Boolean,
    newBio: String,
    onEditClick: () -> Unit,
    onBioChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    colorScheme: ColorScheme
) {
    var bioError by remember { mutableStateOf<String?>(null) }

    fun validateBio(input: String): String? {
        if (input.isEmpty()) return null
        return when {
            input.length > 500 -> "Описание не может быть длиннее 500 символов"
            !input.matches(Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s.,!?'\"-]+$")) -> "Описание может содержать только буквы, цифры, пробелы и знаки препинания"
            else -> null
        }
    }

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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "О себе",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (!isEditing) {
                    Text(
                        text = "Изменить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.clickable { onEditClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = newBio,
                    onValueChange = { input ->
                        onBioChanged(input)
                        bioError = validateBio(input)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("О себе") },
                    isError = bioError != null,
                    supportingText = bioError?.let { { Text(it, color = colorScheme.error) } },
                    textStyle = TextStyle(
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = bioError == null
                    ) {
                        Text(
                            "Сохранить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Отмена",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = bio?.ifEmpty { "Не указано" } ?: "Не указано",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
@Composable
fun EditGoalsSection(
    goals: String?,
    isEditing: Boolean,
    newGoals: String,
    onEditClick: () -> Unit,
    onGoalsChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    colorScheme: ColorScheme
) {
    var goalsError by remember { mutableStateOf<String?>(null) }

    fun validateGoals(input: String): String? {
        if (input.isEmpty()) return null
        return when {
            input.length > 300 -> "Цели не могут быть длиннее 300 символов"
            !input.matches(Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s.,!?'\"-]+$")) -> "Цели могут содержать только буквы, цифры, пробелы и знаки препинания"
            else -> null
        }
    }

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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Цели",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (!isEditing) {
                    Text(
                        text = "Изменить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.clickable { onEditClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = newGoals,
                    onValueChange = { input ->
                        onGoalsChanged(input)
                        goalsError = validateGoals(input)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Цели") },
                    isError = goalsError != null,
                    supportingText = goalsError?.let { { Text(it, color = colorScheme.error) } },
                    textStyle = TextStyle(
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = goalsError == null
                    ) {
                        Text(
                            "Сохранить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Отмена",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = goals?.ifEmpty { "Не указано" } ?: "Не указано",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EditInterestSection(
    interests: List<String>,
    onAddInterest: () -> Unit,
    colorScheme: ColorScheme
) {
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
                    modifier = Modifier.clickable { onAddInterest() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val rows = interests.chunked(4)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowInterests ->
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rowInterests) { interest ->
                            InterestItem(
                                interest = interest,
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
                if (interests.isEmpty()) {
                    Text(
                        text = "Не указано",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun InterestItem(
    interest: String,
    colorScheme: ColorScheme
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(16.dp)
            )
            .wrapContentWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = interest,
            fontFamily = montserratFont,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            softWrap = false
        )
    }
}

@Composable
fun InterestSelectionMenu(
    availableInterests: List<String>,
    selectedInterests: List<String>,
    onInterestToggled: (String) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    AlertDialog(
        onDismissRequest = { /* Не закрываем при нажатии на фон */ },
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
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableInterests) { interest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selectedInterests.contains(interest)) Color(0xFF4CAF50).copy(
                                    alpha = 0.1f
                                ) else colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onInterestToggled(interest) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedInterests.contains(interest),
                            onCheckedChange = { onInterestToggled(interest) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                uncheckedColor = colorScheme.onSurface.copy(alpha = 0.6f)
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
                onClick = { onDismiss() }
            ) {
                Text(
                    text = "Готово",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(
                    text = "Отмена",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}