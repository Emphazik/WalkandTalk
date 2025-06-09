package ru.walkAndTalk.ui.screens.main.feed.notifications

import ru.walkAndTalk.domain.model.Notification

data class NotificationsViewState(
    val notifications: List<Notification> = emptyList(),
    val isLoadingNotifications: Boolean = false,
    val notificationsError: String? = null
)