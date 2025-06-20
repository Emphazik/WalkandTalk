package ru.walkAndTalk.ui.screens.main.feed.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Notification
import ru.walkAndTalk.domain.model.NotificationType
import ru.walkAndTalk.ui.screens.EventDetails
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import ru.walkAndTalk.ui.theme.montserratFont
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Locale
import kotlin.math.exp

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
    val lazyListState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Уведомления",
                        fontFamily = montserratFont,
                        fontSize = 22.sp,
                        color = colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { viewModel.loadNotifications() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh64),
                        contentDescription = "Refresh notifications",
                        tint = colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            PullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = state.isLoadingNotifications,
                onRefresh = { viewModel.loadNotifications() }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = lazyListState
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
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                                    text = "Произошла техническая ошибка. Просим прощения!",
                                    color = colorScheme.error,
                                    fontFamily = montserratFont,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.loadNotifications() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Попробовать снова",
                                        fontSize = 14.sp,
                                        fontFamily = montserratFont
                                    )
                                }
                            }
                        }
                    } else if (state.notifications.isEmpty()) {
                        item {
                            Text(
                                text = "Уведомления отсутствуют",
                                color = colorScheme.onSurface,
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(state.notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { viewModel.onNotificationClick(notification) },
                                formatDate = { isoDate -> formatDateTime(isoDate) },
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                },
                shape = CircleShape,
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top"
                )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) colorScheme.surface else colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    maxLines = 3,
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

fun formatDateTime(isoDate: String?): String? {
    return isoDate?.let {
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
                        ).format(offsetDateTime.toInstant().toEpochMilli())
                    }"
                offsetDateTime.toLocalDate() == now.toLocalDate().minusDays(1) ->
                    "Вчера, ${
                        SimpleDateFormat(
                            "HH:mm",
                            Locale("ru")
                        ).format(offsetDateTime.toInstant().toEpochMilli())
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
    }
}