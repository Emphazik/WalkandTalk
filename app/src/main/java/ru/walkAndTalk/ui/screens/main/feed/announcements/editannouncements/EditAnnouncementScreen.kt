package ru.walkAndTalk.ui.screens.main.feed.announcements.editannouncements

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAnnouncementScreen(
    announcementId: String,
    onNavigateBack: () -> Unit,
    onAnnouncementUpdated: () -> Unit,
    onAnnouncementDeleted: () -> Unit,
    viewModel: EditAnnouncementViewModel = koinViewModel(),
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
    var activityTypeId by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var activityTypeExpanded by remember { mutableStateOf(false) }

    val activityTypes by feedViewModel.activityTypes.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(state.announcement) {
        state.announcement?.let { announcement ->
            title = announcement.title
            description = announcement.description ?: ""
            location = announcement.location ?: ""
            activityTypeId = announcement.activityTypeId
            imageUri = announcement.imageUrl?.let { Uri.parse(it) }
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EditAnnouncementSideEffect.NavigateBack -> onNavigateBack()
            is EditAnnouncementSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
            is EditAnnouncementSideEffect.AnnouncementUpdated -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Объявление обновлено и ожидает модерации")
                    onAnnouncementUpdated()
                }
            }
            is EditAnnouncementSideEffect.AnnouncementDeleted -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Объявление удалено")
                    onAnnouncementDeleted()
                }
            }
        }
    }

    LaunchedEffect(announcementId) {
        viewModel.loadAnnouncement(announcementId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать объявление", fontFamily = montserratFont) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Назад",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete64),
                            contentDescription = "Удалить",
                            tint = colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(
                    text = "Ошибка: ${state.error}",
                    color = colorScheme.error,
                    fontFamily = montserratFont
                )
            } else if (state.announcement != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Log.d("EditAnnouncementScreen", "Клик по Box, expanded: $activityTypeExpanded")
                                activityTypeExpanded = true
                            }
                    ) {
                        OutlinedTextField(
                            value = activityTypes.find { it.id == activityTypeId }?.name ?: "Выберите тип",
                            onValueChange = {},
                            label = { Text("Тип активности", fontFamily = montserratFont) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать тип",
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = activityTypeExpanded && activityTypes.isNotEmpty(),
                            onDismissRequest = {
                                Log.d("EditAnnouncementScreen", "Закрытие DropdownMenu")
                                activityTypeExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activityTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name, fontFamily = montserratFont) },
                                    onClick = {
                                        Log.d("EditAnnouncementScreen", "Выбран тип: ${type.name}, id: ${type.id}")
                                        activityTypeId = type.id
                                        activityTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
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
                                if (title.isBlank() || location.isBlank() || activityTypeId.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Заполните название, место и тип активности")
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
                                        state.announcement?.imageUrl
                                    }
                                    isUploading = false
                                    val updatedAnnouncement = state.announcement!!.copy(
                                        title = title,
                                        description = description,
                                        location = location,
                                        activityTypeId = activityTypeId,
                                        imageUrl = imageUrl
                                    )
                                    viewModel.updateAnnouncement(updatedAnnouncement)
                                }
                            },
                            enabled = title.isNotBlank() && !location.isBlank() && !activityTypeId.isBlank() && !isUploading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Сохранить", fontFamily = montserratFont)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Подтверждение удаления", fontFamily = montserratFont) },
                    text = { Text("Вы уверены, что хотите удалить объявление '${state.announcement?.title}'?", fontFamily = montserratFont) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteAnnouncement()
                            showDeleteDialog = false
                        }) {
                            Text("Удалить", fontFamily = montserratFont, color = colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Отмена", fontFamily = montserratFont)
                        }
                    }
                )
            }
        }
    }
}