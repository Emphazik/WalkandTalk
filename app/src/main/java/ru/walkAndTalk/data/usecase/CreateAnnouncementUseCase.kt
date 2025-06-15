package ru.walkAndTalk.data.usecase

import android.util.Log
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.repository.AnnouncementsRepository

class CreateAnnouncementUseCase(
    private val announcementsRepository: AnnouncementsRepository
) {
//    suspend operator fun invoke(announcement: Announcement): Result<Unit> {
//        return announcementsRepository.createAnnouncement(announcement.toDto("0"))
//    }
    suspend operator fun invoke(announcement: Announcement): Result<Unit> {
        Log.d("CreateAnnouncementUseCase", "Входной announcement: $announcement")
        return announcementsRepository.createAnnouncement(announcement)
    }
}