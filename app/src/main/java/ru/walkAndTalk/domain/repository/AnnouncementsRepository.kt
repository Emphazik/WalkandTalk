package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.domain.model.Announcement

interface AnnouncementsRepository {
    suspend fun fetchAllAnnouncements(): List<Announcement>
    suspend fun fetchAnnouncementById(announcementId: String): Announcement?
    //    suspend fun createAnnouncement(announcement: AnnouncementDto): Result<Unit>
    suspend fun createAnnouncement(announcement: Announcement): Result<Unit>
    suspend fun updateAnnouncement(announcement: Announcement): Result<Unit>
    suspend fun deleteAnnouncement(announcementId: String, userId: String): Result<Unit>
}