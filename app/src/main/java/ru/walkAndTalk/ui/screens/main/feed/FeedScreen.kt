package ru.walkAndTalk.ui.screens.main.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@Composable
fun FeedScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = koinViewModel()
) {
    val state by feedViewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    feedViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.NavigateToEventDetails -> {
                navController.navigate(EventDetails.createRoute(sideEffect.eventId))
            }
            else -> {
                // Делегируем все остальные случаи MainScreen
            }
        }
    }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Мероприятия рядом",
                    fontFamily = montserratFont,
                    fontSize = 24.sp,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { feedViewModel.onSearchQueryChange(it) },
                    onFilterClick = { /* Заглушка для фильтра */ },
                    colorScheme = colorScheme
                )
            }
            if (state.isLoading) {
                item {
                    Text(
                        text = "Загрузка...",
                        modifier = Modifier.padding(16.dp),
                        color = colorScheme.onBackground
                    )
                }
            } else if (state.error != null) {
                item {
                    Text(
                        text = "Ошибка: ${state.error}",
                        modifier = Modifier.padding(16.dp),
                        color = colorScheme.error
                    )
                }
            } else {
                items(state.events) { event ->
                    val isParticipating = feedViewModel.participationState
                        .map { it[event.id] ?: false }
                        .collectAsState(initial = false).value

                    EventCard(
                        event = event,
                        viewModel = feedViewModel,
                        isParticipating = isParticipating
                    )
                }
            }
        }
    }


@Composable
fun EventCard(
    event: Event,
    viewModel: FeedViewModel,
    isParticipating: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    var showLeaveConfirmation by remember { mutableStateOf(false) } // Добавляем состояние для диалога

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
            // Изображение мероприятия
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
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            // Контент карточки
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Дата
                Text(
                    text = viewModel.formatEventDate(event.eventDate),
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Название
                Text(
                    text = event.title,
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Место и описание
                Text(
                    text = "${event.location} • ${event.description}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка для теста обновления изображения
//                Button(
//                    onClick = {
//                        viewModel.updateEventImage(event.id, "https://example.com/new_image.jpg")
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(40.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = colorScheme.secondary
//                    ),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        text = "Обновить изображение",
//                        fontFamily = montserratFont,
//                        fontSize = 14.sp,
//                        color = colorScheme.onSecondary
//                    )
//                }
//                // Кнопка "Подробнее"
//                Button(
//                    onClick = { viewModel.onEventClick(event.id) },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(40.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = colorScheme.primary
//                    ),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        text = "Подробнее",
//                        fontFamily = montserratFont,
//                        fontSize = 14.sp,
//                        color = colorScheme.onPrimary
//                    )
//                }
                Spacer(modifier = Modifier.height(6.dp))
                // Кнопка "Присоединиться"

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

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    text = "Поиск мероприятий...",
                    fontFamily = ru.walkAndTalk.ui.screens.main.montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search64),
                    contentDescription = "Search icon",
                    tint = colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
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
                contentDescription = "Filter icon",
                tint = colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}