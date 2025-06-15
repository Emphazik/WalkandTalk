package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.ActivityTypeDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.ActivityType
import ru.walkAndTalk.domain.repository.ActivityTypesRepository

class ActivityTypesRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : ActivityTypesRepository {
    override suspend fun fetchAll(): List<ActivityType> {
        return try {
            val activityTypeDtos = supabaseWrapper.postgrest[Table.ACTIVITY_TYPES]
                .select()
                .decodeList<ActivityTypeDto>()
            activityTypeDtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}