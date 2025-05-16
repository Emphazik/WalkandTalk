package ru.walkAndTalk.ui.screens.main.feed

sealed class FeedSideEffect {
    data class NavigateToEventDetails(val eventId: String) : FeedSideEffect()
    data class ParticipateInEvent(val eventId: String) : FeedSideEffect()
    data class ShowError(val message: String) : FeedSideEffect()
    data class LeaveEventSuccess(val eventId: String) : FeedSideEffect()
}