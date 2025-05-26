package ru.walkAndTalk.ui.screens.main.chats

import ru.walkAndTalk.ui.screens.main.search.SearchSideEffect

sealed class ChatsSideEffect {
    data class NavigateToChat(val chatId: String): ChatsSideEffect()
    data class ShowError(val message: String): ChatsSideEffect()
}