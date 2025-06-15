package ru.walkAndTalk.ui.screens.main.feed.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.UserEventRepository


class EventDetailsViewModel(
    private val eventsRepository: EventsRepository,
    private val usersRepository: UserEventRepository,
    private val eventParticipantsRepository: EventParticipantsRepository,
    private val chatsRepository: ChatsRepository,
    private val interestsRepository: InterestsRepository,
    private val currentUserId: String
) : ViewModel(), ContainerHost<EventDetailsState, EventDetailsSideEffect> {

    private val interests = mutableMapOf<String, String>() // tagId -> tagName

    override val container: Container<EventDetailsState, EventDetailsSideEffect> = container(EventDetailsState())

    fun loadEvent(eventId: String) = intent {
        println("EventDetailsViewModel: Loading event $eventId")
        reduce { state.copy(isLoading = true, error = null) }
        try {
            interestsRepository.fetchAll().forEach { interest ->
                interests[interest.id] = interest.name
            }
            val event = eventsRepository.fetchEventById(eventId)
            if (event != null) {
                val organizerName = usersRepository.getUserName(event.creatorId)
                val participantsCount = eventParticipantsRepository.getParticipantsCount(eventId)
                val isCurrentUserOrganizer = event.creatorId == currentUserId
                val tagNames = event.tagIds.map { interests[it] ?: it } // Маппим ID в названия
                println("EventDetailsViewModel: Loaded event $eventId, tags=${event.tagIds}, participants=$participantsCount, isOrganizer=$isCurrentUserOrganizer")
                reduce {
                    state.copy(
                        event = event.copy(organizerName = organizerName),
                        participantsCount = participantsCount,
                        isLoading = false,
                        isCurrentUserOrganizer = isCurrentUserOrganizer,
                        tagNames = tagNames // Сохраняем названия тегов
                    )
                }
            } else {
                reduce { state.copy(isLoading = false, error = "Мероприятие не найдено") }
                postSideEffect(EventDetailsSideEffect.ShowError("Мероприятие не найдено"))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(EventDetailsSideEffect.ShowError(e.message ?: "Ошибка загрузки мероприятия"))
        }
    }

    fun onJoinChatClick(eventId: String) = intent {
        try {
            val chat = chatsRepository.createGroupChat(eventId, currentUserId)
            println("EventDetailsViewModel: Group chat created or found for eventId=$eventId, chatId=${chat.id}")
            postSideEffect(EventDetailsSideEffect.NavigateToChat(chat.id))
        } catch (e: Exception) {
            println("EventDetailsViewModel: Error creating/finding group chat for eventId=$eventId, error=${e.message}")
            postSideEffect(EventDetailsSideEffect.ShowError(e.message ?: "Ошибка создания чата"))
        }
    }

    fun onEditEventClick(eventId: String) = intent {
        postSideEffect(EventDetailsSideEffect.NavigateToEditEvent(eventId))
    }

    fun onDeleteEventClick(eventId: String) = intent {
        try {
            eventsRepository.deleteEvent(eventId, currentUserId).onSuccess {
                eventParticipantsRepository.removeAllParticipants(eventId).onSuccess {
                    val chat = chatsRepository.findGroupChatByEventId(eventId)
                    if (chat != null) {
                        chatsRepository.deleteChat(chat.id)
                    }
                    println("EventDetailsViewModel: Event $eventId deleted")
                    postSideEffect(EventDetailsSideEffect.EventDeleted)
                }.onFailure { error ->
                    println("EventDetailsViewModel: Error removing participants for event $eventId: ${error.message}")
                    postSideEffect(EventDetailsSideEffect.ShowError("Ошибка удаления участников: ${error.message}"))
                }
            }.onFailure { error ->
                println("EventDetailsViewModel: Error deleting event $eventId: ${error.message}")
                postSideEffect(EventDetailsSideEffect.ShowError("Ошибка удаления мероприятия: ${error.message}"))
            }
        } catch (e: Exception) {
            println("EventDetailsViewModel: Exception deleting event $eventId: ${e.message}")
            postSideEffect(EventDetailsSideEffect.ShowError("Ошибка: ${e.message}"))
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
        return "${dateTime.dayOfMonth} $month ${dateTime.year}, ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    }
}