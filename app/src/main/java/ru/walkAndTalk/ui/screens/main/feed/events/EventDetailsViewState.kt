package ru.walkAndTalk.ui.screens.main.feed.events

import ru.walkAndTalk.domain.model.Event

data class EventDetailsState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)