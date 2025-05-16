package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EventParticipantDto(
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("joined_at") val joinedAt: String? = null
)