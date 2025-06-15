package ru.walkAndTalk.ui.screens.main.feed.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Notification
import ru.walkAndTalk.domain.model.NotificationType
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.theme.montserratFont
import java.time.OffsetDateTime
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    userId: String,
    viewModel: NotificationsViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state = viewModel.container.stateFlow.collectAsState().value
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.NavigateToEventDetails -> {
                navController.navigate(EventDetails(sideEffect.eventId))
            }

            is FeedSideEffect.NavigateToChat -> {
                navController.navigate("chat/${sideEffect.chatId}")
            }

            is FeedSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 24.dp,
                title = {
                    Text(
                        text = "Уведомления",
                        fontFamily = montserratFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Кнопка обновления
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh64),
                            contentDescription = "Refresh notifications",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = state.isLoadingNotifications,
            onRefresh = { viewModel.loadNotifications() }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.notifications.any { !it.isRead }) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            TextButton(
                                onClick = { viewModel.markAllNotificationsAsRead() },
                                modifier = Modifier.padding(end = 4.dp) // Минимальный отступ справа
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Прочесть всё",
                                        fontFamily = montserratFont,
                                        fontSize = 14.sp,
                                        color = colorScheme.primary
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_done),
                                        contentDescription = "Mark all as read",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(16.dp) // Компактная иконка
                                    )
                                }
                            }
                        }
                    }
                }
//                if (state.notifications.any { !it.isRead }) {
//                    item {
//                        // Кнопка "Прочесть всё" вверху списка
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp),
//                            contentAlignment = Alignment.TopEnd
//                        ) {
//                            IconButton(
//                                onClick = { viewModel.markAllNotificationsAsRead() },
//                                modifier = Modifier.size(32.dp) // Компактный размер
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.ic_done),
//                                    contentDescription = "Mark all as read",
//                                    tint = colorScheme.primary,
//                                    modifier = Modifier.size(20.dp)
//                                )
//                            }
//                        }
//                    }
//                }
                if (state.isLoadingNotifications) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.notificationsError != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
