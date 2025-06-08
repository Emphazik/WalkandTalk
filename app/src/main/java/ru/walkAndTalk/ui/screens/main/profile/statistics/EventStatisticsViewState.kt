package ru.walkAndTalk.ui.screens.main.profile.statistics

import androidx.compose.runtime.MutableState
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.EventReview

data class EventStatisticsViewState(
    val pastEvents: List<Event> = emptyList(),
    val reviews: Map<String, EventReview> = emptyMap(),
    val reviewInputs: Map<String, ReviewInput> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)