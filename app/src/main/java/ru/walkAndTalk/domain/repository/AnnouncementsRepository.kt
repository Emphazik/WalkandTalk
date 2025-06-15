package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.domain.model.Announcement

interface AnnouncementsRepository {
    suspend fun fetchAllAnnouncements(): List<Announcement>
//    suspend fun createAnnouncement(announcement: AnnouncementDto): Result<Unit>
    suspend fun createAnnouncement(announcement: Announcement): Result<Unit>
}