package ru.walkAndTalk.ui.screens.profile

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.main.montserratFont

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    userId: String
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    // Launcher для выбора изображения
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Обработка выбранного изображения
            }
        }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.LaunchImagePicker -> {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                launcher.launch(intent)
            }
//            is ProfileSideEffect.OnNavigateEditProfile -> {
//                // Логика навигации на экран редактирования профиля
//            }
//            is ProfileSideEffect.OnNavigateExit -> {
//                // Логика выхода из профиля (очистка токена, навигация на экран авторизации)
//            }
//            is ProfileSideEffect.OnNavigateStatisticUser -> {
//                // Логика навигации на экран статистики
//            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileHeader(
                    name = state.name.toString(),
                    onEditClick = { intent -> launcher.launch(intent) },
                    photoURL = state.photoURL,
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
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
                    Box {
                        Text(
                            text = state.selectedCityStatus.toString(),
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { viewModel.onCityStatusClick() }
                        )
                        DropdownMenu(
                            expanded = state.showCityStatusMenu,
                            onDismissRequest = viewModel::onDismissCityStatusMenu,
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            state.cityStatuses.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = status,
                                            fontFamily = montserratFont,
                                            fontSize = 16.sp,
                                            color = colorScheme.onSurface
                                        )
                                    },
                                    onClick = { viewModel.onCityStatusSelected(status) }
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) } // Увеличенный отступ перед карточками
            item {
                BioSection(
                    bio = state.bio ?: "О себе не указано",
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) } // Отступ между карточками
            item {
                InterestSection(
                    interests = state.interests,
                    onAddInterest = { viewModel.onAddInterestClick() },
                    onRemoveInterest = { interest -> viewModel.onInterestRemoved(interest) },
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) } // Отступ между карточками
            item {
                GoalsSection(
                    goals = state.goals ?: "Цели не указаны",
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // Отступ для кнопок
        }

        // Кнопки внизу по центру
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            ActionButtons(
                onStatsClick = { /* Заглушка */ },
                onEditClick = { viewModel.onEditClick() },
                onLogoutClick = { /* Заглушка */ },
                colorScheme = colorScheme
            )
        }

        // Меню выбора интересов
        if (state.showInterestSelection) {
            InterestSelectionMenu(
                availableInterests = state.availableInterests,
                onDismiss = viewModel::onDismissInterestSelection,
                onInterestSelected = viewModel::onInterestSelected
            )
        }
    }

    LaunchedEffect(userId) {
        viewModel.onCreate(userId)
    }
}

@Composable
fun ProfileHeader(
    name: String,
    onEditClick: (Intent) -> Unit,
    photoURL: String?,
    colorScheme: ColorScheme
) {
    var showEditMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = { showEditMenu = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(32.dp)
                .background(colorScheme.primary.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more128),
                contentDescription = "Edit photo",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(
            expanded = showEditMenu,
            onDismissRequest = { showEditMenu = false },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp) // Расположение строго под иконкой
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Изменить изображение",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )
                },
                onClick = {
                    showEditMenu = false
                    val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                    onEditClick(intent)
                }
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = photoURL?.let { rememberAsyncImagePainter(it) } ?: painterResource(id = R.drawable.preview_profile),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorScheme.onSurface.copy(alpha = 0.2f))
                    .border(2.dp, colorScheme.onSurface.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                fontFamily = montserratFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InterestSection(
    interests: List<String>,
    onAddInterest: () -> Unit,
    onRemoveInterest: (String) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Интересы",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Добавить",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.clickable { onAddInterest() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(interests) { interest ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = interest,
                                fontFamily = montserratFont,
                                fontSize = 14.sp,
                                color = colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clear128),
                                contentDescription = "Remove interest",
                                tint = colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onRemoveInterest(interest) }
                            )
                        }
                    }
                }
                if (interests.isEmpty()) {
                    item {
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
}

@Composable
fun BioSection(
    bio: String,
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
                text = bio,
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun GoalsSection(
    goals: String,
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
                text = goals,
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ActionButtons(
    onStatsClick: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    colorScheme: ColorScheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStatsClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Статистика мероприятий",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = colorScheme.onSecondaryContainer
            )
        }
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Редактировать профиль",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = colorScheme.onSecondaryContainer
            )
        }
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.errorContainer),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Выйти",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun Divider(colorScheme: ColorScheme) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colorScheme.outline)
    )
}

@Composable
fun InterestSelectionMenu(
    availableInterests: List<String>,
    onDismiss: () -> Unit,
    onInterestSelected: (String) -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        availableInterests.forEach { interest ->
            DropdownMenuItem(
                text = { Text(interest, fontFamily = montserratFont, fontSize = 16.sp) },
                onClick = { onInterestSelected(interest) }
            )
        }
    }
}