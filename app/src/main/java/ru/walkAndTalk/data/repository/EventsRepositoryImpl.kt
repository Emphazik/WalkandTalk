package ru.walkAndTalk.data.repository

import android.util.Log
import kotlinx.datetime.LocalDateTime
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.model.EventParticipantDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.EventParticipant
import ru.walkAndTalk.domain.repository.EventInterestsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import java.time.format.DateTimeFormatter

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

    override suspend fun getEventsByIds(eventIds: Set<String>): List<Event> {
        return try {
            val events = supabaseWrapper.postgrest[Table.EVENTS]
                .select { filter { isIn("id", eventIds.toList()) } }
                .decodeList<EventDto>()
            Log.d("EventsRepository", "Fetched ${events.size} events: $events")
            events.map { eventDto ->
                val tagNames = eventInterestsRepository.fetchTagsForEvent(eventDto.id)
                    .map { it.name }
                eventDto.toDomain(tagNames)
            }
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error fetching events by IDs: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun fetchPastEventsForUser(userId: String): List<Event> {
        try {
            val currentTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<EventParticipantDto>()
                .mapNotNull { participantDto ->
                    try {
                        val participant = participantDto.toDomain() // Преобразуем сначала
                        supabaseWrapper.postgrest[Table.EVENTS]
                            .select {
                                filter {
                                    eq("id", participant.eventId) // Используем не-null eventId
                                    lt("event_date", currentTime)
                                }
                            }
                            .decodeSingleOrNull<EventDto>()
                            ?.let { eventDto ->
                                val tagNames = eventInterestsRepository.fetchTagsForEvent(eventDto.id)
                                    .map { it.name }
                                eventDto.toDomain(tagNames)
                            }
                    } catch (e: IllegalArgumentException) {
                        null // Пропускаем невалидные записи (например, null eventId)
                    }
                }
        } catch (e: Exception) {
            throw RepositoryException("Failed to fetch past events for user: $userId", e)
        }
    }

    override suspend fun fetchParticipantStatus(userId: String, eventId: String): EventParticipant? {
        try {
            return supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("event_id", eventId)
                    }
                }
                .decodeSingleOrNull<EventParticipantDto>()
                ?.toDomain()
        } catch (e: IllegalArgumentException) {
            return null // Не валидная запись
        } catch (e: Exception) {
            throw RepositoryException("Failed to fetch participant status for user: $userId, event: $eventId", e)
        }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)