package ru.walkAndTalk.ui.screens.main.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.theme.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = koinViewModel()
) {
    val state by feedViewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    feedViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.NavigateToEventDetails -> {
                navController.navigate(EventDetails.createRoute(sideEffect.eventId))
            }
            is FeedSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
            else -> {
                // Делегируем остальные случаи MainScreen
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    text = "Мероприятия",
                    fontFamily = montserratFont,
                    fontSize = 24.sp,
                    color = colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Уведомлений нет")
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        tint = colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { feedViewModel.refreshEvents() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh64),
                        contentDescription = "Refresh",
                        tint = colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { feedViewModel.onSearchQueryChange(it) },
                onClearQuery = { feedViewModel.onClearQuery() },
                onSortSelected = { sortType -> feedViewModel.onSortSelected(sortType) },
                colorScheme = colorScheme
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
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
                } else if (state.events.isEmpty() && state.searchQuery.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ничего не найдено",
                            modifier = Modifier.padding(16.dp),
                            color = colorScheme.onBackground
                        )
                    }
                } else if (state.events.isEmpty()) {
                    item {
                        Text(
                            text = "Мероприятия отсутствуют",
                            modifier = Modifier.padding(16.dp),
                            color = colorScheme.onBackground
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
fun EventCard(
    event: Event,
    viewModel: FeedViewModel,
    isParticipating: Boolean
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
                    text = viewModel.formatEventDate(event.eventDate),
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
                    overflow = TextOverflow.Ellipsis // Добавляем многоточие
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Location",
                        tint = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.location}",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (event.tagIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically, // Выравнивание по центру
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tags:",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f) // Чипы занимают оставшееся место
                        ) {
                            event.tagIds.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colorScheme.primaryContainer,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontFamily = montserratFont,
                                        fontSize = 12.sp,
                                        color = colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSortSelected: (SortType) -> Unit,
    colorScheme: ColorScheme
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = {
                println("SearchBar: Query=$it")
                onQueryChange(it)
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    text = "Поиск по имени и по #тегу",
                    fontFamily = montserratFont,
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
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        println("SearchBar: ClearQuery")
                        onQueryChange("")
                        onClearQuery()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clear128),
                            contentDescription = "Clear search",
                            tint = colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
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
        Box {
            IconButton(
                onClick = { showFilterMenu = true },
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
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("По дате (новые → старые)") },
                    onClick = {
                        onSortSelected(SortType.DateNewestFirst)
                        showFilterMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("По дате (старые → новые)") },
                    onClick = {
                        onSortSelected(SortType.DateOldestFirst)
                        showFilterMenu = false
                    }
                )
            }
        }
    }
}

enum class SortType {
    DateNewestFirst,
    DateOldestFirst
}
