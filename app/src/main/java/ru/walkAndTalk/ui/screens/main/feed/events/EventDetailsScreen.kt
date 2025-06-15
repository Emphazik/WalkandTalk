package ru.walkAndTalk.ui.screens.main.feed.events

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import org.koin.core.context.GlobalContext.get

@Composable
fun EventDetailsScreen(
    onNavigateBack: () -> Unit,
    eventId: String,
    viewModel: EventDetailsViewModel = koinViewModel(),
    feedViewModel: FeedViewModel,
    onNavigateToEditEvent: (String) -> Unit,
    onNavigateToChat: (String) -> Unit // Новый callback
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isParticipating = feedViewModel.participationState
        .map { it[eventId] ?: false }
        .collectAsState(initial = false).value

    viewModel.loadEvent(eventId)

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EventDetailsSideEffect.OnNavigateBack -> onNavigateBack()
            is EventDetailsSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
            is EventDetailsSideEffect.NavigateToChat -> {
                onNavigateToChat(sideEffect.chatId)
            }
            is EventDetailsSideEffect.NavigateToEditEvent -> {
                onNavigateToEditEvent(sideEffect.eventId)
            }
            is EventDetailsSideEffect.EventDeleted -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Мероприятие удалено")
                }
                onNavigateBack()
            }
        }
    }
//    viewModel.collectSideEffect { sideEffect ->
//        when (sideEffect) {
//            is EventDetailsSideEffect.OnNavigateBack -> onNavigateBack()
//            is EventDetailsSideEffect.ShowError -> {
//                coroutineScope.launch {
//                    snackbarHostState.showSnackbar(sideEffect.message)
//                }
//            }
//            is EventDetailsSideEffect.NavigateToChat -> {
//                navController.navigate("chat/${sideEffect.chatId}")
//            }
//            is EventDetailsSideEffect.NavigateToEditEvent -> {
//                navController.navigate("edit_event/${sideEffect.eventId}")
//            }
//            is EventDetailsSideEffect.EventDeleted -> {
//                coroutineScope.launch {
//                    snackbarHostState.showSnackbar("Мероприятие удалено")
//                }
//                onNavigateBack()
//            }
//        }
//    }

    LaunchedEffect(feedViewModel) {
        feedViewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is FeedSideEffect.ParticipateInEvent -> {
                    if (sideEffect.eventId == eventId) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Вы успешно записались на '${state.event?.title ?: "мероприятие"}' ${state.event?.let { viewModel.formatEventDate(it.eventDate) } ?: ""}!"
                            )
                        }
                    }
                }
                is FeedSideEffect.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(sideEffect.message)
                    }
                }
                is FeedSideEffect.LeaveEventSuccess -> {
                    if (sideEffect.eventId == eventId) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Вы успешно отменили участие в '${state.event?.title ?: "мероприятии"}'!"
                            )
                        }
                    }
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
        } else if (state.event != null) {
//            EventDetailsContent(
//                event = state.event!!,
//                participantsCount = state.participantsCount,
//                isCurrentUserOrganizer = state.isCurrentUserOrganizer,
//                onNavigateBack = onNavigateBack,
//                onParticipateClick = { feedViewModel.onParticipateClick(eventId) },
//                isParticipating = isParticipating,
//                onLeaveClick = { feedViewModel.onLeaveEventClick(eventId) },
//                onJoinChatClick = { viewModel.onJoinChatClick(eventId) },
//                onEditEventClick = { viewModel.onEditEventClick(eventId) },
//                onDeleteEventClick = { viewModel.onDeleteEventClick(eventId) },
//                colorScheme = colorScheme,
//                viewModel = viewModel
//            )
            EventDetailsContent(
                event = state.event!!,
                participantsCount = state.participantsCount,
                isCurrentUserOrganizer = state.isCurrentUserOrganizer,
                onNavigateBack = onNavigateBack,
                onParticipateClick = { feedViewModel.onParticipateClick(eventId) },
                isParticipating = isParticipating,
                onLeaveClick = { feedViewModel.onLeaveEventClick(eventId) },
                onJoinChatClick = { viewModel.onJoinChatClick(eventId) },
                onEditEventClick = { viewModel.onEditEventClick(eventId) },
                onDeleteEventClick = { viewModel.onDeleteEventClick(eventId) },
                colorScheme = colorScheme,
                tagNames = state.tagNames,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun EventDetailsContent(
    event: Event,
    participantsCount: Int,
    isCurrentUserOrganizer: Boolean,
    onNavigateBack: () -> Unit,
    onParticipateClick: () -> Unit,
    isParticipating: Boolean,
    onLeaveClick: () -> Unit,
    onJoinChatClick: () -> Unit,
    onEditEventClick: () -> Unit,
    onDeleteEventClick: () -> Unit,
    colorScheme: ColorScheme,
    viewModel: EventDetailsViewModel,
    tagNames: List<String> // Добавлено
) {
    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


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
                painter = event.eventImageUrl?.let {
                    rememberAsyncImagePainter(
                        model = it,
                        error = errorPainter
                    )
                } ?: errorPainter,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f)), // Уменьшена прозрачность
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
                var menuExpanded by remember { mutableStateOf(false) }
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
                            .align(Alignment.TopEnd) // Выравнивание под кнопкой
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать", fontFamily = montserratFont, color = colorScheme.onSurface) },
                            onClick = {
                                menuExpanded = false
                                onEditEventClick()
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
                    text = event.title,
                    fontFamily = montserratFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.formatEventDate(event.eventDate),
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
                        text = event.location,
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
                    text = event.description,
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
                        Row(
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
                    text = if (participantsCount > 0) "Кол-во участников: $participantsCount"
                    else "Кол-во участников: Станьте первым!",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Организатор: ${event.organizerName ?: "Неизвестно"}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Button(
            onClick = {
                if (isParticipating) {
                    showLeaveConfirmation = true
                } else {
                    onParticipateClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isParticipating) colorScheme.secondaryContainer else colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isParticipating) "Отменить участие" else "Участвовать",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = if (isParticipating) colorScheme.onSecondaryContainer else colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (isParticipating) {
                    onJoinChatClick()
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Вы должны участвовать в мероприятии, чтобы присоединиться к чату")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer),
            shape = RoundedCornerShape(8.dp),
            enabled = isParticipating
        ) {
            Text(
                text = "Присоединиться к чату",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showLeaveConfirmation) {
            AlertDialog(
                onDismissRequest = { showLeaveConfirmation = false },
                title = { Text("Подтверждение", fontFamily = montserratFont) },
                text = { Text("Вы уверены, что хотите отменить участие в '${event.title}'?", fontFamily = montserratFont) },
                confirmButton = {
                    TextButton(onClick = {
                        onLeaveClick()
                        showLeaveConfirmation = false
                    }) {
                        Text("Да", fontFamily = montserratFont)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveConfirmation = false }) {
                        Text("Нет", fontFamily = montserratFont)
                    }
                }
            )
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Подтверждение удаления", fontFamily = montserratFont) },
                text = { Text("Вы уверены, что хотите удалить мероприятие '${event.title}'?", fontFamily = montserratFont) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteEventClick()
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