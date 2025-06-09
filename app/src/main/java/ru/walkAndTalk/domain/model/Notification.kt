package ru.walkAndTalk.domain.model

import kotlinx.serialization.Serializable

data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val content: String,
    val createdAt: String, // ISO формат, например, "2025-06-09T16:00:00Z"
    val isRead: Boolean,
    val relatedId: String? // ID события или чата
)
