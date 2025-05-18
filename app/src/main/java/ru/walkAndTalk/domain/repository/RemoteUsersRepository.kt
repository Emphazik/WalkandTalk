package ru.walkAndTalk.domain.repository

import android.net.Uri
import ru.walkAndTalk.domain.model.User

interface RemoteUsersRepository {
    suspend fun add(user: User)
    suspend fun fetchAll(): List<User>
    suspend fun fetchById(id: String): User?
    suspend fun fetchByVkId(id: Long): User?
    suspend fun fetchByEmail(email: String): User?
    suspend fun updateVKId(userId: String, vkId: Long)
    suspend fun registerNewUser(vkUser: User): User
    suspend fun updateCityKnowledgeLevel(userId: String, levelId: String)
    suspend fun updateBio(userId: String, bio: String)
    suspend fun updateGoals(userId: String, goals: String)
    suspend fun uploadProfileImage(userId: String, imageUri: Uri, fileName: String)
    suspend fun getProfileImageUrl(userId: String, fileName: String): String
    suspend fun updateProfileImageUrl(userId: String, imageUrl: String)
    suspend fun updateUserProfile(
        userId: String,
        fullName: String? = null,
        birthDate: String? = null,
        photoURL: String? = null,
        bio: String? = null,
        goals: String? = null
    )
    suspend fun logout()

    //Для SearchViewModel
    suspend fun searchUsers(query: String): List<User>

}