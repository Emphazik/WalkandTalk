package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.FeedItem
import ru.walkAndTalk.domain.model.FeedItemType

interface FeedRepository {
    suspend fun fetchFeedItems(city: String? = null, type: FeedItemType? = null): List<FeedItem>
    suspend fun createEvent(event: Event): Result<Unit>
    suspend fun createAnnouncement(announcement: Announcement): Result<Unit>
}