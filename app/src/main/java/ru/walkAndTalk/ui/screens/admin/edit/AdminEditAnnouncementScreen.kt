package ru.walkAndTalk.ui.screens.admin.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.admin.AdminSideEffect
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditAnnouncementScreen(
    announcementId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = koinViewModel(),
    feedViewModel: FeedViewModel = koinViewModel()
) {
    val state by viewModel.container.stateFlow.collectAsState()
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
    var activityTypeExpanded by remember { mutableStateOf(false) }

    val activityTypes by feedViewModel.activityTypes.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(announcementId) {
        val announcement = state.announcements.find { it.id == announcementId }
        if (announcement != null) {
            title = announcement.title
            description = announcement.description ?: ""
            location = announcement.location ?: ""
            activityTypeId = announcement.activityTypeId
            imageUri = announcement.imageUrl?.let { Uri.parse(it) }
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AdminSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                    if (sideEffect.message.contains("Объявление обновлено")) {
                        onNavigateBack()
                    }
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать объявление", fontFamily = montserratFont) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Назад",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
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
                            .clickable { activityTypeExpanded = true }
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
                            onDismissRequest = { activityTypeExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activityTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name, fontFamily = montserratFont) },
                                    onClick = {
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
                        TextButton(onClick = { onNavigateBack() }) {
                            Text(
                                text = "Отмена",
                                fontFamily = montserratFont,
                                color = colorScheme.error
                            )
                        }
                        Button(
                            onClick = {
                                if (title.isBlank() || activityTypeId.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Заполните название и тип активности")
                                    }
                                    return@Button
                                }
                                isUploading = true
                                coroutineScope.launch {
//                                    viewModel.updateAnnouncement(
//                                        announcementId = announcementId,
//                                        title = title,
//                                        description = description,
//                                        location = location,
//                                        activityTypeId = activityTypeId,
//                                        imageUri = imageUri
//                                    )
                                    isUploading = false
                                }
                            },
                            enabled = title.isNotBlank() && activityTypeId.isNotBlank() && !isUploading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Сохранить", fontFamily = montserratFont)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}