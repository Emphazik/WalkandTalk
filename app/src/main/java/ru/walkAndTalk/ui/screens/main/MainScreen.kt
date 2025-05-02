package ru.walkAndTalk.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import ru.walkAndTalk.R

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val tabs = listOf("Лента", "Поиск", "Чаты", "Профиль")

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(64.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> R.drawable.ic_home
                                        1 -> R.drawable.ic_poisk
                                        2 -> R.drawable.ic_default_chat
                                        else -> R.drawable.ic_main_profile
                                    }
                                ),
                                contentDescription = title,
                                tint = if (state.selectedTab == index) Color(0xFF007AFF) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontFamily = montserratFont,
                                fontSize = 12.sp,
                                color = if (state.selectedTab == index) Color(0xFF007AFF) else Color.Gray
                            )
                        },
                        selected = state.selectedTab == index,
                        onClick = { viewModel.onSelectedTabChange(index) }
                    )
                }
            }
        }
    ) { padding ->
        when (state.selectedTab) {
            0 -> FeedScreen(padding)
            1 -> SearchScreen(padding)
            2 -> ChatsScreen(padding)
            3 -> ProfileScreen(padding)
        }
    }
}

@Composable
fun FeedScreen(padding: PaddingValues) {
    // Пример данных для ленты
    val events = remember {
        listOf(
            Event("Пикник в парке Горького", "14:00, 16 апреля", "Москва", 4),
            Event("Вечерняя прогулка по Неве", "19:00, 17 апреля", "Санкт-Петербург", 6),
            Event("Кофейная встреча", "10:00, 18 апреля", "Москва", 3)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(padding)
    ) {
        item {
            Text(
                text = "Мероприятия рядом",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(events) { event ->
            EventCard(event)
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* Переход к деталям мероприятия */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_profile),
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = event.title,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${event.time} • ${event.location}",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${event.participants} участников",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = Color(0xFF007AFF)
                )
            }
        }
    }
}

@Composable
fun SearchScreen(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Поиск мероприятий\n(в разработке)",
            fontFamily = montserratFont,
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
@Composable
fun ChatsScreen(padding: PaddingValues) {
    // Пример данных для чатов
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
            .background(Color(0xFFF5F5F5))
            .padding(padding)
    ) {
        item {
            Text(
                text = "Ваши чаты",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(chats) { chat ->
            ChatCard(chat)
        }
    }
}
@Composable
fun ChatCard(chat: Chat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* Переход к чату */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chat.eventName,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = chat.lastMessage,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = chat.lastMessageTime,
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007AFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontFamily = montserratFont,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.preview_profile),
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ваш профиль",
            fontFamily = montserratFont,
            fontSize = 24.sp
        )
        Text(
            text = "Здесь будет информация о вас\n(в разработке)",
            fontFamily = montserratFont,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

data class Event(
    val title: String,
    val time: String,
    val location: String,
    val participants: Int
)

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