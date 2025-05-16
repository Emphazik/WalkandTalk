package ru.walkAndTalk.domain.repository

interface EventParticipantsRepository {
    suspend fun joinEvent(eventId: String, userId: String): Result<Unit>
    suspend fun isUserParticipating(eventId: String, userId: String): Boolean
}