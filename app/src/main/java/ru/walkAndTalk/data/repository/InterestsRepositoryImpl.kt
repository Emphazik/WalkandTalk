package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.InterestDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Interest
import ru.walkAndTalk.domain.repository.InterestsRepository

class InterestsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : InterestsRepository {

    override suspend fun fetchById(id: String): Interest? {
        return supabaseWrapper.postgrest[Table.INTERESTS]
            .select { filter { InterestDto::id eq id } }
            .decodeSingleOrNull<InterestDto>()
            ?.toDomain()
    }

    override suspend fun fetchAll(): List<Interest> {
        return supabaseWrapper.postgrest[Table.INTERESTS]
            .select()
            .decodeList<InterestDto>()
            .map { it.toDomain() }
    }
}