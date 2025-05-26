package ru.walkAndTalk.ui.screens.main.chats

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.ui.theme.montserratFont
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    userId: String,
    navController: NavHostController,
    viewModel: ChatsViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var selectedChat by remember { mutableStateOf<Chat?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Подход 1: Через savedStateHandle
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refreshChats")?.observeForever { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.loadChats(userId)
                println("ChatsScreen: Refreshed chats after returning from ChatScreen (via savedStateHandle)")
                // Сбрасываем флаг, чтобы избежать повторного срабатывания
                navController.currentBackStackEntry?.savedStateHandle?.set("refreshChats", false)
            }
        }
    }

    // Подход 2: Через currentBackStackEntryFlow
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            println("ChatsScreen: Current route=${backStackEntry.destination.route}")
            if (backStackEntry.destination.route == "ru.walkAndTalk.ui.screens.Chats") {
                viewModel.loadChats(userId)
                println("ChatsScreen: Returned to ChatsScreen, refreshing chats (via route)")
            }
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatsSideEffect.NavigateToChat -> {
                navController.navigate("chat/${sideEffect.chatId}")
                println("ChatsScreen: Navigate to chat id=${sideEffect.chatId}")
            }

            is ChatsSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
        }
    }

    // UI
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                expandedHeight = 32.dp,
                title = {
                    Text(
                        text = "Чаты",
                        fontFamily = montserratFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Поиск пока не реализован")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = {
                        viewModel.loadChats(userId)
                        println("ChatsScreen: Refresh chats triggered")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh64),
                            contentDescription = "Обновить",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Text(
                        text = "Ошибка: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                state.chats.isEmpty() -> {
                    Text(
                        text = "Чатов пока нет",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        items(state.chats) { chat ->
                            ChatItem(
                                chat = chat,
                                userId = userId, // Передаем userId
                                onClick = {
                                    navController.navigate("chat/${chat.id}")
                                },
                                onLongPress = {
                                    selectedChat = chat
                                    showBottomSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
        if (showBottomSheet && selectedChat != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    selectedChat = null
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                var showDeleteDialog by remember { mutableStateOf(false) }
                var showClearHistoryDialog by remember { mutableStateOf(false) }

                // Синхронизируем selectedChat с state.chats при каждом рендере
                val syncedSelectedChat = state.chats.find { it.id == selectedChat!!.id } ?: selectedChat!!

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = when (syncedSelectedChat.type) {
                                "private" -> "Собеседник: ${syncedSelectedChat.participantName ?: "Неизвестный пользователь"}"
                                "group" -> "Мероприятие: ${syncedSelectedChat.eventName ?: "Групповой чат"}"
                                else -> "Чат"
                            },
                            fontFamily = montserratFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (syncedSelectedChat.isMuted) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notifications_off32),
                                contentDescription = "Уведомления отключены",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Отключить/включить уведомления
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.toggleMuteChat(syncedSelectedChat.id, !syncedSelectedChat.isMuted)
                                showBottomSheet = false // Закрываем меню после действия
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notifications),
                                contentDescription = "Уведомления",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (syncedSelectedChat.isMuted) "Включить уведомления" else "Отключить уведомления",
                                fontFamily = montserratFont,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Отметить прочитанным
                    syncedSelectedChat.unreadCount?.let {
                        if (it > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.markChatAsRead(syncedSelectedChat.id)
                                        showBottomSheet = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_double_check64),
                                        contentDescription = "Прочитано",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Отметить прочитанным",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    // Очистить историю
                    TextButton(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clear_history64),
                                contentDescription = "Очистить историю",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Очистить историю",
                                fontFamily = montserratFont,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Удалить чат
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete64),
                                contentDescription = "Удалить чат",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Удалить чат",
                                fontFamily = montserratFont,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = {
                                Text(
                                    text = "Удалить чат?",
                                    fontFamily = montserratFont,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            text = {
                                Text(
                                    text = "Вы уверены, что хотите удалить этот чат? Это действие нельзя отменить.",
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.deleteChat(syncedSelectedChat.id)
                                            showDeleteDialog = false
                                            showBottomSheet = false
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Удалить",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDeleteDialog = false }
                                ) {
                                    Text(
                                        text = "Отмена",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )
                    }

                    if (showClearHistoryDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearHistoryDialog = false },
                            title = {
                                Text(
                                    text = "Очистить историю?",
                                    fontFamily = montserratFont,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            text = {
                                Text(
                                    text = "Вы уверены, что хотите очистить историю сообщений? Это действие нельзя отменить.",
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.clearChatHistory(syncedSelectedChat.id)
                                            showClearHistoryDialog = false
                                            showBottomSheet = false
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Очистить",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showClearHistoryDialog = false }
                                ) {
                                    Text(
                                        text = "Отмена",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    userId: String, // Добавляем userId для проверки отправителя
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1-я колонка: Аватар
            if (chat.participantUser?.profileImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(chat.participantUser.profileImageUrl),
                    contentDescription = chat.participantName ?: chat.eventName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.participantName?.firstOrNull()?.toString() ?: "G",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2-я колонка: Название и последнее сообщение
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (chat.type) {
                            "private" -> chat.participantName ?: "Неизвестный пользователь"
                            "group" -> chat.eventName ?: "Групповой чат"
                            else -> "Чат"
                        },
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.isMuted) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notifications_off32),
                            contentDescription = "Уведомления отключены",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lastMessage = chat.lastMessage ?: "Нет сообщений"
                    val displayMessage = if (chat.participantIds.contains(userId)) "Вы: $lastMessage" else lastMessage
                    Text(
                        text = displayMessage,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = chat.lastMessageTime?.let { formatMessageTime(it) } ?: "",
                        fontFamily = montserratFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 4-я колонка: Статус (unreadCount или иконки)
            when {
                chat.unreadCount != null && chat.unreadCount > 0 -> {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                chat.lastMessage != null -> {
                    // Если есть последнее сообщение, определяем статус
                    val isReadByOthers = chat.isMessageRead == true
                    Icon(
                        painter = painterResource(
                            id = if (isReadByOthers) R.drawable.ic_double_check64 else R.drawable.ic_single_check64
                        ),
                        contentDescription = if (isReadByOthers) "Прочитано другими" else "Отправлено",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun formatMessageTime(time: String): String {
    return try {
        // Парсим как OffsetDateTime, чтобы сохранить информацию о часовом поясе
        val offsetDateTime = OffsetDateTime.parse(time)
        // Получаем текущее время с учетом часового пояса устройства (CDT, UTC-5)
        val zoneId = ZoneId.of("America/Chicago") // CDT, под нужный часовой пояс
        val now = LocalDateTime.now(zoneId)
        val formatter = when {
            offsetDateTime.toLocalDate() == now.toLocalDate() -> SimpleDateFormat("HH:mm", Locale.getDefault())
            offsetDateTime.year == now.year -> SimpleDateFormat("dd MMM", Locale.getDefault())
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        }
        // Преобразуем в миллисекунды с учетом смещения
        formatter.format(offsetDateTime.toInstant().toEpochMilli())
    } catch (e: Exception) {
        println("ChatsScreen: Error parsing date: $e")
        "Неизвестно"
    }
}