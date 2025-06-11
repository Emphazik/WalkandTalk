package ru.walkAndTalk.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.R
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.ui.screens.Auth
import ru.walkAndTalk.ui.screens.Main
import ru.walkAndTalk.ui.theme.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    userId: String,
    viewModel: AdminViewModel = koinViewModel()
) {
    val state = viewModel.container.stateFlow.collectAsState().value
    val colorScheme = MaterialTheme.colorScheme
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is AdminSideEffect.NavigateToAuth -> {
                    navController.navigate(Auth) { // Используем типизированный маршрут
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is AdminSideEffect.NavigateToMain -> {
                    navController.navigate(Main(sideEffect.userId)) {
                        popUpTo("Admin/$userId") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is AdminSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
                else -> Unit
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Админ-панель",
                        fontFamily = montserratFont,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.switchToUserMode(userId) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_switch_user),
                            contentDescription = "Switch to user mode",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    TextButton(onClick = { showLogoutDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Выйти",
                                fontFamily = montserratFont,
                                fontSize = 16.sp,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_leave64),
                                contentDescription = "Logout",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Пользователи",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(state.users) { user ->
                UserCard(
                    user = user,
                    onClick = { viewModel.onUserClick(user.id) },
                    onDelete = { viewModel.deleteUser(user.id) }
                )
            }
            item {
                Text(
                    text = "Мероприятия",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(state.events) { event ->
                EventCard(
                    event = event,
                    onClick = { viewModel.onEventClick(event.id) },
                    onStatusChange = { status -> viewModel.updateEventStatus(event.id, status) }
                )
            }
            item {
                Text(
                    text = "Объявления",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(state.announcements) { announcement ->
                AnnouncementCard(
                    announcement = announcement,
                    onClick = { viewModel.onAnnouncementClick(announcement.id) },
                    onStatusChange = { status -> viewModel.updateAnnouncementStatus(announcement.id, status) }
                )
            }
        }
    }
    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Подтверждение выхода",
                    fontFamily = montserratFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите выйти из аккаунта?",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.onLogout()
                    }
                ) {
                    Text(
                        text = "Да",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "Нет",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}


@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.email,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete64),
                    contentDescription = "Delete user",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "approved", "rejected", "reported")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                fontFamily = montserratFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Статус: ${event.status}",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { onStatusChange("approved") }) {
                    Text("Одобрить")
                }
                Button(onClick = { onStatusChange("rejected") }) {
                    Text("Отклонить")
                }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text("Выбрать статус")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "approved", "rejected", "reported")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = announcement.title,
                fontFamily = montserratFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Статус: ${announcement.status}",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { onStatusChange("approved") }) {
                    Text("Одобрить")
                }
                Button(onClick = { onStatusChange("rejected") }) {
                    Text("Отклонить")
                }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text("Выбрать статус")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}