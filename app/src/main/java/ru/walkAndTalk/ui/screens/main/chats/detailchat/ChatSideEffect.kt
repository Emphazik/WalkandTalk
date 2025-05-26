package ru.walkAndTalk.ui.screens.main.chats.detailchat

sealed class ChatSideEffect {
    data class ShowError(val message: String): ChatSideEffect()
}