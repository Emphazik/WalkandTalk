import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.ui.screens.Profile
import ru.walkAndTalk.ui.screens.main.search.SearchSideEffect
import ru.walkAndTalk.ui.screens.main.search.SearchViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    userId: String,
    navController: NavController,
    viewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.NavigateToProfile -> {
                navController.navigate(Profile(sideEffect.userId, viewOnly = true))
            }
            is SearchSideEffect.NavigateToMessage -> {
                navController.navigate("chat/${sideEffect.chatId}")
            }
            is SearchSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleSearch() },
                        modifier = Modifier.size(48.dp) 
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Закрыть поиск",
                            tint = colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp), 
                        placeholder = {
                            Text(
                                text = "Поиск по имени или #интересам",
                                fontFamily = montserratFont,
                                fontSize = 12.sp, 
                                color = colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 14.sp, 
                            fontFamily = montserratFont,
                            color = colorScheme.onSurface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp), 
                        trailingIcon = {
                            var showSortMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_filter64),
                                        contentDescription = "Сортировка",
                                        tint = colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("По имени: А-Я", fontFamily = montserratFont, fontSize = 14.sp) },
                                        onClick = {
                                            viewModel.onSortSelected(UserSortType.NameAscending)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("По имени: Я-А", fontFamily = montserratFont, fontSize = 14.sp) },
                                        onClick = {
                                            viewModel.onSortSelected(UserSortType.NameDescending)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 24.dp), // Отступ слева 24.dp для выравнивания с карточками, top как в FeedScreen
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Поиск людей",
                        fontFamily = montserratFont,
                        fontSize = 24.sp, 
                        color = colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.toggleSearch() },
                        modifier = Modifier.size(48.dp) 
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refreshUsers() },
                        modifier = Modifier.size(48.dp) 
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh64),
                            contentDescription = "Обновить",
                            tint = colorScheme.onBackground
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp)) 
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
                    .padding(top = 8.dp) 
            ) {
                if (state.isSearchActive && state.searchQuery.isEmpty()) {
                    Text(
                        text = "Начните вводить для поиска...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = colorScheme.onSurface,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (state.isLoading) {
                    Text(
                        text = "Загрузка...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = colorScheme.onSurface,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (state.error != null) {
                    Text(
                        text = "Ошибка: ${state.error}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = colorScheme.error,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (state.users.isEmpty() && state.searchQuery.isNotEmpty()) {
                    Text(
                        text = "Ничего не найдено",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = colorScheme.onSurface,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (state.users.isEmpty()) {
                    Text(
                        text = "Пользователи не найдены",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = colorScheme.onSurface,
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp), 
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp) 
                    ) {
                        items(state.users.filter { it.id != userId && !it.isAdmin }) { user ->
                            UserCard(
                                user = user,
                                viewModel = viewModel,
                                interestNames = state.interestNames,
                                onMessageClick = { viewModel.onMessageClick(user.id) }
                            )
                        }
                    }
                }
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
fun UserCard(
    user: User,
    viewModel: SearchViewModel,
    interestNames: Map<String, String>,
    onMessageClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = user.profileImageUrl.let { rememberAsyncImagePainter(it) },
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = user.city ?: "Город не указан",
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (user.interestIds.isEmpty()) {
                    Text(
                        text = "Не указаны интересы",
                        fontFamily = montserratFont,
                        fontSize = 10.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        user.interestIds.forEach { interestId ->
                            val interestName = interestNames[interestId] ?: interestId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.primaryContainer,
                                modifier = Modifier.padding(vertical = 1.dp)
                            ) {
                                Text(
                                    text = interestName,
                                    fontFamily = montserratFont,
                                    fontSize = 10.sp,
                                    color = colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.onUserClick(user.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile64),
                        contentDescription = "Перейти к профилю",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMessageClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat64),
                        contentDescription = "Написать сообщение",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

enum class UserSortType {
    NameAscending,
    NameDescending
}