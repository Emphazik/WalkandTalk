package ru.walkAndTalk.ui.screens.main.feed

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.FeedItemType
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.theme.montserratFont
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = koinViewModel()
) {
//    val state by feedViewModel.collectAsState()
    val state by feedViewModel.container.stateFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var createType by remember { mutableStateOf<FeedItemType?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    feedViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.NavigateToEventDetails -> {
                navController.navigate(EventDetails.createRoute(sideEffect.eventId))
            }
            is FeedSideEffect.NavigateToAnnouncementDetails -> {
                navController.navigate("announcement_details/${sideEffect.announcementId}")
            }
            is FeedSideEffect.NavigateToChat -> {
                navController.navigate("chat/${sideEffect.chatId}")
            }
            is FeedSideEffect.NavigateToNotifications -> {
                navController.navigate("notifications")
            }
            is FeedSideEffect.ShowMessage, is FeedSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        (sideEffect as? FeedSideEffect.ShowMessage)?.message
                            ?: (sideEffect as FeedSideEffect.ShowError).message
                    )
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Лента",
                    fontFamily = montserratFont,
                    fontSize = 24.sp,
                    color = colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Создать",
                        tint = colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { feedViewModel.navigateToNotifications() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = "Уведомления",
                        tint = colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { feedViewModel.refreshFeed() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh64),
                        contentDescription = "Обновить",
                        tint = colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { feedViewModel.onSearchQueryChange(it) },
                onClearQuery = { feedViewModel.onClearQuery() },
                onFilterClick = { showFilterSheet = true },
                colorScheme = colorScheme
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
//                    Text(
//                        "Всего элементов: ${state.items.size}, объявлений: ${state.items.filterIsInstance<Announcement>().size}",
//                        modifier = Modifier.padding(16.dp)
//                    )
                }
                if (state.isLoading) {
                    item { Text("Загрузка...", modifier = Modifier.padding(16.dp)) }
                } else if (state.error != null) {
                    item {
                        Text(
                            "Ошибка: ${state.error}",
                            modifier = Modifier.padding(16.dp),
                            color = colorScheme.error
                        )
                    }
                } else if (state.items.isEmpty() && state.searchQuery.isNotEmpty()) {
                    item { Text("Ничего не найдено", modifier = Modifier.padding(16.dp)) }
                } else if (state.items.isEmpty()) {
                    item { Text("Записи отсутствуют", modifier = Modifier.padding(16.dp)) }
                } else {
                    items(state.items) { item ->
                        when (item) {
                            is Event -> EventCard(
                                event = item,
                                viewModel = feedViewModel,
                                tagNames = item.tagIds.map { tagId ->
                                    state.tagNames[tagId]!!
                                },
                                isParticipating = feedViewModel.isUserParticipating(item.id)
                            )
                            is Announcement -> AnnouncementCard(
                                announcement = item,
                                viewModel = feedViewModel
                            )
                        }
                    }
                }
            }
        }
        if (showCreateDialog) {
            CreateContentDialog(
                onDismiss = { showCreateDialog = false; createType = null },
                onTypeSelected = { createType = it },
                onCreateEvent = { feedViewModel.onCreateEvent(it); showCreateDialog = false },
                onCreateAnnouncement = {
                    feedViewModel.onCreateAnnouncement(it); showCreateDialog = false
                },
                selectedType = createType,
                snackbarHostState = snackbarHostState
            )
        }
        if (showFilterSheet) {
            FilterBottomSheet(
                onDismiss = { showFilterSheet = false },
                onApplyFilters = { sortType, city, type ->
                    feedViewModel.onSortSelected(sortType)
                    feedViewModel.onCitySelected(city)
                    feedViewModel.onTypeSelected(type)
                    showFilterSheet = false
                },
                currentSortType = state.sortType,
                currentCity = state.selectedCity,
                currentType = state.selectedType
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Bottom)
                .padding(16.dp)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onFilterClick: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Поиск по названию, #тегу",
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search64),
                    contentDescription = "Поиск",
                    tint = colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange(""); onClearQuery() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clear128),
                            contentDescription = "Очистить",
                            tint = colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .background(colorScheme.surface, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_filter64),
                contentDescription = "Фильтры",
                tint = colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    onApplyFilters: (SortType?, String?, FeedItemType?) -> Unit,
    currentSortType: SortType?,
    currentCity: String?,
    currentType: FeedItemType?
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedSortType by remember { mutableStateOf(currentSortType) }
    var cityInput by remember { mutableStateOf(currentCity ?: "") }
    var selectedType by remember { mutableStateOf(currentType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Фильтры",
                fontFamily = montserratFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Сортировка",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            listOf(
                SortType.DateNewestFirst to "По дате (новые → старые)",
                SortType.DateOldestFirst to "По дате (старые → новые)",
                SortType.City to "По городу"
            ).forEach { (sortType, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSortType == sortType,
                        onClick = { selectedSortType = sortType }
                    )
                    Text(
                        text = label,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Тип записи",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            listOf(
                null to "Все",
                FeedItemType.EVENT to "Мероприятия",
                FeedItemType.ANNOUNCEMENT to "Объявления"
            ).forEach { (type, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                    Text(
                        text = label,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Город",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                placeholder = {
                    Text(
                        text = "Введите город",
                        fontFamily = montserratFont,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        selectedSortType = null
                        cityInput = ""
                        selectedType = null
                        onApplyFilters(null, null, null)
                    }
                ) {
                    Text(
                        text = "Сбросить",
                        fontFamily = montserratFont,
                        color = colorScheme.error
                    )
                }
                Button(
                    onClick = {
                        onApplyFilters(
                            selectedSortType,
                            if (cityInput.isBlank()) null else cityInput,
                            selectedType
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Применить",
                        fontFamily = montserratFont
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateContentDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (FeedItemType) -> Unit,
    onCreateEvent: (Event) -> Unit,
    onCreateAnnouncement: (Announcement) -> Unit,
    selectedType: FeedItemType?,
    snackbarHostState: SnackbarHostState
) {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var activityTypeId by remember { mutableStateOf("") } // Теперь ID
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val feedViewModel: FeedViewModel = koinViewModel()
    val interests by feedViewModel.interests.collectAsState(initial = emptyList())
    val activityTypes by feedViewModel.activityTypes.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf<java.time.LocalDateTime?>(null) }
    var activityTypeExpanded by remember { mutableStateOf(false) }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (selectedType == null) "Создать запись" else if (selectedType == FeedItemType.EVENT) "Создать мероприятие" else "Создать объявление",
                fontFamily = montserratFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedType == null) {
                Button(
                    onClick = { onTypeSelected(FeedItemType.EVENT) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Создать мероприятие",
                        fontFamily = montserratFont
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onTypeSelected(FeedItemType.ANNOUNCEMENT) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Создать объявление",
                        fontFamily = montserratFont
                    )
                }
            } else {
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
                // Image upload
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading
                    ) {
                        Text(
                            text = "Выбрать изображение",
                            fontFamily = montserratFont
                        )
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
                // Tags selection
                Text(
                    text = "Теги",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        val isSelected = interest.name in selectedTags
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedTags = if (isSelected)
                                        selectedTags - interest.name
                                    else
                                        selectedTags + interest.name
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = interest.name,
                                fontFamily = montserratFont,
                                fontSize = 12.sp,
                                color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedType == FeedItemType.EVENT) {
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
                } else {
                    Log.d("CreateContentDialog", "ActivityTypes: $activityTypes, activityTypeId: $activityTypeId")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Log.d("CreateContentDialog", "Клик по Box, expanded: $activityTypeExpanded")
                                activityTypeExpanded = true
                            }
                    ) {
                        OutlinedTextField(
                            value = activityTypes.find { it.id == activityTypeId }?.name ?: "Выберите тип",
                            onValueChange = {},
                            label = { Text("Тип активности", fontFamily = montserratFont) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false, // Отключаем интерактивность самого TextField
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
                                Log.d("CreateContentDialog", "Закрытие DropdownMenu")
                                activityTypeExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activityTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name, fontFamily = montserratFont) },
                                    onClick = {
                                        Log.d("CreateContentDialog", "Выбран тип: ${type.name}, id: ${type.id}")
                                        activityTypeId = type.id
                                        activityTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
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
                            if (selectedType == FeedItemType.EVENT) {
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
                                eventDate = selectedDateTime!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                            } else {
                                if (activityTypeId.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Выберите тип активности")
                                    }
                                    return@Button
                                }
                            }
                            isUploading = true
                            coroutineScope.launch {
                                val imageUrl = imageUri?.let {
                                    val file =
                                        File(context.cacheDir, "upload_${UUID.randomUUID()}.jpg")
                                    context.contentResolver.openInputStream(it)?.use { input ->
                                        file.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    Log.d(
                                        "CreateContentDialog",
                                        "Загрузка изображения: ${file.absolutePath}"
                                    )
                                    val uploadResult = if (selectedType == FeedItemType.EVENT) {
                                        feedViewModel.uploadEventImage(Uri.fromFile(file))
                                            .getOrNull()
                                    } else {
                                        feedViewModel.uploadEventImage(Uri.fromFile(file))
                                            .getOrNull()
                                    }
                                    file.delete()
                                    Log.d(
                                        "CreateContentDialog",
                                        "Результат загрузки: $uploadResult"
                                    )
                                    uploadResult
                                }
                                isUploading = false
                                when (selectedType) {
                                    FeedItemType.EVENT -> {
                                        val tagUuids = selectedTags.mapNotNull { tagName ->
                                            interests.find { it.name == tagName }?.id
                                        }
                                        val pendingStatusId = feedViewModel.getEventStatusId("pending")
                                            ?: "ac32ffcc-8f69-4b71-8a39-877c1eb95e04"
                                        Log.d("CreateContentDialog", "pendingStatusId: $pendingStatusId")
                                        val event = Event(
                                            id = UUID.randomUUID().toString(),
                                            creatorId = feedViewModel.currentUserId,
                                            title = title,
                                            description = description,
                                            location = location,
                                            eventDate = eventDate,
                                            createdAt = Clock.System.now().toString(),
                                            eventImageUrl = imageUrl,
                                            tagIds = tagUuids,
                                            statusId = pendingStatusId,
                                            maxParticipants = maxParticipants.toIntOrNull(),
                                            organizerName = null
                                        )
                                        Log.d("CreateContentDialog", "Создаваемое событие: $event")
                                        onCreateEvent(event)
                                    }
                                    FeedItemType.ANNOUNCEMENT -> {
                                        val tagUuids = selectedTags.mapNotNull { tagName ->
                                            interests.find { it.name == tagName }?.id
                                        }
                                        val pendingStatusId = feedViewModel.getEventStatusId("pending")
                                            ?: "ac32ffcc-8f69-4b71-8a39-877c1eb95e04"
                                        Log.d("CreateContentDialog", "pendingStatusId для объявления: $pendingStatusId")
                                        val announcement = Announcement(
                                            id = UUID.randomUUID().toString(),
                                            title = title,
                                            description = description,
                                            creatorId = feedViewModel.currentUserId,
                                            activityTypeId = activityTypeId,
                                            statusId = pendingStatusId,
                                            createdAt = Clock.System.now().toString(),
                                            updatedAt = null,
                                            location = location,
                                            imageUrl = imageUrl
                                        )
                                        Log.d("CreateContentDialog", "Создаваемое объявление: $announcement")
                                        onCreateAnnouncement(announcement)
                                    }
                                }
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && location.isNotBlank() && !isUploading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Создать",
                            fontFamily = montserratFont
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showDatePicker) {
                Log.d("CreateContentDialog", "Показ DatePickerDialog")
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            showTimePicker = true
                        }) { Text("ОК") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
                    }
                ) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
                    DatePicker(state = datePickerState)
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateTime = java.time.LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(millis),
                            ZoneId.systemDefault()
                        ).withHour(12).withMinute(0)
                    }
                }
            }

            if (showTimePicker) {
                Log.d("CreateContentDialog", "Показ TimePickerDialog")
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text("Выберите время", fontFamily = montserratFont) },
                    text = {
                        val timePickerState = rememberTimePickerState()
                        TimePicker(state = timePickerState)
                        selectedDateTime = selectedDateTime?.withHour(timePickerState.hour)?.withMinute(timePickerState.minute)
                    },
                    confirmButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("ОК") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
                    }
                )
            }
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    viewModel: FeedViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val activityTypes by viewModel.activityTypes.collectAsState(initial = emptyList())
    val activityTypeName = activityTypes.find { it.id == announcement.activityTypeId }?.name ?: "Неизвестно"
    val isOwnAnnouncement = announcement.creatorId == viewModel.currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { viewModel.onAnnouncementClick(announcement.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            announcement.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(
                        model = url,
                        error = painterResource(id = R.drawable.default_event_image)
                    ),
                    contentDescription = announcement.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = announcement.title,
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
//                announcement.description?.let {
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = it,
//                        fontFamily = montserratFont,
//                        fontSize = 14.sp,
//                        color = colorScheme.onSurface.copy(alpha = 0.6f)
//                    )
//                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Тип: $activityTypeName",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                announcement.location?.let {
                    Text(
                        text = "Локация: $it",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (!isOwnAnnouncement) {
                    Button(
                        onClick = { viewModel.onMessageClick(announcement.creatorId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Написать сообщение",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    viewModel: FeedViewModel,
    isParticipating: Boolean,
    tagNames: List<String> // Добавлено
) {
    val colorScheme = MaterialTheme.colorScheme
    var showLeaveConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { viewModel.onEventClick(event.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = if (event.eventImageUrl.isNullOrBlank()) {
                    painterResource(id = R.drawable.default_event_image)
                } else {
                    rememberAsyncImagePainter(event.eventImageUrl)
                },
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = viewModel.formatEventDate(event.eventDate) ?: "",
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.title,
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                event.organizerName?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Организатор: $it",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Место",
                        tint = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
//                if (event.tagIds.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Text(
//                            text = "Теги:",
//                            fontFamily = montserratFont,
//                            fontSize = 14.sp,
//                            color = colorScheme.onSurface.copy(alpha = 0.6f),
//                            fontWeight = FontWeight.Bold
//                        )
//                        FlowRow(
//                            horizontalArrangement = Arrangement.spacedBy(4.dp),
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            tagNames.forEach { tagName ->
//                                Surface(
//                                    shape = RoundedCornerShape(12.dp),
//                                    color = colorScheme.primaryContainer,
//                                    modifier = Modifier.padding(vertical = 2.dp)
//                                ) {
//                                    Text(
//                                        text = tagName,
//                                        fontFamily = montserratFont,
//                                        fontSize = 12.sp,
//                                        color = colorScheme.onPrimaryContainer,
//                                        modifier = Modifier.padding(
//                                            horizontal = 8.dp,
//                                            vertical = 4.dp
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
                if (tagNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Теги:",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val maxTags = 3
                            val displayedTags = tagNames.take(maxTags)
                            displayedTags.forEach { tagName ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colorScheme.primaryContainer,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tagName,
                                        fontFamily = montserratFont,
                                        fontSize = 12.sp,
                                        color = colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                            if (tagNames.size > maxTags) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colorScheme.primaryContainer,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+${tagNames.size - maxTags}",
                                        fontFamily = montserratFont,
                                        fontSize = 12.sp,
                                        color = colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (isParticipating) {
                            showLeaveConfirmation = true
                        } else {
                            viewModel.onParticipateClick(event.id)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isParticipating) colorScheme.secondaryContainer else colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isParticipating) "Вы уже участвуете" else "Присоединиться",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = if (isParticipating) colorScheme.onSecondaryContainer else colorScheme.onPrimaryContainer
                    )
                }
                if (showLeaveConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showLeaveConfirmation = false },
                        title = { Text("Подтверждение") },
                        text = { Text("Вы уверены, что хотите отменить участие в '${event.title}'?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.onLeaveEventClick(event.id)
                                    showLeaveConfirmation = false
                                }
                            ) {
                                Text("Да")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showLeaveConfirmation = false }
                            ) {
                                Text("Нет")
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class SortType {
    DateNewestFirst,
    DateOldestFirst,
    City
}