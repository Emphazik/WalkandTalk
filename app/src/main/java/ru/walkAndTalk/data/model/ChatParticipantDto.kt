package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatParticipantDto(
    @SerialName("chat_id") val chatId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("last_read_at") val lastReadAt: String? = null,
    @SerialName("is_muted") val isMuted: Boolean = false,
    @SerialName("last_seen") val lastSeen: String? = null // Новое поле
)