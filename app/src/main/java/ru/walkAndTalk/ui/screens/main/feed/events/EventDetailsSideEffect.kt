package ru.walkAndTalk.ui.screens.main.feed.events

sealed interface EventDetailsSideEffect {
    object OnNavigateBack : EventDetailsSideEffect
}