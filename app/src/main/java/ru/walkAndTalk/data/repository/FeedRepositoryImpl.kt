package ru.walkAndTalk.data.repository

import android.util.Log
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.FeedItem
import ru.walkAndTalk.domain.model.FeedItemType
import ru.walkAndTalk.domain.repository.AnnouncementsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.FeedRepository
import java.time.Instant

class FeedRepositoryImpl(
    private val eventsRepository: EventsRepository,
    private val announcementsRepository: AnnouncementsRepository
) : FeedRepository {
    override suspend fun fetchFeedItems(city: String?, type: FeedItemType?): List<FeedItem> {
        val events = eventsRepository.fetchAllEvents()
            .filter { city == null || it.location.contains(city, ignoreCase = true) }
            .filter { type == null || type == FeedItemType.EVENT }
        val announcements = announcementsRepository.fetchAllAnnouncements()
            .filter { city == null || it.location?.contains(city, ignoreCase = true) == true }
            .filter { type == null || type == FeedItemType.ANNOUNCEMENT }
        return (events + announcements).sortedByDescending { Instant.parse(it.createdAt) }
    }

    override suspend fun createEvent(event: Event): Result<Unit> {
        Log.d("FeedRepository", "Создание event: $event")
        return eventsRepository.createEvent(event)
    }

    override suspend fun createAnnouncement(announcement: Announcement): Result<Unit> {
        Log.d("FeedRepository", "Создание announcement: $announcement")
        return announcementsRepository.createAnnouncement(announcement)
    }
}