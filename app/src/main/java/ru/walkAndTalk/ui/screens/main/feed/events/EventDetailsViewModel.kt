package ru.walkAndTalk.ui.screens.main.feed.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.UserEventRepository


class EventDetailsViewModel(
    private val eventsRepository: EventsRepository,
    private val usersRepository: UserEventRepository,
    private val eventParticipantsRepository: EventParticipantsRepository
) : ViewModel(), ContainerHost<EventDetailsState, EventDetailsSideEffect> {

    override val container: Container<EventDetailsState, EventDetailsSideEffect> = container(EventDetailsState())

    fun loadEvent(eventId: String) = intent {
        println("EventDetailsViewModel: Loading event $eventId")
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val event = eventsRepository.fetchEventById(eventId)
            if (event != null) {
                val organizerName = usersRepository.getUserName(event.creatorId)
                val participantsCount = eventParticipantsRepository.getParticipantsCount(eventId)
                println("EventDetailsViewModel: Loaded event $eventId, tags=${event.tagIds}, participants=$participantsCount")
                reduce {
                    state.copy(
                        event = event.copy(organizerName = organizerName),
                        participantsCount = participantsCount,
                        isLoading = false
                    )
                }
            } else {
                reduce { state.copy(isLoading = false, error = "Мероприятие не найдено") }
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = "Ошибка загрузки: ${e.message}") }
        }
    }

    fun onNavigateBack() = intent {
        postSideEffect(EventDetailsSideEffect.OnNavigateBack)
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
        return "${dateTime.dayOfMonth} $month ${dateTime.year}, ${dateTime.hour}:${
            dateTime.minute.toString().padStart(2, '0')
        }"
    }
}
