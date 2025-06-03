package ru.walkAndTalk.ui.screens.main.chats

import androidx.compose.runtime.Immutable
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.User

@Immutable
data class ChatsViewState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val users: List<User> = emptyList(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
)