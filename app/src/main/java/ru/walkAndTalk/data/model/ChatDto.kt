package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String, // "group" или "private"
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)