package ru.walkAndTalk.ui.screens.main.feed

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaZoneId
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.usecase.CreateAnnouncementUseCase
import ru.walkAndTalk.data.usecase.CreateEventUseCase
import ru.walkAndTalk.data.usecase.FetchFeedItemsUseCase
import ru.walkAndTalk.domain.model.ActivityType
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.FeedItem
import ru.walkAndTalk.domain.model.FeedItemType
import ru.walkAndTalk.domain.model.Interest
import ru.walkAndTalk.domain.repository.ActivityTypesRepository
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository
import java.io.File
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Locale
import kotlin.collections.set

class FeedViewModel(
    private val fetchFeedItemsUseCase: FetchFeedItemsUseCase,
    private val createEventUseCase: CreateEventUseCase,
    private val createAnnouncementUseCase: CreateAnnouncementUseCase,
    private val eventParticipantsRepository: EventParticipantsRepository,
    private val chatsRepository: ChatsRepository,
    private val interestsRepository: InterestsRepository,
    private val storageRepository: StorageRepository,
    private val remoteUsersRepository: RemoteUsersRepository,
    private val activityTypesRepository: ActivityTypesRepository,
    val currentUserId: String
) : ViewModel(), ContainerHost<FeedViewState, FeedSideEffect> {

    override val container: Container<FeedViewState, FeedSideEffect> = container(FeedViewState())

    private val _participationState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val participationState = _participationState.asStateFlow()

    private val _interests = MutableStateFlow<List<Interest>>(emptyList())
    val interests = _interests.asStateFlow()

    private val _activityTypes = MutableStateFlow<List<ActivityType>>(emptyList())
    val activityTypes = _activityTypes.asStateFlow()

    private var allFeedItems: List<FeedItem> = emptyList()

    init {
        refreshFeed()
        loadInterests()
        loadActivityTypes()
    }

    private fun loadInterests() = intent {
        viewModelScope.launch {
            try {
                _interests.value = interestsRepository.fetchAll()
            } catch (e: Exception) {
                postSideEffect(FeedSideEffect.ShowError("Ошибка загрузки тегов"))
            }
        }
    }

    private fun loadActivityTypes() = intent {
        viewModelScope.launch {
            try {
                _activityTypes.value = activityTypesRepository.fetchAll()
            } catch (e: Exception) {
                postSideEffect(FeedSideEffect.ShowError("Ошибка загрузки типов активностей"))
            }
        }
    }

    suspend fun uploadEventImage(imageUri: Uri): Result<String> {
        return try {
            val eventId = "event-${System.currentTimeMillis()}"
            val fileName = "events/$eventId/${eventId}.jpg"
            Log.d("FeedViewModel", "Загрузка изображения для event, fileName: $fileName")
            val uploadResult = storageRepository.uploadEventImage(fileName, imageUri)
            uploadResult.onSuccess { Log.d("FeedViewModel", "Изображение загружено: $it") }
                .onFailure { Log.e("FeedViewModel", "Ошибка загрузки изображения: ${it.message}") }
            val imageUrl = storageRepository.createSignedUrl("events-images", fileName)
                .getOrElse {
                    Log.e("FeedViewModel", "Ошибка получения URL: ${it.message}")
                    throw it
                }
            Log.d("FeedViewModel", "URL изображения: $imageUrl")
            Result.success(imageUrl)
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Исключение при загрузке изображения: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun refreshFeed() = intent {
        loadFeedItems()
    }

    suspend fun getEventStatusId(statusName: String): String? {
        return eventParticipantsRepository.getStatusIdByName(statusName)
    }

    private fun loadFeedItems() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val interests = interestsRepository.fetchAll().associate { interest ->
                interest.id to interest.name
            }
            Log.d("FeedViewModel", "Загрузка элементов с city: ${state.selectedCity}, type: ${state.selectedType}")
            allFeedItems = fetchFeedItemsUseCase(state.selectedCity, state.selectedType)
            Log.d("FeedViewModel", "Загружено элементов: ${allFeedItems.size}, объявления: ${allFeedItems.filterIsInstance<Announcement>().size}")
            val sortedItems = applySort(state.sortType, allFeedItems)
            reduce { state.copy(items = sortedItems, tagNames = interests, isLoading = false) }
            loadParticipationStates()
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Ошибка загрузки ленты: ${e.message}", e)
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }

    fun onSearchQueryChange(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
        val filteredItems = if (query.startsWith("#") && query.length > 1) {
            val tagQuery = query.drop(1).trim()
            allFeedItems.filter { item ->
                (item as? Event)?.tagIds?.any { tag -> tag.contains(tagQuery, ignoreCase = true) } == true
            }
        } else {
            allFeedItems.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                        item.description?.contains(query, ignoreCase = true) == true
            }
        }
        val sortedItems = applySort(state.sortType, filteredItems)
        reduce { state.copy(items = sortedItems) }
    }

    fun onSortSelected(sortType: SortType?) = intent {
        val sortedItems = applySort(sortType, if (state.searchQuery.isEmpty()) allFeedItems else state.items)
        reduce { state.copy(sortType = sortType, items = sortedItems) }
    }

    fun onCitySelected(city: String?) = intent {
        reduce { state.copy(selectedCity = city) }
        loadFeedItems()
    }

    fun onTypeSelected(type: FeedItemType?) = intent {
        reduce { state.copy(selectedType = type) }
        loadFeedItems()
    }

    private fun applySort(sortType: SortType?, items: List<FeedItem>): List<FeedItem> {
        return when (sortType) {
            SortType.DateNewestFirst -> items.sortedByDescending { Instant.parse(it.createdAt) }
            SortType.DateOldestFirst -> items.sortedBy { Instant.parse(it.createdAt) }
            SortType.City -> items.sortedBy { it.location ?: "" }
            null -> items
        }
    }

    fun onCreateEvent(event: Event) = intent{
        try {
            Log.d("FeedViewModel", "Создание мероприятия: $event")
            val user = remoteUsersRepository.fetchById(currentUserId)
            Log.d("FeedViewModel", "Пользователь: $user")
            val modifiedEvent = event.copy(organizerName = user?.name)
            Log.d("FeedViewModel", "Модифицированное событие: $modifiedEvent")
            val result = createEventUseCase(modifiedEvent)
            result.onSuccess {
                Log.d("FeedViewModel", "Мероприятие успешно создано")
                postSideEffect(FeedSideEffect.ShowMessage("Мероприятие создано и ожидает модерации"))
                refreshFeed()
            }.onFailure { error ->
                Log.e("FeedViewModel", "Ошибка создания мероприятия: ${error.message}", error)
                postSideEffect(FeedSideEffect.ShowError("Ошибка создания мероприятия: ${error.message}"))
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Исключение при создании мероприятия: ${e.message}", e)
            postSideEffect(FeedSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun onCreateAnnouncement(announcement: Announcement) = intent{
        try {
            Log.d("FeedViewModel", "Создание объявления: $announcement")
            val user = remoteUsersRepository.fetchById(currentUserId)
            Log.d("FeedViewModel", "Пользователь: $user")
            val modifiedAnnouncement = announcement.copy() // Можно добавить модификации, если нужно
            Log.d("FeedViewModel", "Модифицированное объявление: $modifiedAnnouncement")
            val result = createAnnouncementUseCase(modifiedAnnouncement)
            result.onSuccess {
                Log.d("FeedViewModel", "Объявление успешно создано")
                postSideEffect(FeedSideEffect.ShowMessage("Объявление создано и ожидает модерации"))
                refreshFeed()
            }.onFailure { error ->
                Log.e("FeedViewModel", "Ошибка создания объявления: ${error.message}", error)
                postSideEffect(FeedSideEffect.ShowError("Ошибка создания объявления: ${error.message}"))
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Исключение при создании объявления: ${e.message}", e)
            postSideEffect(FeedSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    private fun loadParticipationStates() {
        viewModelScope.launch {
            val eventIds = allFeedItems.filterIsInstance<Event>().map { it.id }
            val states = eventIds.associateWith { eventId ->
                eventParticipantsRepository.isUserParticipating(eventId, currentUserId)
            }
            _participationState.value = states
        }
    }

    fun onClearQuery() = intent {
        reduce { state.copy(searchQuery = "") }
        val sortedItems = applySort(state.sortType, allFeedItems)
        reduce { state.copy(items = sortedItems, error = null) }
    }

    fun onEventClick(eventId: String) = intent {
        postSideEffect(FeedSideEffect.NavigateToEventDetails(eventId))
    }

    fun navigateToNotifications() = intent {
        postSideEffect(FeedSideEffect.NavigateToNotifications)
    }

    fun onParticipateClick(eventId: String) = intent {
        try {
            val result = eventParticipantsRepository.joinEvent(eventId, currentUserId)
            result.onSuccess {
                viewModelScope.launch {
                    val updatedStates = _participationState.value.toMutableMap().apply {
                        this[eventId] = true
                    }
                    _participationState.value = updatedStates
                }
                postSideEffect(FeedSideEffect.ParticipateInEvent(eventId))
                refreshFeed()
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
                refreshFeed()
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

    fun onAnnouncementClick(announcementId: String) = intent {
        postSideEffect(FeedSideEffect.NavigateToAnnouncementDetails(announcementId))
    }

    fun onMessageClick(userId: String) = intent {
        try {
            val chatId = chatsRepository.findOrCreatePrivateChat(currentUserId, userId)
            postSideEffect(FeedSideEffect.NavigateToChat(chatId.toString()))
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Ошибка создания чата: ${e.message}", e)
            postSideEffect(FeedSideEffect.ShowError("Ошибка создания чата"))
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