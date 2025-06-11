package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.User

interface AdminUsersRepository {
    suspend fun fetchAllUsers(): List<User>
    suspend fun fetchUserById(userId: String): User?
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
}