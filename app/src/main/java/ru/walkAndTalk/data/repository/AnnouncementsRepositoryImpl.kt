package ru.walkAndTalk.data.repository

import android.util.Log
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
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

    override suspend fun fetchAllAnnouncementsAdmin(): List<Announcement> {
        return try {
            val announcementDtos = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .select {
                }
                .decodeList<AnnouncementDto>()
            val announcements = announcementDtos.map { it.toDomain() }
            Log.d("AnnouncementsRepository", "Загружены объявления: $announcements")
            announcements
        } catch (e: Exception) {
            Log.e("EventsRepository", "Ошибка загрузки мероприятий: ${e.message}", e)
            emptyList()
        }
    }
    override suspend fun fetchAnnouncementById(announcementId: String): Announcement? {
        return try {
            val announcementDto = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .select {
                    filter {
                        eq("id", announcementId)
                    }
                }
                .decodeSingleOrNull<AnnouncementDto>()
            val announcement = announcementDto?.toDomain()
            Log.d("AnnouncementsRepository", "Загружено объявление: $announcement")
            announcement
        } catch (e: Exception) {
            Log.e("AnnouncementsRepository", "Ошибка загрузки объявления с id=$announcementId: ${e.message}", e)
            null
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

    override suspend fun updateAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            val announcementDto = announcement.toDto()
            Log.d("AnnouncementsRepository", "Обновление объявления: $announcementDto")
            supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .update(announcementDto) {
                    filter { eq("id", announcement.id) }
                }
            Log.d("AnnouncementsRepository", "Объявление с id=${announcement.id} успешно обновлено")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AnnouncementsRepository", "Ошибка обновления объявления с id=${announcement.id}: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAnnouncement(announcementId: String, userId: String): Result<Unit> {
        return try {
            val announcementDto = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .select {
                    filter {
                        eq("id", announcementId)
                    }
                }
                .decodeSingleOrNull<AnnouncementDto>()

            if (announcementDto == null) {
                Log.e("AnnouncementsRepository", "Объявление с id=$announcementId не найдено")
                return Result.failure(IllegalArgumentException("Объявление не найдено"))
            }

            if (announcementDto.creatorId != userId) {
                Log.e("AnnouncementsRepository", "Пользователь $userId не имеет прав для удаления объявления $announcementId")
                return Result.failure(IllegalStateException("У вас нет прав для удаления этого объявления"))
            }

            supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .delete {
                    filter {
                        eq("id", announcementId)
                    }
                }
            Log.d("AnnouncementsRepository", "Объявление с id=$announcementId успешно удалено")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AnnouncementsRepository", "Ошибка удаления объявления с id=$announcementId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAnnouncementAdmin(announcementId: String): Result<Unit> {
        return try {
            val announcementDto = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .select {
                    filter {
                        eq("id", announcementId)
                    }
                }
                .decodeSingleOrNull<AnnouncementDto>()

            if (announcementDto == null) {
                Log.e("AnnouncementsRepository", "Объявление с id=$announcementId не найдено")
                return Result.failure(IllegalArgumentException("Объявление не найдено"))
            }

            supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
                .delete {
                    filter {
                        eq("id", announcementId)
                    }
                }
            Log.d("AnnouncementsRepository", "Объявление с id=$announcementId успешно удалено администратором")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AnnouncementsRepository", "Ошибка удаления объявления с id=$announcementId: ${e.message}", e)
            Result.failure(e)
        }
    }
}