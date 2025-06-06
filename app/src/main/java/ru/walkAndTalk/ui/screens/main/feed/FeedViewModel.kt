package ru.walkAndTalk.ui.screens.main.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository

class FeedViewModel(
    private val eventsRepository: EventsRepository,
    private val eventParticipantsRepository: EventParticipantsRepository,
    private val remoteUsersRepository: RemoteUsersRepository,
    private val chatsRepository: ChatsRepository, // Add ChatsRepository
    private val currentUserId: String
) : ViewModel(), ContainerHost<FeedViewState, FeedSideEffect> {

    override val container: Container<FeedViewState, FeedSideEffect> = container(FeedViewState())

    private val _participationState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val participationState = _participationState.asStateFlow()

    private var allEvents: List<ru.walkAndTalk.domain.model.Event> = emptyList()

    init {
        refreshEvents()
        loadParticipationStates()
    }

    fun refreshEvents() = intent {
        loadEvents()
    }

    private fun loadEvents() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            allEvents = eventsRepository.fetchAllEvents()
            println("FeedViewModel: Loaded ${allEvents.size} events")
            val sortedEvents = applySort(state.sortType, allEvents)
            reduce { state.copy(events = sortedEvents, isLoading = false) }
            loadParticipationStates() // Перемещаем сюда
        } catch (e: Exception) {
            println("FeedViewModel: LoadEvents error=${e.message}")
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }

    private fun loadParticipationStates() {
        viewModelScope.launch {
//            val eventIds = container.stateFlow.value.events.map { it.id }
            val eventIds = allEvents.map { it.id } // Используем allEvents
            val states = eventIds.associateWith { eventId ->
                eventParticipantsRepository.isUserParticipating(eventId, currentUserId)
            }
            println("FeedViewModel: Loaded participation states for ${states.size} events")
            _participationState.value = states
        }
    }

    fun onSearchQueryChange(query: String) = intent {
        println("FeedViewModel: SearchQuery=$query")
        reduce { state.copy(searchQuery = query) }
        try {
            val filteredEvents = if (query.startsWith("#") && query.length > 1) {
                val tagQuery = query.drop(1).trim()
                allEvents.filter { event ->
                    event.tagIds.any { tag ->
                        tag.contains(tagQuery, ignoreCase = true)
                    }
                }
            } else {
                // Обычный поиск по названию и описанию
                allEvents.filter { event ->
                    event.title.contains(query, ignoreCase = true) ||
                            event.description.contains(query, ignoreCase = true)
                }
            }
            println("FeedViewModel: Filtered ${filteredEvents.size} events for query=$query")
            val sortedEvents = applySort(state.sortType, filteredEvents)
            reduce { state.copy(events = sortedEvents) }
        } catch (e: Exception) {
            println("FeedViewModel: Search error=${e.message}")
            reduce { state.copy(error = e.message) }
        }
    }

    fun onClearQuery() = intent {
        println("FeedViewModel: ClearQuery")
        reduce { state.copy(searchQuery = "") }
        val sortedEvents = applySort(state.sortType, allEvents)
        reduce { state.copy(events = sortedEvents, error = null) }
    }

    fun onSortSelected(sortType: SortType) = intent {
        println("FeedViewModel: SortType=$sortType")
        val sortedEvents =
            applySort(sortType, if (state.searchQuery.isEmpty()) allEvents else state.events)
        reduce { state.copy(events = sortedEvents, sortType = sortType) }
    }

    private fun applySort(sortType: SortType?, events: List<Event>): List<Event> {
        return when (sortType) {
            SortType.DateNewestFirst -> events.sortedByDescending {
                Instant.parse(it.eventDate).toEpochMilliseconds()
            }

            SortType.DateOldestFirst -> events.sortedBy {
                Instant.parse(it.eventDate).toEpochMilliseconds()
            }

            null -> events
        }
    }

    fun onEventClick(eventId: String) = intent {
        postSideEffect(FeedSideEffect.NavigateToEventDetails(eventId))
    }

    fun onParticipateClick(eventId: String) = intent {
        try {
            val isParticipating =
                eventParticipantsRepository.isUserParticipating(eventId, currentUserId)
            if (isParticipating) {
                postSideEffect(FeedSideEffect.ShowError("Вы уже участвуете в этом мероприятии"))
                return@intent
            }

            val result = eventParticipantsRepository.joinEvent(eventId, currentUserId)
            result.onSuccess {
                viewModelScope.launch {
                    val updatedStates = _participationState.value.toMutableMap().apply {
                        this[eventId] = true
                    }
                    _participationState.value = updatedStates
                }
                postSideEffect(FeedSideEffect.ParticipateInEvent(eventId))
                refreshEvents()
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
                viewModelScope.launch {
                    val updatedStates = _participationState.value.toMutableMap().apply {
                        this[eventId] = false
                    }
                    _participationState.value = updatedStates

                    // Remove user from group chat
                    val chat = chatsRepository.findGroupChatByEventId(eventId)
                    if (chat != null) {
                        chatsRepository.removeUserFromChat(chat.id, currentUserId)
                    }
                }
                postSideEffect(FeedSideEffect.LeaveEventSuccess(eventId))
                refreshEvents()
            }.onFailure { error ->
                postSideEffect(
                    FeedSideEffect.ShowError(
                        error.message ?: "Ошибка при отмене участия"
                    )
                )
            }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

//    fun isUserParticipating(eventId: String): Boolean {
//        return runBlocking {
//            eventParticipantsRepository.isUserParticipating(eventId, currentUserId)
//        }
//    }

    fun isUserParticipating(eventId: String): Boolean {
        return _participationState.value[eventId] ?: false
    }

    fun updateEventImage(eventId: String, imageUrl: String) = intent {
        try {
            eventsRepository.updateEventImage(eventId, imageUrl)
            refreshEvents()
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
        return "${dateTime.dayOfMonth} $month ${dateTime.year}, ${dateTime.hour}:${
            dateTime.minute.toString().padStart(2, '0')
        }"
    }
}