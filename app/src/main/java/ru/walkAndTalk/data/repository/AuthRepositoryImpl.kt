package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.model.User
import ru.walkAndTalk.domain.repository.AuthRepository
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
class AuthRepositoryImpl(
    private val postgrest: Postgrest
) : AuthRepository {
    override suspend fun authenticateUser(vkId: String, email: String, phone: String, name: String): User {
        val existingUser = postgrest["users"]
            .select { // Блок select для выбора колонок (если нужно)
                filter { // Блок filter для фильтрации
                    eq("vk_id", vkId)
                }
            }
            .decodeSingleOrNull<User>()

        return if (existingUser != null) {
            existingUser
        } else {
            val newUser = User(
                id = "",
                email = email,
                phone = phone,
                name = name,
                profileImageUrl = null,
                vkId = vkId
            )
            postgrest["users"].insert(newUser).decodeSingle<User>()
        }
    }
}