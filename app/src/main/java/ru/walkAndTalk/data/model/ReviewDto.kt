package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("reviewer_id") val reviewerId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
