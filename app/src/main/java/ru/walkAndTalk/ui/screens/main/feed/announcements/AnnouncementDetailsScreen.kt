package ru.walkAndTalk.ui.screens.main.feed.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@Composable
fun AnnouncementDetailsScreen(
    onNavigateBack: () -> Unit,
    announcementId: String,
    viewModel: AnnouncementDetailsViewModel = koinViewModel(),
    feedViewModel: FeedViewModel,
    onNavigateToEditAnnouncement: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    viewModel.loadAnnouncement(announcementId)

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AnnouncementDetailsSideEffect.OnNavigateBack -> onNavigateBack()
            is AnnouncementDetailsSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
            is AnnouncementDetailsSideEffect.NavigateToEditAnnouncement -> {
                onNavigateToEditAnnouncement(sideEffect.announcementId)
            }
            is AnnouncementDetailsSideEffect.AnnouncementDeleted -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Объявление удалено")
                }
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(feedViewModel) {
        feedViewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is FeedSideEffect.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(sideEffect.message)
                    }
                }
                is FeedSideEffect.NavigateToChat -> {
                    onNavigateToChat(sideEffect.chatId)
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        if (state.isLoading) {
            Text(
                text = "Загрузка...",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                color = colorScheme.onBackground
            )
        } else if (state.error != null) {
            Text(
                text = "Ошибка: ${state.error}",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                color = colorScheme.error
            )
        } else if (state.announcement != null) {
            AnnouncementDetailsContent(
                announcement = state.announcement!!,
                isCurrentUserOrganizer = state.isCurrentUserOrganizer,
                onNavigateBack = onNavigateBack,
                onMessageClick = { feedViewModel.onMessageClick(state.announcement!!.creatorId) },
                onEditAnnouncementClick = { viewModel.onEditAnnouncementClick(announcementId) },
                onDeleteAnnouncementClick = { viewModel.onDeleteAnnouncementClick(announcementId) },
                colorScheme = colorScheme,
                viewModel = viewModel,
                tagNames = state.tagNames,
                activityTypeName = state.activityTypeName
            )
        }
    }
}

@Composable
fun AnnouncementDetailsContent(
    announcement: Announcement,
    isCurrentUserOrganizer: Boolean,
    onNavigateBack: () -> Unit,
    onMessageClick: () -> Unit,
    onEditAnnouncementClick: () -> Unit,
    onDeleteAnnouncementClick: () -> Unit,
    colorScheme: ColorScheme,
    viewModel: AnnouncementDetailsViewModel,
    tagNames: List<String>,
    activityTypeName: String
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val errorPainter = painterResource(id = R.drawable.default_event_image)
            androidx.compose.foundation.Image(
                painter = announcement.imageUrl?.let {
                    rememberAsyncImagePainter(
                        model = it,
                        error = errorPainter
                    )
                } ?: errorPainter,
                contentDescription = announcement.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f)),
                            startY = 0f,
                            endY = 250.dp.value
                        )
                    )
            )
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            if (isCurrentUserOrganizer) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more128),
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(colorScheme.surface)
                            .align(Alignment.TopEnd)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать", fontFamily = montserratFont, color = colorScheme.onSurface) },
                            onClick = {
                                menuExpanded = false
                                onEditAnnouncementClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить", fontFamily = montserratFont, color = colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = announcement.title,
                    fontFamily = montserratFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.formatAnnouncementDate(announcement.createdAt),
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Location",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = announcement.location ?: "Не указано",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Описание",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = announcement.description ?: "Описание отсутствует",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (tagNames.isNotEmpty()) {
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
                            tagNames.forEach { tagName ->
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = "Тип активности: $activityTypeName",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Организатор: ${announcement.organizerName ?: "Неизвестно"}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Button(
            onClick = onMessageClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primaryContainer),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Написать организатору",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Подтверждение удаления", fontFamily = montserratFont) },
                text = { Text("Вы уверены, что хотите удалить объявление '${announcement.title}'?", fontFamily = montserratFont) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteAnnouncementClick()
                        showDeleteConfirmation = false
                    }) {
                        Text("Удалить", fontFamily = montserratFont, color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Отмена", fontFamily = montserratFont)
                    }
                }
            )
        }
    }
}
