package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.EventParticipantDto
import ru.walkAndTalk.domain.model.EventParticipant

fun EventParticipantDto.toDomain(): EventParticipant {
    requireNotNull(eventId) { "Event ID cannot be null" }
    requireNotNull(userId) { "User ID cannot be null" }
    requireNotNull(joinedAt) { "Joined at timestamp cannot be null" }
    return EventParticipant(
        eventId = eventId,
        userId = userId,
        joinedAt = joinedAt
    )
}

fun EventParticipant.toDto(): EventParticipantDto {
    return EventParticipantDto(
        eventId = eventId,
        userId = userId,
        joinedAt = joinedAt
    )
}