package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.data.model.User

interface AuthRepository {
    suspend fun authenticateUser(vkId: String, email: String, phone: String, name: String): User
}