package ru.walkAndTalk.ui.screens.main.feed.events

sealed class EventDetailsSideEffect {
    data object OnNavigateBack : EventDetailsSideEffect()
    data class ShowError(val message: String) : EventDetailsSideEffect()
    data class NavigateToChat(val chatId: String) : EventDetailsSideEffect()
    data class NavigateToEditEvent(val eventId: String) : EventDetailsSideEffect() // Для редактирования
    object EventDeleted : EventDetailsSideEffect() // Для удаления
}
