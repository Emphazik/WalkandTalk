package ru.walkAndTalk.domain.repository

interface EventParticipantsRepository {
    suspend fun joinEvent(eventId: String, userId: String): Result<Unit>
    suspend fun isUserParticipating(eventId: String, userId: String): Boolean
    suspend fun leaveEvent(eventId: String, userId: String): Result<Unit>
    suspend fun getParticipantsCount(eventId: String): Int // Новый метод
    suspend fun getStatusIdByName(name: String): String?
    suspend fun removeAllParticipants(eventId: String): Result<Unit> // Новый метод
}