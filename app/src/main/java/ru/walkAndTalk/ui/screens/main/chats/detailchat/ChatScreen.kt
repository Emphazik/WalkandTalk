package ru.walkAndTalk.ui.screens.main.chats.detailchat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.ui.theme.montserratFont
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    userId: String,
    navController: NavHostController,
    viewModel: ChatViewModel = koinViewModel(parameters = { parametersOf(chatId, userId) })
) {
//    val state by viewModel.collectAsState()
    val state by viewModel.container.stateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    val isScrolledUp by remember {
        derivedStateOf {
            val lastVisibleItemIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleItemIndex != null && lastVisibleItemIndex < state.messages.size - 1
        }
    }

    LaunchedEffect(state.messages) {
        val lastMessage = state.messages.lastOrNull()
        println("ChatScreen: Messages updated, messagesCount=${state.messages.size}, lastMessageId=${lastMessage?.id}, isRead=${lastMessage?.isRead}")
        viewModel.forceUpdateReadStatus()
        viewModel.loadChat()
    }

    LaunchedEffect(state.messages, state.isLoading) {
        if (!state.isLoading && state.messages.isNotEmpty()) {
            lazyListState.scrollToItem(state.messages.size - 1)
            println("ChatScreen: Scrolled to last message on initial load")
        }
    }

//    LaunchedEffect(state.messages.size) {
//        if (state.messages.isNotEmpty()) {
//            lazyListState.animateScrollToItem(state.messages.size - 1)
//            println("ChatScreen: Scrolled to last message after new message")
//        }
//    }
    LaunchedEffect(state.messages.size) {
        val lastMessage = state.messages.lastOrNull()
        println("ChatScreen: Messages size changed, messagesCount=${state.messages.size}, lastMessageId=${lastMessage?.id}, isRead=${lastMessage?.isRead}, messageIds=${state.messages.map { it.id }}")
        if (state.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.messages.size - 1)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = sideEffect.message,
                    duration = SnackbarDuration.Short
                )
                println("ChatScreen: Showing error snackbar: ${sideEffect.message}")
            }

            is ChatSideEffect.ShowCopySuccess -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short
                    )
                    println("ChatScreen: Showing copy success snackbar: ${sideEffect.message}")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { /* Отключен встроенный snackbarHost */ },
            topBar = {
                if (state.isSearchActive) {
                    // Режим поиска
                    TopAppBar(
                        title = {
                            TextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 8.dp)
                                    .background(Color.White, shape = RoundedCornerShape(8.dp)), // Явный фон с закруглением
                                placeholder = {
                                    Text(
                                        text = "Поиск сообщений...",
                                        fontSize = 16.sp,
                                        fontFamily = montserratFont,
                                        color = Color.Gray
                                    )
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = montserratFont,
                                    color = Color.Black
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                trailingIcon = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Очистить поиск",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.toggleSearch() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Отменить поиск",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Дополнительные действия поиска, если нужны */ }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Поиск",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                } else {
                    // Обычный режим
                    TopAppBar(
                        expandedHeight = 48.dp,
                        title = {
                            if (state.selectedMessageIds.isEmpty()) {
                                Column {
                                    Text(
                                        text = if (state.chat?.type == "group") {
                                            state.chat?.eventName ?: "Групповой чат"
                                        } else {
                                            state.chat?.participantName ?: "Неизвестный пользователь"
                                        },
                                        fontFamily = montserratFont,
                                        fontSize = if (state.chat?.type == "group") 15.sp else 18.sp, // Different font sizes
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1, // Limit to one line
                                        overflow = TextOverflow.Ellipsis // Add ellipsis for overflow
                                    )
                                    if (state.chat?.type == "group") {
                                        val participantCount = viewModel.getParticipantCount()
                                        Text(
                                            text = "$participantCount участников",
                                            fontFamily = montserratFont,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Выбрано: ${state.selectedMessageIds.size}",
                                    fontFamily = montserratFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (state.selectedMessageIds.isNotEmpty()) {
                                    viewModel.clearSelection()
                                } else {
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "refreshChats",
                                        true
                                    )
                                    navController.popBackStack()
                                    println("ChatScreen: Returning to ChatsScreen, set refreshChats=true")
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        actions = {
                            if (state.selectedMessageIds.isEmpty()) {
                                IconButton(onClick = { viewModel.toggleSearch() }) { // Заменяем уведомление на функционал поиска
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Поиск",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                IconButton(onClick = { showBottomSheet = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_more128),
                                        contentDescription = "Меню",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            } else {
                                if (state.selectedMessageIds.size == 1) {
                                    val selectedMessage =
                                        state.messages.first { it.id in state.selectedMessageIds }
                                    if (selectedMessage.senderId == userId) {
                                        IconButton(
                                            onClick = { viewModel.onEditClick(selectedMessage) }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_edit64),
                                                contentDescription = "Редактировать",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.toggleShowDeleteDialog() }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_delete64),
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (state.error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ошибка: ${state.error}",
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = montserratFont,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        val filteredMessages = if (state.searchQuery.isNotEmpty()) {
                            state.messages.filter {
                                it.content.contains(state.searchQuery, ignoreCase = true)
                            }
                        } else {
                            state.messages
                        }
//                        LazyColumn(
//                            state = lazyListState,
//                            modifier = Modifier
//                                .weight(1f)
//                                .padding(horizontal = 16.dp),
//                            verticalArrangement = Arrangement.spacedBy(8.dp),
//                            contentPadding = PaddingValues(vertical = 16.dp)
//                        ) {
//                            val groupedMessages = filteredMessages.groupBy { message ->
//                                OffsetDateTime.parse(message.createdAt).toLocalDate()
//                            }
//                            groupedMessages.entries.forEachIndexed { index, (date, messages) ->
//                                item {
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 8.dp),
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Spacer(
//                                            modifier = Modifier
//                                                .weight(1f)
//                                                .height(1.dp)
//                                                .background(
//                                                    MaterialTheme.colorScheme.onSurface.copy(
//                                                        alpha = 0.2f
//                                                    )
//                                                )
//                                        )
//                                        Text(
//                                            text = formatDateForSeparator(messages.first().createdAt),
//                                            fontFamily = montserratFont,
//                                            fontSize = 12.sp,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                            modifier = Modifier.padding(horizontal = 8.dp)
//                                        )
//                                        Spacer(
//                                            modifier = Modifier
//                                                .weight(1f)
//                                                .height(1.dp)
//                                                .background(
//                                                    MaterialTheme.colorScheme.onSurface.copy(
//                                                        alpha = 0.2f
//                                                    )
//                                                )
//                                        )
//                                    }
//                                }
//                                itemsIndexed(messages, key = { _, message -> "${message.id}-${message.isRead}" }) { msgIndex, message ->
//                                    val showSenderName = if (msgIndex > 0) {
//                                        val prevMessage = messages[msgIndex - 1]
//                                        message.senderId != prevMessage.senderId && message.senderName != null
//                                    } else {
//                                        message.senderName != null
//                                    }
//                                    MessageItem(
//                                        message = message,
//                                        currentUserId = userId,
//                                        isSelected = message.id in state.selectedMessageIds,
//                                        showSenderName = showSenderName,
//                                        onLongClick = { viewModel.toggleMessageSelection(message.id) },
//                                        onCopy = { viewModel.showCopySuccess() }
//                                    )
//                                }
//                            }
//                        }
//                    }
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            val groupedMessages = filteredMessages.groupBy { message ->
                                OffsetDateTime.parse(message.createdAt).toLocalDate()
                            }
                            groupedMessages.entries.forEachIndexed { index, (date, messages) ->
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(1.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.2f
                                                    )
                                                )
                                        )
                                        Text(
                                            text = formatDateForSeparator(messages.first().createdAt),
                                            fontFamily = montserratFont,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(1.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.2f
                                                    )
                                                )
                                        )
                                    }
                                }
                                itemsIndexed(
                                    messages,
                                    key = { _, message -> message.id }) { msgIndex, message ->
                                    val showSenderName = if (msgIndex > 0) {
                                        val prevMessage = messages[msgIndex - 1]
                                        message.senderId != prevMessage.senderId && message.senderName != null
                                    } else {
                                        message.senderName != null
                                    }
                                    MessageItem(
                                        message = message,
                                        currentUserId = userId,
                                        isSelected = message.id in state.selectedMessageIds,
                                        showSenderName = showSenderName,
                                        onLongClick = { viewModel.toggleMessageSelection(message.id) },
                                        onCopy = { viewModel.showCopySuccess() }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = { viewModel.onInputTextChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .background(MaterialTheme.colorScheme.background),
                        placeholder = { Text("Введите сообщение...") },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.onSendMessageClick(chatId) },
                                enabled = state.inputText.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Отправить",
                                    tint = if (state.inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f
                                    )
                                )
                            }
                        }
                    )
                }

                if (isScrolledUp) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(state.messages.size - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 72.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Прокрутить вниз",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (showBottomSheet && state.chat != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        var showClearHistoryDialog by remember { mutableStateOf(false) }

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
                                    text = state.chat?.participantName ?: state.chat?.eventName ?: "Чат",
                                    fontFamily = montserratFont,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (state.chat?.isMuted == true) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_notifications_off32),
                                        contentDescription = "Уведомления отключены",
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.toggleMuteChat(
                                            chatId,
                                            !(state.chat?.isMuted ?: false)
                                        )
                                        showBottomSheet = false
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
                                        text = if (state.chat?.isMuted == true) "Включить уведомления" else "Отключить уведомления",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            state.chat?.unreadCount?.let {
                                if (it > 0) {
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.markChatAsRead(chatId)
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
                            TextButton(
                                onClick = { viewModel.toggleShowDeleteDialog() },
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

                            if (state.showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { viewModel.toggleShowDeleteDialog() },
                                    title = { Text("Удаление сообщений") },
                                    text = { Text("Вы уверены, что хотите удалить ${state.selectedMessageIds.size} выбранных сообщений?") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                viewModel.onDeleteMessagesClick(
                                                    state.selectedMessageIds.toList(),
                                                    isLocal = false
                                                )
                                            }
                                        ) {
                                            Text("Удалить")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { viewModel.toggleShowDeleteDialog() }) {
                                            Text("Отмена")
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
                                                    viewModel.clearChatHistory(chatId)
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

                if (state.showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.toggleShowDeleteDialog() },
                        title = {
                            Text(
                                text = "Удалить ${state.selectedMessageIds.size} сообщений?",
                                fontFamily = montserratFont,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        text = {
                            Text(
                                text = "Вы уверены, что хотите удалить выбранные сообщения? Это действие нельзя отменить.",
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.onDeleteMessagesClick(state.selectedMessageIds.toList())
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
                                onClick = { viewModel.toggleShowDeleteDialog() }
                            ) {
                                Text(
                                    text = "Отмена",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                }

                if (state.showEditDialog && state.editingMessageId != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.toggleShowEditDialog() },
                        title = {
                            Text(
                                text = "Редактировать сообщение",
                                fontFamily = montserratFont,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        text = {
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { viewModel.onInputTextChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Введите новый текст...") }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (state.inputText.trim().isNotEmpty()) {
                                        viewModel.editMessage(
                                            state.editingMessageId!!,
                                            state.inputText.trim()
                                        )
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Сообщение не может быть пустым")
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = "Сохранить",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.toggleShowEditDialog() }
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

        // Оставляем только один SnackbarHost вверху
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                val alpha by animateFloatAsState(
                    targetValue = if (snackbarData.visuals.message.isNotEmpty()) 1f else 0f,
                    animationSpec = tween(durationMillis = 300)
                )
                Snackbar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .alpha(alpha),
                    action = {
                        TextButton(onClick = { snackbarData.dismiss() }) {
                            Text("ОК")
                        }
                    },
                    containerColor = when {
                        snackbarData.visuals.message.contains("успешно", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    contentColor = when {
                        snackbarData.visuals.message.contains("успешно", ignoreCase = true) -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        if (!snackbarData.visuals.message.contains("успешно", ignoreCase = true)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Предупреждение",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = snackbarData.visuals.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    currentUserId: String,
    isSelected: Boolean = false,
    showSenderName: Boolean,
    onLongClick: () -> Unit,
    onCopy: () -> Unit
) {
    LaunchedEffect(message.id, message.isRead) {
            println("MessageItem: Rendering message id=${message.id}, content=${message.content}, isRead=${message.isRead}, isOwn=${message.senderId == currentUserId}")
        }
    val isOwnMessage = message.senderId == currentUserId
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.background
            )
    ) {
        if (!isOwnMessage && showSenderName) {
            Text(
                text = message.senderName ?: "Unknown",
                fontFamily = montserratFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(start = 12.dp, bottom = 4.dp)
                    .align(Alignment.Start)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Message", message.content)
                        clipboard.setPrimaryClip(clip)
                        onCopy()
                    },
                    onLongClick = onLongClick
                ),
            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOwnMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = message.content,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = formatMessageTimeForChat(message.createdAt),
                            fontFamily = montserratFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isOwnMessage) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(
                                    id = if (message.isRead) R.drawable.ic_double_check64 else R.drawable.ic_single_check64
                                ),
                                contentDescription = if (message.isRead) "Прочитано" else "Отправлено",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

    @SuppressLint("SimpleDateFormat")
    fun formatMessageTimeForChat(time: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(time)
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(offsetDateTime.toInstant().toEpochMilli())
        } catch (e: Exception) {
            println("ChatScreen: Error parsing date: $e")
            "Неизвестно"
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDateForSeparator(time: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(time)
            val zoneId = ZoneId.of("America/Chicago")
            val now = LocalDateTime.now(zoneId)
            val date = offsetDateTime.toLocalDate()
            val yesterday = now.toLocalDate().minusDays(1)
            when {
                date == now.toLocalDate() -> "Сегодня"
                date == yesterday -> "Вчера"
                date.year == now.year -> SimpleDateFormat("dd MMMM", Locale.getDefault()).format(
                    offsetDateTime.toInstant().toEpochMilli()
                )

                else -> SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(offsetDateTime.toInstant().toEpochMilli())
            }
        } catch (e: Exception) {
            println("ChatScreen: Error parsing date for separator: $e")
            "Неизвестно"
        }
    }
