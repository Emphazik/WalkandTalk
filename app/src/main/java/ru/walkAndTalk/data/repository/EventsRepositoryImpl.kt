package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.EventInterestsRepository
import ru.walkAndTalk.domain.repository.EventsRepository

class EventsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
    private val eventInterestsRepository: EventInterestsRepository
) : EventsRepository {

    override suspend fun fetchAllEvents(): List<Event> {
        val events = supabaseWrapper.postgrest[Table.EVENTS]
            .select()
            .decodeList<EventDto>()
        return events.map { eventDto ->
            val tagNames = eventInterestsRepository.fetchTagsForEvent(eventDto.id)
                .map { it.name }
            eventDto.toDomain(tagNames)
        }
    }

    override suspend fun fetchEventById(id: String): Event? {
        val eventDto = supabaseWrapper.postgrest[Table.EVENTS]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<EventDto>() ?: return null
        val tagNames = eventInterestsRepository.fetchTagsForEvent(eventDto.id)
            .map { it.name }
        return eventDto.toDomain(tagNames)
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