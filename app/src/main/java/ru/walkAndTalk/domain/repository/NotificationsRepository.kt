package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.data.model.NotificationDto
import ru.walkAndTalk.domain.model.Notification

interface NotificationsRepository {
    suspend fun fetchUserNotifications(userId: String): List<NotificationDto>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun subscribeToNotifications(userId: String, onUpdate: (NotificationDto) -> Unit)
    suspend fun unsubscribeFromNotifications()

    suspend fun createNotifications(
        userIds: List<String>,
        type: String,
        content: String,
        relatedId: String?
    ): List<NotificationDto>
}