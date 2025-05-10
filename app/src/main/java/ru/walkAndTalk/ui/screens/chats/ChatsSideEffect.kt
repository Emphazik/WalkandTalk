package ru.walkAndTalk.ui.screens.chats

sealed class ChatsSideEffect {
    data class NavigateToChat(val chatId: String) : ChatsSideEffect()
}