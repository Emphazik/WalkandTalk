package ru.walkAndTalk.ui.screens.main.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Notification
import ru.walkAndTalk.domain.model.NotificationType
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.NotificationsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Locale

class FeedViewModel(
    private val eventsRepository: EventsRepository,
    private val eventParticipantsRepository: EventParticipantsRepository,
    private val remoteUsersRepository: RemoteUsersRepository,
    private val chatsRepository: ChatsRepository, // Add ChatsRepository
    private val currentUserId: String,
    private val notificationsRepository: NotificationsRepository,
    ) : ViewModel(), ContainerHost<FeedViewState, FeedSideEffect> {

    override val container: Container<FeedViewState, FeedSideEffect> = container(FeedViewState())

    private val _participationState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val participationState = _participationState.asStateFlow()

    private var allEvents: List<Event> = emptyList()

    init {
        refreshEvents()
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
            loadParticipationStates()
        } catch (e: Exception) {
            println("FeedViewModel: LoadEvents error=${e.message}")
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }



    private fun loadParticipationStates() {
        viewModelScope.launch {
            val eventIds = allEvents.map { it.id }
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

    fun navigateToNotifications() = intent {
        postSideEffect(FeedSideEffect.NavigateToNotifications)
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
                    val chat = chatsRepository.findGroupChatByEventId(eventId)
                    if (chat != null) {
                        chatsRepository.removeUserFromChat(chat.id, currentUserId)
                    }
                }
                postSideEffect(FeedSideEffect.LeaveEventSuccess(eventId))
                refreshEvents()
            }.onFailure { error ->
                postSideEffect(FeedSideEffect.ShowError(error.message ?: "Ошибка при отмене участия"))
            }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

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

    fun formatEventDate(isoDate: String?): String? {
        if (isoDate == null) return null
        return try {
            val offsetDateTime = OffsetDateTime.parse(isoDate)
            val zoneId = TimeZone.currentSystemDefault().toJavaZoneId()
            val now = OffsetDateTime.now(zoneId)
            when {
                offsetDateTime.toLocalDate() == now.toLocalDate() -> "Сегодня, ${SimpleDateFormat("HH:mm", Locale("ru")).format(offsetDateTime.toInstant().toEpochMilli())}"
                offsetDateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> "Вчера, ${SimpleDateFormat("HH:mm", Locale("ru")).format(offsetDateTime.toInstant().toEpochMilli())}"
                else -> SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ru")).format(offsetDateTime.toInstant().toEpochMilli())
            }
        } catch (e: Exception) {
            println("FeedViewModel: Error parsing date: $e")
            null
        }
    }
}