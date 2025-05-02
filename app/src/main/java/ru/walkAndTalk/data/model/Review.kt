package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    @SerialName("reviewer_id") val reviewerId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("event_id") val eventId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
