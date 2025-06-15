package ru.walkAndTalk.ui.screens.main.feed.events.editevents

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel

class EditEventViewModel(
    private val eventsRepository: EventsRepository,
    private val feedViewModel: FeedViewModel
) : ViewModel(), ContainerHost<EditEventViewState, EditEventSideEffect> {

    override val container: Container<EditEventViewState, EditEventSideEffect> = container(EditEventViewState())

    fun loadEvent(eventId: String) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val event = eventsRepository.fetchEventById(eventId)
            if (event != null) {
                reduce { state.copy(event = event, isLoading = false) }
            } else {
                reduce { state.copy(isLoading = false, error = "Мероприятие не найдено") }
                postSideEffect(EditEventSideEffect.ShowError("Мероприятие не найдено"))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(EditEventSideEffect.ShowError(e.message ?: "Ошибка загрузки"))
        }
    }

    fun updateEvent(event: Event) = intent {
        try {
            val updatedEvent = event.copy(
                statusId = feedViewModel.getEventStatusId("pending") ?: "ac32ffcc-8f69-4b71-8a39-877c1eb95e04"
            )
            eventsRepository.updateEvent(updatedEvent).onSuccess {
                postSideEffect(EditEventSideEffect.EventUpdated)
            }.onFailure { error ->
                postSideEffect(EditEventSideEffect.ShowError("Ошибка обновления: ${error.message}"))
            }
        } catch (e: Exception) {
            postSideEffect(EditEventSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun navigateBack() = intent {
        postSideEffect(EditEventSideEffect.NavigateBack)
    }
}