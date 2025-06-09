package ru.walkAndTalk.ui.screens.main.feed.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaZoneId
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Notification
import ru.walkAndTalk.domain.model.NotificationType
import ru.walkAndTalk.domain.repository.NotificationsRepository
import ru.walkAndTalk.ui.screens.main.feed.FeedSideEffect
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Locale

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository,
    private val currentUserId: String
) : ViewModel(), ContainerHost<NotificationsViewState, FeedSideEffect> {
    override val container: Container<NotificationsViewState, FeedSideEffect> = container(NotificationsViewState())

    init {
        loadNotifications()
        viewModelScope.launch {
            notificationsRepository.subscribeToNotifications(currentUserId) { notificationDto ->
                intent {
                    val notification = toDomain(notificationDto)
                    val updatedNotifications = (state.notifications + notification)
                        .sortedByDescending { it.createdAt }
                    reduce { state.copy(notifications = updatedNotifications) }
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            notificationsRepository.unsubscribeFromNotifications()
        }
        super.onCleared()
    }

    fun loadNotifications() = intent {
        reduce { state.copy(isLoadingNotifications = true, notificationsError = null) }
        try {
            val notificationDtos = notificationsRepository.fetchUserNotifications(currentUserId)
            val notifications = notificationDtos.map { toDomain(it) }
            reduce { state.copy(notifications = notifications.sortedByDescending { it.createdAt }, isLoadingNotifications = false) }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Не удалось загрузить уведомления: ${e.message}"))
            reduce { state.copy(isLoadingNotifications = false, notificationsError = e.message) }
        }
    }

    fun markNotificationAsRead(notificationId: String) = intent {
        try {
            notificationsRepository. markAsRead(notificationId)
            val updatedNotifications = state.notifications.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            reduce { state.copy(notifications = updatedNotifications) }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Ошибка при отметке уведомления: ${e.message}"))
        }
    }

    fun markAllNotificationsAsRead() = intent {
        try {
            notificationsRepository.markAllAsRead(currentUserId)
            val updatedNotifications = state.notifications.map { it.copy(isRead = true) }
            reduce { state.copy(notifications = updatedNotifications) }
        } catch (e: Exception) {
            postSideEffect(FeedSideEffect.ShowError("Ошибка при отметке всех уведомлений: ${e.message}"))
        }
    }

    fun onNotificationClick(notification: Notification) = intent {
        markNotificationAsRead(notification.id)
        when (notification.type) {
            NotificationType.NewEvent -> notification.relatedId?.let {
                postSideEffect(FeedSideEffect.NavigateToEventDetails(it))
            }
            NotificationType.NewMessage -> notification.relatedId?.let {
                postSideEffect(FeedSideEffect.NavigateToChat(it))
            }
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