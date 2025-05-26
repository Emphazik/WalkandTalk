package ru.walkAndTalk.ui.screens.main.chats.detailchat

sealed class ChatIntent {
    data class UpdateInputText(val text: String): ChatIntent()
    data class SendMessage(val chatId: String): ChatIntent()
}