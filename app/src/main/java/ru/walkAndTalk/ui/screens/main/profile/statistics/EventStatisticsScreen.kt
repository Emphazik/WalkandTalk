package ru.walkAndTalk.ui.screens.main.profile.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.ui.screens.main.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventStatisticsScreen(
    userId: String,
    navController: NavController,
    viewModel: EventStatisticsViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
            if (sideEffect is EventStatisticsSideEffect.ShowError) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
//                                tint = colorScheme.onPrimary
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Статистика мероприятий",
                            fontFamily = montserratFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
//                            color = colorScheme.onPrimary,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.refreshEvents() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_refresh64),
                                contentDescription = "Refresh",
//                                tint = colorScheme.onPrimary
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = colorScheme.primary,
//                    titleContentColor = colorScheme.onPrimary,
//                    navigationIconContentColor = colorScheme.onPrimary,
//                    actionIconContentColor = colorScheme.onPrimary
//                ),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorScheme.primary
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Ошибка",
                        color = colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.pastEvents.isEmpty() -> {
                    Text(
                        text = "Вы пока не посещали мероприятия",
                        fontFamily = montserratFont,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(state.pastEvents) { event ->
                            EventCard(
                                event = event,
                                reviewInput = state.reviewInputs[event.id] ?: ReviewInput(),
                                onRatingChanged = { rating ->
                                    viewModel.onRatingChanged(event.id, rating)
                                },
                                onCommentChanged = { comment ->
                                    viewModel.onCommentChanged(event.id, comment)
                                },
                                onSubmitReview = {
                                    viewModel.onSubmitReview(event.id)
                                },
                                onEditReview = {
                                    viewModel.onEditReview(event.id)
                                },
                                onDeleteReview = {
                                    viewModel.onDeleteReview(event.id)
                                },
                                formatEventDate = { viewModel.formatEventDate(event.eventDate) },
                                colorScheme = colorScheme
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
    EventCard(
        event = Event(
            id = "1",
            title = "Sample Event",
            eventDate = "2023-10-10T10:00:00",
            location = "Moscow",
            creatorId = "user123",
            description = "Sample description",
            createdAt = "2023-10-01T10:00:00",
            eventImageUrl = "https://example.com/event.jpg",
            organizerName = "Sample Organizer",
            tagIds = emptyList()
        ),
        reviewInput = ReviewInput(rating = 3, comment = "Отличное мероприятие!", isSubmitted = true),
        onRatingChanged = {},
        onCommentChanged = {},
        onSubmitReview = {},
        onEditReview = {},
        onDeleteReview = {},
        formatEventDate = { "10.10.2023, 10:00" },
        colorScheme = MaterialTheme.colorScheme
    )
}

@Composable
fun EventCard(
    event: Event,
    reviewInput: ReviewInput,
    onRatingChanged: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onEditReview: () -> Unit,
    onDeleteReview: () -> Unit,
    formatEventDate: (String) -> String,
    colorScheme: ColorScheme
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val buttonColor = Color(0xFF6200EE) // Универсальный фиолетовый для светлой и темной тем

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить отзыв?",
                    fontFamily = montserratFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить свой отзыв?",
                    fontFamily = montserratFont,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteReview()
                    }
                ) {
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
                        color = colorScheme.onSurface
                    )
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Event Image with Rating Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = if (event.eventImageUrl.isNullOrBlank()) {
                        painterResource(id = R.drawable.default_event_image)
                    } else {
                        rememberAsyncImagePainter(
                            model = event.eventImageUrl,
                            placeholder = painterResource(id = R.drawable.default_event_image),
                            error = painterResource(id = R.drawable.default_event_image)
                        )
                    },
                    contentDescription = "Event Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    (0..4).forEach { index ->
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star128),
                            contentDescription = "Star $index",
                            tint = if (index < reviewInput.rating) Color(0xFFFFCA28) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onRatingChanged(index + 1) }
                                .padding(2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                fontFamily = montserratFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Дата: ${formatEventDate(event.eventDate)}",
                fontFamily = montserratFont,
                fontSize = 12.sp,
                color = colorScheme.onSurface.copy(alpha = 0.6f)
            )
            event.location?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Место: $it",
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Comment Input
            OutlinedTextField(
                value = reviewInput.comment,
                onValueChange = onCommentChanged,
                label = {
                    Text(
                        text = "Ваш отзыв",
                        fontFamily = montserratFont,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = montserratFont,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface
                ),
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedIndicatorColor = buttonColor,
                    unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.3f),
                    cursorColor = buttonColor,
                    focusedLabelColor = buttonColor,
                    unfocusedLabelColor = colorScheme.onSurface.copy(alpha = 0.6f),
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Review Buttons
            if (!reviewInput.isSubmitted) {
                Button(
                    onClick = onSubmitReview,
                    enabled = !reviewInput.isSubmitting && reviewInput.rating > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (reviewInput.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Отправить отзыв",
                            fontFamily = montserratFont,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onEditReview,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Сохранить изменения",
                            fontFamily = montserratFont,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF44336).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete64),
                            contentDescription = "Delete Review",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}