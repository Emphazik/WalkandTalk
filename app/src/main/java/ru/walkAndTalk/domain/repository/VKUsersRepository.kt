package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.User

interface VKUsersRepository {
    suspend fun fetchUser(id: Long): User
}