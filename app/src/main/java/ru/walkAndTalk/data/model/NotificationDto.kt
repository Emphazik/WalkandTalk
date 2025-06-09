package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("type") val type: String, // Хранится как строка из notification_types
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("related_id") val relatedId: String?
)