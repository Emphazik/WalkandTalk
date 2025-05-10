package ru.walkAndTalk.ui.screens.chats

import androidx.compose.runtime.Immutable
import ru.walkAndTalk.domain.model.Chat

@Immutable
data class ChatsViewState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)