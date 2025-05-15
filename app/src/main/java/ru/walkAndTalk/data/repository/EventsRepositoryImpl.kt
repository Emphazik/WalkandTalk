package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.EventsRepository

class EventsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : EventsRepository {

    override suspend fun fetchAllEvents(): List<Event> {
        return supabaseWrapper.postgrest[Table.EVENTS]
            .select()
            .decodeList<EventDto>()
            .map { it.toDomain() }
    }

    override suspend fun fetchEventById(id: String): Event? {
        return supabaseWrapper.postgrest[Table.EVENTS]
            .select { filter { EventDto::id eq id } }
            .decodeSingleOrNull<EventDto>()
            ?.toDomain()
    }

    override suspend fun updateEventImage(eventId: String, imageUrl: String) {
        supabaseWrapper.postgrest[Table.EVENTS].update(
            mapOf(
                "event_image_url" to imageUrl
            )
        ) {
            filter { EventDto::id eq eventId }
        }
    }
}