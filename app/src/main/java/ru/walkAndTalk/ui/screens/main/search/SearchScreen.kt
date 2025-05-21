import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
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
fun SearchScreen(viewModel: SearchViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.NavigateToProfile -> {
                // Здесь можно добавить навигацию к профилю пользователя
            }
            else -> {
                // Делегируем остальные случаи MainScreen
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            onClearQuery = { viewModel.onClearQuery() },
            onSortSelected = { viewModel.onSortSelected(it as UserSortType) },
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
        } else {
            LazyColumn {
                items(state.users) { user ->
                    UserCard(user = user, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, viewModel: SearchViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { viewModel.onUserClick(user.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = user.profileImageUrl?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.preview_profile),
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.name,
                fontFamily = montserratFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            user.bio?.let { bio ->
                Text(
                    text = bio,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.onUserClick(user.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Перейти к профилю",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}
enum class UserSortType {
    NameAscending,
    NameDescending
}