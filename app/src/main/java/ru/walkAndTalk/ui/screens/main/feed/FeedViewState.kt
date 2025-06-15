package ru.walkAndTalk.ui.screens.main.feed

import androidx.compose.runtime.Immutable
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.FeedItem
import ru.walkAndTalk.domain.model.FeedItemType
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
    val notificationsError: String? = null,
    val selectedCity: String? = null,
    val selectedType: FeedItemType? = null,
    val items: List<FeedItem> = emptyList(),
    val tagNames: Map<String, String> = emptyMap()
)

