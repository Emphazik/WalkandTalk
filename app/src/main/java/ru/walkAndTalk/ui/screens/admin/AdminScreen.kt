package ru.walkAndTalk.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.ui.theme.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userId: String,
    onEventEditClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onAnnouncementEditClick: (String) -> Unit,
    onAnnouncementClick: (String) -> Unit,
    viewModel: AdminViewModel = koinViewModel()
) {
    val state by viewModel.container.stateFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSwitchUserDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(AdminTab.Users) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
        viewModel.loadData()
        viewModel.loadAnnouncements()
    }
    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
            when (sideEffect) {
                is AdminSideEffect.ShowError -> scope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
                is AdminSideEffect.ShowSuccess -> scope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
                is AdminSideEffect.NavigateToEditEvent -> onEventEditClick(sideEffect.eventId)
                is AdminSideEffect.NavigateToEditAnnouncement -> onAnnouncementEditClick(sideEffect.announcementId)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (data.visuals.message.contains("Ошибка", ignoreCase = true))
                        colorScheme.errorContainer else colorScheme.primaryContainer,
                    contentColor = if (data.visuals.message.contains("Ошибка", ignoreCase = true))
                        colorScheme.onErrorContainer else colorScheme.onPrimaryContainer,
                    actionColor = colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Админ-панель",
                        fontFamily = montserratFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { showSwitchUserDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_switch_user),
                            contentDescription = "Switch to user mode",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_leave64),
                            contentDescription = "Logout",
                            tint = colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == AdminTab.Users) {
                FloatingActionButton(
                    onClick = { viewModel.navigateToAddUser() },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add user",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = when (selectedTab) {
                            AdminTab.Users -> "Поиск по имени или email..."
                            AdminTab.Events -> "Поиск по названию или описанию..."
                            AdminTab.Announcements -> "Поиск по названию или описанию..."
                        },
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search64),
                        contentDescription = "Search",
                        tint = colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clear128),
                                contentDescription = "Clear",
                                tint = colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = montserratFont,
                    fontSize = 14.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {})
            )

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = colorScheme.surface,
                contentColor = colorScheme.primary,
                divider = { Divider(color = colorScheme.primary.copy(alpha = 0.2f)) }
            ) {
                AdminTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedTab == tab) colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            ),
                        text = {
                            Text(
                                text = tab.title,
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == tab) colorScheme.primary else colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    when (selectedTab) {
                        AdminTab.Users -> {
                            val filteredUsers = state.users.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.email.contains(searchQuery, ignoreCase = true)
                            }
                            items(filteredUsers) { user ->
                                UserCard(
                                    user = user,
                                    onClick = { viewModel.onUserClick(user.id) },
                                    onDelete = { viewModel.deleteUser(user.id) },
                                    onEdit = { viewModel.navigateToEditUser(user.id) },
                                    onProfile = { viewModel.navigateToProfile(user.id) }
                                )
                            }
                        }

                        AdminTab.Events -> {
                            val filteredEvents = state.events.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.description?.contains(searchQuery, ignoreCase = true) == true
                            }
                            if (filteredEvents.isEmpty()) {
                                item {
                                    Text(
                                        text = "Нет мероприятий",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            }
                            items(filteredEvents) { event ->
                                AdminEventCard(
                                    event = event,
                                    onClick = { viewModel.navigateToEventDetails(event.id) },
                                    onApprove = { viewModel.approveEvent(event.id) },
                                    onReject = { viewModel.rejectEvent(event.id) },
                                    onEdit = { viewModel.navigateToEditEvent(event.id) },
                                    onDelete = { viewModel.deleteEventAdmin(event.id) },
                                    onChangeStatus = { newStatus ->
                                        viewModel.changeEventStatus(event.id, newStatus)
                                    },
                                    formatEventDate = { isoDate -> viewModel.formatEventDate(isoDate) }
                                )
                            }
                        }

                        AdminTab.Announcements -> {
                            val filteredAnnouncements = state.announcements.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.description?.contains(searchQuery, ignoreCase = true) == true
                            }
                            if (filteredAnnouncements.isEmpty()) {
                                item {
                                    Text(
                                        text = "Нет объявлений",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            }
                            items(filteredAnnouncements) { announcement ->
                                AdminAnnouncementCard(
                                    announcement = announcement,
                                    onApprove = { viewModel.changeAnnouncementStatus(announcement.id, "approved") },
                                    onReject = { viewModel.changeAnnouncementStatus(announcement.id, "rejected") },
                                    onEdit = { viewModel.navigateToEditAnnouncement(announcement.id) },
                                    onDelete = { viewModel.deleteAnnouncementAdmin(announcement.id) },
                                    onChangeStatus = { status ->
                                        viewModel.changeAnnouncementStatus(announcement.id, status)
                                    },
                                    onClick = { viewModel.navigateToAnnouncementDetails(announcement.id) },
                                    formatEventDate = { isoDate -> viewModel.formatEventDate(isoDate) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Подтверждение выхода",
                    fontFamily = montserratFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.onLogout()
                }) {
                    Text(
                        text = "Выйти",
                        fontFamily = montserratFont,
                        color = colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Отмена",
                        fontFamily = montserratFont,
                        color = colorScheme.primary
                    )
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSwitchUserDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchUserDialog = false },
            title = {
                Text(
                    text = "Подтверждение перехода",
                    fontFamily = montserratFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите перейти в пользовательский режим?",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSwitchUserDialog = false
                    viewModel.switchToUserMode(userId)
                }) {
                    Text(
                        text = "Перейти",
                        fontFamily = montserratFont,
                        color = colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchUserDialog = false }) {
                    Text(
                        text = "Отмена",
                        fontFamily = montserratFont,
                        color = colorScheme.error
                    )
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AdminEventCard(
    event: Event,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatus: ((String) -> Unit)? = null,
    onClick: () -> Unit,
    formatEventDate: (String?) -> String?
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isPending = event.statusId == "ac32ffcc-8f69-4b71-8a39-877c1eb95e04"
    val statusMenuAnchor = remember { mutableStateOf(Offset(0f, 0f)) }
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
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
                    text = formatEventDate(event.eventDate) ?: event.eventDate,
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
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (event.statusId) {
                        "ac32ffcc-8f69-4b71-8a39-877c1eb95e04" -> "На модерации"
                        "7513755c-ce44-459f-a071-07080e90f450" -> "Одобрено"
                        "a9a0a4ec-b0b3-4684-ae6f-d576c52b9926" -> "Отклонено" // Замените на актуальный UUID
                        else -> "Неизвестный статус"
                    },
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = when (event.statusId) {
                        "ac32ffcc-8f69-4b71-8a39-877c1eb95e04" -> colorScheme.secondary
                        "7513755c-ce44-459f-a071-07080e90f450" -> colorScheme.primary
                        "a9a0a4ec-b0b3-4684-ae6f-d576c52b9926" -> colorScheme.error
                        else -> colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                event.organizerName?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Организатор: $it",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Место",
                        tint = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPending) {
                        OutlinedButton(
                            onClick = { onApprove?.invoke() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.primary)
                        ) {
                            Text(
                                text = "Одобрить",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { onReject?.invoke() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.error)
                        ) {
                            Text(
                                text = "Отклонить",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.primary)
                        ) {
                            Text(
                                text = "Редактировать",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.error)
                        ) {
                            Text(
                                text = "Удалить",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit64),
                            contentDescription = "Редактировать",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete64),
                            contentDescription = "Удалить",
                            tint = colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showStatusMenu = true },
                            modifier = Modifier
                                .size(24.dp)
                                .onGloballyPositioned { coordinates ->
                                    statusMenuAnchor.value = Offset(
                                        x = coordinates.positionInParent().x,
                                        y = coordinates.positionInParent().y + coordinates.size.height
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Изменить статус",
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (showStatusMenu && onChangeStatus != null) {
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false },
                                modifier = Modifier
                                    .background(colorScheme.surface)
                                    .offset {
                                        IntOffset(
                                            statusMenuAnchor.value.x.toInt(),
                                            statusMenuAnchor.value.y.toInt()
                                        )
                                    }
                            ) {
                                listOf(
                                    "pending" to "ac32ffcc-8f69-4b71-8a39-877c1eb95e04",
                                    "approved" to "7513755c-ce44-459f-a071-07080e90f450",
                                    "rejected" to "a9a0a4ec-b0b3-4684-ae6f-d576c52b9926"
                                ).forEach { (status, statusId) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = when (status) {
                                                        "pending" -> "На модерации"
                                                        "approved" -> "Одобрено"
                                                        "rejected" -> "Отклонено"
                                                        else -> status
                                                    },
                                                    fontFamily = montserratFont,
                                                    fontSize = 14.sp,
                                                    color = if (event.statusId == statusId) colorScheme.primary else colorScheme.onSurface
                                                )
                                                if (event.statusId == statusId) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onChangeStatus(status)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Подтверждение удаления") },
            text = { Text("Вы уверены, что хотите удалить мероприятие '${event.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onProfile: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
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
                    .background(colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    color = colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row {
                IconButton(onClick = onProfile) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile64),
                        contentDescription = "View profile",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit64),
                        contentDescription = "Edit user",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete64),
                        contentDescription = "Delete user",
                        tint = colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить пользователя",
                    fontFamily = montserratFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить пользователя ${user.name}?",
                    fontFamily = montserratFont,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(
                        text = "Удалить",
                        fontFamily = montserratFont,
                        color = colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Отмена",
                        fontFamily = montserratFont,
                        color = colorScheme.primary
                    )
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AdminAnnouncementCard(
    announcement: Announcement,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatus: (String) -> Unit,
    onClick: () -> Unit, // Для перехода на AnnouncementDetailsScreen
    formatEventDate: (String?) -> String? // Для совместимости с AdminEventCard
) {
    val colorScheme = MaterialTheme.colorScheme
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isPending = announcement.statusId == "ac32ffcc-8f69-4b71-8a39-877c1eb95e04"
    val statusMenuAnchor = remember { mutableStateOf(Offset(0f, 0f)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }, // Клик для перехода на детали
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            announcement.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(
                        model = url,
                        error = painterResource(id = R.drawable.default_event_image)
                    ),
                    contentDescription = announcement.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = announcement.title,
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (announcement.statusId) {
                        "ac32ffcc-8f69-4b71-8a39-877c1eb95e04" -> "На модерации"
                        "7513755c-ce44-459f-a071-07080e90f450" -> "Одобрено"
                        "a9a0a4ec-b0b3-4684-ae6f-d576c52b9926" -> "Отклонено"
                        "3c8536f0-753c-4d51-9106-c56d5e0ec7ff" -> "Жалоба" // Замените на актуальный UUID
                        else -> "Неизвестный статус"
                    },
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = when (announcement.statusId) {
                        "ac32ffcc-8f69-4b71-8a39-877c1eb95e04" -> colorScheme.secondary
                        "7513755c-ce44-459f-a071-07080e90f450" -> colorScheme.primary
                        "a9a0a4ec-b0b3-4684-ae6f-d576c52b9926" -> colorScheme.error
                        "3c8536f0-753c-4d51-9106-c56d5e0ec7ff" -> colorScheme.error
                        else -> colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                announcement.description?.let {
                    Text(
                        text = it,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                announcement.location?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "Место",
                            tint = colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it,
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPending) {
                        OutlinedButton(
                            onClick = { onApprove?.invoke() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.primary)
                        ) {
                            Text(
                                text = "Одобрить",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { onReject?.invoke() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.error)
                        ) {
                            Text(
                                text = "Отклонить",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.primary)
                        ) {
                            Text(
                                text = "Редактировать",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.error)
                        ) {
                            Text(
                                text = "Удалить",
                                fontFamily = montserratFont,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit64),
                            contentDescription = "Редактировать",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete64),
                            contentDescription = "Удалить",
                            tint = colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showStatusMenu = true },
                            modifier = Modifier
                                .size(24.dp)
                                .onGloballyPositioned { coordinates ->
                                    statusMenuAnchor.value = Offset(
                                        x = coordinates.positionInParent().x,
                                        y = coordinates.positionInParent().y + coordinates.size.height
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Изменить статус",
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (showStatusMenu) {
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false },
                                modifier = Modifier
                                    .background(colorScheme.surface)
                                    .offset {
                                        IntOffset(
                                            statusMenuAnchor.value.x.toInt(),
                                            statusMenuAnchor.value.y.toInt()
                                        )
                                    }
                            ) {
                                listOf(
                                    "pending" to "ac32ffcc-8f69-4b71-8a39-877c1eb95e04",
                                    "approved" to "7513755c-ce44-459f-a071-07080e90f450",
                                    "rejected" to "a9a0a4ec-b0b3-4684-ae6f-d576c52b9926",
                                    "reported" to "3c8536f0-753c-4d51-9106-c56d5e0ec7ff" // Замените на актуальный UUID
                                ).forEach { (status, statusId) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = when (status) {
                                                        "pending" -> "На модерации"
                                                        "approved" -> "Одобрено"
                                                        "rejected" -> "Отклонено"
                                                        "reported" -> "Жалоба"
                                                        else -> status
                                                    },
                                                    fontFamily = montserratFont,
                                                    fontSize = 14.sp,
                                                    color = if (announcement.statusId == statusId) colorScheme.primary else colorScheme.onSurface
                                                )
                                                if (announcement.statusId == statusId) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onChangeStatus(status)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Подтверждение удаления") },
            text = { Text("Вы уверены, что хотите удалить объявление '${announcement.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

enum class AdminTab(val title: String) {
    Users("Пользователи"),
    Events("Мероприятия"),
    Announcements("Объявления")
}