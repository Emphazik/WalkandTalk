package ru.walkAndTalk.ui.screens.feed

sealed class FeedSideEffect {
    data class NavigateToEventDetails(val eventId: String) : FeedSideEffect()
}