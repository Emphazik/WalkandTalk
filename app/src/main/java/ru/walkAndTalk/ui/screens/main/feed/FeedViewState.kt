package ru.walkAndTalk.ui.screens.main.feed

import androidx.compose.runtime.Immutable
import ru.walkAndTalk.domain.model.Event

@Immutable
data class FeedViewState(
    val events: List<Event> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val photoURL: String? = null, // URL аватара
    val userId: String = "" // ID пользователя
)