package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.User

interface RemoteUsersRepository {
    suspend fun add(user: User)
    suspend fun fetchAll(): List<User>
    suspend fun fetchById(id: String): User?
    suspend fun fetchByVkId(id: Long): User?
}