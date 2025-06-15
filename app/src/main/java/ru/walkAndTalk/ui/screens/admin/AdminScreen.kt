package ru.walkAndTalk.ui.screens.admin

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.ui.screens.AddUser
import ru.walkAndTalk.ui.screens.Auth
import ru.walkAndTalk.ui.screens.Main
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.theme.montserratFont

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminScreen(
//    userId: String,
//    viewModel: AdminViewModel = koinViewModel()
//) {
//    val state by viewModel.container.stateFlow.collectAsState()
//    val colorScheme = MaterialTheme.colorScheme
//    var showLogoutDialog by remember { mutableStateOf(false) }
//    var showSwitchUserDialog by remember { mutableStateOf(false) }
//    val snackbarHostState = remember { SnackbarHostState() }
//    var selectedTab by remember { mutableStateOf(AdminTab.Users) }
//    var searchQuery by remember { mutableStateOf("") }
//    val focusRequester = remember { FocusRequester() }
//
//    LaunchedEffect(Unit) {
//        viewModel.container.sideEffectFlow.collect { sideEffect ->
//            when (sideEffect) {
//                is AdminSideEffect.NavigateToAuth -> {}
//                is AdminSideEffect.NavigateToMain -> {}
//                is AdminSideEffect.NavigateToAddUser -> {}
//                is AdminSideEffect.NavigateToEditUser -> {}
//                is AdminSideEffect.NavigateToProfile -> {}
//                is AdminSideEffect.ShowError -> {
//                    snackbarHostState.showSnackbar(sideEffect.message)
//                }
//                else -> Unit
//            }
//        }
//    }
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        topBar = {
//            TopAppBar(
//                expandedHeight = 56.dp,
//                title = {
//                    Text(
//                        text = "Админ-панель",
//                        fontFamily = montserratFont,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = colorScheme.onBackground
//                    )
//                },
//                actions = {
//                    IconButton(onClick = { showSwitchUserDialog = true }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_switch_user),
//                            contentDescription = "Switch to user mode",
//                            tint = colorScheme.primary,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                    IconButton(onClick = { showLogoutDialog = true }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_leave64),
//                            contentDescription = "Logout",
//                            tint = colorScheme.error,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = colorScheme.surface,
//                    titleContentColor = colorScheme.onSurface
//                )
//            )
//        },
//        floatingActionButton = {
//            if (selectedTab == AdminTab.Users) {
//                FloatingActionButton(
//                    onClick = { viewModel.navigateToAddUser() },
//                    containerColor = colorScheme.primary,
//                    contentColor = colorScheme.onPrimary,
//                    shape = CircleShape,
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .size(56.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Add user",
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(colorScheme.background)
//        ) {
//            // Search Bar
//            TextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp)
//                    .padding(horizontal = 16.dp, vertical = 8.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(colorScheme.surface)
//                    .border(1.dp, colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
//                    .focusRequester(focusRequester),
//                placeholder = {
//                    Text(
//                        text = "Поиск по имени или email...",
//                        fontFamily = montserratFont,
//                        fontSize = 14.sp,
//                        color = colorScheme.onSurface.copy(alpha = 0.5f)
//                    )
//                },
//                leadingIcon = {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_search64),
//                        contentDescription = "Search",
//                        tint = colorScheme.onSurface.copy(alpha = 0.5f),
//                        modifier = Modifier.size(20.dp)
//                    )
//                },
//                trailingIcon = {
//                    if (searchQuery.isNotEmpty()) {
//                        IconButton(onClick = { searchQuery = "" }) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.ic_clear128),
//                                contentDescription = "Clear",
//                                tint = colorScheme.onSurface.copy(alpha = 0.5f),
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                    }
//                },
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = colorScheme.surface,
//                    unfocusedContainerColor = colorScheme.surface,
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedTextColor = colorScheme.onSurface,
//                    unfocusedTextColor = colorScheme.onSurface
//                ),
//                textStyle = LocalTextStyle.current.copy(
//                    fontFamily = montserratFont,
//                    fontSize = 14.sp
//                ),
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//                keyboardActions = KeyboardActions(onSearch = { /* Optional: Handle search action */ })
//            )
//
//            // Tabs
//            TabRow(
//                selectedTabIndex = selectedTab.ordinal,
//                containerColor = colorScheme.surface,
//                contentColor = colorScheme.primary,
//                divider = { Divider(color = colorScheme.primary.copy(alpha = 0.2f)) }
//            ) {
//                AdminTab.values().forEach { tab ->
//                    Tab(
//                        selected = selectedTab == tab,
//                        onClick = { selectedTab = tab },
//                        modifier = Modifier
//                            .padding(vertical = 4.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(
//                                if (selectedTab == tab) colorScheme.primary.copy(alpha = 0.1f)
//                                else Color.Transparent
//                            ),
//                        text = {
//                            Text(
//                                text = tab.title,
//                                fontFamily = montserratFont,
//                                fontSize = 14.sp,
//                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
//                                color = if (selectedTab == tab) colorScheme.primary else colorScheme.onSurface
//                            )
//                        }
//                    )
//                }
//            }
//
//            // Content
//            AnimatedVisibility(
//                visible = state.isLoading,
//                enter = fadeIn(),
//                exit = fadeOut()
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            AnimatedVisibility(
//                visible = !state.isLoading,
//                enter = fadeIn(),
//                exit = fadeOut()
//            ) {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 16.dp),
//                    verticalArrangement = Arrangement.spacedBy(12.dp),
//                    contentPadding = PaddingValues(vertical = 16.dp)
//                ) {
//                    when (selectedTab) {
//                        AdminTab.Users -> {
//                            val filteredUsers = state.users.filter {
//                                it.name.contains(searchQuery, ignoreCase = true) ||
//                                        it.email.contains(searchQuery, ignoreCase = true)
//                            }
//                            items(filteredUsers) { user ->
//                                UserCard(
//                                    user = user,
//                                    onClick = { viewModel.onUserClick(user.id) },
//                                    onDelete = { viewModel.deleteUser(user.id) },
//                                    onEdit = { viewModel.navigateToEditUser(user.id) },
//                                    onProfile = { viewModel.navigateToProfile(user.id) }
//                                )
//                            }
//                        }
//
//                        AdminTab.Events -> {
//                            val filteredEvents = state.events.filter {
//                                it.title.contains(searchQuery, ignoreCase = true) ||
//                                        it.description.contains(searchQuery, ignoreCase = true)
//                            }
//                            items(filteredEvents) { event ->
//                                EventCard(
//                                    event = event,
//                                    onClick = { viewModel.onEventClick(event.id) },
//                                    onStatusChange = { status ->
//                                        viewModel.updateEventStatus(
//                                            event.id,
//                                            status
//                                        )
//                                    }
//                                )
//                            }
//                        }
//
//                        AdminTab.Announcements -> {
//                            val filteredAnnouncements = state.announcements.filter {
//                                it.title.contains(searchQuery, ignoreCase = true) ||
//                                        it.description?.contains(
//                                            searchQuery,
//                                            ignoreCase = true
//                                        ) == true
//                            }
//                            items(filteredAnnouncements) { announcement ->
//                                AnnouncementCard(
//                                    announcement = announcement,
//                                    onClick = { viewModel.onAnnouncementClick(announcement.id) },
//                                    onStatusChange = { status ->
//                                        viewModel.updateAnnouncementStatus(
//                                            announcement.id,
//                                            status
//                                        )
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//    if (showLogoutDialog) {
//        AlertDialog(
//            onDismissRequest = { showLogoutDialog = false },
//            title = {
//                Text(
//                    text = "Подтверждение выхода",
//                    fontFamily = montserratFont,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            },
//            text = {
//                Text(
//                    text = "Вы уверены, что хотите выйти из аккаунта?",
//                    fontFamily = montserratFont,
//                    fontSize = 16.sp,
//                    color = colorScheme.onSurface.copy(alpha = 0.8f)
//                )
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    showLogoutDialog = false
//                    viewModel.onLogout()
//                }) {
//                    Text(
//                        text = "Выйти",
//                        fontFamily = montserratFont,
//                        color = colorScheme.error
//                    )
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showLogoutDialog = false }) {
//                    Text(
//                        text = "Отмена",
//                        fontFamily = montserratFont,
//                        color = colorScheme.primary
//                    )
//                }
//            },
//            containerColor = colorScheme.surface,
//            shape = RoundedCornerShape(16.dp)
//        )
//    }
//    if (showSwitchUserDialog) {
//        AlertDialog(
//            onDismissRequest = { showSwitchUserDialog = false },
//            title = {
//                Text(
//                    text = "Подтверждение перехода",
//                    fontFamily = montserratFont,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            },
//            text = {
//                Text(
//                    text = "Вы уверены, что хотите перейти в пользовательский режим?",
//                    fontFamily = montserratFont,
//                    fontSize = 16.sp,
//                    color = colorScheme.onSurface.copy(alpha = 0.8f)
//                )
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    showSwitchUserDialog = false
//                    viewModel.switchToUserMode(userId)
//                }) {
//                    Text(
//                        text = "Перейти",
//                        fontFamily = montserratFont,
//                        color = colorScheme.primary
//                    )
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showSwitchUserDialog = false }) {
//                    Text(
//                        text = "Отмена",
//                        fontFamily = montserratFont,
//                        color = colorScheme.error
//                    )
//                }
//            },
//            containerColor = colorScheme.surface,
//            shape = RoundedCornerShape(16.dp)
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userId: String,
    viewModel: AdminViewModel = koinViewModel()
) {
    val state by viewModel.container.stateFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSwitchUserDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(AdminTab.Users) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Логирование для диагностики
    LaunchedEffect(state.users) {
        Log.d("AdminScreen", "Users updated: ${state.users.size} users")
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
            // Search Bar
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
                        text = "Поиск по имени или email...",
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
                keyboardActions = KeyboardActions(onSearch = { /* Optional: Handle search action */ })
            )

            // Tabs
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

            // Content
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
                            items(filteredUsers.size) { index ->
                                UserCard(
                                    user = filteredUsers[index],
                                    onClick = { viewModel.onUserClick(filteredUsers[index].id) },
                                    onDelete = { viewModel.deleteUser(filteredUsers[index].id) },
                                    onEdit = { viewModel.navigateToEditUser(filteredUsers[index].id) },
                                    onProfile = { viewModel.navigateToProfile(filteredUsers[index].id) }
                                )
                            }
                        }

                        AdminTab.Events -> {
                            val filteredEvents = state.events.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.description.contains(searchQuery, ignoreCase = true)
                            }
                            items(filteredEvents.size) { index ->
                                EventCard(
                                    event = filteredEvents[index],
                                    onClick = { viewModel.onEventClick(filteredEvents[index].id) },
                                    onStatusChange = { status ->
                                        viewModel.updateEventStatus(
                                            filteredEvents[index].id,
                                            status
                                        )
                                    }
                                )
                            }
                        }

                        AdminTab.Announcements -> {
                            val filteredAnnouncements = state.announcements.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.description?.contains(
                                            searchQuery,
                                            ignoreCase = true
                                        ) == true
                            }
                            items(filteredAnnouncements.size) { index ->
                                AnnouncementCard(
                                    announcement = filteredAnnouncements[index],
                                    onClick = { viewModel.onAnnouncementClick(filteredAnnouncements[index].id) },
                                    onStatusChange = { status ->
                                        viewModel.updateAnnouncementStatus(
                                            filteredAnnouncements[index].id,
                                            status
                                        )
                                    }
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
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "approved", "rejected", "reported")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.title,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = event.statusId)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.description,
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onStatusChange("approved") },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Одобрить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp
                    )
                }
                OutlinedButton(
                    onClick = { onStatusChange("rejected") },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Отклонить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp
                    )
                }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Статус",
                            fontFamily = montserratFont,
                            fontSize = 14.sp
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status.replaceFirstChar { it.uppercase() },
                                        fontFamily = montserratFont
                                    )
                                },
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
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "approved", "rejected", "reported")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = announcement.title,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = announcement.statusId)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = announcement.description ?: "Без описания",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onStatusChange("approved") },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Одобрить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp
                    )
                }
                OutlinedButton(
                    onClick = { onStatusChange("rejected") },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Отклонить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp
                    )
                }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Статус",
                            fontFamily = montserratFont,
                            fontSize = 14.sp
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status.replaceFirstChar { it.uppercase() },
                                        fontFamily = montserratFont
                                    )
                                },
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
fun StatusChip(status: String) {
    val colorScheme = MaterialTheme.colorScheme
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "approved" -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
        "rejected" -> colorScheme.errorContainer to colorScheme.onErrorContainer
        "pending" -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
        "reported" -> Color(0xFFFFB300) to Color.Black
        else -> colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier
            .height(24.dp)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            fontFamily = montserratFont,
            fontSize = 12.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class AdminTab(val title: String) {
    Users("Пользователи"),
    Events("Мероприятия"),
    Announcements("Объявления")
}