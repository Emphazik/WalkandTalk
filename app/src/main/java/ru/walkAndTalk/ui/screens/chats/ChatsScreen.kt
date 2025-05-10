package ru.walkAndTalk.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.ui.screens.chats.ChatsSideEffect
import ru.walkAndTalk.ui.screens.chats.ChatsViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@Composable
fun ChatsScreen(viewModel: ChatsViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    // Обработка побочных эффектов (например, переход к чату)
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatsSideEffect.NavigateToChat -> {
                // Здесь можно добавить навигацию к экрану чата
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp) // Внутренние отступы вместо padding из Scaffold
    ) {
        item {
            Text(
                text = "Ваши чаты",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
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
        } else if (state.chats.isEmpty()) {
            item {
                Text(
                    text = "Чаты отсутствуют",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.onBackground
                )
            }
        } else {
            items(state.chats) { chat ->
                ChatCard(chat = chat, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChatCard(chat: Chat, viewModel: ChatsViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { viewModel.onChatClick(chat.id) },
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
                chat.unreadCount?.let {
                    if (it > 0.toString()) {
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
}