package ru.walkAndTalk.domain.repository

interface UserEventRepository {
    suspend fun getUserName(userId: String): String?
}