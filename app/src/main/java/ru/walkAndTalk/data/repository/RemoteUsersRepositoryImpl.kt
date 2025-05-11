package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.fromDto
import ru.walkAndTalk.data.mapper.fromDtoList
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.RemoteUsersRepository

class RemoteUsersRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : RemoteUsersRepository {

    override suspend fun add(user: User) {
        supabaseWrapper.postgrest[Table.USERS].insert(user.toDto())
    }

    override suspend fun fetchAll(): List<User> {
        return supabaseWrapper.postgrest[Table.USERS]
            .select()
            .decodeList<UserDto>()
            .fromDtoList()
    }

    override suspend fun fetchById(id: String): User? {
        return supabaseWrapper.postgrest[Table.USERS]
            .select { filter { UserDto::id eq id } }
            .decodeSingleOrNull<UserDto>()
            ?.fromDto()
    }

    override suspend fun fetchByVkId(id: Long): User? {
        return supabaseWrapper.postgrest[Table.USERS]
            .select { filter { UserDto::vkId eq id } }
            .decodeSingleOrNull<UserDto>()
            ?.fromDto()
    }

    override suspend fun searchUsers(query: String): List<User> {
//        return supabaseWrapper[Table.USERS]
//            .select {
//                filter {
//                    or(
//                        listOf(
//                            ilike("name", "%$query%"),
//                            ilike("bio", "%$query%")
//                        )
//                    )
//                }
//            }
//            .decodeList<UserDto>()
//            .map { it.toDomain() }
//    }
        return TODO("Provide the return value")
    }

    override suspend fun updateCityKnowledgeLevel(userId: String, levelId: String) {
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("city_knowledge_level_id" to levelId)
            ) {
                filter { UserDto::id eq userId }
            }
    }

    override suspend fun updateBio(userId: String, bio: String) {
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("bio" to bio)
            ) {
                filter { UserDto::id eq userId }
            }
    }

    override suspend fun updateGoals(userId: String, goals: String) {
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("goals" to goals)
            ) {
                filter { UserDto::id eq userId }
            }
    }
}