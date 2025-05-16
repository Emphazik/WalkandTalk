package ru.walkAndTalk.domain.model

data class EventParticipant(
    val eventId: String,
    val userId: String,
    val joinedAt: String
)