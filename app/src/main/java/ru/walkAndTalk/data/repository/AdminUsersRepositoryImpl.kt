package ru.walkAndTalk.data.repository

import android.util.Log
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
        val users = supabaseWrapper.postgrest[Table.USERS]
            .select()
            .decodeList<UserDto>()
            .fromDtoList()
        Log.d("RemoteUsersRepository", "Fetched ${users.size} users")
        return users
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
//        supabaseWrapper.postgrest[Table.USERS].update(
//            mapOf(
//                "name" to user.name,
//                "email" to user.email,
//                "phone" to user.phone,
//                "profile_image_url" to user.profileImageUrl,
//                "bio" to user.bio,
//                "goals" to user.goals,
//                "birthdate" to user.birthdate,
//                "city" to user.city,
//                "show_reviews" to user.showReviews,
//                "is_admin" to user.isAdmin,
//                "gender" to user.gender,
//                "last_login" to user.lastLogin?.toString()
//            ).filterValues { it != null }
//        ) {
//            filter { eq("id", user.id) }
//        }
        supabaseWrapper.postgrest[Table.USERS].update(
            {
                set("name", user.name)
                set("email", user.email)
                set("phone", user.phone)
                set("password", user.password)
                set("profile_image_url", user.profileImageUrl)
                set("vk_id", user.vkId)
                set("interest_ids", user.interestIds)
                set("city_knowledge_level_id", user.cityKnowledgeLevelId)
                set("bio", user.bio)
                set("goals", user.goals)
                set("is_admin", user.isAdmin)
                set("gender", user.gender)
                set("updated_at", user.updatedAt.toString())
            }
        ) {
            filter { eq("id", user.id) }
        }
        Log.d("RemoteUsersRepository", "Updated user: ${user.id}")
    }

    override suspend fun deleteUser(userId: String) {
        supabaseWrapper.postgrest[Table.USERS]
            .delete {
                filter { eq("id", userId) }
            }
    }
}