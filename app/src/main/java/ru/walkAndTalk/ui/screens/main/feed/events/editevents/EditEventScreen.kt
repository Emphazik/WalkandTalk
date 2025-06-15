package ru.walkAndTalk.ui.screens.main.feed.events.editevents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onEventUpdated: () -> Unit,
    viewModel: EditEventViewModel = koinViewModel(),
    feedViewModel: FeedViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    val interests by feedViewModel.interests.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(state.event) {
        state.event?.let { event ->
            title = event.title
            description = event.description ?: ""
            location = event.location
            eventDate = event.eventDate
            maxParticipants = event.maxParticipants?.toString() ?: ""
            selectedTags = event.tagIds.filter { UUID.fromString(it) != null } // Фильтруем только UUID
            imageUri = event.eventImageUrl?.let { Uri.parse(it) }
            try {
                val instant = Instant.parse(event.eventDate)
                selectedDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            } catch (e: Exception) {
                // Оставляем пустым, если дата невалидна
            }
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EditEventSideEffect.NavigateBack -> onNavigateBack()
            is EditEventSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
            is EditEventSideEffect.EventUpdated -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Мероприятие обновлено и ожидает модерации")
                    onEventUpdated()
                }
            }
        }
    }

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать мероприятие", fontFamily = montserratFont) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Назад",
                            tint = colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ошибка: ${state.error}",
                    color = colorScheme.error,
                    fontFamily = montserratFont
                )
            }
        } else if (state.event != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название", fontFamily = montserratFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание", fontFamily = montserratFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Место", fontFamily = montserratFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading
                    ) {
                        Text("Выбрать изображение", fontFamily = montserratFont)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Предпросмотр изображения",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (isUploading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Теги",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        val isSelected = interest.id in selectedTags
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedTags = if (isSelected)
                                        selectedTags - interest.id
                                    else
                                        selectedTags + interest.id
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = interest.name,
                                fontFamily = montserratFont,
                                fontSize = 12.sp,
                                color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedDateTime?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) ?: "",
                    onValueChange = {},
                    label = { Text("Дата и время", fontFamily = montserratFont) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Выбрать дату",
                            modifier = Modifier
                                .clickable { showDatePicker = true }
                                .padding(8.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    label = { Text("Макс. участников", fontFamily = montserratFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { viewModel.navigateBack() }) {
                        Text(
                            text = "Отмена",
                            fontFamily = montserratFont,
                            color = colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            if (title.isBlank() || location.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Заполните название и место")
                                }
                                return@Button
                            }
                            if (selectedDateTime == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Выберите дату и время")
                                }
                                return@Button
                            }
                            if (maxParticipants.isNotBlank() && (maxParticipants.toIntOrNull() ?: 0) <= 0) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Макс. участников должно быть положительным числом")
                                }
                                return@Button
                            }
                            isUploading = true
                            coroutineScope.launch {
                                val imageUrl = if (imageUri != null && imageUri.toString().startsWith("content://")) {
                                    val file = File(context.cacheDir, "upload_${UUID.randomUUID()}.jpg")
                                    context.contentResolver.openInputStream(imageUri!!)?.use { input ->
                                        file.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    val uploadResult = feedViewModel.uploadEventImage(Uri.fromFile(file)).getOrNull()
                                    file.delete()
                                    uploadResult
                                } else {
                                    state.event?.eventImageUrl
                                }
                                isUploading = false
                                val updatedEvent = state.event!!.copy(
                                    title = title,
                                    description = description,
                                    location = location,
                                    eventDate = selectedDateTime!!.atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                    maxParticipants = maxParticipants.toIntOrNull(),
                                    tagIds = selectedTags, // Только UUID
                                    eventImageUrl = imageUrl
                                )
                                viewModel.updateEvent(updatedEvent)
                            }
                        },
                        enabled = title.isNotBlank() && !location.isBlank() && !isUploading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Сохранить", fontFamily = montserratFont)
                    }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateTime?.atZone(ZoneId.systemDefault())
                        ?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            showTimePicker = true
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDateTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(millis),
                                    ZoneId.systemDefault()
                                ).withHour(selectedDateTime?.hour ?: 12)
                                    .withMinute(selectedDateTime?.minute ?: 0)
                            }
                        }) { Text("ОК", fontFamily = montserratFont) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Отмена", fontFamily = montserratFont)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedDateTime?.hour ?: 12,
                    initialMinute = selectedDateTime?.minute ?: 0
                )
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text("Выберите время", fontFamily = montserratFont) },
                    text = {
                        TimePicker(state = timePickerState)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                            selectedDateTime = selectedDateTime?.withHour(timePickerState.hour)
                                ?.withMinute(timePickerState.minute)
                        }) { Text("ОК", fontFamily = montserratFont) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Отмена", fontFamily = montserratFont)
                        }
                    }
                )
            }
        }
    }
}}
