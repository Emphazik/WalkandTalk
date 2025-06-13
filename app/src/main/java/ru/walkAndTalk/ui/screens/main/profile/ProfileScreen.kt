package ru.walkAndTalk.ui.screens.main.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.rememberAsyncImagePainter
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.EventReview
import ru.walkAndTalk.ui.screens.main.montserratFont

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onNavigateAuth: () -> Unit,
    onNavigateEditProfile: () -> Unit,
    onNavigateEventStatistics: () -> Unit,
    isOwnProfile: Boolean = true,
    isViewOnly: Boolean = false,
    onBackToAdmin: () -> Unit = {}
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        viewModel.refreshData()
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.OnNavigateExit -> onNavigateAuth()
            is ProfileSideEffect.LaunchImagePicker -> {}
            is ProfileSideEffect.RequestLocationPermission -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = state.photoURL?.let { rememberAsyncImagePainter(it) }
                                ?: painterResource(id = R.drawable.ic_profile1),
                            contentScale = ContentScale.Crop,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorScheme.surface)
                                .border(
                                    2.dp,
                                    colorScheme.onSurface.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.name,
                            fontFamily = montserratFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Дата рождения: ${state.birthDate?.ifEmpty { "Не указано" } ?: "Не указано"}",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = "Location",
                                tint = colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Город: ${state.newCity.takeIf { it != "Не указано" } ?: "Город не указан"}",
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                color = colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Статус в городе:",
                                fontFamily = montserratFont,
                                fontSize = 16.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.selectedCityStatus?.ifEmpty { "Не указано" } ?: "Не указано",
                                fontFamily = montserratFont,
                                fontSize = 16.sp,
                                color = colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        BioSection(
                            bio = state.bio,
                            colorScheme = colorScheme
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        InterestSection(
                            interests = state.interests,
                            colorScheme = colorScheme
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GoalsSection(
                            goals = state.goals,
                            colorScheme = colorScheme
                        )
                    }
                }
            }
            if (state.showReviews && state.reviews.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Отзывы о мероприятиях",
                        fontFamily = montserratFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReviewsCard(
                        reviews = state.reviews,
                        events = state.events,
                        formatEventDate = viewModel::formatEventDate,
                        colorScheme = colorScheme
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (isViewOnly) onBackToAdmin() else navController.popBackStack() },
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = colorScheme.onSurface
                )
            }
            if (isOwnProfile) {
                Box {
                    var showEditMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showEditMenu = true },
                        modifier = Modifier
                            .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more128),
                            contentDescription = "Menu",
                            tint = colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showEditMenu,
                        onDismissRequest = { showEditMenu = false },
                        modifier = Modifier
                            .background(colorScheme.surface)
                            .align(Alignment.TopEnd)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit64),
                                        contentDescription = "Edit Profile",
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Редактировать профиль",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                showEditMenu = false
                                onNavigateEditProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_event64),
                                        contentDescription = "Event Stats",
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Статистика мероприятий",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                showEditMenu = false
                                onNavigateEventStatistics()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_leave64),
                                        contentDescription = "Logout",
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Выйти из аккаунта",
                                        fontFamily = montserratFont,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                showEditMenu = false
                                showLogoutDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showLogoutDialog && isOwnProfile) {
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
}
@Composable
fun BioSection(
    bio: String?,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outline, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "О себе",
                fontFamily = montserratFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bio?.ifEmpty { "О себе не указано" } ?: "О себе не указано",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun GoalsSection(
    goals: String?,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outline, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Цели",
                fontFamily = montserratFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = goals?.ifEmpty { "Цели не указаны" } ?: "Цели не указаны",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun InterestSection(
    interests: List<String>,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outline, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Интересы",
                fontFamily = montserratFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            val rows = interests.chunked(4)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowInterests ->
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rowInterests) { interest ->
                            InterestItem(
                                interest = interest,
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
                if (interests.isEmpty()) {
                    Text(
                        text = "Не указано",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun InterestItem(
    interest: String,
    colorScheme: ColorScheme
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(16.dp)
            )
            .wrapContentWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = interest,
            fontFamily = montserratFont,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            softWrap = false
        )
    }
}

//@Composable
//fun ReviewCard(
//    event: Event,
//    review: EventReview,
//    formatEventDate: (String?) -> String,
//    colorScheme: ColorScheme
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
//        shape = RoundedCornerShape(8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp)
//                    .clip(RoundedCornerShape(8.dp))
//            ) {
//                Image(
//                    painter = if (event.eventImageUrl.isNullOrBlank()) {
//                        painterResource(id = R.drawable.default_event_image)
//                    } else {
//                        rememberAsyncImagePainter(
//                            model = event.eventImageUrl,
//                            placeholder = painterResource(id = R.drawable.default_event_image),
//                            error = painterResource(id = R.drawable.default_event_image)
//                        )
//                    },
//                    contentDescription = "Event Image",
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop
//                )
//                Row(
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(8.dp)
//                        .background(
//                            Color.Black.copy(alpha = 0.5f),
//                            RoundedCornerShape(4.dp)
//                        )
//                        .padding(4.dp),
//                    horizontalArrangement = Arrangement.Start
//                ) {
//                    (0..4).forEach { index ->
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_star128),
//                            contentDescription = "Star $index",
//                            tint = if (index < review.rating) Color(0xFFFFCA28) else Color.White.copy(alpha = 0.5f),
//                            modifier = Modifier
//                                .size(20.dp)
//                                .padding(2.dp)
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = event.title,
//                fontFamily = montserratFont,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold,
//                color = colorScheme.onSurface
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = "Дата: ${formatEventDate(event.eventDate)}",
//                fontFamily = montserratFont,
//                fontSize = 12.sp,
//                color = colorScheme.onSurface.copy(alpha = 0.6f)
//            )
//            event.location?.let {
//                Spacer(modifier = Modifier.height(2.dp))
//                Text(
//                    text = "Место: $it",
//                    fontFamily = montserratFont,
//                    fontSize = 12.sp,
//                    color = colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//
//            review.comment?.let { comment ->
//                Text(
//                    text = comment,
//                    fontFamily = montserratFont,
//                    fontSize = 14.sp,
//                    color = colorScheme.onSurface,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 8.dp)
//                )
//            } ?: Text(
//                text = "Без комментария",
//                fontFamily = montserratFont,
//                fontSize = 14.sp,
//                color = colorScheme.onSurface.copy(alpha = 0.6f),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 8.dp)
//            )
//        }
//    }
//}

@Composable
private fun ReviewsCard(
    reviews: Map<String, EventReview>,
    events: Map<String, Event>,
    formatEventDate: (String?) -> String?,
    colorScheme: ColorScheme
) {
    var expanded by remember { mutableStateOf(false) }
    val maxCollapsedReviews = 2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.95f),
            contentColor = colorScheme.onSurface,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Мои отзывы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary
            )
            reviews.entries.take(if (expanded) Int.MAX_VALUE else maxCollapsedReviews).forEach { (eventId, review) ->
                val event = events[eventId]
                if (event != null) {
                    ReviewItem(
                        review = review,
                        event = event,
                        formatEventDate = formatEventDate,
                        colorScheme = colorScheme
                    )
                }
            }
            if (reviews.size > maxCollapsedReviews) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (expanded) "Свернуть" else "Показать все (${reviews.size})",
                        fontSize = 14.sp,
                        color = colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(
    review: EventReview,
    event: Event,
    formatEventDate: (String?) -> String?,
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            RatingStars(rating = review.rating)
        }
        Text(
            text = formatEventDate(event.eventDate) ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
        if (review.comment.isNullOrEmpty()) {
            Text(
                text = "Без комментария",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        } else {
            Text(
                text = review.comment,
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
        Divider(color = colorScheme.primary.copy(alpha = 0.1f))
    }
}

@Composable
private fun RatingStars(rating: Int) {
    Row {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (star <= rating) Color(0xFFFFD700) else Color.Gray
            )
        }
    }
}