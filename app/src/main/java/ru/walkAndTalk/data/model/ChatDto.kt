package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatDto(
    // SUPABASE - надо переделать таблицы для логики чата
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("type") val type: String,
    @SerialName("interest") val interest: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_message") val lastMessage: String? = null,
    @SerialName("last_message_time") val lastMessageTime: String? = null,
    @SerialName("unread_count") val unreadCount: String? = null

)