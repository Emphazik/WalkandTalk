package ru.walkAndTalk.ui.screens.main.profile

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.main.montserratFont

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateAuth: () -> Unit,
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.onImageSelected(uri)
                }
            }
        }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.LaunchImagePicker -> {
                val intent = Intent(Intent.ACTION_PICK).apply { this.type = "image/*" }
                launcher.launch(intent)
            }
            is ProfileSideEffect.OnNavigateExit -> onNavigateAuth()
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
                    name = state.name,
                    onEditClick = { intent -> launcher.launch(intent) },
                    onLogoutClick = { viewModel.onLogout() },
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
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item {
                BioSection(
                    bio = state.bio,
                    isEditing = state.isEditingBio,
                    newBio = state.newBio,
                    onEditClick = viewModel::onEditBio,
                    onBioChanged = viewModel::onBioChanged,
                    onSaveClick = viewModel::onSaveBio,
                    onCancelClick = viewModel::onCancelBio,
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                InterestSection(
                    interests = state.interests,
                    onAddInterest = { viewModel.onAddInterestClick() },
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                GoalsSection(
                    goals = state.goals,
                    isEditing = state.isEditingGoals,
                    newGoals = state.newGoals,
                    onEditClick = viewModel::onEditGoals,
                    onGoalsChanged = viewModel::onGoalsChanged,
                    onSaveClick = viewModel::onSaveGoals,
                    onCancelClick = viewModel::onCancelGoals,
                    colorScheme = colorScheme
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            ActionButtons(
                onStatsClick = { /* Заглушка */ },
                onEditClick = { viewModel.onEditClick() },
                onLogoutClick = { viewModel.onLogout() },
                colorScheme = colorScheme
            )
        }

        if (state.showInterestSelection) {
            InterestSelectionMenu(
                availableInterests = state.availableInterests,
                selectedInterests = state.tempSelectedInterests,
                onInterestToggled = { interest -> viewModel.onInterestToggled(interest) },
                onDismiss = { viewModel.onDismissInterestSelection() },
                colorScheme = colorScheme
            )
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    onEditClick: (Intent) -> Unit,
    onLogoutClick: () -> Unit,
    photoURL: String?,
    colorScheme: ColorScheme
) {
    var showEditMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        Column(modifier = Modifier.padding(top = 16.dp, end = 16.dp)) {
            IconButton(
                onClick = { showEditMenu = true },
                modifier = Modifier
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
                        val intent = Intent(Intent.ACTION_PICK).apply { this.type = "image/*" }
                        onEditClick(intent)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Выйти",
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onLogoutClick()
                    }
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = photoURL?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.preview_profile),
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
fun BioSection(
    bio: String?,
    isEditing: Boolean,
    newBio: String,
    onEditClick: () -> Unit,
    onBioChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
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
                    text = "О себе",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (!isEditing) {
                    Text(
                        text = "Изменить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.clickable { onEditClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = newBio,
                    onValueChange = onBioChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Введите текст") },
                    textStyle = TextStyle(
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Сохранить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Отмена",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            } else {
                val displayText = bio?.ifEmpty { "О себе не указано" } ?: "О себе не указано"
                Text(
                    text = displayText,
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp) // Добавим небольшой отступ для видимости
                )
            }
        }
    }
}

@Composable
fun GoalsSection(
    goals: String?,
    isEditing: Boolean,
    newGoals: String,
    onEditClick: () -> Unit,
    onGoalsChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
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
                    text = "Цели",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (!isEditing) {
                    Text(
                        text = "Изменить",
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.clickable { onEditClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = newGoals,
                    onValueChange = onGoalsChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Введите текст") },
                    textStyle = TextStyle(
                        fontFamily = montserratFont,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Сохранить",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Отмена",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = goals ?: "Цели не указаны",
                    fontFamily = montserratFont,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InterestSection(
    interests: List<String>,
    onAddInterest: () -> Unit,
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
            // Разделяем интересы на ряды по 4 элемента
            val rows = interests.chunked(4) // Делим список на группы по 4
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
            .wrapContentWidth() // Убедимся, что слово не переносится
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = interest,
            fontFamily = montserratFont,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            softWrap = false // Отключаем перенос слов
        )
    }
}

@Composable
fun InterestSelectionMenu(
    availableInterests: List<String>,
    selectedInterests: List<String>,
    onInterestToggled: (String) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    AlertDialog(
        onDismissRequest = { /* Не закрываем при нажатии на фон */ },
        title = {
            Text(
                text = "Выберите интересы",
                fontFamily = montserratFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableInterests) { interest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selectedInterests.contains(interest)) Color(0xFF4CAF50).copy(alpha = 0.1f) else colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onInterestToggled(interest) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedInterests.contains(interest),
                            onCheckedChange = { onInterestToggled(interest) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                uncheckedColor = colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = interest,
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(
                    text = "Готово",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(
                    text = "Отмена",
                    fontFamily = montserratFont,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
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
//        Button(
//            onClick = onEditClick,
//            modifier = Modifier
//                .fillMaxWidth(0.8f)
//                .height(48.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(
//                text = "Редактировать профиль",
//                fontFamily = montserratFont,
//                fontSize = 16.sp,
//                color = colorScheme.onSecondaryContainer
//            )
//        }
//        Button(
//            onClick = onLogoutClick,
//            modifier = Modifier
//                .fillMaxWidth(0.8f)
//                .height(48.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.errorContainer),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(
//                text = "Выйти",
//                fontFamily = montserratFont,
//                fontSize = 16.sp,
//                color = colorScheme.onErrorContainer
//            )
//        }
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

