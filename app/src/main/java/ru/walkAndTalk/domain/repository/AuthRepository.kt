package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.data.model.UserDto

interface AuthRepository {
    suspend fun authenticateUser(vkId: Long, email: String, phone: String, name: String): UserDto
}