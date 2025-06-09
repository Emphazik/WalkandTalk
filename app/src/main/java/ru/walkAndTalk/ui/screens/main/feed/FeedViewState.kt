package ru.walkAndTalk.ui.screens.main.feed

import androidx.compose.runtime.Immutable
import ru.walkAndTalk.data.model.NotificationDto
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Notification

@Immutable
data class FeedViewState(
    val events: List<Event> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val photoURL: String? = null, // URL аватара
    val userId: String = "", // ID пользователя
    val sortType: SortType? = null,
    val isLoadingNotifications: Boolean = false,
    val notificationsError: String? = null
)