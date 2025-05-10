package ru.walkAndTalk.ui.screens.feed

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.EventsRepository

class FeedViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel(), ContainerHost<FeedViewState, FeedSideEffect> {

    override val container: Container<FeedViewState, FeedSideEffect> = container(FeedViewState())

    init {
        loadEvents()
    }

    private fun loadEvents() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val events = eventsRepository.fetchAllEvents()
            reduce { state.copy(events = events, isLoading = false) }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }

    fun onSearchQueryChange(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
    }

    fun onEventClick(eventId: String) = intent {
        postSideEffect(FeedSideEffect.NavigateToEventDetails(eventId))
    }

    fun formatEventDate(date: String): String {
        return date // Заглушка, заменить на реальную логику форматирования
    }
}