package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id") val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("temp_id") val tempId: String? = null, // Это поле локальное, в базе его нет
    @SerialName("deleted_by") val deletedBy: List<String>? = null
)