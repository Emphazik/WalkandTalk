import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.ui.screens.main.feed.SearchBar
import ru.walkAndTalk.ui.screens.main.search.SearchSideEffect
import ru.walkAndTalk.ui.screens.main.search.SearchViewModel
import ru.walkAndTalk.ui.theme.montserratFont

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    navController: NavController
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(horizontal = 16.dp)
//                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Поиск людей",
                    fontFamily = montserratFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                IconButton(
                    onClick = { viewModel.refreshUsers() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh64),
                        contentDescription = "Refresh",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            UserSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onClearQuery = { viewModel.onClearQuery() },
                onSortSelected = { viewModel.onSortSelected(it) },
                colorScheme = colorScheme
            )

            if (state.isLoading) {
                Text(
                    text = "Загрузка...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            } else if (state.error != null) {
                Text(
                    text = "Ошибка: ${state.error}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else if (state.users.isEmpty() && state.searchQuery.isNotEmpty()) {
                Text(
                    text = "Ничего не найдено",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            } else if (state.users.isEmpty()) {
                Text(
                    text = "Пользователи не найдены",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.users) { user ->
                        UserCard(
                            user = user,
                            viewModel = viewModel,
                            interestNames = state.interestNames
                        )
                    }
                }
            }
        }
    }


    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.NavigateToProfile -> {
                navController.navigate("profile/${sideEffect.userId}")            }

            is SearchSideEffect.NavigateToMessage -> {
                // Навигация к чату
            }

            is SearchSideEffect.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, viewModel: SearchViewModel, interestNames: Map<String, String>) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1-я колонка: Аватарка
            Image(
                painter = user.profileImageUrl.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.preview_profile),
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // 2-я колонка: Имя, город, интересы
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = user.city ?: "Город не указан",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (user.interestIds.isEmpty()) {
                    Text(
                        text = "Не указаны интересы",
                        fontFamily = montserratFont,
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        user.interestIds.forEach { interestId ->
                            val interestName = interestNames[interestId] ?: interestId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.primaryContainer,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = interestName,
                                    fontFamily = montserratFont,
                                    fontSize = 12.sp,
                                    color = colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3-я колонка: Иконки
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { viewModel.onUserClick(user.id) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile64),
                        contentDescription = "Перейти к профилю",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.onMessageClick(user.id) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat64),
                        contentDescription = "Написать сообщение",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSortSelected: (UserSortType) -> Unit,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSortType by remember { mutableStateOf<UserSortType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search64),
                contentDescription = "Search",
                tint = colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Поиск по имени или #интересам",
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear128),
                    contentDescription = "Clear",
                    tint = colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClearQuery() }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                Text(
                    text = selectedSortType?.let {
                        when (it) {
                            UserSortType.NameAscending -> "По имени: А-Я"
                            UserSortType.NameDescending -> "По имени: Я-А"
                        }
                    } ?: "Сортировка",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .clickable { showSortMenu = true }
                        .padding(8.dp)
                )
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "По имени: А-Я",
                                fontFamily = montserratFont,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            selectedSortType = UserSortType.NameAscending
                            onSortSelected(UserSortType.NameAscending)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "По имени: Я-А",
                                fontFamily = montserratFont,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            selectedSortType = UserSortType.NameDescending
                            onSortSelected(UserSortType.NameDescending)
                            showSortMenu = false
                        }
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