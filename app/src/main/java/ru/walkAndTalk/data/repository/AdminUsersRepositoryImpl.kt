package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.mapper.fromDto
import ru.walkAndTalk.data.mapper.fromDtoList
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.AdminUsersRepository

class AdminUsersRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : AdminUsersRepository {
    override suspend fun fetchAllUsers(): List<User> {
        return supabaseWrapper.postgrest[Table.USERS]
            .select()
            .decodeList<UserDto>()
            .fromDtoList()
    }

    override suspend fun fetchUserById(userId: String): User? {
        return supabaseWrapper.postgrest[Table.USERS]
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<UserDto>()
            ?.fromDto()
    }

    override suspend fun updateUser(user: User) {
        supabaseWrapper.postgrest[Table.USERS].update(
            mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "profile_image_url" to user.profileImageUrl,
                "bio" to user.bio,
                "goals" to user.goals,
                "birthdate" to user.birthdate,
                "city" to user.city,
                "show_reviews" to user.showReviews,
                "is_admin" to user.isAdmin,
                "gender" to user.gender,
                "last_login" to user.lastLogin?.toString()
            ).filterValues { it != null }
        ) {
            filter { eq("id", user.id) }
        }
    }

    override suspend fun deleteUser(userId: String) {
        supabaseWrapper.postgrest[Table.USERS]
            .delete {
                filter { eq("id", userId) }
            }
    }
}