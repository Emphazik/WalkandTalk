package ru.walkAndTalk.data.repository


import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.CityKnowledgeLevelDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.CityKnowledgeLevel
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository

class CityKnowledgeLevelRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : CityKnowledgeLevelRepository {

    override suspend fun fetchById(id: String): CityKnowledgeLevel? {
        return supabaseWrapper.postgrest[Table.CITY_KNOWLEDGE_LEVELS]
            .select { filter { CityKnowledgeLevelDto::id eq id } }
            .decodeSingleOrNull<CityKnowledgeLevelDto>()
            ?.toDomain()
    }

    override suspend fun fetchAll(): List<CityKnowledgeLevel> {
        return supabaseWrapper.postgrest[Table.CITY_KNOWLEDGE_LEVELS]
            .select()
            .decodeList<CityKnowledgeLevelDto>()
            .map { it.toDomain() }
    }
}