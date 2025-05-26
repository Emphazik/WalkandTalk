package ru.walkAndTalk.ui.screens.main.chats.detailchat

import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.Message

data class ChatViewState(
    val chatId: String = "",
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)