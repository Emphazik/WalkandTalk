package ru.walkAndTalk.ui.screens.main.chats.detailchat

sealed class ChatSideEffect(open val message: String) { // Убрали data, сделали обычным классом
    data class ShowError(override val message: String) : ChatSideEffect(message)
    data class ShowCopySuccess(override val message: String = "Текст успешно скопирован") : ChatSideEffect(message)
}