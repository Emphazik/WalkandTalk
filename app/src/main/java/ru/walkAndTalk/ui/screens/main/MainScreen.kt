package ru.walkAndTalk.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import ru.walkAndTalk.R
import ru.walkAndTalk.data.model.Event

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val tabs = listOf("Главная", "Поиск", "Чаты", "Профиль")
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.background,
                modifier = Modifier.height(64.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = state.selectedTab == index
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> R.drawable.ic_home1
                                        1 -> R.drawable.ic_people64
                                        2 -> R.drawable.ic_chat64
                                        else -> R.drawable.ic_profile1
                                    }
                                ),
                                contentDescription = title,
                                tint = if (selected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontFamily = montserratFont,
                                fontSize = 12.sp,
                                color = if (selected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        },
                        selected = selected,
                        onClick = { viewModel.onSelectedTabChange(index) }
                    )
                }
            }
        }
    ) { padding ->
        when (state.selectedTab) {
            0 -> FeedScreen(padding, viewModel)
            1 -> SearchScreen(padding)
            2 -> ChatsScreen(padding)
            3 -> ProfileScreen(padding)
        }
    }
}

@Composable
fun FeedScreen(padding: PaddingValues, viewModel: MainViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    var searchQuery by remember { mutableStateOf("") }

    val events = remember {
        listOf(
            Event(
                id = "1",
                creatorId = "creator1",
                title = "Пикник в парке Горького",
                description = "Присоединяйтесь к уютному пикнику в парке Горького. Будем наслаждаться природой и общением!",
                location = "Москва",
                eventDate = "2025-04-16T14:00:00Z",
                createdAt = "2025-04-01T10:00:00Z"
            ),
            Event(
                id = "2",
                creatorId = "creator2",
                title = "Вечерняя прогулка по Неве",
                description = "Романтическая прогулка на закате вдоль реки Невы. Захватите фотоаппарат!",
                location = "Санкт-Петербург",
                eventDate = "2025-04-17T19:00:00Z",
                createdAt = "2025-04-02T12:00:00Z"
            ),
            Event(
                id = "3",
                creatorId = "creator3",
                title = "Кофейная встреча",
                description = "Утренняя встреча за чашкой кофе. Обсудим любимые места в городе.",
                location = "Москва",
                eventDate = "2025-04-18T10:00:00Z",
                createdAt = "2025-04-03T09:00:00Z"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(padding)
    ) {
        item {
            Text(
                text = "Мероприятия рядом",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )

            // Блок поиска
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = {
                        Text(
                            text = "Поиск мероприятий...",
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
                    onClick = { /* Заглушка для фильтра */ },
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
        items(events) { event -> EventCard(event, viewModel) }
    }
}

@Composable
fun EventCard(event: Event, viewModel: MainViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* Заглушка для перехода к деталям мероприятия */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Изображение мероприятия
            Image(
                painter = event.image(),
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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

                // Кнопка "Подробнее"
                Button(
                    onClick = { /* Заглушка для перехода к деталям */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Подробнее",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SearchScreen(padding: PaddingValues) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Поиск новых друзей\n(в разработке)",
            fontFamily = montserratFont,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = colorScheme.onBackground
        )
    }
}

@Composable
fun ChatsScreen(padding: PaddingValues) {
    val colorScheme = MaterialTheme.colorScheme
    val chats = remember {
        listOf(
            Chat("Пикник в парке Горького", "Анна: Отлично, беру плед!", "14:32", 3),
            Chat("Вечерняя прогулка по Неве", "Игорь: Кто за фото на закате?", "12:15", 6),
            Chat("Кофейная встреча", "Мария: Какое кафе выберем?", "09:50", 2)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(padding)
    ) {
        item {
            Text(
                text = "Ваши чаты",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(chats) { chat -> ChatCard(chat) }
    }
}

@Composable
fun ChatCard(chat: Chat) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_chat),
                contentDescription = chat.eventName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.eventName,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = chat.lastMessage,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = chat.lastMessageTime,
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontFamily = montserratFont,
                            fontSize = 12.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(padding: PaddingValues) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedCityStatus by remember { mutableStateOf("Новичок") }
    var showCityStatusMenu by remember { mutableStateOf(false) }
    val cityStatuses = listOf("Новичок", "Знаток", "Эксперт")
    val interests = remember { listOf("История", "Архитектура", "Природа", "Гастрономия", "Искусство", "Спорт", "Музыка").take(7) } // Ограничение до 7

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(padding)
    ) {
        // Иконка редактирования фото в правом верхнем углу
        IconButton(
            onClick = { /* Заглушка для изменения фото */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(32.dp)
                .background(colorScheme.primary.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more128),
                contentDescription = "Edit photo",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Фото профиля
            Image(
                painter = painterResource(id = R.drawable.preview_profile),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 64.dp) // Смещаем вниз из-за иконки
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorScheme.onSurface.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Имя пользователя (заглушка)
            Text(
                text = "Litvin",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Статус в городе
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        text = selectedCityStatus,
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { showCityStatusMenu = true }
                    )
                    DropdownMenu(
                        expanded = showCityStatusMenu,
                        onDismissRequest = { showCityStatusMenu = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        cityStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status,
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedCityStatus = status
                                    showCityStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // О себе (bio)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "О себе",
                        fontFamily = montserratFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Люблю исследовать новые города, увлекаюсь историей и архитектурой. Ищу единомышленников для прогулок!",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Интересы
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                        text = "Добавить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .clickable { /* Заглушка для добавления интереса */ }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(interests) { interest ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = interest,
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp,
                                    color = colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_clear128),
                                    contentDescription = "Remove interest",
                                    tint = colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { /* Заглушка для удаления интереса */ }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Цели (goals)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Цели",
                        fontFamily = montserratFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Хочу найти друзей для совместных прогулок и узнать больше о городе.",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Кнопки привязаны к низу экрана
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { /* Заглушка для статистики */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Статистика мероприятий и отзывов",
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = { /* Заглушка для редактирования профиля */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Редактировать профиль",
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = { /* Заглушка для выхода */ },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Выйти",
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}


data class Chat(
    val eventName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)

//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//
//@Composable
//fun MainScreen(navController: NavHostController) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = "Welcome to Main Screen!",
//            fontSize = 24.sp
//        )
//    }
//}