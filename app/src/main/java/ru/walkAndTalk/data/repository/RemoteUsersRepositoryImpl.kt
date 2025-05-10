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
}