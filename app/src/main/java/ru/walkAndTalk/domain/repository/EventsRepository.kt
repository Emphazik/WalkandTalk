package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.EventParticipant

interface EventsRepository {
    suspend fun fetchAllEvents(): List<Event>
    suspend fun createEvent(event: Event): Result<Unit>
    suspend fun fetchEventById(id: String): Event?
    suspend fun updateEventImage(eventId: String, imageUrl: String)
    suspend fun fetchPastEventsForUser(userId: String): List<Event>
    suspend fun getEventsByIds(eventIds: Set<String>): List<Event>
    suspend fun fetchParticipantStatus(userId: String, eventId: String): EventParticipant?
    suspend fun deleteEvent(eventId: String, creatorId: String): Result<Unit> // Новый метод
    suspend fun updateEvent(event: Event): Result<Unit> // Новый метод

}