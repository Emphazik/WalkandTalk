package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.model.InterestDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Interest
import ru.walkAndTalk.domain.repository.EventInterestsRepository

class EventInterestsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : EventInterestsRepository {

    override suspend fun fetchTagsForEvent(eventId: String): List<Interest> {
        // Получаем tag_ids из таблицы events
        val event = supabaseWrapper.postgrest[Table.EVENTS]
            .select { filter { eq("id", eventId) } }
            .decodeSingleOrNull<EventDto>()

        return if (event != null && event.tagIds.isNotEmpty()) {
            event.tagIds.mapNotNull { tagId ->
                supabaseWrapper.postgrest[Table.INTERESTS]
                    .select { filter { eq("id", tagId) } }
                    .decodeSingleOrNull<InterestDto>()
                    ?.toDomain()
            }
        } else {
            emptyList()
        }
    }
}