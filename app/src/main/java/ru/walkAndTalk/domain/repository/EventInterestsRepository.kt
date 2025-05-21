package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Interest

interface EventInterestsRepository {
    suspend fun fetchTagsForEvent(eventId: String): List<Interest>
}