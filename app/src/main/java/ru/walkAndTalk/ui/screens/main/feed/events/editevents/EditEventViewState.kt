package ru.walkAndTalk.ui.screens.main.feed.events.editevents

import ru.walkAndTalk.domain.model.Event

data class EditEventViewState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
