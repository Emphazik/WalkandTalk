package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("type") val type: String,
    @SerialName("interest") val interest: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)