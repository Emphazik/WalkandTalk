package ru.walkAndTalk.ui.screens.main.chats

sealed class ChatsSideEffect {
    data class NavigateToChat(val chatId: String) : ChatsSideEffect()
}