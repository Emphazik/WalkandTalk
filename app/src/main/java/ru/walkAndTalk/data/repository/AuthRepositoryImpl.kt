package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val postgrest: Postgrest
) : AuthRepository {

    override suspend fun authenticateUser(vkId: Long, email: String, phone: String, name: String): UserDto {
        val existingUser = postgrest["users"]
            .select { // Блок select для выбора колонок (если нужно)
                filter { // Блок filter для фильтрации
                    eq("vk_id", vkId)
                }
            }
            .decodeSingleOrNull<UserDto>()

        return if (existingUser != null) {
            existingUser
        } else {
            val newUser = UserDto(
                id = "",
                email = email,
                phone = phone,
                name = name,
                profileImageUrl = "",
                vkId = vkId
            )
            postgrest["users"].insert(newUser).decodeSingle<UserDto>()
        }
    }

}