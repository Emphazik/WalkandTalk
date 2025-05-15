package ru.walkAndTalk.domain.model

data class Event(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    val location: String,
    val eventDate: String,
    val createdAt: String,
    val eventImageUrl: String?,

    val organizerName: String? = null
)