//                                text = "Ошибка: ${state.notificationsError}",
                                text = "Произошла техническая ошибка. Просим прощения!",
                                color = colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadNotifications() }) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                } else if (state.notifications.isEmpty()) {
                    item {
                        Text(
                            text = "Уведомления отсутствуют",
                            color = colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(state.notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = { viewModel.onNotificationClick(notification) },
                            formatDate = { isoDate ->
                                isoDate?.let {
                                    try {
                                        val offsetDateTime = OffsetDateTime.parse(it)
                                        val zoneId = java.time.ZoneId.systemDefault()
                                        val now = OffsetDateTime.now(zoneId)
                                        when {
                                            offsetDateTime.toLocalDate() == now.toLocalDate() ->
                                                "Сегодня, ${
                                                    SimpleDateFormat(
                                                        "HH:mm",
                                                        Locale("ru")
                                                    ).format(
                                                        offsetDateTime.toInstant().toEpochMilli()
                                                    )
                                                }"

                                            offsetDateTime.toLocalDate() == now.toLocalDate()
                                                .minusDays(1) ->
                                                "Вчера, ${
                                                    SimpleDateFormat(
                                                        "HH:mm",
                                                        Locale("ru")
                                                    ).format(
                                                        offsetDateTime.toInstant().toEpochMilli()
                                                    )
                                                }"

                                            else ->
                                                SimpleDateFormat(
                                                    "dd MMM yyyy, HH:mm",
                                                    Locale("ru")
                                                ).format(offsetDateTime.toInstant().toEpochMilli())
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                } ?: ""
                            },
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    formatDate: (String?) -> String?,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) colorScheme.surface else colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            NotificationType.NewEvent -> colorScheme.primaryContainer
                            NotificationType.NewMessage -> colorScheme.secondaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        when (notification.type) {
                            NotificationType.NewEvent -> R.drawable.ic_event64
                            NotificationType.NewMessage -> R.drawable.ic_chat64
                        }
                    ),
                    contentDescription = notification.type.name,
                    tint = when (notification.type) {
                        NotificationType.NewEvent -> colorScheme.onPrimaryContainer
                        NotificationType.NewMessage -> colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.content,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(notification.createdAt) ?: "",
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary)
                )
            }
        }
    }
}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NotificationsScreen(
//    navController: NavController,
//    viewModel: NotificationsViewModel = koinViewModel()
//) {
//    val state = viewModel.container.stateFlow.collectAsState().value
//    val colorScheme = MaterialTheme.colorScheme
//    val snackbarHostState = remember { SnackbarHostState() }
//    val coroutineScope = rememberCoroutineScope()
//    val pullRefreshState = rememberPullToRefreshState()
//
//    viewModel.collectSideEffect { sideEffect ->
//        when (sideEffect) {
//            is FeedSideEffect.NavigateToEventDetails -> {
//                navController.navigate(EventDetails.createRoute(sideEffect.eventId))
//            }
//
//            is FeedSideEffect.NavigateToChat -> {
//                navController.navigate("chat/${sideEffect.chatId}")
//            }
//
//            is FeedSideEffect.NavigateToNotifications -> {
//                navController.navigate("notifications")
//            }
//
//            is FeedSideEffect.ShowError -> {
//                coroutineScope.launch {
//                    snackbarHostState.showSnackbar(sideEffect.message)
//                }
//            }
//
//            else -> {}
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                expandedHeight = 56.dp,
//                title = {
//                    Text(
//                        text = "Уведомления",
//                        fontFamily = montserratFont,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = colorScheme.onBackground
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = colorScheme.onBackground
//                        )
//                    }
//                },
//                actions = {
//                    // Кнопка "Прочесть всё" с текстовой меткой
//                    if (state.notifications.any { !it.isRead }) {
//                        TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(4.dp)
//                            ) {
//                                Text(
//                                    text = "Прочесть всё",
//                                    fontFamily = montserratFont,
//                                    fontSize = 14.sp,
//                                    color = colorScheme.primary
//                                )
//                                Icon(
//                                    painter = painterResource(id = R.drawable.ic_done),
//                                    contentDescription = "Mark all as read",
//                                    tint = colorScheme.primary
//                                )
//
//                            }
//                        }
//                    }
//                    // Кнопка обновления
//                    IconButton(onClick = { viewModel.loadNotifications() }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_refresh64),
//                            contentDescription = "Refresh notifications",
//                            tint = colorScheme.onBackground
//                        )
//                    }
//
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = colorScheme.background
//                )
//            )
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { paddingValues ->
//        PullToRefreshBox(
//            state = pullRefreshState,
//            isRefreshing = state.isLoadingNotifications,
//            onRefresh = { viewModel.loadNotifications() }
//        ) {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp)
//                    .padding(paddingValues),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                if (state.isLoadingNotifications) {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator()
//                        }
//                    }
////                } else if (state.notificationsError != null) {
////                    item {
////                        Text(
////                            text = "Ошибка: ${state.notificationsError}",
////                            color = colorScheme.error,
////                            modifier = Modifier.padding(16.dp)
////                        )
////                    }
//                } else if (state.notificationsError != null) {
//                    item {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
////                                text = "Ошибка: ${state.notificationsError}",
//                                text = "Произошла техническая ошибка. Просим прощения!",
//                                color = colorScheme.error
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Button(onClick = { viewModel.loadNotifications() }) {
//                                Text("Попробовать снова")
//                            }
//                        }
//                    }
//                } else if (state.notifications.isEmpty()) {
//                    item {
//                        Text(
//                            text = "Уведомления отсутствуют",
//                            color = colorScheme.onSurface,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                } else {
//                    items(state.notifications) { notification ->
//                        NotificationCard(
//                            notification = notification,
//                            onClick = { viewModel.onNotificationClick(notification) },
//                            formatDate = { isoDate ->
//                                isoDate?.let {
//                                    try {
//                                        val offsetDateTime = OffsetDateTime.parse(it)
//                                        val zoneId = java.time.ZoneId.systemDefault()
//                                        val now = OffsetDateTime.now(zoneId)
//                                        when {
//                                            offsetDateTime.toLocalDate() == now.toLocalDate() ->
//                                                "Сегодня, ${
//                                                    SimpleDateFormat(
//                                                        "HH:mm",
//                                                        Locale("ru")
//                                                    ).format(
//                                                        offsetDateTime.toInstant().toEpochMilli()
//                                                    )
//                                                }"
//
//                                            offsetDateTime.toLocalDate() == now.toLocalDate()
//                                                .minusDays(1) ->
//                                                "Вчера, ${
//                                                    SimpleDateFormat(
//                                                        "HH:mm",
//                                                        Locale("ru")
//                                                    ).format(
//                                                        offsetDateTime.toInstant().toEpochMilli()
//                                                    )
//                                                }"
//
//                                            else ->
//                                                SimpleDateFormat(
//                                                    "dd MMM yyyy, HH:mm",
//                                                    Locale("ru")
//                                                ).format(offsetDateTime.toInstant().toEpochMilli())
//                                        }
//                                    } catch (e: Exception) {
//                                        null
//                                    }
//                                } ?: ""
//                            },
//                            colorScheme = colorScheme
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NotificationCard(
//    notification: Notification,
//    onClick: () -> Unit,
//    formatDate: (String?) -> String?,
//    colorScheme: ColorScheme
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() },
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (notification.isRead) colorScheme.surface else colorScheme.surfaceVariant
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape)
//                    .background(
//                        when (notification.type) {
//                            NotificationType.NewEvent -> colorScheme.primaryContainer
//                            NotificationType.NewMessage -> colorScheme.secondaryContainer
//                        }
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    painter = painterResource(
//                        when (notification.type) {
//                            NotificationType.NewEvent -> R.drawable.ic_event64
//                            NotificationType.NewMessage -> R.drawable.ic_chat64
//                        }
//                    ),
//                    contentDescription = notification.type.name,
//                    tint = when (notification.type) {
//                        NotificationType.NewEvent -> colorScheme.onPrimaryContainer
//                        NotificationType.NewMessage -> colorScheme.onSecondaryContainer
//                    },
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//            Spacer(modifier = Modifier.width(12.dp))
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = notification.content,
//                    fontFamily = montserratFont,
//                    fontSize = 14.sp,
//                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
//                    color = colorScheme.onSurface,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = formatDate(notification.createdAt) ?: "",
//                    fontFamily = montserratFont,
//                    fontSize = 12.sp,
//                    color = colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//            if (!notification.isRead) {
//                Box(
//                    modifier = Modifier
//                        .size(8.dp)
//                        .clip(CircleShape)
//                        .background(colorScheme.primary)
//                )
//            }
//        }
//    }
//}