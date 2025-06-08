package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventReviewDto(
    @SerialName("id") val id: String? = null,
    @SerialName("event_id")val eventId: String,
    @SerialName("user_id")val userId: String,
    @SerialName("rating")val rating: Int, // 0 to 5
    @SerialName("comment")val comment: String?,
    @SerialName("created_at") val createdAt: String? = null
)