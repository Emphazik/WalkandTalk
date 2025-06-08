package ru.walkAndTalk.domain.model

data class EventReview(
    val id: String? = null,
    val eventId: String,
    val userId: String,
    val rating: Int, // 0 to 5
    val comment: String?,
    val createdAt: String? = null
)