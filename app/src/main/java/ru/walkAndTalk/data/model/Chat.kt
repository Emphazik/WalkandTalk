package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Chat(
    val id: String,
    @SerialName("event_id") val eventId: String,
    val type: String,
    val interest: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)