package ru.walkAndTalk.ui.screens.main.feed

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventsRepository

class FeedViewModel(
    private val eventsRepository: EventsRepository,
    private val eventParticipantsRepository: EventParticipantsRepository,
    private val currentUserId: String // Предположим, у нас есть ID текущего пользователя
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
        val filteredEvents = eventsRepository.fetchAllEvents().filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(
                query,
                ignoreCase = true
            )
        }
        reduce { state.copy(events = filteredEvents) }
    }

    fun onEventClick(eventId: String) = intent {
        postSideEffect(FeedSideEffect.NavigateToEventDetails(eventId))
    }

    fun onParticipateClick(eventId: String) = intent {
        try {
            val isParticipating = eventParticipantsRepository.isUserParticipating(eventId, currentUserId)
            if (isParticipating) {
                postSideEffect(FeedSideEffect.ShowError("Вы уже участвуете в этом мероприятии"))
                return@intent
            }

            val result = eventParticipantsRepository.joinEvent(eventId, currentUserId)
            result.onSuccess {
                postSideEffect(FeedSideEffect.ParticipateInEvent(eventId))
            }.onFailure { error ->
                postSideEffect(FeedSideEffect.ShowError(error.message ?: "Ошибка при регистрации"))
            }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun onLeaveEventClick(eventId: String) = intent {
        try {
            val result = eventParticipantsRepository.leaveEvent(eventId, currentUserId)
            result.onSuccess {
                postSideEffect(FeedSideEffect.LeaveEventSuccess(eventId))
            }.onFailure { error ->
                postSideEffect(FeedSideEffect.ShowError(error.message ?: "Ошибка при отмене участия"))
            }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun isUserParticipating(eventId: String): Boolean {
        return runBlocking {
            eventParticipantsRepository.isUserParticipating(eventId, currentUserId)
        }
    }

    fun updateEventImage(eventId: String, imageUrl: String) = intent {
        try {
            eventsRepository.updateEventImage(eventId, imageUrl)
            loadEvents() // Перезагружаем события после обновления
        } catch (e: Exception) {
            reduce { state.copy(error = "Ошибка при обновлении изображения: ${e.message}") }
        }
    }


    fun formatEventDate(isoDate: String): String {
        val instant = Instant.parse(isoDate)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = when (dateTime.monthNumber) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            12 -> "декабря"
            else -> ""
        }
        return "${dateTime.dayOfMonth} $month, ${dateTime.hour}:${
            dateTime.minute.toString().padStart(2, '0')
        }"
    }
}