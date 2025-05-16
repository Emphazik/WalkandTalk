package ru.walkAndTalk.ui.screens.main.feed.events

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@Composable
fun EventDetailsScreen(
    onNavigateBack: () -> Unit,
    eventId: String,
    viewModel: EventDetailsViewModel = koinViewModel(),
    feedViewModel: FeedViewModel
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    viewModel.loadEvent(eventId)

// Обработка побочных эффектов EventDetailsViewModel
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EventDetailsSideEffect.OnNavigateBack -> onNavigateBack()
        }
    }

    // Обработка побочных эффектов FeedViewModel
    LaunchedEffect(feedViewModel) {
        feedViewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is FeedSideEffect.ParticipateInEvent -> {
                    if (sideEffect.eventId == eventId) { // Убедимся, что это событие для текущего экрана
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Вы успешно записались на '${state.event?.title ?: "мероприятие"}' ${
                                    state.event?.let {
                                        viewModel.formatEventDate(
                                            it.eventDate
                                        )
                                    } ?: ""
                                }!"
                            )
                        }
                    }
                }

                is FeedSideEffect.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = sideEffect.message // Отображаем ошибку (например, "Вы уже участвуете")
                        )
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
            EventDetailsContent(
                event = state.event!!,
                onNavigateBack = onNavigateBack,
                onParticipateClick = { feedViewModel.onParticipateClick(eventId) }, // Вызов через FeedViewModel
                isParticipating = feedViewModel.isUserParticipating(eventId),
                onLeaveClick = { feedViewModel.onLeaveEventClick(eventId) },
                colorScheme = colorScheme,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun EventDetailsContent(
    event: Event,
    onNavigateBack: () -> Unit,
    onParticipateClick: () -> Unit,
    isParticipating: Boolean,
    onLeaveClick: () -> Unit,
    colorScheme: ColorScheme,
    viewModel: EventDetailsViewModel = koinViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Изображение с затемнением и текстом
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Определяем Painter для ошибки заранее
            val errorPainter = painterResource(id = R.drawable.default_event_image)

            // Изображение
            androidx.compose.foundation.Image(
                painter = event.eventImageUrl?.let {
                    rememberAsyncImagePainter(
                        model = it,
                        error = errorPainter // Передаем уже созданный Painter
                    )
                } ?: errorPainter,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            // Градиентное затемнение
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = 250.dp.value
                        )
                    )
            )

            // Кнопка "Назад"
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

            // Заголовок и дата
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

        // Карточка с деталями
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
                // Место
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

                // Описание
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

                // Кол-во участников
                Text(
                    text = "Кол-во участников: 50",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Организатор
                Text(
                    text = "Организатор: ${event.organizerName ?: "Неизвестно"}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Кнопка "Участвовать"
        Button(
            onClick = {
                if (isParticipating) {
                    onLeaveClick()
                } else {
                    onParticipateClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
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
    }
}
