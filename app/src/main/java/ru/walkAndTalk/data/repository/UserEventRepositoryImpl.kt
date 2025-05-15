package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.repository.UserEventRepository

class UserEventRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : UserEventRepository
{
    override suspend fun getUserName(userId: String): String? {
        return try {
            val user = supabaseWrapper.postgrest["users"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserDto>()
            user?.name
        } catch (e: Exception) {
            println("Ошибка при получении имени пользователя: ${e.message}")
            null
        }
    }
}