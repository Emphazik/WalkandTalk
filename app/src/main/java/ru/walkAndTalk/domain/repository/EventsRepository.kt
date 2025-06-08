package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.EventParticipant

interface EventsRepository {
    suspend fun fetchAllEvents(): List<Event>
    suspend fun fetchEventById(id: String): Event?
    suspend fun updateEventImage(eventId: String, imageUrl: String)
    suspend fun fetchPastEventsForUser(userId: String): List<Event>
    suspend fun fetchParticipantStatus(userId: String, eventId: String): EventParticipant?
}