package ru.walkAndTalk.data.repository

import android.util.Log
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.repository.AnnouncementsRepository

class AnnouncementsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : AnnouncementsRepository {
    override suspend fun fetchAllAnnouncements(): List<Announcement> {
        return try {
            val announcementDtos = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .select {
                    filter {
                        eq("status_id", "7513755c-ce44-459f-a071-07080e90f450")
                    }
                }
                .decodeList<AnnouncementDto>()
            val announcements = announcementDtos.map { it.toDomain() }
            Log.d("AnnouncementsRepository", "Загружены объявления: $announcements")
            announcements
        } catch (e: Exception) {
            Log.e("AnnouncementsRepository", "Ошибка загрузки объявлений: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun createAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            val announcementDto = announcement.toDto()
            Log.d("AnnouncementsRepository", "AnnouncementDto перед вставкой: $announcementDto")
            supabaseWrapper.postgrest[Table.ANNOUNCEMENTS].insert(announcementDto)
            Log.d("AnnouncementsRepository", "Объявление успешно вставлено")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AnnouncementsRepository", "Ошибка вставки объявления: ${e.message}", e)
            Result.failure(e)
        }
    }
}