package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.repository.AuthRepository
import java.time.Instant
import java.util.UUID

class AuthRepositoryImpl(
    private val postgrest: Postgrest,
    private val supabaseWrapper: SupabaseWrapper
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
                vkId = vkId,
                cityKnowledgeLevelId = null, // Установим позже
                bio = null, // Установим позже
                goals = null, // Установим позже
                createdAt = Instant.now().toString(),
                updatedAt = null,
                password = UUID.randomUUID().toString()
            )
            postgrest["users"].insert(newUser).decodeSingle<UserDto>()
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return supabaseWrapper.auth.currentUserOrNull()?.id
    }

}