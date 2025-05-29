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
    val error: String? = null,
    val isParticipantActive: Boolean = false, // Новое поле для активности собеседника
    val selectedMessageIds: Set<String> = emptySet(), // Добавили поле для выбранных сообщений
    val editingMessageId: String? = null,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
)