package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Event

interface EventsRepository {
    suspend fun fetchAllEvents(): List<Event>
    suspend fun fetchEventById(id: String): Event?
}